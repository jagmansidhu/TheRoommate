---
name: verify-room-membership
description: Model-check TheRoommate room caps in RoomServiceImplt.createRoom and joinRoom (at most 3 rooms per user, 6 members per room, no duplicate membership) with TLA+ TLC. Use when a change touches those methods, countRoomsByUserId, or RoomMemberEntity.
---

# Verify room membership caps

A person must not end up in more than 3 rooms, a room must not end up with more than 6 members, and the same person must not be stored twice in one room. Breaking that lets a household grow past the limit the product states, so the "slots left" count and the join error no longer match who is actually in the room.

## When a change needs this check

Run it when a diff touches any of these:

- `backend/src/main/java/com/roomate/app/service/implementation/RoomServiceImplt.java` methods `createRoom` and `joinRoom`
- `backend/src/main/java/com/roomate/app/repository/RoomRepository.java` method `countRoomsByUserId`
- `backend/src/main/java/com/roomate/app/entities/room/RoomMemberEntity.java` (the table has indexes on `room_id` and `user_id`, and no uniqueness constraint on that pair)

`removeMemberFromRoom`, `leaveRoom`, `removeRoom`, and `updateMemberRole` are outside this model. A change that only touches them does not need this run. A change that adds a lock, `SERIALIZABLE`, or a unique pair on `room_member` does.

## Run

Java 21 and the vendored TLC jar. From the repo root:

```bash
python3 .cursor/skills/verify-room-membership/scripts/check.py
```

TLC is `tla2tools` 1.8.0 (`TLC2 Version 2026.10.28.014557`), jar at `.cursor/skills/verify-room-membership/tools/tla2tools-1.8.0.jar`. The script uses `/usr/libexec/java_home -v 21`. One run is eight tiny models and finishes in a few seconds.

Exit codes:

- `2` — the known finding. The four cap checks are still in the Java, and two overlapping calls can both pass the read and both save. This is the current code. It is not a pass.
- `0` — every scenario held. That happens if the reads and the save became one atomic step (a lock or `SERIALIZABLE` on both methods). Read the methods before treating it as a fix, then move the sync commit below.
- `1` — something else. A missing or rewritten check, a lock on only one of the two methods, a new unique constraint, TLC crashing, or a model that no longer matches the source. Do not weaken the spec to get back to exit `2`.

Read every TLC result with the pstack `/statechart` skill. It shows each invariant violation as its shortest path in plain words, and it draws how the states connect when a run holds. Use that skill's current commands. Do not copy them here.

## What the model is

`RoomMembership.tla` is the design of `createRoom` and `joinRoom`, not a second product. `Begin` is the unlocked read. `Commit` is `roomRepository.save`. They are separate steps because `countRoomsByUserId` is a plain `COUNT`, `room.getMembers().size()` is a plain collection read, and `alreadyMember` is a plain scan. Both methods are `@Transactional` under PostgreSQL `READ COMMITTED`, which does not lock the rows just counted. `ChecksAreAtomic` is true only when both method bodies contain a lock or `SERIALIZABLE`. The script sets the four guard flags by reading those bodies: `countRoomsByUserId` together with `>= 3` in each method, `.size() >= 6` in `joinRoom`, and `alreadyMember` in `joinRoom`.

Each scenario is one counter and one or two callers.

| Scenario | Starts at | Callers | Java |
|---|---|---|---|
| `createUserCapRace` | 2 rooms | two `createRoom` | the `>= 3` check, then `save` |
| `createUserCapGuard` | 3 rooms | one `createRoom` | that same check must reject |
| `joinUserCapRace` | 2 rooms | two `joinRoom` | the `>= 3` check in `joinRoom` |
| `joinUserCapGuard` | 3 rooms | one `joinRoom` | that check must reject |
| `roomCapRace` | 5 members | two `joinRoom` | `.size() >= 6`, then `save` |
| `roomCapGuard` | 6 members | one `joinRoom` | that check must reject |
| `dupRace` | not a member | two `joinRoom` of the same person | `alreadyMember`, then `save` |
| `dupGuard` | already a member | one `joinRoom` | `alreadyMember` must reject |

The invariant is `count <= Limit` (`3`, `6`, or `1`). A guard scenario passes when the check is present, because the single caller is already at the cap and `Begin` is disabled. A race scenario fails when the check is present and the read and the save are separate steps.

## The finding, in the code's words

Exit `2` is this interleaving of `createRoom`, and the same shape on the other three race scenarios. TLC's shortest `createUserCapRace` trace is:

1. The user is already in 2 rooms. Both calls are before the read.
2. The first `createRoom` runs `countRoomsByUserId`, sees 2, and gets past `>= 3`. It has not saved.
3. The second `createRoom` runs the same count, still sees 2, and also gets past the check.
4. The first call saves. The user is in 3 rooms.
5. The second call saves. The user is in 4 rooms.

`CapOk` says 3. The same two-read-then-two-save trace takes a room from 5 members to 7, and takes a non-member to two `room_member` rows. `RoomMemberEntity` has no unique `(room_id, user_id)`, so the database does not stop the duplicate. `RoomServiceUnitTest` stubs `countRoomsByUserId`, so it never runs these two calls together.

A counterexample whose single caller is already at the cap means the check is gone or the parser can no longer see it. That is a different bug from the race. Exit `1` names the missing flag. A `TypeOk` failure is a modeling error, not a product bug.

## Coverage record

Confirmed against commit `cc8e6c0c4d0448aa335357413be7878dadf0553c`.

- `RoomServiceImplt.createRoom` — the `>= 3` check is immediately before `generateUniqueRoomCode` and `roomRepository.save`
- `RoomServiceImplt.joinRoom` — the `>= 3` check, then `members.size() >= 6`, then `alreadyMember`, then `roomRepository.save`
- `RoomRepository.countRoomsByUserId` — `SELECT COUNT(r) FROM RoomEntity r JOIN r.members m WHERE m.user.id = :id`, no lock
- `RoomMemberEntity` — `@Table` indexes only, no unique constraint on the pair

Move this commit forward only after you have read those methods at the new commit and the spec still describes them: `Begin` is still that read, `Commit` is still that save, and the flags the script prints still match the branches. A green exit `0` is not by itself a reason to move it.

## Scope

This covers two concurrent callers, the counts above, and the checks those two methods perform before save. It does not cover role changes, leaving, deleting a room, room-code generation, or the UI. It does not run the Java. A pass is a pass of this model under these bounds.

Do not change `RoomServiceImplt` to silence the finding unless the user asks. Do not delete a guard in the spec so the race disappears.
