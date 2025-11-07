# API Endpoints — Detailed Reference

This file documents the live API endpoints implemented in this repository (as of Nov 7, 2025). The router is `servlets.ApiServlet` and canonical paths are declared in `servlets.ApiConstants`.

Each endpoint below includes:
- HTTP method and path
- Handler (class + static method)
- Authentication / headers required
- Request shape (JSON or query params)
- Success responses and typical error codes

---

## 1) Create / Register user

- Method: POST
- Path: /v1/users
- Handler: `servlets.usersCreate.UsersCreator.createUser(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Public (no API key or session required)
- Request JSON body (required):

```json
{
  "email": "user@example.com",
  "pass": "user-password",
  "fullName": "John Doe",
  "redirect": "https://yourfrontend/app/welcome"
}
```

- Behavior: Validates that the email is not already taken. Creates a new user row with `accType`="password", inserts an email verification token and emails a verification link (`/v1/users/verify?token=...&redirect=...`). Transactional: if insert fails, rolls back.
- Success: 201 Created with JSON { type: "success", message: "User registered successfully. Please check your email account verification link." }
- Errors: 409 Conflict (user exists), 400 Bad Request (invalid inputs), 500 Internal Server Error

---

## 2) Verify email (browser flow)

- Method: GET
- Path: /v1/users/verify?token={token}&redirect={url}
- Handler: `servlets.usersCreate.UsersVerifier.verifyUser(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Public — intended to be opened from an email link in a browser
- Query params:
  - `token` (string, required)
  - `redirect` (string, URL to redirect to after verification)
- Behavior: Validates token, marks user verified, expires token, creates session cookie for the user, and redirects to the provided `redirect` URL with a success or error query string.
- Success: HTTP redirect to `redirect?type=success&msg=Email verified successfully.`
- Errors: Redirect to `redirect?type=error&msg=...` for missing/invalid token or server errors

---

## 3) Resend verification email

- Method: POST
- Path: /v1/users/verify/resend
- Handler: `servlets.usersCreate.UsersVerifier.resendVerifyEmail(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Public
- Request JSON body:

```json
{
  "email": "user@example.com",
  "redirect": "https://yourfrontend/app/welcome"
}
```

- Behavior: If user exists and is not verified, invalidates any old token, creates a new one, emails the verification link, and returns success JSON.
- Success: 200 OK { type: "success", message: "A new verification email has been sent." }
- Errors: 404 Not Found (no such user), 400 Bad Request, 500 Internal Server Error

---

## 4) Get current user's public profile

- Method: GET
- Path: /v1/users/me
- Handler: `servlets.usersInfo.UsersInfoManager.getUserInfo(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Requires session cookie created by `AuthUtil` (see `AuthUtil.createAndSetAuthCookie`)
- Behavior: Reads session cookie, looks up user, and returns JSON user object (public profile fields).
- Success: 200 OK with user JSON (uuid, email, fullName, profilePic, isVerified, accType, createdAt, lastLogin, metadata, permissions)
- Errors: 401 Unauthorized (no/invalid session), 404 Not Found (user missing), 500 Internal Server Error

---

## 5) Update current user's profile

- Method: PATCH
- Path: /v1/users/me
- Handler: `servlets.usersInfo.UsersInfoManager.updateUserInfo(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Requires session cookie
- Request JSON body (all optional):

```json
{
  "email": "newemail@example.com",
  "profile_pic": "https://example.com/pic.jpg",
  "full_name": "Jane Doe"
}
```

- Behavior: Validates session, applies email change (with format check and uniqueness), or updates profile picture / full name. Email changes are blocked for `accType` != "password" (e.g., google accounts). Uses DB transaction for consistency.
- Success: 200 OK { type: "success", message: "Profile updated successfully." } or 200 OK when no change provided
- Errors: 400 Bad Request (invalid email), 403 Forbidden (google-linked) , 409 Conflict (email already used), 401 Unauthorized, 500 Internal Server Error

---

## 6) Update authenticated user's password

- Method: POST
- Path: /v1/users/me/password
- Handler: `servlets.usersCreate.UsersPassUpdater.updateUserPass(HttpServletRequest, HttpServletResponse, Map<String,String>)` (method referenced in router)
- Auth: Requires session cookie
- Request JSON body (expected):

```json
{
  "old": "current-password",
  "new": "new-password"
}
```

- Behavior: Validates session, checks old password (SHA-256 compare), and updates stored password hash. Prevents password operations for Google-linked accounts.
- Success: 200 OK { type: "success", message: "Password is updated!" }
- Errors: 401 Unauthorized/Bad Request for invalid old password or missing fields, 500 Internal Server Error

Note: The router also registers password-reset endpoints (see next section); the reset-init/update handlers are currently wired to `USERS_PASSWORD_RESET_INIT` and `USERS_PASSWORD_RESET_UPDATE` constants — some implementations may be unimplemented or stubbed.

---

## 7) Password reset flow (tokened)

- Initiate reset
  - Method: POST
  - Path: /v1/users/password-resets
  - Handler: mapped to `USERS_PASSWORD_RESET_INIT` in router (currently the router points to a placeholder that returns 501 Not Implemented in `ApiServlet` initial static block)

- Complete reset
  - Method: PUT
  - Path: /v1/users/password-resets
  - Handler: mapped to `USERS_PASSWORD_RESET_UPDATE` (also currently not implemented in the router — placeholder returns 501)

- Behavior: Expected flow is: POST creates a reset token and emails it; PUT consumes the token and sets the new password. Confirm implementation status in `servlets.userPasswords` and DB DAOs.

---

## 8) Sessions (list, create/login, delete)

1) List sessions

- Method: GET
- Path: /v1/users/sessions/me
- Handler: `servlets.userSessions.UsersSessionList.listUserSessions(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Requires session cookie
- Behavior: Retrieves sessions for the authenticated user. The implementation enforces that a user can only view their own sessions (unless admin privileges are added). Returns an array of session objects: session_id, user_uuid, created_at, last_accessed_at, expires_at, user_agent, is_active.
- Success: 200 OK with JSON { type: "success", sessions: [...] }
- Errors: 401 Unauthorized, 404 Not Found, 500 Internal Server Error

2) Create session (login)

- Method: POST
- Path: /v1/users/sessions
- Handler: `servlets.userSessions.UsersSessionCreate.createUserSession(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Public (login operation)
- Request JSON body:

```json
{
  "email": "user@example.com",
  "pass": "user-password"
}
```

- Behavior: Validates credentials (password matches stored SHA-256 hash), ensures account is verified and accType != "google" (Google accounts must use OAuth flow). Updates lastLogin and sets auth cookie.
- Success: 200 OK { type: "success", message: "Logged In Successfully, Redirecting...." }
- Errors: 400 Bad Request (invalid credentials / use google), 500 Internal Server Error

3) Delete a specific session

- Method: DELETE
- Path: /v1/users/sessions/{sessionId}
- Handler: `servlets.userSessions.UsersSessionDelete.deleteUserSession(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Requires session cookie (the user must be logged in)
- Behavior: Validates session cookie, checks that the requesting user is the owner of the session being invalidated, then invalidates it via `SessionDAO.invalidateSession`. Returns success or permission error.
- Success: 200 OK { type: "success", message: "Session Removed Successfully" }
- Errors: 401 Unauthorized, 400 Bad Request (missing sessionId), 403 Forbidden (not the owner), 500 Internal Server Error

4) Delete current session (logout)

- Method: DELETE
- Path: /v1/users/sessions/current
- Handler: `servlets.userSessions.UsersSessionDeleteCurrent.deleteCurrentUserSession(HttpServletRequest, HttpServletResponse, Map<String,String>)`
- Auth: Session cookie optional — function will clear cookie(s) and invalidate local HttpSession if present
- Behavior: Invalidates the server HttpSession (if any), clears authentication cookie and user cookie, and returns success JSON.
- Success: 200 OK { type: "success", message: "Logged out and cookie removed" }

---

## Notes, headers and auth

- Session cookie: authentication relies on `AuthUtil.createAndSetAuthCookie` and `AuthUtil.getUserUUIDFromAuthCookie` to read and validate the cookie. Most `/v1/users/me*` and `/v1/users/sessions*` endpoints require a valid session cookie.
- API key & client-id header: The codebase contains `auth.ApiKeyFilter` and `utils.ApiKeyManager` used for broader `/v1/*` protection strategies. For the user-facing endpoints above, session-based auth is primary; backend endpoints (future or internal) may require `X-HridayKh-In-Auth-Key` or `X-HridayKh-In-Client-ID` headers.

---

If you want, I can now:

- Add example curl requests for each endpoint (including required headers and sample bodies).
- Generate a machine-readable YAML/JSON list from `ApiConstants.java` to feed into tests or a Postman collection.
- Scan `servlets/userPasswords` to flesh out the password-reset flow and produce exact request/response shapes.

Generated from source: `servlets.ApiConstants` and handler classes under `src/main/java/servlets`.
