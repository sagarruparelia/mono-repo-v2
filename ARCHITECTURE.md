# Mono-Repo V2 Architecture

## Overview

A full-stack monorepo combining React micro-frontends with a Spring Boot backend, orchestrated by Nx.

```
┌─────────────────────────────────────────────────────────────────┐
│                        Production Stack                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Browser                                                        │
│      │                                                           │
│      ▼                                                           │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  Nginx (web-cl container)                                │  │
│   │  ├─ Static: web-cl SPA         → /                       │  │
│   │  ├─ MFE Bundles                → /mfe/profile/*          │  │
│   │  │                             → /mfe/summary/*          │  │
│   │  └─ API Proxy                  → /api/* → bff:8080       │  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│                              ▼                                   │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  Spring Boot (bff container)                             │  │
│   │  ├─ OAuth2/OIDC Authentication                           │  │
│   │  ├─ Session Management                                   │  │
│   │  └─ REST APIs (/api/auth/*, /api/profile/*, /api/summary)│  │
│   └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│                              ▼                                   │
│   ┌──────────────────────────────────────────────────────────┐  │
│   │  MongoDB (optional)                                      │  │
│   └──────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Build System** | Nx | 22.4.5 |
| **Frontend** | React + TypeScript | 19.2.4 / 5.9.2 |
| **Bundler** | Vite | 7.0.0 |
| **Routing** | TanStack Router | 1.120.0 |
| **Data Fetching** | React Query | 5.80.0 |
| **Backend** | Spring Boot (WebFlux) | 4.0.2 |
| **Java** | Temurin | 25 |
| **Auth** | OAuth2/OIDC + PKCE | - |
| **Container** | Docker + Nginx | 1.25-alpine |
| **Testing** | Vitest + Playwright | 4.0.0 / 1.36.0 |

---

## Project Structure

```
mono-repo-v2/
├── apps/
│   ├── web-cl/              # Main React SPA
│   ├── web-cl-e2e/          # Playwright E2E tests
│   ├── mfe-profile/         # Profile micro-frontend
│   ├── mfe-summary/         # Summary micro-frontend
│   └── bff/                 # Spring Boot backend
├── libs/
│   ├── shared-auth/         # Auth context, hooks, API
│   ├── shared-query/        # React Query configuration
│   ├── shared-ui/           # Shared UI components
│   └── web-component-wrapper/ # React→Web Component adapter
├── docker/
│   ├── bff/Dockerfile
│   ├── web-cl/Dockerfile
│   ├── web-cl/nginx.conf
│   └── docker-compose.yml
└── .github/workflows/ci.yml
```

---

## Applications

### web-cl (Main Application)

The primary React SPA serving as the shell application.

| Property | Value |
|----------|-------|
| Port (dev) | 4202 |
| Framework | React 19 + TanStack Router |
| Entry | `src/main.tsx` |
| Routes | `src/routes/` (file-based) |

**Key Routes:**
- `/` → Redirect to `/dashboard`
- `/dashboard` → Main dashboard
- `/profile` → Embedded mfe-profile
- `/summary` → Embedded mfe-summary

**Dependencies:**
- `@mono-repo-v2/shared-auth` - Authentication
- `@mono-repo-v2/shared-query` - Query client
- `@mono-repo-v2/mfe-profile` - Profile MFE (React import)
- `@mono-repo-v2/mfe-summary` - Summary MFE (React import)

---

### mfe-profile / mfe-summary (Micro-Frontends)

Dual-mode React applications that can run standalone or as web components.

| Property | mfe-profile | mfe-summary |
|----------|-------------|-------------|
| Port (dev) | 4203 | 4204 |
| Custom Element | `<mfe-profile>` | `<mfe-summary>` |
| API Endpoint | `/api/profile/{userId}` | `/api/summary/{userId}` |

**Build Modes:**

```bash
# Standalone (full React app)
nx build mfe-profile

