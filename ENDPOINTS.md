# Auth Service — Endpoints Reference

This document lists all HTTP endpoints in the `auth` service (from the code in `auth/src/main/java`). It collects each route, HTTP method(s), the responsible handler, path parameters, expected inputs (where obvious or inferred), responses and notable side-effects or cookies.

Files referenced:
- `auth/src/main/java/servlets/ApiServlet.java`
- `auth/src/main/java/servlets/ApiConstants.java`
- `auth/src/main/java/google/GoogleLoginInitiateServlet.java`
- `auth/src/main/java/google/OAuth2CallbackServlet.java`

Note: some request/response body shapes are inferred from common patterns. For precise payload schemas, consult the handler implementations under:
- `auth/src/main/java/servlets/usersCreate/`
- `auth/src/main/java/servlets/usersInfo/`
- `auth/src/main/java/servlets/userSessions/`
- `auth/src/main/java/servlets/userPasswords/`

---

# Summary list

- POST /v1/users
- GET  /v1/users/verify
- POST /v1/users/verify/resend
- GET  /v1/users/me
- PATCH /v1/users/me
- GET  /v1/users/internal/me
- PATCH /v1/users/internal/me
- POST /v1/users/password-resets
- PUT  /v1/users/password-resets
- POST /v1/users/me/password
- GET  /v1/users/sessions/me
- POST /v1/users/sessions
- DELETE /v1/users/sessions/{sessionId}
- DELETE /v1/users/sessions/current
- GET  /googleLoginInitiate
- GET  /oauth2callback

---

# Detailed endpoints

### 2) POST /v1/users
- Handler: `servlets.usersCreate.UsersCreator::createUser`
- Purpose: create a new user account ("register").
- Request body (inferred): JSON with at least `email` and `password`. May include `fullName`, `profilePic`, and other metadata.
  - Assumption: typical create user payload: { "email": "user@example.com", "password": "p@ss", "fullName": "Name" }
- Responses (inferred):
  - 201 Created (or 200) with user summary (uuid, email, createdAt) on success.
  - 400 Bad Request for validation errors.
  - 409 Conflict if user already exists.
- Side-effects: creates DB user record. May send verification email depending on implementation.
- Cookies: none guaranteed; some implementations set auth cookie on create — check `UsersCreator`.

Notes: see `auth/src/main/java/servlets/usersCreate/UsersCreator.java` for exact request fields and response structure.

---

### 3) GET /v1/users/verify
- Handler: `servlets.usersCreate.UsersVerifier::verifyUser`
- Purpose: verify a user's email address (link visited from email).
- How used: `ApiConstants.USERS_VERIFY_EMAIL` is intended to be opened directly in a browser.
- Request parameters: commonly a `token` or `code` query parameter (e.g. `/v1/users/verify?token=...`). Check `UsersVerifier` for exact param name.
- Responses: likely redirect or render a confirmation page. Could return JSON if used by API clients.
- Side-effects: marks user email verified in DB.

---

### 4) POST /v1/users/verify/resend
- Handler: `servlets.usersCreate.UsersVerifier::resendVerifyEmail`
- Purpose: resend verification email to the user.
- Request body (inferred): JSON with `email` (or logged-in context uses current user).
- Responses: 200 on success (email queued), 400/404 if missing or not found.

---

### 5) GET /v1/users/me
- Handler: `servlets.usersInfo.UsersInfoManager::getUserInfo`
- Purpose: return authenticated user's public profile information.
- Auth: requires valid auth/session cookie/header (check `UsersInfoManager` and `AuthUtil`).
- Request: none beyond auth; may accept query params for fields.
- Response: 200 with user info JSON (email, fullName, profilePic, createdAt, lastLogin, etc.).

---

### 6) PATCH /v1/users/me
- Handler: `servlets.usersInfo.UsersInfoManager::updateUserInfo`
- Purpose: update authenticated user's public profile (e.g., fullName, profilePic).
- Auth: required.
- Request body (inferred): JSON with fields to update, e.g. { "fullName": "New Name", "profilePic": "https://..." }
- Responses: 200 with updated user, 400 for validation errors, 401/403 for auth issues.
- Side-effects: updates DB record.

---

### 7) GET /v1/users/internal/me
- Handler: `servlets.usersInfo.UsersInternalManager::getUserInternalInfo`
- Purpose: return internal user information for the current (authenticated) user. This may include non-public fields such as metadata, permissions, refresh token expiry, etc.
- Auth: required and likely restricted to internal clients or admin roles.
- Response: 200 + internal fields JSON.

---

