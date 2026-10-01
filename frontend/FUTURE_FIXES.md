# Frontend design future fixes

Local stand-in for GitHub issues. `gh` is not set up; track work here until issues can be filed upstream.

**Sources:** design audit canvas (`frontend-design-audit.canvas.tsx`), deferred plan on `design/landing-future-plans` (`FUTURE_PLANS.md`), spot-check of working tree on 2026-08-05.

**Status key:** `Open` = still true on current checkout · `Partial` = fixed on a `design/*` branch but not on this tree · `Done` = skip (listed under Resolved for history)

**How to use:** pick an `FF-XX` id, implement on a focused branch, check acceptance criteria, mark Status `Done` with branch/PR note. When `gh` works, create one GitHub issue per `FF-XX` using the Title + body sections below.

---

## Reference pages (page consistency)

Use these as the visual source of truth when aligning other surfaces. Do not invent a new look; copy the living-paper language from the strongest pages.

| Rank | Surface | Why it is the reference | Key files |
|------|---------|-------------------------|-----------|
| 1 | **Rooms list** | Clearest in-app living-paper page: Syne labels, parchment cards, occupancy bars, burnt-sienna primary actions | `webpages/room/Rooms.jsx`, `styling/Rooms.css` |
| 2 | **Landing (Home)** | Strongest marketing atmosphere: grain, marquee, bento, Fraunces/Syne hierarchy | `webpages/Home.jsx`, `styling/Home.css` |
| 3 | **Login / Register** | Auth cards + dotted cream largely match brand (except `!important` button overrides) | `webpages/auth/*`, `styling/Components.css` |

**Weaker surfaces to bring into line (target = Rooms + Landing):**

| Surface | Current fit | Main gaps |
|---------|-------------|-----------|
| Dashboard / Profile | Partial | Email greeting, mixed chrome, leftover form/alert patterns |
| Calendar / Budget | Mostly aligned headers | Calendar header button shotgun selectors; Budget uses undefined `--lp-blue` |
| Room details | Split | `rd-*` hybrid; blue-leaning header/monogram on older trees |
| Onboarding | Out of system | Blue gradients / elevated white cards / `DaRoommate` copy |
| Messages / Friends | Dead | Files exist, not routed in `App.jsx` |

**Consistency rules for follow-up work:**

1. Tokens from living-paper (`--lp-*`, `--f-*`) only; no new Inter/blue SaaS stack.
2. Section headers use the Rooms eyebrow + Fraunces title pattern where the page is an app surface.
3. Standard actions use the consolidated `.btn` primitive (see FF-01), not page-local button families.
4. Surfaces share parchment/ink/border language from Rooms cards; landing bento may keep layout modifiers.
5. Copy says **TheRoommate**; roommate voice (rooms/members), not landlord voice (portfolio/residents).

---

## Issue index

| ID | Priority | Area | Title | Status |
|----|----------|------|-------|--------|
| FF-01 | P0 | Buttons | Consolidate button systems to one living-paper `.btn` | Done (`design/ff-01-btn-consolidate`) |
| FF-02 | P0 | Landing | Add full-bleed product imagery to landing hero | Open |
| FF-03 | P0 | Landing | Make TheRoommate brand hero-level in first viewport | Partial (`design/landing-future-plans`) |
| FF-04 | P0 | Tokens | Finish living-paper token consolidation (remove Inter/blue leftovers) | Partial (`design/tokens-orange-living-paper`) |
| FF-05 | P1 | Page consistency | Align weak pages to Rooms + Landing reference | Partial (`design/page-visual-consistency`) |
| FF-06 | P1 | Cards | Consolidate card/surface primitives | Open |
| FF-07 | P1 | App shell | Fix mobile nav labels (no icons, text hidden at 768px) | Open |
| FF-08 | P1 | Forms | Unify form focus vs disabled styles | Open |
| FF-09 | P1 | Copy | Fix brand typos, landlord framing, greeting, marketing claims | Open |
| FF-10 | P1 | Auth / Verify | Replace VerifyHandler phantom Tailwind + `alert()` | Open |
| FF-11 | P1 | Budget | Remove undefined `--lp-blue` usage | Partial (tokens branch) |
| FF-12 | P2 | Cleanup | Delete dead CSS and decide Friends/Message fate | Open |
| FF-13 | P2 | A11y | Apply `prefers-reduced-motion` app-wide | Open |
| FF-14 | P2 | Theming | Resolve half-built dark mode (ship or remove) | Open |
| FF-15 | P2 | PWA | Fix `manifest.json` CRA leftovers | Open |
| FF-16 | P2 | Primitives | Consolidate modal overlays, alerts, shared keyframes | Open |

