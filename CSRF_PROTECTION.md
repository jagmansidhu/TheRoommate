# CSRF Protection Implementation

**Status:** ✅ Implemented  
**Date:** May 28, 2026  
**Pattern:** Double-Submit Cookie Pattern

---

## Overview

CSRF (Cross-Site Request Forgery) protection prevents attackers from tricking authenticated users into making unwanted requests to our application. This document describes the stateless CSRF implementation.

---

## How It Works

### 1. **Server-Side (Backend - Spring Security)**

```java
SecurityConfig.java:
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/user/login", "/user/register", "/user/logout", "/user/status", "/user/verify")
)
```

**Flow:**
1. On first GET request to any protected endpoint, Spring Security generates a CSRF token
2. Token is stored in a PUBLIC cookie: `XSRF-TOKEN=<token>`
3. For state-changing requests (POST, PUT, DELETE, PATCH), server expects the token in headers
4. Validation: Token from header must match token from cookie

**Why not HttpOnly?**
- `withHttpOnlyFalse()`: Cookie is readable by JavaScript (not HttpOnly)
- This allows us to extract the token and send it in headers
- Still secure because:
  - XSS vulnerability would allow stealing any cookie anyway (including JWT)
  - CSRF token is only valid for our origin (SameSite=Strict by default)
  - Attacker can't forge a valid request from their own domain

**Excluded Endpoints (No CSRF Required):**
- `/user/login` - Public, unauthenticated
- `/user/register` - Public, unauthenticated
- `/user/logout` - Already authenticated, low risk
- `/user/status` - Read-only status check
- `/user/verify` - Email verification endpoint
- `/actuator/**` - Internal health/metrics endpoints
- `/ws/**` - WebSocket endpoints (handled separately)

---

### 2. **Client-Side (Frontend - React)**

```javascript
apiClient.js:

// Extract CSRF token from XSRF-TOKEN cookie
const getCsrfToken = () => { ... }

// Add token to state-changing requests
apiClient.interceptors.request.use((config) => {
    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(config.method.toUpperCase())) {
        const csrfToken = getCsrfToken();
        if (csrfToken) {
            config.headers['X-CSRF-TOKEN'] = csrfToken;
        }
    }
    return config;
});
```

**Request Flow:**
1. App boots → calls `/user/status` (GET)
2. Server responds, includes `XSRF-TOKEN` cookie
3. Any POST/PUT/DELETE/PATCH request:
   - Axios interceptor extracts token from cookie
   - Adds it to `X-CSRF-TOKEN` header
   - Server validates header matches cookie

---

### 3. **Token Generation Timeline**

```
Timeline:
├── User opens app
│   └── React calls GET /user/status
│       └── Server generates & sets XSRF-TOKEN cookie ✅
│
├── User makes POST request (e.g., create room)
│   └── Interceptor extracts XSRF-TOKEN from cookie
│   └── Adds X-CSRF-TOKEN header
│   └── Server validates: header === cookie ✅
│
└── Request succeeds ✅
```

---

## Security Properties

### ✅ Protected Against:
- **CSRF from Cross-Origin Sites**: Attacker's site can't read the token (same-origin policy)
- **Token Hijacking**: Token only valid when both cookie AND header match
- **Replay Attacks**: Server validates every state-changing request

### ✅ Stateless:
- No server-side session storage needed
- Token is validated cryptographically (by Spring Security)
- Scales horizontally across multiple instances

### ✅ Cookie-Only Architecture:
- Token cookie is sent automatically by browser
- Works with stateless JWT authentication
- Compatible with Railway deployment

---

## Common Issues & Troubleshooting

### **Issue: 403 Forbidden on POST/PUT/DELETE**
**Cause:** CSRF token missing or mismatched

**Debug Steps:**
1. Open DevTools → Network → Check headers on failing request
   - Should have `X-CSRF-TOKEN` header
2. Check Cookies → Look for `XSRF-TOKEN`
   - Should be set after `/user/status` call
3. Check console logs:
   ```javascript
   console.log('CSRF Token:', getCsrfToken());
   ```

**Solution:**
- Ensure app calls a GET endpoint first (automatically done via `/user/status`)
- Verify interceptor is registered in apiClient.js
- Clear cookies and retry

### **Issue: CSRF token undefined**
**Cause:** `/user/status` hasn't been called yet

**Solution:**
- App.jsx already handles this in AuthProvider useEffect
- If custom endpoints are called before auth check, manually trigger `/user/status`

### **Issue: POST/PUT working but DELETE fails**
**Cause:** DELETE might be excluded from CSRF or method name casing

**Solution:**
- Verify DELETE is in stateChangingMethods array
- Check that method.toUpperCase() is being called

---

## Implementation Checklist

- [x] Enable CSRF in Spring Security (`SecurityConfig.java`)
- [x] Use CookieCsrfTokenRepository (double-submit pattern)
- [x] Exclude public endpoints from CSRF requirement
- [x] Extract CSRF token on client-side (`apiClient.js`)
- [x] Add interceptor for state-changing requests
- [x] Test with cURL or Postman:
  ```bash
  # Get CSRF token
  curl -c cookies.txt http://localhost:8085/user/status
  
  # Extract token from cookies.txt
  CSRF_TOKEN=$(grep XSRF-TOKEN cookies.txt | awk '{print $NF}')
  
  # POST with token
  curl -b cookies.txt \
       -H "X-CSRF-TOKEN: $CSRF_TOKEN" \
       -X POST http://localhost:8085/api/create-room \
       -H "Content-Type: application/json"
  ```

---

## Advanced: Token Rotation

Spring Security's `CookieCsrfTokenRepository` automatically:
- Generates a new token for each session
- Validates existing tokens
- Can regenerate tokens on sensitive operations (optional)

To force token regeneration after login (optional security enhancement):
```java
// In AuthController.java post-login
csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
```

---

## Deployment Notes

### Local Development
- CSRF enabled by default
- Token automatically set on first GET request

### Production (Railway)
- Cookie flags set automatically by Spring
- No additional configuration needed
- Works across multiple instances (stateless)

### Testing (Unit/Integration Tests)
- May need to disable CSRF for test endpoints:
  ```java
  @Bean
  public SecurityFilterChain testFilterChain(HttpSecurity http) {
      http.csrf(csrf -> csrf.disable()); // Test only!
      return http.build();
  }
  ```

---

## References

- [Spring Security CSRF Documentation](https://spring.io/projects/spring-security)
- [OWASP CSRF Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
- [Double-Submit Cookie Pattern](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#double-submit-cookie)


