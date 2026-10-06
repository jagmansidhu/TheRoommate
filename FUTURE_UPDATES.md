# Future Updates & Deferred Work

Tracked improvements that are intentionally deferred — not forgotten.
Each item has a reason for deferral and suggested trigger for when to revisit.

---

## 🔵 CRA → Vite Migration

**Status:** Deferred  
**Revisit when:** CRA blocks a dependency upgrade, or the 16 react-scripts HIGH vulns become CRITICALs.

**What it is:**  
`create-react-app` (`react-scripts`) is effectively unmaintained since 2022. Its internal webpack toolchain contains 16 HIGH-severity vulnerabilities that cannot be patched without a breaking upgrade. These only affect the **dev server**, not the production nginx bundle — which is why this is deferred and not urgent.

**What migration looks like:**
1. `npm create vite@latest . -- --template react`
2. Move `index.html` to project root (Vite standard)
3. Replace `process.env.REACT_APP_*` with `import.meta.env.VITE_*`
4. Update `frontend/Dockerfile` to use `npm run build` (same command, different output dir: `dist/` not `build/`)
5. Update `nginx.conf` to serve from `dist/`
6. Remove `react-scripts`, `ajv` override, `--legacy-peer-deps` hack

**Estimated effort:** 3–4 hours  
**Benefit:** Eliminates all 16 trapped HIGH vulns, 10–50× faster HMR, smaller bundle output.

---

## 🔵 Email Verification Re-enablement

**Status:** Deferred until SMTP is configured  
**Revisit when:** `EMAIL_HOST` / `EMAIL_ID` / `EMAIL_PASSWORD` env vars are set in Railway.

**What to do:**
1. In [`UserEntity.java`](file:///Users/jagman/IdeaProjects/TheRoommate/backend/src/main/java/com/roomate/app/entities/UserEntity.java#L37): change `private boolean enabled = true;` → `private boolean enabled = false;`
2. The `VerificationToken` flow already exists in `UserServiceImplementation` — just needs the default flipped.
3. Test with a real SMTP provider (Gmail App Password or SendGrid free tier).

---

## 🔵 Re-enable CSRF Protection

**Status:** Deferred (disabled in SecurityConfig)  
**Revisit when:** Before any public launch or when hardening security.

**What to do:**  
See [`CSRF_PROTECTION.md`](file:///Users/jagman/IdeaProjects/TheRoommate/CSRF_PROTECTION.md) for the double-submit cookie plan.  
The `apiClient.js` interceptor is already wired — only server-side needs to be switched back on.

---

## 🔵 Add Pagination to List Endpoints

**Status:** Deferred  
**Revisit when:** A room accumulates 100+ chores or ledger entries, or API response times increase.

**Affected endpoints:** `/api/chores/{roomId}`, `/api/utility/{roomId}`, `/api/budget/entries`, `/api/grocery`, `/api/ledger`  
**What to do:** Add `Pageable` param to repositories + controllers, return `Page<T>` DTOs.

---

## 🔵 `@RestControllerAdvice` Global Exception Handler

**Status:** Deferred  
**Revisit when:** Adding more controllers or when debugging becomes painful.

**What to do:** Create `GlobalExceptionHandler.java` in `config/` that maps:
- `UserApiError` → 400/409
- `EntityNotFoundException` → 404  
- Unhandled `Exception` → 500 with logged trace ID  
Remove the boilerplate `try/catch` from every controller method.

---

## 🔵 Split `App.jsx` into Provider Files

**Status:** Deferred  
**Revisit when:** Adding a new provider or when the file grows past 600 lines.

**What to do:** Extract into:
- `src/providers/AuthProvider.jsx`
- `src/providers/UserProvider.jsx`  
- `src/providers/AppDataProvider.jsx`
- `src/context/index.js` (all context exports)
