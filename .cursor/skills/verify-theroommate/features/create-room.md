# Create a room

Create a room lets a member save a name, address, and description, then see that room on the list.

## Sub-features

- `create-open` opens the modal from the list.
- `create-save` stores the room and shows it on `My Rooms`.
- `create-cancel` closes the modal and does not add the draft.

## How to get to it (user POV)

- On `My Rooms`, choose `+ New Room`.
- On the empty list, choose `Create Room`.
- The seeded user has one room, so the list is reached at `/rooms?manage=true`. The header `Rooms` link does not stay on the list.

## Driving it with verify-theroommate

Preconditions:

- `doctor: ok`.
- Signed in as `test1@example.com`, currently in one room.
- No room is named `Verify Cottage`.
- The browser is on `http://localhost:3100/rooms?manage=true` and the heading is `My Rooms`.

- **Open the modal.** Choose `+ New Room`. The heading `Create New Room` appears, with textboxes `Room Name`, `Address`, and `Description`, and buttons `Cancel` and `Create Room`.
- **Cancel a draft.** Fill `Room Name` with `Discard Cottage`, then choose `Cancel`. The modal closes. The list has `University House` and has no `Discard Cottage`.
- **Save a room.** Choose `+ New Room` again. Fill `Room Name` with `Verify Cottage`, `Address` with `1 Verify Lane`, and `Description` with `Created by verification`. Choose `Create Room`. The modal closes. The list shows `Verify Cottage` and `University House`. The subtitle mentions `2 rooms`.
- **Confirm stored state.** Reload `http://localhost:3100/rooms?manage=true`. Both names are still on the list. `GET http://localhost:18085/api/rooms` includes `Verify Cottage` and `University House`.
- **Proof.** Save the list after reload to `artifacts/create-room/list.aria.txt` and `artifacts/create-room/list.png`. `notes.txt` records `create-save` and the entry `/rooms?manage=true`.

## Gotchas

- With exactly one room, `/rooms` redirects before the `+ New Room` button can be used. Start from `/rooms?manage=true`. If that redirect happens, the list entry was not reached. Report it. Do not create the room from another path and mark this one verified.
- The seeded user may belong to at most 3 rooms. A fourth create fails in the API. The modal text is the generic `Failed to create room. Please try again.` Leave the user at one room plus `Verify Cottage` only.
- After a second room exists, the header `Rooms` link no longer redirects. A later rooms-redirect proof on this same database will not see the one-room behavior. Say that in the rooms notes.
- `Cancel` must be chosen before `Create Room` on the discard draft. Submitting `Discard Cottage` stores it.
- Cleanup drops the verification database, including `Verify Cottage`. Keep the artifacts.