### Resolved / skip (do not re-open as new work without re-checking)

These were called fixed in the audit session or live on design branches. Re-verify after merges:

- Button systems consolidated on `design/ff-01-btn-consolidate` (FF-01 Done).
- Brand string standardization started on `design/brand-theroommate` (current tree still has `DaRoommate` / `TheRoomate` leftovers; tracked in FF-09).
- Typographic hero brand on `design/landing-future-plans` (tracked as Partial in FF-03).
- Living-paper token restore on `design/tokens-orange-living-paper` (tracked as Partial in FF-04).
- Page consistency pass on `design/page-visual-consistency` (tracked as Partial in FF-05).

---

## Top priorities (start here)

1. **FF-04 Tokens** — Inter/`--primary-*` blue leftovers keep fighting living-paper pages.
2. **FF-05 Page consistency** — Onboarding + Room details + Dashboard toward Rooms/Landing.
3. **FF-03 + FF-02 Landing** — brand-level hero, then real imagery / full-bleed shell.
4. **FF-09 Copy** — quick trust wins (`TheRoommate`, roommate voice, name greeting).

---

## FF-01 — Consolidate button systems to one living-paper `.btn`

- **Priority:** P0
- **Area:** Buttons
- **Status:** Done (`design/ff-01-btn-consolidate`)
- **Related files:** `src/index.css`, `src/styling/Home.css`, `src/styling/Rooms.css`, `src/styling/Components.css`, `src/styling/Calendar.css`, `src/styling/Dashboard.css`, `src/webpages/Home.jsx`, `src/webpages/room/Rooms.jsx`, `src/webpages/auth/Login.jsx`, `src/webpages/auth/Register.jsx`, modals under `src/webpages/room/modals/`, `src/component/Calendar.jsx`

### Problem / current state (historical)

Four parallel systems plus specialized controls:

| System | Defined in | Used by |
|--------|------------|---------|
| Global `.btn` + modifiers | `index.css` | Modals, auth, onboarding, Budget, Calendar, App chrome, VerifyHandler |
| Landing `.btn-*-lp` / `.btn-cta-*` | `Home.css` | `Home.jsx` Links only |
| Rooms `.pm-btn` + modifiers | `Rooms.css` | `Rooms.jsx` header/empty-state |
| Auth overrides | `Components.css` | Login/Register via `.auth-submit` + `.auth-form .btn-primary` (`!important`) |

Same action looked different by page. Hover / `:focus-visible` / `:disabled` were incomplete. Naming trap: Rooms `.pm-btn-ghost` was a parchment secondary box; landing `.btn-ghost-lp` was an underline text link.

### Proposed fix (concrete plan)

**Canonical API (CSS-first; no React `<Button>` required yet):**

```text
.btn                 // base: Syne, uppercase, tracking, 4px, inline-flex
.btn-primary         // --lp-orange fill + matching border
.btn-secondary       // parchment + ink border  (alias target for pm-btn-ghost)
.btn-ghost           // optional bordered transparent
.btn-danger          // transparent + sienna border/text
.btn-sm / .btn-lg    // density
.btn-block or .w-full
.btn-link            // NEW: underline text CTA (landing Sign in)
.btn-on-dark         // NEW: cream/muted on dark CTA band
```