### 8) PATCH /v1/users/internal/me
- Handler: `servlets.usersInfo.UsersInternalManager::updateUserInternalInfo`
- Purpose: update internal metadata for the authenticated user (permissions, metadata blobs).
- Auth: required and likely privileged.
- Request: JSON describing internal fields to update.

---

### 9) POST /v1/users/password-resets
- Handler: registered in `ApiServlet` with a small lambda returning 501 Not Implemented:
  - In `ApiServlet`, POST and PUT for `ApiConstants.USERS_PASSWORD_RESET` both map to
    (req, resp, params) -> HttpUtil.sendJson(resp, HttpServletResponse.SC_NOT_IMPLEMENTED, "error", "unimplemented endpoint")
- Purpose: intended for creating a password reset request (send reset email). Currently unimplemented.
- Expected behaviour when implemented: POST would accept { "email": "..." } and send reset instructions, returning 200.

---

### 10) PUT /v1/users/password-resets
- Handler: same as above — currently returns 501 Not Implemented.
- Purpose (when implemented): accept a reset `token` and `newPassword`, then update password.
- Expected request: { "token": "...", "newPassword": "..." }

---

### 11) POST /v1/users/me/password
- Handler: `servlets.userPasswords.UsersPassUpdater::updateUserPass`
- Purpose: allow authenticated user to update their password (change password flow).
- Auth: required (must be logged in).
- Request body (inferred): { "currentPassword": "...", "newPassword": "..." } or { "newPassword": "..." } depending on implementation.
- Responses: 200 on success, 400 validation, 401 unauthorized if current password wrong.
- Side-effects: updates stored password hash.

---

### 12) GET /v1/users/sessions/me
- Handler: `servlets.userSessions.UsersSessionList::listUserSessions`
- Purpose: list active sessions for the authenticated user.
- Auth: required.
- Response: 200 with array of session objects: { sessionId, createdAt, lastAccessAt, ip, userAgent, expiresAt }

---

### 13) POST /v1/users/sessions
- Handler: `servlets.userSessions.UsersSessionCreate::createUserSession`
- Purpose: create a new session (login), likely returns auth cookie or token.
- Request body (inferred): credentials (email + password) OR an external token depending on flow.
- Response: 200/201 with session object and sets auth cookie (see `AuthUtil.createAndSetAuthCookie` used elsewhere in Google flow).
- Side-effects: creates session record, issues cookies.

---

### 14) DELETE /v1/users/sessions/{sessionId}
- Handler: `servlets.userSessions.UsersSessionDelete::deleteUserSession`
- Purpose: delete a specific session for the authenticated user. `sessionId` is a path parameter.
- Path params: `sessionId` (string)
- Auth: required; user may only delete their own sessions unless privileged.
- Response: 200 on success, 404 if session not found, 403 if trying to delete another user's session.
- Side-effects: removes session record / revokes token.

---

### 15) DELETE /v1/users/sessions/current
- Handler: `servlets.userSessions.UsersSessionDeleteCurrent::deleteCurrentUserSession`
- Purpose: delete the current session (logout).
- Auth: required.
- Response: 200 on success. Side-effect: clears auth cookie, removes session record.

---

# Google OAuth endpoints (non-API web endpoints)

These are implemented in `auth/src/main/java/google/`.

### 16) GET /googleLoginInitiate
- Handler: `google.GoogleLoginInitiateServlet::doGet`
- Purpose: start the Google OAuth 2.0 flow. Builds authorization URL and redirects browser to Google.
- Query parameters (required):
  - `redirect` — the URL the app should redirect back to after login (encoded and stored in state)
  - `source` — source identifier used by the callback to decide linking vs new account (e.g., `glink`)
- Behaviour:
  - Validates `redirect` and `source` (responds 400 JSON if blank).
  - Creates a state JSON via `utils.GoogleUtil.genStateJson(...)` containing a `csrf` token and `redirect` and `source`.
  - Stores `oauth_state` in server session (`HttpSession`) with CSRF string.
  - Builds Google auth URL with `redirect_uri` = `dbAuth.BACK_HOST + "/oauth2callback"` and encoded `state`.
  - Redirects user agent to Google for authorization.
- Notes: uses `GoogleAuthorizationCodeFlow` with scopes `openid, email, profile`.

### 17) GET /oauth2callback
- Handler: `google.OAuth2CallbackServlet::doGet` (main processing in `processAuthCode`)
- Purpose: handle Google's authorization code callback, exchange code for tokens, verify ID token, create or link user, set cookies and redirect back to original `redirect` URL.
- Query parameters:
  - `code` — authorization code from Google (present on successful consent)
  - `error` — error parameter from Google if user denied or error occurred
  - `state` — opaque encoded state previously provided to Google (contains csrf token and `redirect` and `source`). The servlet decodes and validates the `csrf` against `oauth_state` in `HttpSession`.
