# Minimal Frontend Design for Auth Service (Updated)


This document describes a minimal, functional React frontend for the authentication API, updated with:
---

# Functional Flow & Abstraction Design

## Central API Abstraction

- All API requests go through a single function, e.g. `api.request<T>(options): Promise<T>`
	- Handles: base URL, headers (including `X-HridayKh-In-Client-ID`), `credentials: 'include'`, JSON parsing, error normalization.
- Each feature (login, register, etc.) has its own API function (e.g., `api.login`, `api.register`, `api.getMe`, `api.updateProfile`, `api.getSessions`, etc.) that calls `api.request` with the right method/path/body and handles feature-specific logic (e.g., response shape, error mapping).
- Page components only call the feature API (never the central request directly).

**Example structure:**

```
src/
	api/
		client.ts   // central request function
		auth.ts     // login, register, getMe, updateProfile, changePassword, resendVerification
		sessions.ts // getSessions, deleteSession
	pages/
		Login.tsx
		Register.tsx
		Profile.tsx
		ChangePassword.tsx
		Sessions.tsx
```

---

## Page-by-Page Functional Flow

### Login Page
1. User enters email/password and clicks "Sign in".
2. Calls `api.login(email, password)`:
		- Calls `api.request({ method: 'POST', path: '/v1/users/sessions', body: { email, pass } })`
		- On 200: calls `api.getMe()` to fetch user profile
		- On error: shows error, always shows "Resend verification" button
3. On success: sets user in context, redirects to `?redirect` or `/profile`.
4. On "Sign in with Google": browser navigates to `/googleLoginInitiate?redirect=...`
5. On "Resend verification": calls `api.resendVerification(email)`

### Register Page
1. User enters email, name, password and clicks "Create account".
2. Calls `api.register(email, password, fullName)`:
		- Calls `api.request({ method: 'POST', path: '/v1/users', body: { email, password, fullName } })`
		- On success: shows "Check your email" message, then redirects
		- On error: shows error, always shows "Resend verification" button
3. On "Resend verification": calls `api.resendVerification(email)`

### Profile Page
1. On mount, calls `api.getMe()` to load user info.
2. Shows all user fields (uuid, email, fullName, profilePic, createdAt, lastLogin, etc.).
3. User edits profile and clicks "Save": calls `api.updateProfile({ fullName, profilePic })`
4. On "Logout": calls `api.logout()`, clears context, redirects
5. Links to Change Password and Sessions pages

### Change Password Page
1. User enters current and new password, clicks "Change password".
2. Calls `api.changePassword(currentPassword, newPassword)`:
		- Calls `api.request({ method: 'POST', path: '/v1/users/me/password', body: { currentPassword, newPassword } })`
		- On success: shows confirmation, redirects
		- On error: shows error

### Sessions Page
1. On mount, calls `api.getSessions()` to load all sessions.
2. Shows session info (sessionId, createdAt, lastAccessAt, ip, userAgent, expiresAt).
3. User clicks "Revoke" on a session: calls `api.deleteSession(sessionId)`
4. User clicks "Logout everywhere": calls `api.logout()`
5. On current session revoke, redirects to `/login` or `?redirect`

---

## TypeScript Types (Recommended)

```ts
// User profile (from README)
export interface User {
	uuid: string;
	email: string;
	fullName?: string;
	profilePic?: string;
	isVerified?: boolean;
	createdAt: number;
	updatedAt: number;
	lastLogin?: number;
	metadata?: any;
	permissions?: any;
	accType: string;
	googleId?: string;
	refreshToken?: string;
	refreshTokenExpiresAt?: number;
	internal?: any;
}

// Session info (from README)
export interface Session {
	sessionId: string;
	createdAt: number;
	lastAccessedAt: number;
	expiresAt: number;
	ip?: string;
	userAgent?: string;
}

// API error response
export interface ApiError {
	type: 'error';
	message: string;
}

// API success response (generic)
export interface ApiSuccess<T> {
	type: 'success';
	data: T;
}
```

---

## Summary of Abstraction Flow

- Page component (e.g., `Login.tsx`) calls feature API (e.g., `api.login`)
- Feature API calls central `api.request` with method/path/body
- Central API handles headers, cookies, error normalization
- All types are defined in a shared `types.ts` file

---

This structure keeps each page simple, testable, and easy to maintain. You can add more features or swap out the backend easily by only changing the API layer.
- Support for ?redirect param on all pages (login, register, profile, change password, sessions)
- Register page always shows "Resend verification" option on error
- Dedicated Change Password page
- Sessions page/section for logged-in devices
- User and session info fields as per backend README

---


## Pages & API Endpoints


### 1. Login Page (`/login`)
**Purpose:** Sign in with email/password or Google. Show errors and allow resending verification if needed. After successful login, redirect to the ?redirect param if present, else to /profile.

