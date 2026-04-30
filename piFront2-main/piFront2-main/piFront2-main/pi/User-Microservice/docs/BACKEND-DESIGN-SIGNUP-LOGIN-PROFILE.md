# Backend Design: Visitor → Sign up → Login → Profile & Dashboard

## Scenario (Angular frontend)

1. Visitor lands on **homepage**
2. Clicks **Sign up as Student** (or Tutor) → single form with **user + profile** fields
3. Submits form → backend creates **user + profile in one request**
4. User **logs in** (email + password) → receives JWT
5. User opens **profile & dashboard** → backend returns **current user** and **current user's profile** using JWT

---

## Architecture Overview

```
[Angular]  →  POST /auth/register (user + profile)  →  [Backend]  →  DB (user + student_profile / tutor_profile)
[Angular]  →  POST /auth/login (email, password)   →  [Backend]  →  JWT + user info
[Angular]  →  GET /users/me, GET /studentProfile/me (with Bearer JWT)  →  [Backend]  →  current user & profile
```

---

## What the backend provides

| Step | Frontend action | Backend API | Notes |
|------|-----------------|------------|--------|
| Sign up | Submit form (user + profile) | `POST /user/auth/register` or `POST /user/users/addUser` | One request; profile fields optional in body |
| Login | Submit email + password | `POST /user/auth/login` | Returns JWT + user (no password); frontend stores JWT |
| Profile / Dashboard | Request "my" data with JWT | `GET /user/users/me`, `GET /user/studentProfile/me` or `GET /user/tutorProfile/me` | Requires `Authorization: Bearer <token>` |

---

## Changes in current backend

1. **One-step registration with profile data**
   - `RegisterUserRequest`: add optional fields for student (`level`, `learningGoals`) and tutor (`bio`, `specialization`, `experienceYears`, `hourlyRate`).
   - On user create, create profile by role and fill it from these fields when present.

2. **Authentication (login + JWT)**
   - `UserRepository.findByEmail` for login.
   - Spring Security + JWT: login endpoint returns JWT; filter validates token and sets current user in `SecurityContext`.
   - Public: `POST /auth/login`, `POST /auth/register` (or `/users/addUser`). Protected: everything else (or allow read for “me” only).

3. **“Me” endpoints**
   - `GET /users/me` → current user from JWT.
   - `GET /studentProfile/me` → current user’s student profile (404 if not student).
   - `GET /tutorProfile/me` → current user’s tutor profile (404 if not tutor).
   - Repositories: `StudentProfileRepository.findByUser_Id`, `TutorProfileRepository.findByUser_Id`.

4. **CORS** (for Angular on another port)
   - Allow origin of the Angular app in Spring (e.g. `http://localhost:4200`).

---

## API summary (after implementation)

Base URL: `http://localhost:8222/user` (context path = `/user`).

- **Public (no JWT)**
  - `POST /user/users/addUser` — register (user + optional profile data); body includes `role` and optional profile fields.
  - `POST /user/auth/register` — same as above; alternative entry for signup.
  - `POST /user/auth/login` — login; body: `{ "email", "password" }`; response: `{ "token", "user" }`.

- **Protected (require header: `Authorization: Bearer <token>`)**
  - `GET /user/users/me` — current user.
  - `GET /user/studentProfile/me` — current user’s student profile.
  - `GET /user/tutorProfile/me` — current user’s tutor profile.
  - Other existing endpoints (get/update user, get/update/delete profiles, etc.) require authentication.

This design lets the frontend drive the flow: one signup request, one login request, then “me” and “my profile” for dashboard and profile UI.
