# PiFront — Agent Context & Architecture Reference

> Last updated: 2026-02-28  
> Purpose: Full context document for any AI agent working on this codebase.  
> Read this first before making any change to the project.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Current Folder Structure](#2-current-folder-structure)
3. [Key Architectural Choices](#3-key-architectural-choices)
4. [Routing — Nested Layout Shells](#4-routing--nested-layout-shells)
5. [Core Layer](#5-core-layer)
   - [Models](#models)
   - [AuthService](#authservice)
   - [authGuard](#authguard)
   - [roleGuard](#roleguard)
6. [Layout System](#6-layout-system)
   - [PublicLayoutComponent](#publiclayoutcomponent)
   - [DashboardLayoutComponent](#dashboardlayoutcomponent)
   - [AdminLayoutComponent](#adminlayoutcomponent)
   - [DashboardHeaderComponent](#dashboardheadercomponent)
   - [SidebarComponent](#sidebarcomponent)
7. [Nav Config Pattern](#7-nav-config-pattern)
8. [Login Workflow](#8-login-workflow)
9. [Signup Workflow](#9-signup-workflow)
10. [Logout](#10-logout)
11. [Rules for Agents](#11-rules-for-agents)

---

## 1. Project Overview

**PiFront** is an **Angular 18+ SSR application** (Angular Universal) using **exclusively standalone components** (no `NgModule` anywhere).

- **Backend API:** `http://localhost:8080/auth`
- **Rendering:** Server-Side Rendering via `server.ts` + `main.server.ts`
- **Auth:** JWT token stored in `localStorage` (SSR-safe)
- **Three user roles:** `STUDENT`, `TUTOR`, `ADMIN`
- **Three visual zones:** Public frontoffice, Authenticated jungle dashboards, Admin backoffice

---

## 2. Current Folder Structure

```
src/app/
│
├── app.component.ts              ← <router-outlet> + global toast/notification overlays
├── app.routes.ts                 ← Nested route tree with layout shell wrappers
├── app.config.ts                 ← Root providers: Router, HttpClient(withFetch + interceptor), Hydration, Animations
├── app.config.server.ts          ← SSR-specific providers
│
├── shared/                       ← Global overlays used across the app
│   └── toast/                    ← `app-toast`
│
├── core/                         ← Singleton, app-wide concerns
│   ├── guards/
│   │   ├── auth.guard.ts         ← Checks token exists → else redirect /login
│   │   └── role.guard.ts         ← Checks role matches route → else redirect own dashboard
│   ├── services/
│   │   └── auth.service.ts       ← JWT login/register/logout + localStorage helpers
│   └── models/
│       ├── user.model.ts         ← LoginRequest, LoginResponse, RegisterRequest, ForgotPasswordRequest, ResetPasswordRequest
│       └── nav-item.model.ts     ← NavItem { label, route, icon? }
│
├── frontoffice/
│   ├── layout/
│   │   ├── header/               ← Public header (selector: app-header)
│   │   │   ├── header.component.ts
│   │   │   ├── header.component.html
│   │   │   └── header.component.css
│   │   ├── footer/               ← Public footer (selector: app-footer)
│   │   │   ├── footer.component.ts
│   │   │   ├── footer.component.html
│   │   │   └── footer.component.css
│   │   ├── public-layout/        ← Shell: app-header + router-outlet + app-footer
│   │   │   └── public-layout.component.ts
│   │   └── dashboard-layout/     ← Shell: app-front-dashboard-header + app-front-sidebar + router-outlet
│   │       └── dashboard-layout.component.ts
│   │
│   ├── shared/                   ← Shared frontoffice helpers and overlays
│   │   ├── dashboard-material.imports.ts ← Common Material imports bundle for dashboard pages
│   │   └── app-notification-center/ ← Global notification stack rendered from AppComponent
│   │
│   ├── pages/
│   │   ├── landingpage/          ← Route: ""  (inside PublicLayout)
│   │   ├── login/                ← Route: "login"  (inside PublicLayout)
│   │   ├── signup/               ← Route: "signup" (inside PublicLayout)
│   │   ├── forgot-password/      ← Route: "forgot-password" (inside PublicLayout)
│   │   ├── reset-password/       ← Route: "reset-password" (inside PublicLayout)
│   │   └── verify-2fa/           ← Route: "verify-2fa" (inside PublicLayout)
│   │
│   └── jungle/                   ← Authenticated user dashboards
│       ├── student/
│       │   ├── student-nav.ts    ← STUDENT_NAV: NavItem[]
│       │   └── student-dashboard/  ← Route: "student/dashboard" 🔒 STUDENT only
│       └── tutor/
│           ├── tutor-nav.ts      ← TUTOR_NAV: NavItem[]
│           └── tutor-dashboard/  ← Route: "tutor/dashboard" 🔒 TUTOR only
│
└── backoffice/
    ├── layout/
    │   ├── header/               ← DashboardHeaderComponent (selector: app-dashboard-header)
    │   │   ├── header.component.ts    @Input() navItems + user badge + logout
    │   │   ├── header.component.html
    │   │   └── header.component.css
    │   ├── sidebar/              ← SidebarComponent (selector: app-sidebar)
    │   │   ├── sidebar.component.ts   @Input() navItems
    │   │   ├── sidebar.component.html
    │   │   └── sidebar.component.css
    │   └── admin-layout/         ← Shell: app-dashboard-header + app-sidebar + router-outlet
    │       └── admin-layout.component.ts   (also exports ADMIN_NAV)
    └── pages/
        └── admin-dashboard/      ← Route: "admin/dashboard" 🔒 ADMIN only
            └── admin-dashboard.component.ts
```

> 🔒 = protected by both `authGuard` AND `roleGuard`

---

## 3. Key Architectural Choices

| Concern | Solution |
|---|---|
| Rendering | Angular Universal SSR (`server.ts` + `main.server.ts`) |
| Components | All standalone — **never use NgModule** |
| Routing | Nested layout shells — each zone has its own parent layout route |
| HTTP | `provideHttpClient(withFetch(), withInterceptors([authInterceptor]))` — bearer token is attached centrally |
| Auth state | JWT token + user JSON stored in `localStorage`; 2FA handoff data stored in `sessionStorage` |
| App shell | Root `AppComponent` stays thin: router outlet plus global toast and notification center overlays |
| Shared imports | `frontoffice/shared/dashboard-material.imports.ts` bundles common Angular Material modules for dashboard-heavy screens |
| SSR safety | All `localStorage` access wrapped with `isPlatformBrowser(platformId)` |
| Route protection | Two guards: `authGuard` (token check) + `roleGuard(role)` (role check) |
| Navigation config | `NavItem[]` arrays defined per role, passed as `@Input()` to header & sidebar |
| Forms | Reactive Forms (`FormBuilder`, `Validators`) — no template-driven forms |

---

## 4. Routing — Nested Layout Shells

```typescript
// app.routes.ts
export const routes: Routes = [

  // ── Public Shell ──────────────────────────────────────────────
  // Renders: <app-header> + <router-outlet> + <app-footer>
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      { path: '',       component: LandingpageComponent },
      { path: 'login',  component: LoginComponent },
      { path: 'signup', component: SignupComponent },
      { path: 'forgot-password', component: ForgotPasswordComponent },
      { path: 'reset-password', component: ResetPasswordComponent },
      { path: 'verify-2fa', component: Verify2FAComponent },
    ]
  },

  // ── Authenticated Shell ────────────────────────────────────────
  // Renders: <app-front-dashboard-header> + <app-front-sidebar> + <router-outlet>
  // authGuard: must be logged in
  // roleGuard: per child, only the matching role can enter
  {
    path: '',
    component: DashboardLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'student/dashboard', component: StudentDashboardComponent, canActivate: [roleGuard('STUDENT')] },
      { path: 'tutor/dashboard',   component: TutorDashboardComponent,   canActivate: [roleGuard('TUTOR')]   },
    ]
  },

  // ── Admin Shell ────────────────────────────────────────────────
  // Renders: <app-dashboard-header> + <app-sidebar> + <router-outlet>
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, roleGuard('ADMIN')],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];
```

### Important: Two routes share `path: ''`
Angular matches the **first route that satisfies the guards**. The public shell always matches for unauthenticated users. The dashboard shell is reached only when `authGuard` passes.
The authenticated shell is much broader than this excerpt and mixes eager `component` routes with `loadComponent()` and `loadChildren()` feature routes; current examples include `student/ai-insights`, `student/forum`, `student/collaboration`, `tutor/sessions`, `tutor/analytics`, `admin/collaboration`, and `admin/payments/*`.

---

## 5. Core Layer

### Models

**`core/models/user.model.ts`** — all auth-related interfaces and auth recovery payloads:

```typescript
interface LoginRequest    { email: string; password: string; }
interface UserResponse    { id, firstName, lastName, email, role, accountStatus, createdAt, updatedAt, + optional profile + 2FA fields }
interface LoginResponse   { token: string; user: UserResponse; + optional requires2FA/email/twoFactorMethod }
interface RegisterRequest { firstName, lastName, email, password, role, + optional student/tutor fields }
interface ForgotPasswordRequest { email: string; }
interface ResetPasswordRequest { token: string; newPassword: string; }
```

**`core/models/nav-item.model.ts`** — navigation item shape:

```typescript
interface NavItem { label: string; route: string; icon?: string; }
```

---

### AuthService

**File:** `core/services/auth.service.ts`  
**API base:** `http://localhost:8080/auth`

| Method | HTTP | Description |
|---|---|---|
| `login(request)` | `POST /auth/login` | Saves `auth_token` + `auth_user` to localStorage via `tap()` |
| `register(request)` | `POST /auth/register` | Returns `UserResponse` |
| `forgotPassword(request)` | `POST /auth/forgot-password` | Triggers password reset email flow |
| `resetPassword(request)` | `POST /auth/reset-password` | Completes the reset with `token` + `newPassword` |
| `logout()` | — | Removes `auth_token` and `auth_user` from localStorage |
| `getToken()` | — | Returns JWT string or `null` |
| `getCurrentUser()` | — | Returns parsed `UserResponse` or `null` |
| `isLoggedIn()` | — | `true` if token exists |
| `getUserRole()` | — | Returns `'STUDENT' \| 'TUTOR' \| 'ADMIN' \| null` |

> All interfaces are now defined in `core/models/user.model.ts` and **re-exported** from `auth.service.ts` for backward compatibility. Import from either location. Prefer the shared `core/services/auth.service.ts` for new work; the older `frontoffice/jungle/student/services/auth.service.ts` is a legacy/dev-only in-memory service and should not be extended for new app features.

---

### authGuard

**File:** `core/guards/auth.guard.ts`

Functional `CanActivateFn`. Checks `authService.isLoggedIn()`.  
- ✅ Token present → allow
- ❌ No token → redirect to `/login`

---

### roleGuard

**File:** `core/guards/role.guard.ts`

Guard **factory** — call it with the required role: `roleGuard('STUDENT')`.  
Returns a `CanActivateFn` that checks `authService.getUserRole()` against the required role.

**Redirect matrix when role doesn't match:**

| Logged-in role | Required role | Redirected to |
|---|---|---|
| `STUDENT` | `TUTOR` or `ADMIN` | `/student/dashboard` |
| `TUTOR` | `STUDENT` or `ADMIN` | `/tutor/dashboard` |
| `ADMIN` | `STUDENT` or `TUTOR` | `/admin/dashboard` |
| `null` | any | `/login` |

> Always pair `roleGuard` **after** `authGuard`: `canActivate: [authGuard, roleGuard('ROLE')]`

---

## 6. Layout System

### PublicLayoutComponent

**File:** `frontoffice/layout/public-layout/public-layout.component.ts`  
**Selector:** `app-public-layout`  
**Template:** `<app-header> + <router-outlet> + <app-footer>`

Wraps the existing `HeaderComponent` (public nav with Login/Sign Up buttons) and `FooterComponent`. Used for all public pages (landing, login, signup, forgot password, reset password, verify 2FA).

---

### DashboardLayoutComponent

**File:** `frontoffice/layout/dashboard-layout/dashboard-layout.component.ts`  
**Selector:** `app-dashboard-layout`  
**Template:** `<app-front-dashboard-header [navItems]> + <app-front-sidebar [navItems]> + <router-outlet>`

**Nav resolution logic** (happens once in constructor):
```typescript
const role = authService.getUserRole();
this.navItems = role === 'TUTOR' ? TUTOR_NAV : STUDENT_NAV;
```

The layout resolves which nav to use based on role, then passes it **down** as `@Input()` to both the header and sidebar. Neither header nor sidebar ever touches `AuthService` directly. This is the frontoffice dashboard shell; the admin shell uses the backoffice `DashboardHeaderComponent` and `SidebarComponent`.

---

### AdminLayoutComponent

**File:** `backoffice/layout/admin-layout/admin-layout.component.ts`  
**Selector:** `app-admin-layout`  
**Template:** Same structure as DashboardLayout but uses hardcoded `ADMIN_NAV`.  
**Also exports:** `ADMIN_NAV: NavItem[]`

Keep `ADMIN_NAV` aligned with the current admin routes in `app.routes.ts` (for example: `dashboard`, `event-dashboard`, `users`, `courses`, `forum`, `collaboration`, `ai`, `payments`, `availability`, `bookings`, `quiz`).

---

### DashboardHeaderComponent

**File:** `backoffice/layout/header/header.component.ts`  
**Selector:** `app-dashboard-header`

```typescript
@Input() navItems: NavItem[]   // Nav links rendered in the top bar
```

Displays: logo, nav links (from `navItems`), logged-in user name, role badge (colour-coded), logout button.  
Calls `authService.getCurrentUser()` for user info.  
`logout()` calls `authService.logout()` then navigates to `/login`.

This is the admin shell header (`app-dashboard-header`); the authenticated learner/tutor shell uses `FrontDashboardHeaderComponent` under `frontoffice/layout/dashboard-layout/`.

**Role badge colours:**
- `STUDENT` → green
- `TUTOR` → blue
- `ADMIN` → red

---

### SidebarComponent

**File:** `backoffice/layout/sidebar/sidebar.component.ts`  
**Selector:** `app-sidebar`

```typescript
@Input() navItems: NavItem[]   // Links rendered in the left sidebar
```

Purely presentational. Iterates `navItems` and renders `routerLink` + `routerLinkActive` styled links.

---

## 7. Nav Config Pattern

Each role owns its nav config as a plain TypeScript constant:

```typescript
// frontoffice/jungle/student/student-nav.ts
export const STUDENT_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/student/dashboard', icon: '🏠' },
  { label: 'AI Insights', route: '/student/ai-insights', icon: '🧠' },
  { label: 'Learning Path', route: '/student/learning-path', icon: '🗺️' },
  { label: 'Forum', route: '/student/forum', icon: '💬' },
  { label: 'Collaboration Hub', route: '/student/collaboration', icon: '🤝' },
  { label: 'Events', route: '/events', icon: '📅' },
  { label: 'Quizzes', route: '/student/quiz', icon: '📝' },
  { label: 'My Profile', route: '/student/profile', icon: '👤' },
  { label: 'Settings', route: '/student/settings', icon: '⚙️' },
];

// frontoffice/jungle/tutor/tutor-nav.ts
export const TUTOR_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/tutor/dashboard', icon: '🏠' },
  { label: 'Rooms', route: '/tutor/rooms', icon: '🧩' },
  { label: 'Challenges', route: '/tutor/challenges', icon: '📝' },
  { label: 'Analytics', route: '/tutor/analytics', icon: '📈' },
  { label: 'Sessions', route: '/tutor/sessions', icon: '⏱️' },
  { label: 'Availability', route: '/tutor/availability', icon: '🗓️' },
  { label: 'Students', route: '/tutor/students', icon: '👥' },
  { label: 'Quizzes', route: '/tutor/quiz', icon: '📝' },
];

// backoffice/layout/admin-layout/admin-layout.component.ts (exported from here)
export const ADMIN_NAV: NavItem[] = [
  { label: 'Dashboard', route: '/admin/dashboard' },
  { label: 'Event Dashboard', route: '/admin/event-dashboard' },
  { label: 'Users', route: '/admin/users' },
  { label: 'Courses', route: '/admin/courses' },
  { label: 'Forum Moderation', route: '/admin/forum' },
  { label: 'Collab Overview', route: '/admin/collaboration' },
  { label: 'AI Overview', route: '/admin/ai' },
  { label: 'Payments', route: '/admin/payments/dashboard' },
  { label: 'Bookings', route: '/admin/bookings' },
  { label: 'Quiz', route: '/admin/quiz' },
];
```

**To add a new page to a role:** add a `NavItem` entry to that role's nav file and add the route to `app.routes.ts`. Nothing else changes.

---

## 8. Login Workflow

### Step-by-step

1. **Form** — `LoginComponent` builds a Reactive Form: `email` (required, email), `password` (required, min 6), `rememberMe` (checkbox, UI only).
2. **Validation** — `submitted = true` on submit triggers all inline error messages. Invalid form stops here.
3. **HTTP** — `authService.login({ email, password })` → `POST http://localhost:8080/auth/login`
4. **Storage** — On success, `tap()` in `AuthService` stores:
   - `localStorage["auth_token"]` = JWT string
   - `localStorage["auth_user"]` = JSON stringified `UserResponse`
5. **Redirect** by auth result:

   - If `response.requires2FA` is true, store `2fa_email` and `2fa_method` in `sessionStorage` and redirect to `/verify-2fa`.
   - Otherwise redirect by role:

| Role | Redirects to |
|---|---|
| `STUDENT` | `/student/dashboard` |
| `TUTOR` | `/tutor/dashboard` |
| `ADMIN` | `/admin/dashboard` |
| unknown | `/` |

6. **Password recovery** lives on separate public routes: `ForgotPasswordComponent` calls `authService.forgotPassword()` on `/forgot-password`, and `ResetPasswordComponent` calls `authService.resetPassword()` on `/reset-password?token=...`.

7. **Error handling:**

| HTTP Status | Message |
|---|---|
| `401` | "Invalid email or password." |
| other | "An error occurred. Please try again." |

### Sequence Diagram

```
User          LoginComponent        AuthService          Backend
 │                  │                    │                  │
 │── fills form ───►│                    │                  │
 │                  │─ validate() ──────►│                  │
 │                  │── login(creds) ───►│── POST /login ──►│
 │                  │                    │◄── {token,user} ─│
 │                  │                    │─ localStorage ───┤
 │                  │◄── LoginResponse ──│                  │
  │                  │─ 2FA or navigate by role │            │
 │◄── redirected ───│                    │                  │
```

The 2FA path pauses here, then resumes in `verify-2fa` after the token is stored and the user profile is loaded.

---

## 9. Signup Workflow

`SignupComponent` calls `authService.register()` → `POST http://localhost:8080/auth/register`.

Role-specific fields in `RegisterRequest`:

| Field | STUDENT | TUTOR |
|---|---|---|
| `firstName`, `lastName`, `email`, `password`, `role` | ✅ | ✅ |
| `level`, `learningGoals` | ✅ | — |
| `bio`, `specialization`, `experienceYears`, `hourlyRate` | — | ✅ |

On success → redirect to `/login`.

---

## 10. Logout

`authService.logout()` removes both localStorage keys:
```
localStorage.removeItem('auth_token')
localStorage.removeItem('auth_user')
```
The `DashboardHeaderComponent` calls this on the logout button click and then navigates to `/login`.  
After logout, both `authGuard` and `roleGuard` will redirect any protected route attempt back to `/login`.

---

## 11. Rules for Agents

> Read carefully before making any change.

1. **Never create a new component if one already exists.** Check the folder structure above first. Rename or repurpose the existing component instead.

2. **Never use NgModule.** All components are standalone. Use `imports: []` in the `@Component` decorator.

3. **Never put layout shell header/footer in `AppComponent`.** Keep the root component thin: `AppComponent` should only host the router outlet plus the shared toast/notification overlays. Layout is handled by shell components (`PublicLayoutComponent`, `DashboardLayoutComponent`, `AdminLayoutComponent`).

4. **Match the router imports to the template.** Use `RouterModule` for shells/menus that rely on `routerLink` + `routerLinkActive`, and import standalone directives like `RouterLink`/`RouterOutlet` directly in lighter pages when that is all they use. Do not introduce `NgModule`.

5. **Guards must be chained correctly:** `canActivate: [authGuard, roleGuard('ROLE')]`. Never apply `roleGuard` without `authGuard` preceding it on the same route or parent route.

6. **All localStorage access must be SSR-safe.** Wrap with `isPlatformBrowser(this.platformId)`. Inject `PLATFORM_ID` via `@Inject(PLATFORM_ID)`.

7. **Nav items live in nav config files**, not in components. Add new nav entries to the relevant `*-nav.ts` file. The layout components pick them up automatically.

8. **Models live in `core/models/`.** Do not re-declare interfaces inline in services or components. Prefer the shared `core/services/auth.service.ts` over the legacy student-scoped `frontoffice/jungle/student/services/auth.service.ts` for new auth logic.

9. **The `DashboardLayoutComponent` resolves nav once in the constructor** based on role. Do not add role-checking logic inside `DashboardHeaderComponent`/`SidebarComponent` or the frontoffice `FrontDashboardHeaderComponent`/`FrontSidebarComponent` — they are purely presentational and receive data via `@Input()`.

10. **When adding a new role's dashboard:**
    - Create a `*-nav.ts` file with the nav items
    - Create a layout component (or reuse `DashboardLayoutComponent` if the shell is the same)
    - Add routes to `app.routes.ts` with `canActivate: [authGuard, roleGuard('NEW_ROLE')]`
    - Add the redirect case to `login.component.ts` and `role.guard.ts`