**Visual source of truth:** Rooms + landing filled primary (not auth’s borderless `!important` primary).

Target specs:

- Font: `var(--f-label)`, weight 700, `letter-spacing: ~0.07em`, `text-transform: uppercase`
- Radius: `4px`
- Padding: default `10px 22px`; sm `7px 14px`; lg `13px 28px`
- Primary: `var(--lp-orange)` fill/border, white text
- Secondary: `var(--lp-parchment)` + `1.5px solid var(--lp-border)`, ink text
- Add hover / `:focus-visible` / `:disabled` on the primitive
- Prefer opt-in `.btn` over styling every bare `button` (protects Calendar nav, password toggles, leave pills)

**Migration map:**

| Old | New | Action |
|-----|-----|--------|
| `.pm-btn` | `.btn` | Replace in `Rooms.jsx`; delete Rooms button block |
| `.pm-btn-primary` | `.btn-primary` | Direct |
| `.pm-btn-ghost` | `.btn-secondary` | Rename (semantic fix) |
| `.btn-primary-lp` | `.btn .btn-primary .btn-lg` | Hero primary Link |
| `.btn-ghost-lp` | `.btn-link` | Hero secondary |
| `.btn-cta-primary` | `.btn .btn-primary .btn-lg` (+ dark band context) | Bottom CTA |
| `.btn-cta-ghost` | `.btn-link .btn-on-dark` | Dark band underline |
| Auth `!important` block | `.btn .btn-primary .btn-lg .w-full` | Delete after base matches |
| Calendar header `button:not(.nav-btn)` | explicit `.btn.btn-primary` on Create Event | Stop shotgun primary |

**Keep out of phase 1:** `.nav-btn`, `.close-btn`, `.rd-back-btn` / `.rd-leave-btn`, `.pm-card-action-btn`, `.quick-action-btn`, `.password-toggle`, `.onboarding-skip`, `.modal-close`.

### Acceptance criteria

- [x] One living-paper `.btn` primitive used for standard actions app-wide
- [x] Grep clean: no `.pm-btn`, `.btn-primary-lp`, `.btn-ghost-lp`, `.btn-cta-`, `.auth-submit` button styling
- [x] Consistent hover / `:focus-visible` / `:disabled`
- [x] No `!important` in button typography/color rules
- [ ] Visual pass: Landing hero + CTA band, Rooms header, Login/Register, one room modal (primary/secondary/danger), Calendar create + delete, Budget sm actions (pending manual check)

---

## FF-02 — Add full-bleed product imagery to landing hero

- **Priority:** P0
- **Area:** Landing
- **Status:** Open
- **Related files:** `src/webpages/Home.jsx`, `src/styling/Home.css`, `src/App.jsx`, `src/styling/App.css`, `frontend/public/`

### Problem / current state

Landing first viewport has no edge-to-edge product imagery. The image column was removed from `Home.css`. `frontend/public/` has no real hero assets. Emoji / fake chrome stand in. Landing also sits inside app `content-wrapper` padding (`App.jsx`), so marketing cannot go full-bleed.

### Proposed fix

1. Break `/` out of (or override) `content-wrapper` padding so the hero can be edge-to-edge.
2. Add real asset(s) under `frontend/public/` (shared-living atmosphere or product UI shot).
3. Keep hero budget: brand, one headline, one short supporting sentence, one CTA group, one dominant image. No badges/chips/callouts on media.
4. Pair with FF-03 (brand-level wordmark) so imagery does not replace the brand signal.

### Acceptance criteria

- [ ] First viewport includes a dominant full-bleed (or edge-to-edge) visual plane
- [ ] Brand test still passes with imagery (TheRoommate unmistakable with nav removed)
- [ ] Mobile crop is intentional and clean
- [ ] Motion respects `prefers-reduced-motion`
- [ ] Hero is not constrained by app `content-wrapper` padding

