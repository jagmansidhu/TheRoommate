# Dashboard

The dashboard shows the signed-in person's email and how many rooms, chores due today, and upcoming bills they have.

## Sub-features

- `dashboard-open` shows the dashboard for the seeded user.
- `dashboard-counts` shows `Active Rooms` as `1` for that user.
- `dashboard-nav` returns to the dashboard from the header.

## How to get to it (user POV)

- Sign in. The app opens `/dashboard`.
- Choose `Dashboard` in the header.
- Choose the `TheRoommate` logo in the header while signed in.

## Driving it with verify-theroommate

Preconditions:

- `doctor: ok`.
- Signed in as `test1@example.com` through the sign-in recipe. The browser shows `Dashboard`.

- **Read the header.** The heading is `Dashboard`. The line under it is `Welcome back, test1@example.com`. The header contains `Dashboard`, `Calendar`, `Profile`, `Rooms`, `Personal`, and `Logout`.
- **Read the counts.** The stat labeled `Active Rooms` shows `1`. `Chores Due Today` and `Upcoming Bills` are visible. Their numbers come from due dates, so do not require a specific chore name.
- **Header entry.** Choose `Rooms`, then choose `Dashboard` in the header. The heading is `Dashboard` again. With one seeded room, `Rooms` itself opens the room detail. That is expected. The return trip is the dashboard entry under test.
- **Confirm stored rooms.** `GET http://localhost:18085/api/rooms` with the seeded cookie includes `University House`.
- **Proof.** Save `artifacts/dashboard/home.aria.txt` and `artifacts/dashboard/home.png`. `notes.txt` records `dashboard-counts` and the entry `header Dashboard`. The screenshot shows `TheRoommate`, `Dashboard`, `test1@example.com`, and `Active Rooms`.

## Gotchas

- Onboarding (`Welcome to DaRoommate`) replaces this page when the client believes there are zero rooms and `localStorage.hasSeenOnboarding` is not `true`. Treat onboarding as a failed dashboard proof.
- A fresh sign-in currently always takes this path. `App.jsx` stores `appRooms` as `{"data":[],"cachedAt":...}` before the load effect reads it, and that effect only fetches when the stored value is missing or the literal `[]`. No `/api/rooms` request is made. The seeded room is still on the API. Do not mark `dashboard-counts` verified from onboarding.
- `Active Rooms` is the rooms array length. A stale `localStorage.appRooms` from another origin does not apply here, because this stack is port 3100. Do not copy site data from port 3000.
- Chore due dates in the seed are relative to the boot time. `Chores Due Today` may be `0`. That is not a failed dashboard.
