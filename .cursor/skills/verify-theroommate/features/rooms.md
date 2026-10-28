# Rooms

Rooms shows the signed-in person's rooms. With exactly one room, opening the list goes straight into that room. The list itself stays put when the address includes `manage=true`.

## Sub-features

- `rooms-redirect` opens the only seeded room from the `Rooms` header link.
- `rooms-list` shows `My Rooms` and `University House` at `/rooms?manage=true`.
- `rooms-empty` is the list for a person with no rooms. The seeded user cannot show it.

## How to get to it (user POV)

- Choose `Rooms` in the header.
- Open `http://localhost:3100/rooms`.
- Open `http://localhost:3100/rooms?manage=true` to stay on the list.

## Driving it with verify-theroommate

Preconditions:

- `doctor: ok`.
- Signed in as `test1@example.com`.
- That user is a member of exactly one room, `University House`.

- **Redirect entry.** Choose `Rooms` in the header. The address becomes `/rooms/<id>` and the heading is `University House`. The page is not titled `My Rooms`.
- **List entry.** Go to `http://localhost:3100/rooms?manage=true`. The heading is `My Rooms`. A card heading reads `University House`. The address line contains `123 College Ave, Apt 4B`. The role tag reads `Owner`. The buttons `Join with Code` and `+ New Room` are on the page.
- **Open the card.** Choose the `University House` card. The address becomes `/rooms/<id>` and the heading is `University House`.
- **Confirm stored rooms.** `GET http://localhost:18085/api/rooms` includes one object named `University House`.
- **Proof.** Save the list, before opening the card, to `artifacts/rooms/list.aria.txt` and `artifacts/rooms/list.png`. `notes.txt` records `rooms-list` and the entry `/rooms?manage=true`.

## Gotchas

- The seeded user has one room, so `/rooms` and the header `Rooms` link both redirect. Do not call the room detail page a proof of the list.
- The redirect looks at `window.location.search` for `manage=true`. Any other query still redirects.
- A second membership removes the redirect. If an earlier create-room run left a second room in this database, say so and do not pretend the redirect was verified.
- The empty state heading is `No rooms yet`. The seeded user does not reach it. A failure to load rooms can look like that empty state or like onboarding. Check the rooms JSON before describing an empty list as intended.
