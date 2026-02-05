# Security Flow Documentation

This document describes the authentication and authorization flow in the BFF application.

## Table of Contents

- [Overview](#overview)
- [OAuth2 Clients](#oauth2-clients)
- [OIDC Callback & Frontend Routing](#oidc-callback--frontend-routing)
- [Login Flow](#login-flow)
- [Secure API Call Flow](#secure-api-call-flow)
- [Persona-Based Authorization](#persona-based-authorization)
- [Session Management](#session-management)
- [Endpoint Security Matrix](#endpoint-security-matrix)
- [Code References](#code-references)

---

## Overview

The BFF is a **fully reactive application** built on Spring WebFlux with Netty as the embedded server.

**Stack:**
- Spring Boot 4.0.2 / WebFlux (reactive-only)
- Netty (non-blocking I/O)
- Reactor (Mono/Flux)
- No Servlet API

**OAuth2 Architecture:**

1. **HSID** (authorization_code) - Authenticates users via OIDC
2. **HCP** (client_credentials) - Makes service-to-service API calls

After authentication, users are enriched with a `SessionInfo` containing their **persona** (`"self"` or `"representative"`), which is used for fine-grained endpoint authorization.

### Reactive Enforcement

All controller methods **must** return `Mono<T>` or `Flux<T>`. The `@RequiredPersona` annotation enforces this at runtime:

```java
// ✓ Correct - returns Mono
@RequiredPersona(Persona.SELF)
public Mono<Response> endpoint() { ... }

// ✗ Error - non-reactive return type
@RequiredPersona(Persona.SELF)
public Response endpoint() { ... }  // Throws IllegalStateException
```

---

## OAuth2 Clients

| Client | Grant Type | Purpose | Authentication |
|--------|------------|---------|----------------|
| HSID | `authorization_code` | User login via OIDC | Public client (PKCE) |
| HCP | `client_credentials` | Backend service calls | Client ID + Secret |

### Configuration (application.yml)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          hsid:
            client-id: ${HSID_CLIENT_ID}
            client-authentication-method: none
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: openid, profile, email
          hcp:
            client-id: ${HCP_CLIENT_ID}
            client-secret: ${HCP_CLIENT_SECRET}
            authorization-grant-type: client_credentials
```

---

## OIDC Callback & Frontend Routing

The `OidcCallbackRedirectFilter` handles routing at the root level:

### Routing Logic

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        OidcCallbackRedirectFilter                            │
│                        (Ordered.HIGHEST_PRECEDENCE)                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │ Has ?code=...&state=... ?     │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                  YES                              NO
                    │                               │
                    ▼                               ▼
    ┌───────────────────────────┐   ┌───────────────────────────────┐
    │ At /login/oauth2/code/* ? │   │ Is backend path?              │
    └───────────────┬───────────┘   │ (/api/**, /actuator/**,       │
                    │               │  /login/oauth2/**, /oauth2/**)│
            ┌───────┴───────┐       └───────────────┬───────────────┘
            │               │                       │
          YES              NO               ┌───────┴───────┐
            │               │               │               │
            ▼               ▼             YES              NO
    Pass through    302 Redirect to         │               │
    (Spring         /login/oauth2/code/hsid ▼               ▼
     Security)      ?code=...&state=...  Pass through   302 Redirect
                                         (Backend)      to Frontend
```

### Request Examples

| Request | Action |
|---------|--------|
| `GET /?code=abc&state=xyz` | → `302` to `/login/oauth2/code/hsid?code=abc&state=xyz` |
| `GET /dashboard?code=abc&state=xyz` | → `302` to `/login/oauth2/code/hsid?code=abc&state=xyz` |
| `GET /login/oauth2/code/hsid?code=abc&state=xyz` | → Pass through (Spring Security handles) |
| `GET /api/users` | → Pass through (backend handles) |
| `GET /actuator/health` | → Pass through (backend handles) |
| `GET /` | → `302` to frontend (`app.frontend-redirect-path`) |
| `GET /some-frontend-route` | → `302` to frontend |

### Why This Is Needed

HSID may redirect the OAuth2 callback to URLs other than the configured callback path:
- Root URL: `https://bff.example.com/?code=...&state=...`
- Frontend route: `https://bff.example.com/dashboard?code=...&state=...`

This filter ensures these requests are properly routed to Spring Security's OAuth2 callback handler.

### Configuration

The filter uses `app.frontend-redirect-path` for non-backend, non-OIDC requests.

---

## Login Flow

### Sequence Diagram

```
  Browser              BFF                    HSID (IdP)           User Service        PSN
     │                  │                        │                      │               │
     │ GET /api/auth/login                       │                      │               │
     │─────────────────>│                        │                      │               │
     │                  │                        │                      │               │
     │ 302 Redirect     │                        │                      │               │
     │<─────────────────│                        │                      │               │
     │                  │                        │                      │               │
     │ User authenticates at HSID                │                      │               │
     │<─────────────────────────────────────────>│                      │               │
     │                  │                        │                      │               │
     │ Callback with auth code                   │                      │               │
     │─────────────────>│                        │                      │               │
     │                  │                        │                      │               │
     │                  │ Exchange code for tokens                      │               │
     │                  │───────────────────────>│                      │               │
     │                  │<───────────────────────│                      │               │
     │                  │                        │                      │               │
     │                  │ Fetch OIDC userinfo    │                      │               │
     │                  │───────────────────────>│                      │               │
     │                  │<───────────────────────│                      │               │
     │                  │                        │                      │               │
     │                  │ ═══════════════════════════════════════════════════════════  │
     │                  │   CustomOidcUserService.loadUser() - User Enrichment         │
     │                  │ ═══════════════════════════════════════════════════════════  │
     │                  │                        │                      │               │
     │                  │ POST /user-info (HCP client_credentials)     │               │
     │                  │─────────────────────────────────────────────>│               │
     │                  │<─────────────────────────────────────────────│               │
     │                  │                        │                      │               │
     │                  │ If memberType == "PR": POST /managed-members │               │
     │                  │──────────────────────────────────────────────────────────────>│
     │                  │<──────────────────────────────────────────────────────────────│
     │                  │                        │                      │               │
     │                  │ Build SessionInfo with persona               │               │
     │                  │ Create EnrichedOidcUser                      │               │
     │                  │                        │                      │               │
     │                  │ ═══════════════════════════════════════════════════════════  │
     │                  │   End User Enrichment                                        │
     │                  │ ═══════════════════════════════════════════════════════════  │
     │                  │                        │                      │               │
     │ 302 → frontend/dashboard                  │                      │               │
     │ Set-Cookie: SESSION=xxx                   │                      │               │
     │<─────────────────│                        │                      │               │
```

### Step-by-Step

1. **User initiates login** → `GET /api/auth/login`
2. **BFF redirects to HSID** → `/oauth2/authorization/hsid`
3. **User authenticates at HSID** → Enters credentials
4. **HSID redirects back with auth code** → `/login/oauth2/code/hsid?code=...`
5. **BFF exchanges code for tokens** → Calls HSID `/oidc/token`
6. **BFF fetches OIDC user info** → Calls HSID `/oidc/userinfo`
7. **CustomOidcUserService enriches user:**
   - Fetches user info from User Service (via HCP client)
   - Determines persona based on `memberType`:
     - `"PR"` → `persona = "representative"` + fetch managed members
     - Other → `persona = "self"`
   - Creates `SessionInfo` with persona
   - Wraps in `EnrichedOidcUser`
8. **BFF stores session** → SecurityContext saved to WebSession
9. **BFF redirects to frontend** → `302` to configured redirect path

### Persona Determination

```java
// CustomOidcUserService.java
if ("PR".equals(userInfo.memberType())) {
    // PR = Personal Representative
    persona = "representative";
    managedMembers = fetchManagedMembers(enterpriseId);
} else {
    // MB = Member (or any other type)
    persona = "self";
    managedMembers = Map.of();
}
```

---

## Secure API Call Flow

### Sequence Diagram

```
  Browser                Spring Security Filters              AOP Aspect              Controller
     │                           │                                │                       │
     │ GET /api/some-endpoint    │                                │                       │
     │ Cookie: SESSION=xxx       │                                │                       │
     │──────────────────────────>│                                │                       │
     │                           │                                │                       │
     │            ┌──────────────┴──────────────┐                  │                       │
     │            │     SECURITY FILTER CHAIN   │                  │                       │
     │            │                             │                  │                       │
     │            │ 1. Load SecurityContext     │                  │                       │
     │            │    from SESSION cookie      │                  │                       │
     │            │                             │                  │                       │
     │            │ 2. Put SecurityContext      │                  │                       │
     │            │    into Reactor Context     │                  │                       │
     │            │                             │                  │                       │
     │            │ 3. Check /api/** requires   │                  │                       │
     │            │    authenticated() ✓        │                  │                       │
     │            └──────────────┬──────────────┘                  │                       │
     │                           │                                │                       │
     │                           │ Dispatch to controller         │                       │
     │                           │───────────────────────────────>│                       │
     │                           │                                │                       │
     │                           │          ┌─────────────────────┴─────────────────────┐ │
     │                           │          │ PersonaAuthorizationAspect               │ │
     │                           │          │ @Around("@annotation(requiredPersona)")  │ │
     │                           │          │                                          │ │
     │                           │          │ 4. Get SecurityContext from              │ │
     │                           │          │    ReactiveSecurityContextHolder         │ │
     │                           │          │                                          │ │
     │                           │          │ 5. Extract EnrichedOidcUser              │ │
     │                           │          │    → SessionInfo → persona               │ │
     │                           │          │                                          │ │
     │                           │          │ 6. Check: persona ∈ allowed?             │ │
     │                           │          │    YES → proceed()                       │ │
     │                           │          │    NO  → 403 Forbidden                   │ │
     │                           │          └─────────────────────┬─────────────────────┘ │
     │                           │                                │                       │
     │                           │                                │ If authorized         │
     │                           │                                │──────────────────────>│
     │                           │                                │                       │
     │ 200 OK                    │                                │                       │
     │<──────────────────────────────────────────────────────────────────────────────────│
```

### Security Filter Chain

1. **WebSessionServerSecurityContextRepository** - Loads `SecurityContext` from session cookie
2. **ReactorContextWebFilter** - Populates Reactor Context with `SecurityContext`
3. **AuthorizationWebFilter** - Enforces `/api/**` requires `authenticated()`

### Aspect Authorization

For endpoints with `@RequiredPersona`:

1. Aspect intercepts method call
2. Retrieves `SecurityContext` from `ReactiveSecurityContextHolder`
3. Extracts `EnrichedOidcUser` → `SessionInfo` → `persona`
4. Compares persona against allowed values
5. **Match** → Proceeds with controller method
6. **No Match** → Returns `403 Forbidden`

---

## Persona-Based Authorization

### @RequiredPersona Annotation

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredPersona {
    String[] value();  // Allowed persona values
}
```

### Persona Constants

```java
public final class Persona {
    public static final String SELF = "self";
    public static final String REPRESENTATIVE = "representative";
}
```

### Usage Examples

```java
// Only self persona allowed
@RequiredPersona(Persona.SELF)
public Mono<Response> selfOnlyEndpoint() { ... }

// Only representative persona allowed
@RequiredPersona(Persona.REPRESENTATIVE)
public Mono<Response> representativeOnlyEndpoint() { ... }

// Both personas allowed
@RequiredPersona({Persona.SELF, Persona.REPRESENTATIVE})
public Mono<Response> anyPersonaEndpoint() { ... }
```

### Authorization Aspect Flow

```java
// PersonaAuthorizationAspect.java
@Around("@annotation(requiredPersona)")
public Object checkPersona(ProceedingJoinPoint joinPoint, RequiredPersona requiredPersona) {
    String[] allowedPersonas = requiredPersona.value();

    Mono<Void> authorizationCheck = ReactiveSecurityContextHolder.getContext()
        .flatMap(securityContext -> {
            EnrichedOidcUser user = (EnrichedOidcUser) securityContext
                .getAuthentication().getPrincipal();
            String actualPersona = user.getSessionInfo().persona();

            if (allowedSet.contains(actualPersona)) {
                return Mono.empty();  // Authorized
            }
            return Mono.error(new PersonaAccessDeniedException(...));  // 403
        });

    // Chain authorization before method execution
    return authorizationCheck.then(methodMono);
}
```

---

## Session Management

### Architecture

```
Browser Cookie                    BFF Server (In-Memory WebSession)
┌─────────────┐                  ┌──────────────────────────────────┐
│ SESSION=abc │  ───────────────>│ WebSession "abc"                 │
└─────────────┘                  │  ├─ SecurityContext              │
                                 │  │    └─ Authentication          │
                                 │  │         └─ EnrichedOidcUser   │
                                 │  │              └─ SessionInfo   │
                                 │  │                   └─ persona  │
                                 │  │                                │
                                 │  └─ OAuth2AuthorizedClient       │
                                 │       └─ accessToken             │
                                 │       └─ refreshToken            │
                                 └──────────────────────────────────┘
```

### Security Context Structure

```
SecurityContext
  └─ Authentication
       ├─ principal: EnrichedOidcUser
       │     ├─ subject: "hsid-uuid-12345"
       │     ├─ email: "user@example.com"
       │     ├─ name: "John Doe"
       │     ├─ idToken: OidcIdToken
       │     ├─ userInfo: OidcUserInfo
       │     └─ additionalAttributes:
       │           └─ "sessionInfo": SessionInfo
       │                 ├─ enterpriseId: "ENT123"
       │                 ├─ hsidUuid: "hsid-uuid-12345"
       │                 ├─ sessionStartTime: Instant
       │                 ├─ sessionEndTime: Instant
       │                 ├─ persona: "self" | "representative"
       │                 └─ managedMembers: Map<String, List<DelegatePermission>>
       │
       ├─ authorities: [SCOPE_openid, SCOPE_profile, SCOPE_email]
       └─ authenticated: true
```

### Session Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `app.session.duration-minutes` | 30 | Session duration in minutes |

---

## Endpoint Security Matrix

| Endpoint | Spring Security | @RequiredPersona | Access |
|----------|-----------------|------------------|--------|
| `GET /api/auth/session` | `permitAll()` | - | Public |
| `GET /api/auth/login` | `permitAll()` | - | Public |
| `POST /api/auth/logout` | `authenticated()` | - | Any authenticated |
| `GET /actuator/health` | `permitAll()` | - | Public |
| `GET /actuator/info` | `permitAll()` | - | Public |
| `GET /api/persona-test/self-only` | `authenticated()` | `Persona.SELF` | Self only |
| `GET /api/persona-test/representative-only` | `authenticated()` | `Persona.REPRESENTATIVE` | Representative only |
| `GET /api/persona-test/any-persona` | `authenticated()` | `{SELF, REPRESENTATIVE}` | Both |
| `GET /api/**` (other) | `authenticated()` | - | Any authenticated |

---

## Code References

### Files

| File | Purpose |
|------|---------|
| `config/SecurityConfig.java` | Security filter chain, OAuth2 config, CORS |
| `config/OidcCallbackRedirectFilter.java` | OIDC callback routing, frontend redirect |
| `security/CustomOidcUserService.java` | User enrichment, persona determination |
| `security/EnrichedOidcUser.java` | Extended OidcUser with SessionInfo |
| `security/Persona.java` | Persona constants (SELF, REPRESENTATIVE) |
| `security/RequiredPersona.java` | Authorization annotation |
| `security/PersonaAuthorizationAspect.java` | AOP aspect for persona check |
| `security/PersonaAccessDeniedException.java` | 403 exception |
| `model/SessionInfo.java` | Session data record with persona |
| `controller/PersonaTestController.java` | Test endpoints |

### Key Code Locations

| Component | Location | Description |
|-----------|----------|-------------|
| Security filter chain | `SecurityConfig.java:74-94` | Endpoint authorization rules |
| OAuth2 login config | `SecurityConfig.java:83-86` | Success/failure handlers |
| User enrichment | `CustomOidcUserService.java:40-64` | loadUser() override |
| Persona logic | `CustomOidcUserService.java:51-58` | memberType → persona mapping |
| Aspect pointcut | `PersonaAuthorizationAspect.java:38` | `@Around` annotation |
| Persona check | `PersonaAuthorizationAspect.java:44-60` | Authorization logic |

---

## Error Responses

### 401 Unauthorized

Returned when accessing `/api/**` without authentication.

### 403 Forbidden

Returned when persona doesn't match `@RequiredPersona` requirements.

```json
{
  "timestamp": "2024-01-15T10:30:00.000+00:00",
  "path": "/api/persona-test/self-only",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Required persona: [self], actual persona: representative"
}
```

---

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `HSID_CLIENT_ID` | Yes | - | OIDC client ID for user auth |
| `HCP_CLIENT_ID` | Yes | - | Service account client ID |
| `HCP_CLIENT_SECRET` | Yes | - | Service account client secret |
| `HSID_IDP_ORIGIN` | No | `https://nonprod.identity.healthsafe-id.com` | OIDC provider URL |
| `FRONTEND_REDIRECT_PATH` | No | `http://localhost:4202/dashboard` | Post-login redirect |
| `FRONTEND_ERROR_PATH` | No | `http://localhost:4202/auth-error` | Auth error redirect |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:4202,...` | Allowed CORS origins |
