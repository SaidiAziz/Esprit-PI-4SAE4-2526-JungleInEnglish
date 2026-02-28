# PiFront — Architecture & Login Workflow

> Generated: 2026-02-28

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Folder Structure](#2-folder-structure)
3. [Key Architectural Choices](#3-key-architectural-choices)
4. [Routing](#4-routing)
5. [Core Layer](#5-core-layer)
   - [AuthService](#authservice)
   - [AuthGuard](#authguard)
6. [Login Workflow](#6-login-workflow)
   - [Step-by-step](#step-by-step)
   - [Sequence Diagram](#sequence-diagram)
   - [Role-based Redirect](#role-based-redirect)
   - [Error Handling](#error-handling)
7. [Signup Workflow](#7-signup-workflow)
8. [Logout](#8-logout)

---

## 1. Project Overview

**PiFront** is an **Angular 17+ application** with **Server-Side Rendering (SSR)** via Angular Universal.  
It uses exclusively **standalone components** (no `NgModule`).

The app connects to a backend REST API running at:
```
http://localhost:8081/auth
```

---

## 2. Folder Structure

```
PiFront/
├── angular.json              ← Angular CLI workspace config
├── package.json
├── server.ts                 ← Express SSR server entry point
├── tsconfig.json
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── index.html
    ├── main.ts               ← Browser bootstrap
    ├── main.server.ts        ← SSR bootstrap
    ├── styles.css            ← Global styles
    │
    └── app/
        ├── app.component.ts  ← Root component
        ├── app.config.ts     ← Root providers (Router, HttpClient, Hydration)
        ├── app.config.server.ts ← SSR-specific providers
        ├── app.routes.ts     ← Centralised route definitions
        │
        ├── core/             ← Singleton, app-wide concerns
        │   ├── guards/
        │   │   └── auth.guard.ts      ← Protects private routes
        │   └── services/
        │       └── auth.service.ts   ← JWT auth logic + localStorage
        │
        ├── frontoffice/      ← Public-facing area
        │   ├── layout/
        │   │   ├── header/   ← Public navigation header
        │   │   └── footer/   ← Public footer
        │   ├── pages/
        │   │   ├── landingpage/  ← Route: "/"
        │   │   ├── login/        ← Route: "/login"
        │   │   └── signup/       ← Route: "/signup"
        │   └── jungle/       ← Authenticated user dashboards
        │       ├── student/
        │       │   └── student-dashboard/  ← Route: "/student/dashboard" 🔒
        │       └── tutor/
        │           └── tutor-dashboard/    ← Route: "/tutor/dashboard"   🔒
        │
        └── backoffice/       ← Admin area (layout scaffolded, no routes yet)
            └── layout/
                ├── header/
                └── sidebar/
```

> 🔒 = protected by `authGuard`

---

## 3. Key Architectural Choices

| Concern | Solution |
|---|---|
| Rendering | Angular Universal SSR (`server.ts` + `main.server.ts`) |
| Components | Standalone (no NgModule) |
| Routing | `provideRouter(routes)` — flat route array in `app.routes.ts` |
| HTTP | `provideHttpClient(withFetch())` — fetch-based for SSR compatibility |
| Auth state | JWT token + user object stored in **localStorage** |
| Route protection | Functional `CanActivateFn` guard (`authGuard`) |
| Forms | **Reactive Forms** (`FormBuilder`, `Validators`) |
| Change Detection | `provideZoneChangeDetection({ eventCoalescing: true })` |

---

## 4. Routing

```typescript
// app.routes.ts
export const routes: Routes = [
  { path: '',                component: LandingpageComponent },
  { path: 'login',           component: LoginComponent },
  { path: 'signup',          component: SignupComponent },

  // 🔒 Protected routes
  { path: 'student/dashboard', component: StudentDashboardComponent, canActivate: [authGuard] },
  { path: 'tutor/dashboard',   component: TutorDashboardComponent,   canActivate: [authGuard] },

  { path: '**', redirectTo: '' }  // Wildcard fallback
];
```

---

## 5. Core Layer

### AuthService

**File:** `src/app/core/services/auth.service.ts`  
**API base:** `http://localhost:8081/auth`

#### Data models

```typescript
// Sent to POST /auth/login
interface LoginRequest {
  email: string;
  password: string;
}

// Received from the backend after login/register
interface UserResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'STUDENT' | 'TUTOR' | 'ADMIN';
  accountStatus: string;
  createdAt: string;
  updatedAt: string;
}

// Full login response (token + user)
interface LoginResponse {
  token: string;
  user: UserResponse;
}
```

#### Methods

| Method | Description |
|---|---|
| `login(request)` | POST `/auth/login`, saves token & user to localStorage |
| `register(request)` | POST `/auth/register`, returns `UserResponse` |
| `logout()` | Removes `auth_token` and `auth_user` from localStorage |
| `getToken()` | Returns the JWT string from localStorage (or `null`) |
| `getCurrentUser()` | Returns the parsed `UserResponse` from localStorage |
| `isLoggedIn()` | Returns `true` if a token exists in localStorage |
| `getUserRole()` | Returns `'STUDENT' \| 'TUTOR' \| 'ADMIN' \| null` |

> **SSR Safety:** every localStorage access is wrapped with `isPlatformBrowser(platformId)` to avoid errors during server-side rendering.

---

### AuthGuard

**File:** `src/app/core/guards/auth.guard.ts`

```typescript
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;               // ✅ token found → allow navigation
  }

  router.navigate(['/login']); // ❌ no token → redirect to login
  return false;
};
```

---

## 6. Login Workflow

### Step-by-step

1. **Form rendering**  
   `LoginComponent` initialises a Reactive Form with three controls:
   - `email` — required, must be a valid email format
   - `password` — required, minimum 6 characters
   - `rememberMe` — checkbox (stored in the form, not sent to the API)

2. **Client-side validation**  
   On submit, `submitted = true` forces all error messages to appear.  
   If the form is **invalid**, submission stops here.

3. **HTTP request**  
   If the form is valid, the component:
   - Sets `loading = true` and disables the form (prevents double-submit)
   - Calls `authService.login({ email, password })`
   - Which fires `POST http://localhost:8081/auth/login`

4. **Token & user storage**  
   Inside `AuthService.login()`, a `tap()` operator intercepts the successful response and stores:
   ```
   localStorage["auth_token"] = "<JWT string>"
   localStorage["auth_user"]  = "<JSON stringified UserResponse>"
   ```

5. **Role-based redirect** *(see table below)*  
   `LoginComponent` reads `response.user.role` and navigates accordingly.

6. **Error handling**  
   On failure, the form is re-enabled and an inline error message is shown.

---

### Sequence Diagram

```
User              LoginComponent          AuthService           Backend API
 │                     │                      │                      │
 │── fills form ──────►│                      │                      │
 │   (email+password)  │                      │                      │
 │                     │ onSubmit()           │                      │
 │                     │─ validate form ─────►│                      │
 │                     │  (Reactive Forms)    │                      │
 │                     │                      │                      │
 │                     │── login(email,pwd) ─►│                      │
 │                     │                      │── POST /auth/login ─►│
 │                     │                      │                      │
 │                     │                      │◄── { token, user } ──│
 │                     │                      │                      │
 │                     │                      │ localStorage.set(     │
 │                     │                      │   "auth_token", jwt) │
 │                     │                      │   "auth_user", user) │
 │                     │                      │                      │
 │                     │◄── LoginResponse ────│                      │
 │                     │                      │                      │
 │                     │ role-based redirect  │                      │
 │◄── navigates ───────│                      │                      │
```

---

### Role-based Redirect

| Role | Redirects to |
|---|---|
| `STUDENT` | `/student/dashboard` |
| `TUTOR` | `/tutor/dashboard` |
| `ADMIN` (or unknown) | `/` (landing page) |

---

### Error Handling

| HTTP Status | Message shown |
|---|---|
| `401 Unauthorized` | *"Invalid email or password."* |
| Any other error | *"An error occurred. Please try again."* |

---

## 7. Signup Workflow

`SignupComponent` collects user information and calls `AuthService.register()`.  
The `RegisterRequest` model supports both roles:

| Field | STUDENT | TUTOR |
|---|---|---|
| `firstName`, `lastName`, `email`, `password`, `role` | ✅ | ✅ |
| `level`, `learningGoals` | ✅ | — |
| `bio`, `specialization`, `experienceYears`, `hourlyRate` | — | ✅ |

The backend endpoint is `POST http://localhost:8081/auth/register`.  
On success, the user is typically redirected to `/login`.

---

## 8. Logout

Calling `AuthService.logout()` removes both keys from localStorage:

```typescript
localStorage.removeItem('auth_token');
localStorage.removeItem('auth_user');
```

After logout, any attempt to access a guarded route (`/student/dashboard`, `/tutor/dashboard`) will be intercepted by `authGuard` and redirected to `/login`.