---

## FF-03 — Make TheRoommate brand hero-level in first viewport

- **Priority:** P0
- **Area:** Landing / Brand
- **Status:** Partial — implemented on `design/landing-future-plans` (`h1.hero-brand`), missing on current checkout (eyebrow + headline only; name in subcopy/nav/footer)
- **Related files:** `src/webpages/Home.jsx`, `src/styling/Home.css`

### Problem / current state

On current tree, first viewport brand signal is weak: eyebrow “Shared living, sorted”, headline “Your home, finally in sync.”, name only in subcopy/nav/footer. Fails brand test (page could belong to another product after removing nav).

### Proposed fix

Restore pattern from `design/landing-future-plans`:

- `h1.hero-brand` “TheRoommate” as hero-level mark
- Demote tagline to supporting line (`p.hero-title` or equivalent)
- Remove competing eyebrow if it fights the brand
- Keep reduced-motion-safe sizing on mobile

### Acceptance criteria

- [ ] With nav removed, first viewport still reads as TheRoommate
- [ ] Brand is larger/stronger than the supporting headline
- [ ] No secondary marketing blocks in the first viewport beyond brand, one headline, one sentence, CTA group (+ imagery from FF-02)

---

## FF-04 — Finish living-paper token consolidation

- **Priority:** P0
- **Area:** Tokens
- **Status:** Partial — `design/tokens-orange-living-paper` restores many surfaces; current tree still has dual stacks in `:root`
- **Related files:** `src/index.css`, `src/styling/Onboarding.css`, `src/styling/RoomDetails.css`, `src/styling/Dashboard.css`, `src/styling/App.css`, `src/styling/Components.css`, `src/webpages/Budget.jsx`

### Problem / current state

`index.css` still defines living-paper fonts (`Fraunces` / `Syne` / `DM Mono`) alongside legacy `--font-family: Inter…` and blue `--primary-500: #3b82f6`. Onboarding still uses blue gradients. Focus rings and some chrome still bind to blue primary tokens. Home.css also redefines LP tokens locally.

### Proposed fix

1. Merge/rebase `design/tokens-orange-living-paper` (or re-apply) onto the active design line.
2. Make `--lp-*` / `--f-*` the only brand tokens for UI color/type.
3. Delete or alias-away Inter/blue leftovers once no selectors depend on them.
4. Stop redefining LP tokens in page CSS; import from `:root` only.
5. Coordinate with FF-11 (`--lp-blue`).

### Acceptance criteria

- [ ] No Inter/Roboto as intentional brand fonts in UI chrome
- [ ] Primary interactive color is burnt sienna (`--lp-orange`), not `#3b82f6`, on app pages
- [ ] Onboarding / Room details / Dashboard do not reintroduce blue SaaS gradients as the page identity
- [ ] Page CSS does not redefine a second LP token block without reason

---

## FF-05 — Align weak pages to Rooms + Landing reference

- **Priority:** P1
- **Area:** Page consistency
- **Status:** Partial — `design/page-visual-consistency` exists; re-verify after merge
- **Related files:** `src/webpages/OnboardingPage.jsx`, `src/styling/Onboarding.css`, `src/webpages/room/RoomDetailsPage.jsx`, `src/styling/RoomDetails.css`, `src/webpages/Dashboard.jsx`, `src/styling/Dashboard.css`, `src/webpages/Profile.jsx`, `src/styling/Personal.css`, `src/styling/Calendar.css`

### Problem / current state

Audit consistency map: Rooms and Landing are strong; auth aligned; Onboarding out of system; Room details split; Dashboard/Profile partial. Users feel like they changed products when moving between routes.

### Proposed fix

