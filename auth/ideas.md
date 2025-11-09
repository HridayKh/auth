#### 1. Multi-Factor Authentication (MFA / 2FA)

#### 2. Forgot Password

#### 3. Account Deletion

#### 4. More Social Logins

#### 5. Centralized Security Logging and Monitoring

#### 6. Enhanced Session Management Features

#### 7. Unlink Google

#### 8. session names

# TODO:
- verify acc type is consistent and use enums
- store session creation method, google or password
- forgot password flow
- unlink google
- verify linked google acc has same email as the og password acc


# Refactor to be more restful

Objects:
user
verify link
session

Base: /v1/users/

POST / (register)

GET /me (get profile, specify /me to indicate current user ONLY clearly)
PUT /me (update profile)
POST /me/password (update pass)

GET /admin/current (get admin profile)
PATCH /admin/current (update admin profile)

GET /verify (get endpoint to be opened in browser)
POST /verify (to send a new verify email)

POST /password-resets/ (to initiate pass reset)
PUT /password-resets/emailToken (to update the password)

GET /sessions (get all sessions of a user)
POST /sessions (login = create session)
DELETE /sessions/current (logout = delete session)
DELETE /sessions/sessionId (remove specific session)

login []
logout []
pass_reset_init []
pass_reset_done []

get_admin []
update_admin []

get_profile []
update_profile []

register []
reverify []
verify []

get session []
remove session []
update password []

# result API Endpoints

USERS_CREATE: POST /v1/users

USERS_INFO_GET: GET /v1/users/me
USERS_INFO_UPDATE: PATCH /v1/users/me
USERS_PASSWORD_UPDATE: POST /v1/users/me/password

USERS_INTERNAL_INFO_GET: GET /v1/users/internal/me
USERS_INTERNAL_INFO_UPDATE: PATCH /v1/users/internal/me

USERS_EMAIL_VERIFY: GET /v1/users/verify    Note: to be opened directly in browser by user
USERS_EMAIL_VERIFY_RESEND: POST /v1/users/verify/resend

USERS_PASSWORD_RESET_INIT: POST /v1/users/password-resets
USERS_PASSWORD_RESET_UPDATE: PUT /v1/users/password-resets

USERS_SESSIONS_LIST: GET /v1/users/sessions/me
USERS_SESSIONS_CREATE: POST /v1/users/sessions
USERS_SESSION_DELETE: DELETE /v1/users/sessions/{sessionId}
USERS_SESSIONS_DELETE_CURRENT: DELETE /v1/users/sessions/current