# Web Component (ES/UMD library)
BUILD_MODE=web-component nx build mfe-profile
```

**Web Component Attributes:**
```html
<mfe-profile
  user-id="123"
  enterprise-id="org-456"
  persona="agent"
  service-base-url="https://api.example.com"
  styles-url="/custom-styles.css"
></mfe-profile>
```

---

### bff (Backend for Frontend)

Spring Boot reactive API handling authentication and data.

| Property | Value |
|----------|-------|
| Port | 8080 |
| Framework | Spring WebFlux (reactive) |
| Java | 25 |
| Build | Maven (`./mvnw`) |

**API Endpoints:**

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/auth/login` | Initiate OAuth2 flow |
| GET | `/api/auth/callback` | OAuth2 callback |
| GET | `/api/auth/session` | Check session status |
| POST | `/api/auth/logout` | End session |
| GET | `/api/profile/{userId\|me}` | Get user profile |
| GET | `/api/summary/{userId\|me}` | Get user summary |
| GET | `/actuator/health` | Health check |

---

## Libraries

### shared-auth

Authentication context, hooks, and API functions.

```typescript
// API
export { getSession, initiateLogin, logout }

// Hooks
export { useSession, useLogin, useLogout }

// Context
export { AuthProvider, useAuth }
export { MfeConfigProvider, useMfeConfig }
export { AuthMfeConfigProvider }

// Types
export type { SessionResponse, UserInfo, MfeConfig, Persona }
```

**MFE Configuration:**
```typescript
interface MfeConfig {
  enterpriseId: string
  persona: 'self' | 'parent' | 'agent' | 'state-worker'
  serviceBaseUrl: string
  isEmbedded: boolean
}
```

---

### shared-query

React Query configuration shared across apps.

```typescript
export { createQueryClient, QueryProvider }

// Default configuration:
// - staleTime: 5 minutes
// - gcTime: 30 minutes
// - retry: 1 (queries), 0 (mutations)
```

---

### web-component-wrapper

Factory for converting React components to Web Components.

```typescript
import { createWebComponent } from '@mono-repo-v2/web-component-wrapper'

createWebComponent({
  tagName: 'mfe-profile',
  Component: ProfileApp,
  observedAttributes: ['user-id', 'enterprise-id', 'persona'],
  shadow: true
})
```

**Provider Chain (auto-wrapped):**
```
<StrictMode>
  <QueryProvider>
    <MfeConfigProvider>
      <YourComponent />
    </MfeConfigProvider>
  </QueryProvider>
</StrictMode>
```

---

## Dependency Graph

```
web-cl (main app)
├── shared-auth
├── shared-query
├── mfe-profile ──┬── shared-auth
│                 ├── shared-query
│                 └── web-component-wrapper
└── mfe-summary ──┬── shared-auth
                  ├── shared-query
                  └── web-component-wrapper

bff (independent - no JS dependencies)
```

**Path Aliases** (tsconfig.base.json):
```json
{
  "@mono-repo-v2/shared-auth": ["libs/shared-auth/src/index.ts"],
  "@mono-repo-v2/shared-query": ["libs/shared-query/src/index.ts"],
  "@mono-repo-v2/mfe-profile": ["apps/mfe-profile/src/app/ProfileApp.tsx"],
  "@mono-repo-v2/mfe-summary": ["apps/mfe-summary/src/app/SummaryApp.tsx"]
}
```

---

## Authentication Flow

