# Employee CRUD Frontend

React + Vite + TypeScript SPA that interacts with the `employee-crud-event-driven` backend services.

## Tech Stack

- **React 19** + **Vite 6** + **TypeScript 5**
- **Tailwind CSS 4** (CSS-first configuration, `@tailwindcss/vite` plugin)
- **React Router 6** (client-side routing with role-based guards)
- **Axios** (HTTP client with automatic token refresh interceptor)
- **Lucide React** (icons)
- **Vitest** + **React Testing Library** (unit tests)

## Prerequisites

- Node.js >= 20
- Backend infrastructure running (see root `README.md` or `docker/README.md`)
- One of the test users: `john@test.com` / `123` (full access) or `mike@other.com` / `123` (employees only)

## Quick Start

```bash
# 1. Install dependencies
npm install

# 2. Choose environment and copy env file
# Local development (direct backend URLs):
cp .env.local .env
# Or via Docker/Nginx proxy:
# cp .env.docker .env

# 3. Start dev server (default http://localhost:5173)
npm run dev

# 4. Build for production
npm run build

# 5. Preview production build
npm run preview

# 6. Run tests
npm test
```

## Environment Files

| File | Purpose |
|---|---|
| `.env.local` | Local dev — direct URLs (localhost:8081, 8083, 8084) |
| `.env.docker` | Docker/Nginx — proxy URLs (localstack.lks.com:8080) |

`VITE_CLIENT_ID` and `VITE_CLIENT_SECRET` correspond to the `newClient` Keycloak client defined in the Keycloak realm.

## Project Structure

```
src/
├── api/              # Axios clients, auth/employee/user API functions
├── components/       # Reusable UI components (DataTable, Modal, etc.)
├── contexts/         # AuthContext (login, logout, token refresh, roles)
├── hooks/            # Custom hooks for employees and users
├── pages/            # Route pages (Login, Dashboard, CRUD pages, Profile, 404)
├── types/            # TypeScript interfaces (mirror backend DTOs)
├── utils/            # JWT decode/helpers
├── App.tsx           # Router + AuthProvider + Layout
├── main.tsx          # Entry point
└── index.css         # Tailwind v4 import
tests/                # Vitest test files
```

## Routes

| Path | Page | Access |
|---|---|---|
| `/login` | Login | Public (redirects if authenticated) |
| `/` | Dashboard | Authenticated |
| `/employees` | Employee list | Authenticated |
| `/employees/new` | Create employee | Authenticated |
| `/employees/:id` | View employee | Authenticated |
| `/employees/:id/edit` | Edit employee | Authenticated |
| `/users` | User list | Authenticated + role |
| `/users/new` | Create user | Authenticated + role |
| `/users/:id` | View user | Authenticated + role |
| `/users/:id/edit` | Edit user | Authenticated + role |
| `/profile` | My Profile | Authenticated |
| `*` | 404 | Authenticated |

## Role-Based Access

User management routes are hidden and guarded behind realm-management roles (`manage-users`, `view-users`, `query-users`).

- **john@test.com**: Full access (Employee + User CRUD)
- **mike@other.com**: Employee CRUD only (Users nav item hidden)

## Authentication Flow

1. User submits credentials via LoginPage
2. POST to Keycloak token endpoint (`grant_type=password`)
3. Access + refresh tokens stored in `localStorage`
4. Axios interceptor auto-attaches `Authorization: Bearer` header
5. On 401, interceptor transparently refreshes the token
6. On refresh failure, user is redirected to `/login`

## Available Scripts

| Command | Description |
|---|---|
| `npm run dev` | Start Vite dev server |
| `npm run build` | TypeScript check + production build |
| `npm run preview` | Preview production build |
| `npm test` | Run Vitest once |
| `npm run test:watch` | Run Vitest in watch mode |
| `npm run lint` | TypeScript type-check only |

## Testing

Tests use Vitest + React Testing Library with simple mocks (no MSW).

```
npm test              # Run once
npm run test:watch    # Watch mode
```

Test files cover:
- Login success path / invalid credential error
- AuthContext (login, logout, role check)
- DataTable (loading, empty, error, populated states)
- ProtectedRoute (unauthenticated, role-missing, authorized)
- JWT utilities (decode, role extraction)

## Security

- `.npmrc` sets `ignore-scripts=true` — prevents `postinstall`/`preinstall` scripts for supply-chain risk mitigation
- No secrets hardcoded — all via `VITE_*` environment variables
- Axios interceptor transparently refreshes expired tokens
- Role-based UI rendering prevents unauthorized access at route level

## Backend References

- [Employee API Swagger](http://localstack.lks.com:8080/service/swagger-ui.html) (via Nginx)
- [Users API Swagger](http://localstack.lks.com:8080/users/swagger-ui.html) (via Nginx)
