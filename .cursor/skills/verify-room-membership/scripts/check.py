#!/usr/bin/env python3
"""Model-check RoomServiceImplt room caps. Guards are read from the Java source."""

from __future__ import annotations

import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SYNC_COMMIT = "cc8e6c0c4d0448aa335357413be7878dadf0553c"
TLC_VERSION = "1.8.0"
TLC_URL = (
    "https://github.com/tlaplus/tlaplus/releases/download/"
    f"v{TLC_VERSION}/tla2tools.jar"
)

ROOT = Path(__file__).resolve().parents[4]
SKILL = Path(__file__).resolve().parents[1]
TLA = SKILL / "RoomMembership.tla"
JAR = SKILL / "tools" / f"tla2tools-{TLC_VERSION}.jar"
SERVICE = (
    ROOT
    / "backend/src/main/java/com/roomate/app/service/implementation/RoomServiceImplt.java"
)
REPOSITORY = ROOT / "backend/src/main/java/com/roomate/app/repository/RoomRepository.java"
MEMBER = ROOT / "backend/src/main/java/com/roomate/app/entities/room/RoomMemberEntity.java"

RACE_SCENARIOS = (
    "createUserCapRace",
    "joinUserCapRace",
    "roomCapRace",
    "dupRace",
)
GUARD_SCENARIOS = (
    "createUserCapGuard",
    "joinUserCapGuard",
    "roomCapGuard",
    "dupGuard",
)
SCENARIOS = RACE_SCENARIOS + GUARD_SCENARIOS

GUARD_OF = {
    "createUserCapRace": "CreateChecksCap",
    "createUserCapGuard": "CreateChecksCap",
    "joinUserCapRace": "JoinChecksUserCap",
    "joinUserCapGuard": "JoinChecksUserCap",
    "roomCapRace": "JoinChecksRoomCap",
    "roomCapGuard": "JoinChecksRoomCap",
    "dupRace": "JoinChecksDuplicate",
    "dupGuard": "JoinChecksDuplicate",
}

LOCK_MARKERS = ("lockmodetype", "pessimistic", "serializable", "for update", "forupdate")


def die(message: str, code: int = 1) -> None:
    print(message, file=sys.stderr)
    raise SystemExit(code)


def extract_method(src: str, signature: str) -> str:
    idx = src.find(signature)
    if idx < 0:
        die(f"model out of sync: {signature} is not in {SERVICE}")
    brace = src.find("{", idx)
    if brace < 0:
        die(f"model out of sync: {signature} has no body")
    depth = 0
    for i in range(brace, len(src)):
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
            if depth == 0:
                return src[brace : i + 1]
    die(f"model out of sync: {signature} has unbalanced braces")
    return ""


def has_lock(body: str) -> bool:
    lowered = body.lower()
    return any(marker in lowered for marker in LOCK_MARKERS)


def java_bin() -> str:
    try:
        home = subprocess.check_output(
            ["/usr/libexec/java_home", "-v", "21"], text=True
        ).strip()
        candidate = os.path.join(home, "bin", "java")
        if os.path.isfile(candidate):
            return candidate
    except (OSError, subprocess.CalledProcessError):
        pass
    return "java"


def ensure_jar() -> None:
    if JAR.is_file() and JAR.stat().st_size > 100_000:
        return
    JAR.parent.mkdir(parents=True, exist_ok=True)
    print(f"downloading TLC {TLC_VERSION}")
    subprocess.check_call(["curl", "-fsSL", "-o", str(JAR), TLC_URL])


def tla_bool(value: bool) -> str:
    return "TRUE" if value else "FALSE"


def run_scenario(flags: dict[str, bool], scenario: str) -> tuple[str, str]:
    cfg = "\n".join(
        [
            "SPECIFICATION Spec",
            "INVARIANT TypeOk",
            "INVARIANT CapOk",
            "CHECK_DEADLOCK FALSE",
            "CONSTANT",
            f'Scenario = "{scenario}"',
            f"CreateChecksCap = {tla_bool(flags['CreateChecksCap'])}",
            f"JoinChecksUserCap = {tla_bool(flags['JoinChecksUserCap'])}",
            f"JoinChecksRoomCap = {tla_bool(flags['JoinChecksRoomCap'])}",
            f"JoinChecksDuplicate = {tla_bool(flags['JoinChecksDuplicate'])}",
            f"ChecksAreAtomic = {tla_bool(flags['ChecksAreAtomic'])}",
            "",
        ]
    )
    with tempfile.TemporaryDirectory(prefix="room-membership-") as tmp:
        cfg_path = Path(tmp) / "check.cfg"
        cfg_path.write_text(cfg)
        shutil.copy(TLA, Path(tmp) / "RoomMembership.tla")
        proc = subprocess.run(
            [
                java_bin(),
                "-XX:+UseParallelGC",
                "-jar",
                str(JAR),
                "-workers",
                "1",
                "-nowarning",
                "-config",
                str(cfg_path),
                "RoomMembership.tla",
            ],
            cwd=tmp,
            text=True,
            capture_output=True,
        )
    out = proc.stdout + "\n" + proc.stderr
    if "Invariant TypeOk is violated" in out:
        return "model-error", out
    if "Invariant CapOk is violated" in out:
        return "violation", out
    if "No error has been found" in out or "Model checking completed. No error has been found." in out:
        return "pass", out
    if proc.returncode == 0 and "Finished" in out:
        return "pass", out
    return "tlc-error", out


