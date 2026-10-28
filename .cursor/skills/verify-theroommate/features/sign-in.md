# Sign in

Sign in lets a person with an account open the dashboard, and it shows an error when the password is wrong.

## Sub-features

- `sign-in-open` shows the login form from each logged-out entry.
- `sign-in-submit` accepts the seeded user and opens the dashboard.
- `sign-in-reject` keeps a wrong password on the login form with an error.

## How to get to it (user POV)

- Open `http://localhost:3100/login`.
- On the logged-out header, choose `Sign In`.
- Open `http://localhost:3100/dashboard` while logged out. The login form is that page.
- On `http://localhost:3100/register`, choose the `Sign in` link.

## Driving it with verify-theroommate

Preconditions:

- `doctor: ok` for `http://localhost:3100` and `http://localhost:18085`.
- Browser origin is `http://localhost:3100` with empty site data for this run.
- Seeded user `test1@example.com` / `Test_123!` exists.

- **Open the form.** Go to `http://localhost:3100/login`. The heading reads `Welcome back`. The textbox `Email address`, the textbox `Password`, and the button `Sign in` are on the page. The header link `TheRoommate` is visible.
- **Reject a bad password.** Fill `Email address` with `test1@example.com` and `Password` with `wrong-password`. Choose `Sign in`. The heading stays `Welcome back` and the text `Invalid email or password. Please try again.` appears. The URL stays on `/login`.
- **Sign in.** Fill `Email address` with `test1@example.com` and `Password` with `Test_123!`. Choose `Sign in`. The heading changes to `Dashboard` and the page contains `Welcome back, test1@example.com`. The stat `Active Rooms` shows `1`.
- **Confirm the session stored a membership.** From a shell, log in with the cookie jar and request rooms. `POST http://localhost:18085/user/login` with that email and password returns HTTP 200. `GET http://localhost:18085/api/rooms` with that cookie returns a JSON array whose name is `University House`. Discard the login body and the cookie jar. Do not put them in artifacts.
- **Proof.** Save an accessibility snapshot and a screenshot of the dashboard to `artifacts/sign-in/dashboard.aria.txt` and `artifacts/sign-in/dashboard.png`. Write `artifacts/sign-in/notes.txt` with the feature id `sign-in-submit` and the entry `/login`. The screenshot shows `TheRoommate`, `Dashboard`, and `test1@example.com`.

## Gotchas

- `login()` in the client sets `localStorage.appAuth` as soon as the HTTP call returns. A dashboard heading without `Active Rooms` equal to `1` does not prove the cookie was stored.
- After a successful sign-in the client still lands on onboarding, heading `Welcome to DaRoommate`, and never requests `/api/rooms`. `App.jsx` writes `appRooms` as `{"data":[],"cachedAt":...}` in an effect that runs before the effect that would fetch. That fetch only runs when `appRooms` is missing or the literal string `[]`, so the object form skips it. `Active Rooms` equal to `1` fails for that reason. Do not click `Skip for now` and call the count verified. The seed is fine: `GET /api/rooms` with the login cookie still returns `University House`.
- An empty rooms response shows onboarding, heading `Welcome to DaRoommate`, instead of the dashboard.
- HTTP 429 on the form shows `Too many requests. Please wait a moment and try again.` Do not keep retrying the bad password.
- The login response body contains a JWT. Leave it out of `artifacts/`.
- `/rooms` with this seeded user redirects away from the list. That redirect is the rooms feature, not this one.
