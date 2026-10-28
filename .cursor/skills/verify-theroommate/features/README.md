# TheRoommate verification map

This directory is the maintained source for verifying the user-facing behavior of TheRoommate. Read this index before driving the app, then use the matching feature file as the recipe.

## Baseline preconditions

- Launch with `.cursor/skills/verify-theroommate/scripts/launch.sh`.
- Web UI at `http://localhost:3100`. API at `http://localhost:18085`.
- Compose project `theroommate-verify` with its own volumes. Do not drive `localhost:3000` or `localhost:8085` from this map.
- Seeded user `test1@example.com` / `Test_123!`, already a member of `University House`.
- Run `.cursor/skills/verify-theroommate/scripts/doctor.sh` and require `doctor: ok`.
- Never drive an instance that was not started by this verification run.

## Driving conventions

- Start every recipe from the baseline state unless its preconditions say otherwise.
- Prefer ARIA roles and accessible names over CSS selectors or screen coordinates.
- Treat every command as literal. Keep quoted names unchanged.
- Drive the browser with the Cursor IDE browser tools.
- Confirm stored rooms with `curl` and the login cookie, as written in the skill. Do not save the login body.
- Cleanup may drop the verification database. Do not remove proof artifacts.

## Proof and skip reporting

- Capture the user action and the resulting state, not only the final screen.
- UI proof includes an accessibility snapshot and a screenshot with `TheRoommate` visible.
- Mutation proof includes a second view of the saved room.
- Record the feature id and entry point in `artifacts/<feature-id>/notes.txt`.
- Report an unreachable path with the attempted step and the unmet precondition.
- Do not report a skipped entry point as verified through a different path.

## Feature entry contract

Each feature file starts with an H1 title and one paragraph describing the user-visible behavior. It then uses exactly four H2 sections in this order.

1. `Sub-features` lists short IDs with one line for each behavior.
2. `How to get to it (user POV)` lists every user entry point.
3. `Driving it with verify-theroommate` starts with `Preconditions:` and uses labeled bullets that pair each user action with an exact step and observable result.
4. `Gotchas` lists traps that can waste or invalidate a verification run.

## Features

- [Sign in](./sign-in.md) covers the login form, a bad password, and landing on the dashboard as the seeded user.
- [Register](./register.md) covers creating an account and returning to sign-in.
- [Dashboard](./dashboard.md) covers the seeded user's home counts.
- [Rooms](./rooms.md) covers the room list and the single-room redirect.
- [Create a room](./create-room.md) covers the create-room modal and a saved room on the list.