```
┌─────────┐     ┌─────────┐     ┌──────────────┐     ┌─────────┐
│ Browser │     │ web-cl  │     │     BFF      │     │  OIDC   │
└────┬────┘     └────┬────┘     └──────┬───────┘     └────┬────┘
     │               │                  │                  │
     │ Click Login   │                  │                  │
     │──────────────>│                  │                  │
     │               │ GET /api/auth/login                 │
     │               │─────────────────>│                  │
     │               │                  │ Generate PKCE    │
     │               │                  │ Store verifier   │
     │               │   302 Redirect   │                  │
     │<─────────────────────────────────│                  │
     │                                  │                  │
     │ GET /authorize?code_challenge=...                   │
     │────────────────────────────────────────────────────>│
     │                                  │                  │
     │ User authenticates               │                  │
     │<────────────────────────────────────────────────────│
     │                                  │                  │
     │ 302 /api/auth/callback?code=X    │                  │
     │─────────────────────────────────>│                  │
     │                                  │ Exchange code    │
     │                                  │─────────────────>│
     │                                  │ Tokens           │
     │                                  │<─────────────────│
     │                                  │                  │
     │                                  │ Create session   │
     │   302 /dashboard + Set-Cookie    │                  │
     │<─────────────────────────────────│                  │
     │                                  │                  │
     │ GET /api/auth/session            │                  │
     │─────────────────────────────────>│                  │
     │   { authenticated, user }        │                  │
     │<─────────────────────────────────│                  │
```

---

## MFE Architecture

### Build Strategy

MFEs use **Web Components** for framework-agnostic distribution.

```
┌──────────────────────────────────────────────────────────────┐
│                    MFE Build Pipeline                         │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  React Component (ProfileApp.tsx)                            │
│         │                                                     │
│         ▼                                                     │
│  Web Component Wrapper (createWebComponent)                  │
│         │                                                     │
│         ▼                                                     │
│  Custom HTML Element (<mfe-profile>)                         │
│         │                                                     │
│         ▼                                                     │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Vite Build (BUILD_MODE=web-component)                  │ │
│  │  ├─ mfe-profile.es.js   (ES Modules)                    │ │
│  │  ├─ mfe-profile.umd.js  (UMD Bundle)                    │ │
│  │  └─ External: react, react-dom (not bundled)            │ │
│  └─────────────────────────────────────────────────────────┘ │
│         │                                                     │
│         ▼                                                     │
│  Served via Nginx at /mfe/profile/                           │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### Consumption Patterns

**1. Direct React Import (in web-cl):**
```tsx
import { ProfileApp } from '@mono-repo-v2/mfe-profile'

function ProfilePage() {
  const { user } = useAuth()
  return <ProfileApp userId={user?.id} />
}
```

**2. Web Component (external apps):**
```html
<!-- Load React (peer dependency) -->
<script src="https://unpkg.com/react@19/umd/react.production.min.js"></script>
<script src="https://unpkg.com/react-dom@19/umd/react-dom.production.min.js"></script>

<!-- Load MFE bundle -->
<script src="https://your-cdn.com/mfe/profile/mfe-profile.umd.js"></script>

<!-- Use custom element -->
<mfe-profile
  user-id="123"
  enterprise-id="org-456"
  persona="agent"
  service-base-url="https://api.example.com">
</mfe-profile>
```

---

## Docker Architecture

### Images

| Image | Base | Port | Purpose |
|-------|------|------|---------|
| mono-repo-bff | eclipse-temurin:25-jre-alpine | 8080 | Spring Boot API |
| mono-repo-frontend | nginx:1.25-alpine | 8080 | SPA + MFE bundles |

### Build Process

```bash
# Frontend (runs in CI, not in Docker)
npm ci
nx build web-cl --configuration=production
BUILD_MODE=web-component nx build mfe-profile --configuration=production
BUILD_MODE=web-component nx build mfe-summary --configuration=production

# BFF (runs in CI, not in Docker)
cd apps/bff && ./mvnw package -DskipTests