1. Diff each weak page against Rooms section chrome (eyebrow, title, parchment atmosphere) and Landing type scale.
2. Prefer merging `design/page-visual-consistency`, then close remaining gaps.
3. Room details: keep `rd-*` layout if needed, but retune header/monogram/borders to LP ink/orange/parchment.
4. Onboarding: drop blue gradient identity; use auth-card / Rooms parchment language.
5. Dashboard/Profile: match Rooms header + tokenized surfaces; fix greeting via FF-09.

### Acceptance criteria

- [ ] Onboarding, Room details, Dashboard, Profile, Calendar, Budget share living-paper tokens and section-header pattern with Rooms
- [ ] No page reintroduces a competing blue/Inter visual system
- [ ] Side-by-side visual check: Rooms vs each migrated page at desktop + ~375px

---

## FF-06 — Consolidate card / surface primitives

- **Priority:** P1
- **Area:** Cards / Tokens
- **Status:** Open
- **Related files:** `src/index.css`, `src/styling/Rooms.css`, `src/styling/RoomDetails.css`, `src/styling/Home.css`, `src/styling/Components.css`, `src/styling/Onboarding.css`, `src/styling/Dashboard.css`, `src/styling/Personal.css`

### Problem / current state

Parallel surfaces: `.card`, `.stat-card`, `.bento-card`, `.pm-card`, `.rd-card`, `.auth-card`, `.onboarding-card`, `.upload-card`, etc. Radius, border, shadow, and type drift across ~10 stylesheets.

### Proposed fix

1. Define one shared living-paper surface in `index.css` (padding, border, radius, background).
2. Migrate page cards to modifiers or layout wrappers; keep interaction-only “card” patterns where removing the container would hurt UX.
3. Landing bento may keep layout modifiers but must share border/ink/parchment tokens.
4. Do not bundle this into FF-01; ship after or beside button work.

### Acceptance criteria

- [ ] Shared surface primitive documented and used on Rooms, Dashboard, Room details, auth
- [ ] Parallel card class families reduced; dead duplicate surface rules removed
- [ ] Landing bento shares tokenized border/background language

---

## FF-07 — Fix mobile nav labels at ≤768px

- **Priority:** P1
- **Area:** App shell
- **Status:** Open
- **Related files:** `src/styling/App.css`, `src/App.jsx`

### Problem / current state

At `max-width: 768px`, `App.css` hides `.nav-link span` (and logo text) with `display: none`. Links are text-only with no icons and no hamburger/drawer. Users get unlabeled chrome plus Logout in a thin bar.

### Proposed fix

Pick one:

1. Hamburger / drawer with full labels, or
2. Icon + accessible `aria-label` / visually-hidden text per link, or
3. Keep abbreviated visible labels (do not hide text without a replacement)

### Acceptance criteria

- [ ] Every primary nav destination remains understandable at ≤768px
- [ ] Logo/brand remains identifiable on small screens
- [ ] Keyboard and screen-reader labels present
- [ ] No reliance on hover-only disclosure

---

## FF-08 — Unify form focus vs disabled styles

- **Priority:** P1
- **Area:** Forms
- **Status:** Open
- **Related files:** `src/index.css`, `src/styling/App.css`, `src/styling/Dashboard.css`

### Problem / current state

- `index.css`: focus uses living-paper orange offset shadow; disabled uses parchment + opacity
- `App.css` `.form-input:focus`: blue primary ring (`--primary-500` / `--primary-100`)
- `App.css` `.form-input:disabled`: orange offset shadow + cream — looks like focus, not disabled
- `Dashboard.css` also redefines `.form-input:focus` / `:disabled`

### Proposed fix

1. Living-paper focus in `index.css` is the single source of truth.
2. Delete or align `.form-input:focus` / `:disabled` in `App.css` and `Dashboard.css`.
3. Disabled must not use the focus offset shadow.

### Acceptance criteria