**UI Elements:**
- Email input
- Password input
- "Sign in" button
- "Sign in with Google" button (redirects to Google login)
- Link to Register page
- Error message area (shows invalid credentials, unverified, etc.)
- "Resend verification" button (shown on error)

**API Endpoints Used:**
- `POST /v1/users/sessions` — login with email/password
- `GET /v1/users/me` — fetch user after login (to check if logged in)
- `POST /v1/users/verify/resend` — resend verification email (if login fails for any reason)

**Notes:**
- On successful login, redirect to the ?redirect param if present, else to `/profile`.
- On any login error, show "Resend verification" option.
- On Google login, redirect to `/googleLoginInitiate?redirect=<frontendOrigin>/<redirect or profile>&source=glink` (handled by backend).

---


### 2. Register Page (`/register`)
**Purpose:** Create a new account. If registration fails for any reason, always show a "Resend verification" option. After success, redirect to ?redirect param if present, else /profile.

**UI Elements:**
- Email input
- Full name input (optional)
- Password input
- "Create account" button
- Link to Login page
- Error/success message area
- "Resend verification" button (always shown on error)

**API Endpoints Used:**
- `POST /v1/users` — register new user
- `POST /v1/users/verify/resend` — resend verification email (always available on error)

**Notes:**
- On success, show "Check your email" message, then redirect to ?redirect or /profile.
- On any error, show resend verification option.

---


### 3. Profile Page (`/profile`)
**Purpose:** View and update your profile. Also provides logout. Has links to Change Password and Sessions pages. Redirects to ?redirect if present after update/logout.

**UI Elements:**
- Show user info: avatar, name, email, verified badge
- Edit form: full name, profile picture URL
- "Save" button
- "Logout" button
- Link/button to Change Password page
- Link/button to Sessions page
- Error/success message area

**API Endpoints Used:**
- `GET /v1/users/me` — fetch current user info (on page load and after edits)
- `PATCH /v1/users/me` — update profile info
- `DELETE /v1/users/sessions/current` — logout

**Notes:**
- On logout, clear user state and redirect to `/login` or ?redirect.
- Show errors inline if update fails.
- Show all user profile fields as per README (uuid, email, fullName, profilePic, createdAt, lastLogin, etc.).


### 4. Change Password Page (`/profile/password`)
**Purpose:** Change your password (for password-based accounts).

**UI Elements:**
- Current password input
- New password input
- "Change password" button
- Error/success message area

**API Endpoints Used:**
- `POST /v1/users/me/password` — change password

**Notes:**
- On success, show confirmation and redirect to ?redirect or /profile.
- On error, show message (e.g., wrong current password, not allowed for Google accounts).

---


### 5. Sessions Page (`/profile/sessions`)
**Purpose:** Show all logged-in devices/sessions and allow revoking them.

**UI Elements:**
- List of sessions (sessionId, createdAt, lastAccessAt, ip, userAgent, expiresAt)
- Button to revoke (delete) each session
- Button to revoke current session (logout everywhere)
- Error/success message area

**API Endpoints Used:**
- `GET /v1/users/sessions/me` — list sessions
- `DELETE /v1/users/sessions/{sessionId}` — revoke a session
- `DELETE /v1/users/sessions/current` — logout current session

**Notes:**
- Show all session info fields as per README (sessionId, createdAt, lastAccessAt, ip, userAgent, expiresAt).
- On revoke, update the list. If current session is revoked, redirect to /login or ?redirect.

---


## Global Behaviors

- All API calls to protected endpoints must include header: `X-HridayKh-In-Client-ID` (from `.env` as `REACT_APP_CLIENT_ID`).
- All fetches must use `credentials: 'include'` so cookies are sent and received.
- Auth state is determined by calling `GET /v1/users/me` (not by storing tokens in localStorage).
- Show clear error messages for all API errors (use `{ type: 'error', message: '...' }` from backend).
- Minimal client-side validation: required fields, password length >= 8.
- All pages support a ?redirect param and redirect there after success (login, register, profile update, change password, logout, session revoke). If not set, default to /profile.

---


## Optional (not included for now)
- Password reset (endpoints not implemented)

---


## Summary Table

| Page            | Route                | Main API Endpoints Used                                      |
|-----------------|----------------------|--------------------------------------------------------------|
| Login           | /login               | POST /v1/users/sessions, GET /v1/users/me, POST /v1/users/verify/resend |
| Register        | /register            | POST /v1/users, POST /v1/users/verify/resend                 |
| Profile         | /profile             | GET /v1/users/me, PATCH /v1/users/me, DELETE /v1/users/sessions/current |
| Change Password | /profile/password    | POST /v1/users/me/password                                   |
| Sessions        | /profile/sessions    | GET /v1/users/sessions/me, DELETE /v1/users/sessions/{sessionId}, DELETE /v1/users/sessions/current |

---

This is the minimal, easy-to-use frontend design. Make any changes you want, then we’ll move on to structuring the frontend code.