# Docker images (just copy artifacts)
docker build -f docker/bff/Dockerfile -t mono-repo-bff .
docker build -f docker/web-cl/Dockerfile -t mono-repo-frontend .
```

### Nginx Configuration

```
┌─────────────────────────────────────────────────────────────┐
│                    Nginx Routing                             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  /                    → /usr/share/nginx/html (SPA)         │
│  /mfe/profile/*       → /usr/share/nginx/html/mfe/profile   │
│  /mfe/summary/*       → /usr/share/nginx/html/mfe/summary   │
│  /api/*               → proxy_pass http://bff:8080/api/     │
│  /health              → 200 "healthy"                        │
│                                                              │
│  Security Headers:                                           │
│  ├─ X-Frame-Options: SAMEORIGIN                             │
│  ├─ X-Content-Type-Options: nosniff                         │
│  ├─ Content-Security-Policy: default-src 'self'...         │
│  ├─ Referrer-Policy: strict-origin-when-cross-origin        │
│  └─ Permissions-Policy: geolocation=(), camera=()...       │
│                                                              │
│  CORS (MFE bundles only):                                   │
│  └─ Allowed origins: https://*.abc.com, https://*.xyz.com  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## CI/CD Pipeline

```yaml
# .github/workflows/ci.yml

on:
  push:
    branches: [main]

jobs:
  changes:        # Detect what changed
    outputs:
      bff: true/false
      frontend: true/false

  build-bff:      # If apps/bff/** changed
    steps:
      - Setup Java 25
      - mvnw package
      - mvnw test
      - docker build

  build-frontend: # If libs/** or apps/web-cl/** or apps/mfe-*/** changed
    steps:
      - Setup Node 24
      - npm ci
      - nx build web-cl
      - BUILD_MODE=web-component nx build mfe-profile
      - BUILD_MODE=web-component nx build mfe-summary
      - nx run-many --target=test
      - docker build
```

**Path Filters:**
| Job | Triggers On |
|-----|-------------|
| BFF | `apps/bff/**` |
| Frontend | `libs/**`, `apps/web-cl/**`, `apps/mfe-*/**`, `package*.json`, `nx.json` |

---

## Development

### Prerequisites

```bash
node -v  # v24.x (see .nvmrc)
java -version  # 25
```

### Commands

```bash
# Install dependencies
npm ci

# Development servers
npm run dev              # web-cl on :4202
npm run dev:mfe-profile  # mfe-profile on :4203
npm run dev:mfe-summary  # mfe-summary on :4204

# BFF
cd apps/bff && ./mvnw spring-boot:run

# Build
npm run build:all        # Build everything

# Test
npm run test             # All tests
npm run lint             # Lint all

# Docker (local)
docker-compose -f docker/docker-compose.yml up
```

### Environment Variables

**Frontend (.env):**
```bash
VITE_BFF_URL=http://localhost:8080
```

**BFF (application.yml or env):**
```bash
OIDC_CLIENT_ID=<client-id>
OIDC_CLIENT_SECRET=<client-secret>
OIDC_AUTHORIZATION_URI=https://provider/authorize
OIDC_TOKEN_URI=https://provider/token
OIDC_REDIRECT_URI=http://localhost/api/auth/callback
FRONTEND_REDIRECT_URI=http://localhost/dashboard
```

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Web Components for MFEs** | Framework-agnostic, works in any HTML page |
| **Dual Build Mode** | Single codebase for standalone dev + embedded production |
| **Context-Based Config** | Supports multi-tenancy via enterpriseId/persona |
| **Spring WebFlux** | Non-blocking I/O scales better for BFF pattern |
| **Nx Monorepo** | Unified tooling, dependency graph, affected builds |
| **Pre-built Artifacts in Docker** | CI caching, faster image builds |
| **Path-Filtered CI** | Only build what changed |

---

## File Quick Reference

| File | Purpose |
|------|---------|
| `nx.json` | Nx configuration, plugins, caching |
| `tsconfig.base.json` | TypeScript paths, compiler options |
| `package.json` | Scripts, dependencies |
| `.nvmrc` | Node version (24) |
| `apps/*/project.json` | Per-app Nx targets |
| `apps/*/vite.config.mts` | Vite build configuration |
| `apps/bff/pom.xml` | Maven dependencies (Java 25) |
| `docker/docker-compose.yml` | Local container orchestration |
| `.github/workflows/ci.yml` | CI/CD pipeline |