- [ ] One focus style for text inputs app-wide (living-paper)
- [ ] Disabled inputs do not use the focus offset shadow
- [ ] Spot-check: modals, auth, Budget, Room details forms

---

## FF-09 — Fix copy and UX writing leftovers

- **Priority:** P1
- **Area:** Copy
- **Status:** Open (partial brand fixes on `design/brand-theroommate`; current tree still broken)
- **Related files:** `src/webpages/room/Rooms.jsx`, `src/webpages/auth/Register.jsx`, `src/webpages/OnboardingPage.jsx`, `src/webpages/room/RoomOnboarding.jsx`, `src/webpages/Dashboard.jsx`, `src/webpages/Home.jsx`, `src/webpages/VerifyHandler.jsx`

### Problem / current state (spot-checked 2026-08-05)

| Problem | Location |
|---------|----------|
| “Property Portfolio” / “Residents” landlord framing | `Rooms.jsx` |
| “Join TheRoomate…” typo | `Register.jsx` |
| “Welcome to DaRoommate” | `OnboardingPage.jsx` |
| “Welcome to DaRoommate!” | `RoomOnboarding.jsx` |
| Dashboard greets with raw email | `Dashboard.jsx` |
| Marketing oversell: “Smart reminders”, “Shared groceries”, “no extra accounts” | `Home.jsx` marquee / how-it-works |
| Verify resend uses `alert()` | `VerifyHandler.jsx` (also FF-10) |

### Proposed fix

- Roommate-first labels on Rooms (rooms / members, not portfolio / residents)
- Greet with display name / first name when available; email fallback only
- Align marketing claims to shipped features (or mark aspirational deliberately)
- Spell **TheRoommate** everywhere in routed UI
- Replace `alert()` with in-page status (with FF-10)

### Acceptance criteria

- [ ] No `DaRoommate` / `TheRoomate` strings in routed UI
- [ ] Rooms copy matches roommate product voice
- [ ] Dashboard greeting prefers name over raw email
- [ ] Landing claims match product reality (or are explicitly approved as future)

---

## FF-10 — Replace VerifyHandler phantom Tailwind + `alert()`

- **Priority:** P1
- **Area:** Auth / Verify
- **Status:** Open
- **Related files:** `src/webpages/VerifyHandler.jsx`, auth/alert styles in `src/styling/Components.css` / `src/index.css`

### Problem / current state

`VerifyHandler.jsx` uses undefined utility classes (`flex`, `flex-col`, `items-center`, `justify-center`, `h-screen`, `text-lg`, `text-green-600`). App is plain global CSS, so success styling does not apply. Resend feedback uses `window.alert`.

### Proposed fix

- Replace phantom utilities with living-paper classes or a small page-scoped stylesheet
- Replace `alert()` with in-page status messaging consistent with auth/error patterns

### Acceptance criteria

- [ ] No undefined utility classes on VerifyHandler
- [ ] Success/error states use living-paper tokens/typography
- [ ] Resend feedback does not use `window.alert`

---

## FF-11 — Remove undefined `--lp-blue` in Budget

- **Priority:** P1
- **Area:** Budget / Tokens
- **Status:** Partial on tokens branch; still present in current `Budget.jsx`
- **Related files:** `src/webpages/Budget.jsx`, `src/index.css`

### Problem / current state

Inline styles reference `var(--lp-blue)`, which is not defined in the living-paper token set. Links/colors silently fail.

### Proposed fix

Replace with an existing token (`--lp-orange` for action links, or ink/muted for secondary links). Do not invent `--lp-blue` unless product explicitly wants a second accent.

### Acceptance criteria

- [ ] No `--lp-blue` references in frontend
- [ ] Budget link/action colors render with defined tokens

---

## FF-12 — Delete dead CSS and decide Friends/Message fate

