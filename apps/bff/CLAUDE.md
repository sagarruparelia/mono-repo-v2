# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=DemoApplicationTests

# Run single test method
./mvnw test -Dtest=DemoApplicationTests#contextLoads
```

## Required Environment Variables

```bash
HSID_CLIENT_ID              # OIDC client ID for user authentication
HCP_CLIENT_ID               # Service account client ID for outbound API calls
HCP_CLIENT_SECRET           # Service account client secret
```

Optional (with defaults):
- `HSID_IDP_ORIGIN` - OIDC provider URL (default: nonprod.identity.healthsafe-id.com)
- `HCP_TOKEN_URI` - HCP OAuth2 token endpoint
- `FRONTEND_REDIRECT_PATH` - Post-login redirect (default: http://localhost:4202/dashboard)
- `FRONTEND_ERROR_PATH` - Auth error redirect (default: http://localhost:4202/auth-error)
- `CORS_ALLOWED_ORIGINS` - Comma-separated origins (default: localhost:4202,4203,4204)

## Architecture

**Stack:** Spring Boot 4.0.2 / WebFlux (reactive) / Java 25

**OAuth2 Flows:**
- **HSID** (authorization_code) - User login via OIDC
- **HCP** (client_credentials) - Service-to-service API calls

**Key Patterns:**
- All endpoints return `Mono<T>` or `Flux<T>` (reactive)
- Cookie-based sessions (no external session store)
- User enrichment happens post-authentication in `CustomOidcUserService`
- Use `@Qualifier("hcpWebClient")` WebClient for outbound calls with automatic OAuth2 tokens

**Package Structure:**
- `config/` - SecurityConfig (OAuth2, CORS, handlers), SessionConfig
- `controller/` - REST endpoints (AuthController)
- `security/` - CustomOidcUserService (user enrichment), EnrichedOidcUser
- `model/` - Data records

**Public Endpoints:** `/api/auth/session`, `/api/auth/login`, `/actuator/health`, `/actuator/info`

**All other `/api/**` require authentication.**