- Behaviour:
  - Validates session `oauth_state` matches `state.csrf`. If invalid: redirect to failure (to `state.redirect` with error message).
  - If `error` present: redirect to failure.
  - If `code` present: call `flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute()` to get tokens.
  - Extracts `idToken` and `refreshToken`.
  - Verifies `idToken` with `GoogleIdTokenVerifier`.
  - Reads token payload: `userId` (sub), `email`, `name`, `picture`.
  - Calls `registerUser(...)` to create or update user in DB and set cookies.

- `registerUser` details (from code):
  - Opens DB connection (`dbAuth.getConnection()`), uses transaction.
  - Looks up existing user by email via `UsersDAO.getUserByEmail(...)`.
  - If user doesn't exist:
    - Creates a new `User` with `accType` = `google`, `isVerified = true`, stores `googleId` and `refreshToken`.
    - Calls `UsersDAO.insertUser(conn, newUser)`.
    - Calls `AuthUtil.createAndSetAuthCookie(conn, request, response, newUser.uuid())` and `HttpUtil.createAndSetUserCookie(response, newUser)`.
  - Else if existing user `accType` is `google` or `both` OR `accType=='pass'` and `source=='glink'`:
    - Builds an updated `User` preserving existing fields and selectively updates `fullName`, `profilePic`, `refreshToken` if provided.
    - If linking (`pass` + `glink`) sets `accType` to `both`.
    - Updates `lastLogin`, calls `UsersDAO.updateUser(conn, newUser)`.
    - Sets auth and user cookies as above.
  - Else: rolls back and redirects to failure: "User already exists with this email".
  - On success: commits and redirects to `redirect` URL with `?type=success&message=Logged%20in%20successfully`.
  - On DB error: logs exception.

- Cookies and side-effects:
  - Auth cookie: `AuthUtil.createAndSetAuthCookie(...)` (implementation sets session cookie / persistent cookie and writes session record).
  - User cookie: `HttpUtil.createAndSetUserCookie(response, newUser)` (likely sets a small client-visible cookie with public user fields for frontend use).
  - DB writes: user insert/update; session creation.

- Error handling:
  - Verifier/token errors redirect to failure and append `?type=error&message=...`.

---

# Where to find handler code
- API router: `auth/src/main/java/servlets/ApiServlet.java`
- Route constants: `auth/src/main/java/servlets/ApiConstants.java`
- Google OAuth: `auth/src/main/java/google/GoogleLoginInitiateServlet.java` and `auth/src/main/java/google/OAuth2CallbackServlet.java`
- User creation & verification: `auth/src/main/java/servlets/usersCreate/UsersCreator.java`, `UsersVerifier.java`
- User info: `auth/src/main/java/servlets/usersInfo/UsersInfoManager.java`, `UsersInternalManager.java`
- Sessions: `auth/src/main/java/servlets/userSessions/`
- Password updates/reset: `auth/src/main/java/servlets/userPasswords/UsersPassUpdater.java`

---

# Testing notes & quick checks
- To test Google OAuth flows locally, ensure `dbAuth.BACK_HOST` matches the configured redirect URI in Google Cloud Console and that `CLIENT_ID`/`CLIENT_SECRET` are set.
- The `/googleLoginInitiate` endpoint requires `redirect` and `source` query params; the servlet stores CSRF in session. Use a browser (not curl) to follow the redirect flow.
- API calls under `/v1/*` expect context path removal: `ApiServlet` strips `req.getContextPath()` when matching route strings.
- Parameterized route example: delete session uses `/v1/users/sessions/{sessionId}`. The router converts `{param}` to regex and fills `pathParams` passed into the handlers.

---

# Assumptions and items to verify
- Request/response JSON schemas are inferred for a few endpoints (Users creation, password update, session creation). Consult the concrete handler files in the `servlets/*` subfolders for exact field names, validation logic, and response codes.
- Password reset endpoints are intentionally unimplemented in the router (they return 501). Implementation likely exists or will be added in `servlets/userPasswords` or a related module.

---

If you want, I can:
- Extract and list exact request/response JSON schemas by opening each handler file under `servlets/*` (I can produce a precise API spec from code).
- Generate an OpenAPI (Swagger) YAML file from the handlers.
- Add example curl commands for each endpoint (authenticated and unauthenticated variants) and short test scripts.

Tell me which of these you'd like me to do next.