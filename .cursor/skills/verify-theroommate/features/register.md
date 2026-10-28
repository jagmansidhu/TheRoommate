# Register

Register lets a new person create an account and then land on the sign-in form. The account can sign in without opening the verification email.

## Sub-features

- `register-open` shows the registration form.
- `register-create` accepts a new person and returns to sign-in.
- `register-duplicate` refuses an email that already exists.
- `register-weak` refuses a password that misses a rule, without calling the API.

## How to get to it (user POV)

- Open `http://localhost:3100/register`.
- On the login form, choose `Create one`.

## Driving it with verify-theroommate

Preconditions:

- `doctor: ok` for `http://localhost:3100` and `http://localhost:18085`.
- No account is registered as `verify-user@example.com`.

- **Open the form.** Go to `http://localhost:3100/register`. The heading reads `Create an account`. Textboxes `First name`, `Last name`, `Email address`, and `Password` are on the page. The button reads `Create account`.
- **Weak password.** Fill the four fields with `Verify`, `User`, `verify-user@example.com`, and `short`. Choose `Create account`. The heading stays `Create an account` and the page shows `At least 8 characters`. The URL stays on `/register`.
- **Create the account.** Replace `Password` with `Verify_123!`. Choose `Create account`. The heading changes to `Welcome back` and the URL is `/login`.
- **Sign in with the new account.** Fill `Email address` with `verify-user@example.com` and `Password` with `Verify_123!`. Choose `Sign in`. The heading reads `Dashboard`. This user has no room, so the page may instead show `Welcome to DaRoommate`. Either heading means the account was stored. `Welcome back` left on `/login` with `Invalid email or password` means it was not.
- **Duplicate email.** Open `/register` again and submit `Verify` / `User` / `test1@example.com` / `Verify_123!`. The form stays up and shows an error that the email already exists.
- **Proof.** Save a snapshot and a screenshot of the login page immediately after the successful create, before the new sign-in, to `artifacts/register/back-to-login.aria.txt` and `artifacts/register/back-to-login.png`. `notes.txt` records `register-create` and the entry `/register`.

## Gotchas

- Password rules are 8 characters, an uppercase letter, a lowercase letter, a number, and one of `@#$%^&+=!`. The rules list is visible only while the password field has focus.
- The button reads `Creating account...` while the request is in flight. Wait for the heading `Welcome back`, not a fixed delay.
- `UserServiceImplementation` sets `enabled` to true on register. Do not wait for the mail link. Mail may be unconfigured on this stack.
- HTTP 409 is the duplicate. HTTP 429 is the rate limit. They are different errors.
- Creating `verify-user@example.com` mutates the verification database. Cleanup drops that database. Keep the artifacts.