def expect_violation(flags: dict[str, bool], scenario: str) -> bool:
    guard = flags[GUARD_OF[scenario]]
    if scenario in GUARD_SCENARIOS:
        return not guard
    return (not flags["ChecksAreAtomic"]) or (not guard)


def main() -> None:
    if not TLA.is_file():
        die(f"missing spec {TLA}")
    service = SERVICE.read_text()
    repository = REPOSITORY.read_text()
    member = MEMBER.read_text()
    create = extract_method(service, "public RoomDto createRoom(")
    join = extract_method(service, "public RoomDto joinRoom(")

    if "uniqueConstraints" in member and "user_id" in member and "room_id" in member:
        die(
            "RoomMemberEntity now declares a uniqueness constraint. "
            "The duplicate-membership model assumes there is none. Re-read the entity "
            "before trusting this check."
        )
    if "countRoomsByUserId" not in repository:
        die("model out of sync: RoomRepository.countRoomsByUserId is gone")

    create_locked = has_lock(create)
    join_locked = has_lock(join)
    if create_locked != join_locked:
        die(
            "partial locking: only one of createRoom and joinRoom shows a lock or "
            "SERIALIZABLE. This model has one atomicity flag. Update the spec before "
            "trusting the result."
        )

    flags = {
        "CreateChecksCap": "countRoomsByUserId" in create and ">= 3" in create,
        "JoinChecksUserCap": "countRoomsByUserId" in join and ">= 3" in join,
        "JoinChecksRoomCap": ".size() >= 6" in join or "size() >= 6" in join,
        "JoinChecksDuplicate": "alreadyMember" in join,
        "ChecksAreAtomic": create_locked and join_locked,
    }

    print(f"sync commit: {SYNC_COMMIT}")
    head = subprocess.check_output(["git", "-C", str(ROOT), "rev-parse", "HEAD"], text=True).strip()
    print(f"HEAD: {head}")
    if head != SYNC_COMMIT:
        diff = subprocess.run(
            ["git", "-C", str(ROOT), "diff", "--stat", SYNC_COMMIT, "--", str(SERVICE), str(REPOSITORY), str(MEMBER)],
            text=True,
            capture_output=True,
        )
        if diff.stdout.strip():
            print("covered code differs from the sync commit. Read the diff before moving the sync commit.")
            print(diff.stdout.strip())
    for name, value in flags.items():
        print(f"{name}: {str(value).lower()}")

    ensure_jar()
    results: dict[str, str] = {}
    traces: dict[str, str] = {}
    for scenario in SCENARIOS:
        status, out = run_scenario(flags, scenario)
        results[scenario] = status
        traces[scenario] = out
        print(f"scenario {scenario}: {status}")
        if status == "tlc-error" or status == "model-error":
            print(out)
            die(f"TLC failed on {scenario}", 1)

    known = (
        all(results[s] == "violation" for s in RACE_SCENARIOS)
        and all(results[s] == "pass" for s in GUARD_SCENARIOS)
        and not flags["ChecksAreAtomic"]
        and all(flags[k] for k in flags if k != "ChecksAreAtomic")
    )
    held = all(results[s] == "pass" for s in SCENARIOS)

    if known:
        print("status: known-findings")
        print(
            "The cap checks are present and still check-then-act. "
            "Two overlapping calls can both pass the read and both save."
        )
        print("--- trace createUserCapRace ---")
        trace = traces["createUserCapRace"]
        error_at = trace.find("Error:")
        print(trace[error_at:] if error_at >= 0 else trace)
        raise SystemExit(2)

    if held:
        print("status: holds")
        print(
            "Every scenario held. If you added a lock or SERIALIZABLE, confirm the "
            "Java still matches this spec, then move the sync commit. Do not treat a "
            "pass as covering remove, leave, or role changes."
        )
        raise SystemExit(0)

    print("status: unexpected")
    for scenario in SCENARIOS:
        wanted = "violation" if expect_violation(flags, scenario) else "pass"
        mark = "ok" if results[scenario] == wanted else "DIFF"
        print(f"  {scenario}: got {results[scenario]}, model-expected {wanted} [{mark}]")
    missing = [name for name, value in flags.items() if name != "ChecksAreAtomic" and not value]
    if missing:
        print("missing guards: " + ", ".join(missing))
        print("A cap check was removed or rewritten so this parser no longer sees it.")
    raise SystemExit(1)


if __name__ == "__main__":
    main()