- **Priority:** P2
- **Area:** Cleanup
- **Status:** Open
- **Related files:** `src/styling/Dashboard.css` (legacy `.room-details-*` ~line 622+), `src/webpages/Friends.jsx`, `src/webpages/Message.jsx`, `src/App.jsx`, unused blocks in `src/styling/Components.css`

### Problem / current state

Legacy room-details rules still live in `Dashboard.css` while the page uses `RoomDetails.css`. `Friends.jsx` / `Message.jsx` exist but are not routed. Unused Components blocks (profile/complete-profile leftovers) add drift.

### Proposed fix

1. Grep for className references before deleting.
2. Remove proven-dead CSS blocks.
3. Either route Friends/Message with intentional UX or remove/archive the placeholders.

### Acceptance criteria

- [ ] Legacy room-details rules removed from `Dashboard.css` (or proven still used)
- [ ] Friends/Message either routed with real UX or removed from the tree
- [ ] Unused Components CSS cleaned or clearly marked
- [ ] No broken references after cleanup

---

## FF-13 — Apply `prefers-reduced-motion` app-wide

- **Priority:** P2
- **Area:** A11y / Motion
- **Status:** Open (no matches in current frontend CSS tree)
- **Related files:** `src/index.css`, `src/styling/Home.css`, `src/styling/Rooms.css`, `src/styling/Calendar.css`, `src/styling/Onboarding.css`, other animated sheets

### Problem / current state

Audit expected landing coverage; current checkout has **no** `prefers-reduced-motion` rules. Marquee, reveals, card rotations, page-enter, and other transitions ignore the OS setting.

### Proposed fix

1. Add a global reduced-motion block in `index.css` (short-circuit non-essential animation/transition).
2. Audit infinite loops / large transforms (Home marquee, Rooms card tilt, Onboarding, modals).
3. Keep page-specific exceptions only when needed.

### Acceptance criteria

- [ ] With OS reduce-motion on, non-essential animations/transitions are disabled or minimized app-wide
- [ ] Essential UI state changes remain understandable without motion
- [ ] Landing marquee/reveals compliant

---

## FF-14 — Resolve half-built dark mode

- **Priority:** P2
- **Area:** Theming
- **Status:** Open
- **Related files:** `src/App.jsx` (`ThemeProvider`), dark rules in `Components.css` / `Dashboard.css` / `RoomDetails.css` / `Onboarding.css`

### Problem / current state

`ThemeProvider` sets `data-theme` from `localStorage darkMode` and exposes `toggleTheme`, but there is no UI toggle. Living-paper pages largely ignore dark tokens. Users can land in a mixed shell from leftover localStorage.

### Proposed fix

Choose one:

1. **Ship dark:** living-paper dark palette + visible toggle + audit primary pages, or
2. **Defer/remove:** remove persistence / dark activation until designed; gate or delete orphaned dark rules

### Acceptance criteria

- [ ] Explicit product decision recorded in this file when closed
- [ ] If shipping: toggle visible; living-paper pages look intentional in dark
- [ ] If deferring: no silent dark activation from localStorage; orphaned dark rules removed or gated

---

## FF-15 — Fix `manifest.json` CRA leftovers

- **Priority:** P2
- **Area:** PWA / Brand
- **Status:** Open
- **Related files:** `frontend/public/manifest.json`

### Problem / current state

Manifest still says “Create React App Sample” / “React App”, with `theme_color` `#000000` and `background_color` `#ffffff`.

### Proposed fix

- Set `name` / `short_name` to TheRoommate
- Align theme/background to living-paper cream (e.g. `#F5F0E8`) and ink as appropriate
- Confirm icons acceptable or replace later

### Acceptance criteria

- [ ] Manifest name/short_name are TheRoommate
- [ ] Theme/background colors match brand (not CRA black/white defaults)
- [ ] No “Create React App Sample” string remains

---

## FF-16 — Consolidate modal overlays, alerts, and shared keyframes

