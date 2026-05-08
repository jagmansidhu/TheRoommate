# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in DaRoomate, please report it by emailing the maintainers directly. **Do NOT create a public GitHub issue.**

Please include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will acknowledge receipt within 48 hours and provide a more detailed response within 7 days.

## Security Measures

### Automated Security Checks

This repository uses GitHub Actions to automatically scan for security issues:

1. **Configuration Safety Check** - Prevents dangerous operations in config files
   - Blocks `ddl-auto: drop` in application.yml
   - Detects hardcoded secrets
   - Validates production configurations

2. **Dependency Security Scan** - Checks for vulnerable dependencies
   - OWASP Dependency Check for Maven
   - npm audit for Node.js
   - License compliance verification

3. **Code Security Scan (SAST)** - Static analysis for vulnerabilities
   - Semgrep for OWASP Top 10
   - SpotBugs for Java
   - SQL injection pattern detection
   - XSS vulnerability checks
   - Secret scanning with TruffleHog

4. **Docker Security Scan** - Container security
   - Hadolint for Dockerfile best practices
   - Trivy for container vulnerability scanning
   - Docker Compose security validation

5. **Database Migration Safety** - Prevents data loss
   - Blocks DROP/TRUNCATE operations
   - Requires WHERE clauses on UPDATE/DELETE
   - Schema validation with tests

6. **PR Security Checklist** - Automated security review checklist

### Security Best Practices

#### Backend (Java/Spring Boot)

**SQL Injection Prevention:**
```java
// ❌ BAD - String concatenation
String query = "SELECT * FROM users WHERE id = " + userId;

// ✅ GOOD - Parameterized query
@Query("SELECT u FROM User u WHERE u.id = :userId")
User findByUserId(@Param("userId") Long userId);
```

**Input Validation:**
```java
// Always use @Valid or manual validation
public ResponseEntity<?> createUser(@Valid @RequestBody UserDto dto) {
    // validation happens automatically
}
```

**Authentication/Authorization:**
```java
// Use @PreAuthorize for method-level security
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
public void deleteUser(Long userId) {
    // ...
}
```

#### Frontend (React)

**XSS Prevention:**
```javascript
// ❌ BAD - dangerouslySetInnerHTML with user input
<div dangerouslySetInnerHTML={{__html: userInput}} />

// ✅ GOOD - React automatically escapes
<div>{userInput}</div>
```

**API Security:**
```javascript
// Always include credentials for cookie-based auth
axios.get('/api/endpoint', { withCredentials: true })
```

#### Database

**Configuration:**
```yaml
# ✅ Production - Use validate or none
spring:
  jpa:
    hibernate:
      ddl-auto: validate

# ❌ NEVER in production
spring:
  jpa:
    hibernate:
      ddl-auto: drop  # This will delete all data!
```

**Migrations:**
- Always test migrations on production-like data
- Include rollback scripts
- Never use `DROP TABLE` without explicit review
- Always include specific `WHERE` clauses in UPDATE/DELETE

#### Docker

**Dockerfile Security:**
```dockerfile
# ✅ Use specific versions
FROM eclipse-temurin:21-jre-alpine

# ✅ Run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# ❌ Never hardcode secrets
# Use build args or runtime environment variables
```

**docker-compose.yml:**
```yaml
# ✅ Use environment variable substitution
environment:
  - DATABASE_PASSWORD=${DB_PASSWORD}

# ❌ Never use
privileged: true
network_mode: host
```

### Secrets Management

**Environment Variables:**
- Use `.env` files for local development (never commit!)
- Use Railway/platform secret management for production
- Store in GitHub Secrets for CI/CD

**Required Environment Variables:**
```bash
# Database
POSTGRESQL_HOST=
POSTGRESQL_PORT=
POSTGRESQL_DATABASE=
POSTGRESQL_USERNAME=
POSTGRESQL_PASSWORD=

# JWT
JWT_KEY=  # Minimum 32 characters

# Email
EMAIL_HOST=
EMAIL_PORT=
EMAIL_ID=
EMAIL_PASSWORD=
```

### CORS Configuration

Current configuration allows:
- Frontend domain only
- Credentials included
- Specific HTTP methods

Update in `SecurityConfig.java` for production domain.

### Rate Limiting

- Implemented with Bucket4j + Redis
- Fail-open pattern (continues without rate limiting if Redis is down)
- Configure limits in `RateLimitingFilter.java`

## Security Checklist for Pull Requests

Before merging:

- [ ] All security scans pass (check Actions tab)
- [ ] No hardcoded secrets
- [ ] Input validation for user data
- [ ] SQL queries use parameterization
- [ ] No dangerous database operations (DROP, TRUNCATE)
- [ ] Frontend properly sanitizes user input
- [ ] Authentication/authorization checks in place
- [ ] Configuration files don't expose secrets
- [ ] Docker containers run as non-root
- [ ] New dependencies scanned for vulnerabilities

## Security Tools Configuration

### Suppression Files

If a security scan produces false positives, add suppressions:

**OWASP Dependency Check:**
Create `backend/dependency-check-suppressions.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <suppress>
        <notes>False positive - not affected</notes>
        <cve>CVE-XXXX-XXXX</cve>
    </suppress>
</suppressions>
```

## Incident Response

If a security vulnerability is discovered:

1. **Immediate Actions:**
   - Assess severity and scope
   - Revoke compromised credentials
   - Block affected endpoints if necessary

2. **Remediation:**
   - Apply fix in private branch
   - Test thoroughly
   - Deploy as hotfix

3. **Post-Incident:**
   - Review how vulnerability was introduced
   - Update security checks to catch similar issues
   - Document in security changelog

## Security Updates

- Dependencies reviewed monthly
- Security patches applied within 7 days
- Critical vulnerabilities within 24 hours

## Contact

For security concerns: [Add security contact email]

---

*Last updated: 2026-05-07*