- **Priority:** P2
- **Area:** Primitives
- **Status:** Open
- **Related files:** `src/index.css`, `src/styling/App.css`, `src/styling/Dashboard.css`, `src/styling/Rooms.css`, `src/styling/Personal.css`, `src/styling/Components.css`, modal JSX under `src/webpages/room/`

### Problem / current state

- Modals: `.modal-backdrop` ≈ `.modal-overlay` across sheets (JSX mostly uses `.modal-overlay`)
- Alerts: `.error-message`, `.pm-alert`, `.alert-error`, `.personal-toast` parallel patterns
- Keyframes (`spin`, `page-enter`) copied across sheets
- Password-rules CSS lives in `Dashboard.css` while Register loads `Components.css`

### Proposed fix

1. One modal overlay/backdrop convention
2. One primary alert/toast pattern for app feedback
3. Move shared keyframes to `index.css`; delete safe copies
4. Relocate password-rules CSS next to auth styles

### Acceptance criteria

- [ ] Single modal overlay/backdrop convention
- [ ] Primary error/success feedback uses one shared pattern (pages may thin-wrap)
- [ ] Password strength rules styled from auth-related CSS, not Dashboard
- [ ] Duplicate keyframes removed where safe

---

## Appendix A — Button system (full plan excerpt)

Absorbs `frontend/FUTURE_PLANS.md` from branch `design/landing-future-plans`. Implement only when FF-01 is intentionally picked up.

### Variants actually used in JSX

| Intent | Classes today | Notes |
|--------|---------------|-------|
| Primary | `.btn.btn-primary`, `.pm-btn.pm-btn-primary`, `.btn-primary-lp`, `.btn-cta-primary` | Filled sienna; CTA band uses cream text on dark section |
| Secondary / quiet fill | `.btn.btn-secondary`, `.pm-btn.pm-btn-ghost` | Parchment + border. Rooms “ghost” ≠ transparent ghost |
| Ghost / text | `.btn-ghost-lp`, `.btn-cta-ghost` | Underline text links on landing |
| Danger | `.btn.btn-danger` | Outline sienna; delete/remove modals + Calendar |
| Size sm | `.btn-sm` | Budget only |
| Size lg | `.btn-lg` | Login/Register (+ auth `!important`) |
| Full width | `.w-full` + auth width rules | Auth submit |
| Icon-only | `.icon-btn` (Budget/Personal.css) | Not `.btn-icon` (defined in `index.css`, unused) |

**Defined but unused in JSX:** `.btn-ghost`, `.btn-icon`, `.pm-btn-danger`.

### Temporary aliases (optional bridge)

```css
/* deprecate during Phase 2, delete in Phase 3 */
.pm-btn { /* alias → .btn */ }
.btn-primary-lp { /* alias → .btn.btn-primary.btn-lg */ }
```

Prefer short-lived aliases + mechanical JSX search/replace over permanent dual APIs.

### Out of scope for FF-01

- Card surface consolidation (FF-06)
- React `<Button>` component (optional later)
- Room Details action rows / Profile quick actions (follow-up if still off after Phase 3)

---

## Appendix B — Product imagery notes

From deferred landing plan:

- Goal: edge-to-edge visual plane for `/` first viewport (chores, bills, shared-living atmosphere, or real product UI shot). Not an inset card, side panel, collage, or floating media block.
- Prerequisites: full-bleed shell escape from `content-wrapper`; real assets under `frontend/public/`.
- Acceptance tied to FF-02.

---

## Appendix C — Filing to GitHub later

When `gh` works, create issues from each `FF-XX` section:

```bash
# Example
gh issue create \
  --title "Consolidate frontend button systems to living-paper .btn" \
  --body-file /tmp/ff-01.md \
  --label "enhancement,frontend"
```

Map Priority: P0 → urgent/design debt, P1 → enhancement, P2 → backlog/tech-debt (use whatever labels exist). Do not invent many new labels.
