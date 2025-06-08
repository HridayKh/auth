# Your New Authentication System: Design Overview
### designed by me, turned into a proper md doc by gemini

Your updated system integrates Google Sign-In, introduces server-side session management for enhanced control, and provides flexibility for account linking and management.

---

### 1. Enhanced User Data Storage (`users` Table)

The `users` table is augmented to support different account types and linkage with Google.

* **`uuid`**: Remains the **primary, unique identifier** for each user within your system.

* **`email`**: User's email.

* **`passwordHash`**: Stores the password hash. This field is now **nullable**, allowing accounts created purely via Google to not have a password.

* **`isVerified`**: Email verification status. Google-signed-up users are typically `true`.

* **`createdAt`, `updatedAt`, `lastLogin`**: Timestamps.

* **`profilePic`, `fullName`, `metadata`, `permissions`**: Existing optional fields.

* **NEW: `google_id` (`VARCHAR(255) UNIQUE NULL`)**: Stores the unique identifier provided by Google for the user. It's nullable for email/password-only accounts and unique to ensure a Google ID maps to only one user in your system.

* **NEW: `acc_type` (`ENUM('email', 'google', 'email_google') NOT NULL DEFAULT 'email'`)**: A crucial field to categorize the user's primary authentication method and linkage status:
    * `email`: User authenticates solely with email and password.
    * `google`: User authenticates solely with their Google account (no password in your system).
    * `email_google`: User initially registered with email/password and has since linked a Google account.

* **NEW: `refresh_token` (`TEXT NULL`)**: Stores the Google OAuth 2.0 Refresh Token. This token is long-lived and allows your backend to obtain new Access Tokens without user re-authentication. It must be stored securely (e.g., encrypted at rest).

* **NEW: `refresh_token_expires_at` (`BIGINT NULL`)**: Stores the expiration time for the Refresh Token (if Google provides one, often they are indefinite until revoked).

---

### 2. New Server-Side Session Management (`sessions` Table)

This is a significant enhancement, moving from a stateless cookie-based system to a stateful, server-controlled session system. This table provides the necessary server-side state for features like "sign out other devices" and immediate session invalidation.

* **`session_id` (`VARCHAR(36) PRIMARY KEY`)**: A unique identifier for each active session instance. This UUID is now the value stored in the client's main authentication cookie.

* **`user_uuid` (`VARCHAR(36) NOT NULL`)**: Links the session to a specific user in the `users` table.

* **`created_at`**: Timestamp when the session was initiated.

* **`last_accessed_at`**: Records the last activity on the session, crucial for idle timeouts.

* **`expires_at`**: The planned expiration timestamp for the session.

* **`ip_address`**: The IP address from which the session was created.

* **`user_agent`**: The browser/device string associated with the session.

* **`is_active` (`BOOLEAN NOT NULL DEFAULT TRUE`)**: A flag that can be set to `FALSE` to immediately invalidate a session from the server-side, without waiting for its natural expiration.

---

### 3. Google Sign-In Flow & Integration Logic

The backend now takes full control of the Google OAuth flow.

* **Initiation**: When a user clicks "Sign in with Google" on the frontend, the browser is **redirected directly to a backend endpoint** (e.g., `/yourapp/google-login-initiate`).

* **Backend Redirect to Google**: This backend endpoint immediately generates a **CSRF `state` parameter** (stored in the HTTP session) and then performs a **server-side redirect** of the user's browser to Google's authentication page.

* **Google Callback**: After user consent, Google redirects the browser **back to your backend's `oauth2callback` servlet** (the `REDIRECT_URI`), sending the `authorization code` and the `state` parameter.

* **State Verification**: The `oauth2callback` servlet first verifies the `state` parameter against the one stored in the user's HTTP session to prevent CSRF attacks.

* **Token Exchange & Verification**: The servlet then exchanges the `authorization code` with Google for the `Access Token` and `ID Token`. It **verifies the `ID Token`'s signature** to confirm its authenticity.

* **User Integration Logic**: This is the core decision-making point:

    1.  **Check by `google_id`**: The system first attempts to find a user in the `users` table using the `google_id` (from the `ID Token`). If found, the user is recognized as a returning Google user. Their `lastLogin` is updated.

    2.  **If no `google_id` match, check by `email`**: If no user is found by `google_id`, the system then checks if a user with the same email address (from the `ID Token`) exists in the `users` table.

        * **Account Linking**: If an existing user is found by email (and they don't already have a `google_id` linked), their `google_id` in the `users` table is **updated** with the new Google ID, and their `acc_type` becomes `email_google`. Their `lastLogin` is updated.

        * **New Google User**: If no user is found by either `google_id` or `email`, a **new user record is created** in the `users` table. The `google_id`, `email`, `fullName`, and `profilePic` are populated from Google. The `passwordHash` is set to `NULL`, and `acc_type` is set to `google`.

* **Session Creation (New Method)**: Once a user (existing or new) is identified/created, a **new session record** is created in the `sessions` table (`session_id`, `user_uuid`, `created_at`, `expires_at`, `ip_address`, `user_agent`, `is_active`).

* **Final Backend Redirect**: After successful authentication and session creation, the backend performs a **server-side redirect** to a final frontend page (e.g., your dashboard), which can now securely assume the user is logged in.

---

### 4. Enhanced Cookie-Based Session Management

Your custom cookie system is updated to work with the new stateful session table.

* **Main Authentication Cookie (`auth_token`)**: This cookie will now contain the **`session_id`** from the `sessions` table (instead of the `user_uuid`), signed by your server-side secret. It will be `HttpOnly` to prevent JavaScript access.

* **Profile Data Cookie (`user_profile_data`)**: A **new, separate cookie** will be introduced to store commonly needed user profile data (like `fullName`, `email`, `profilePic`). This cookie will **not be `HttpOnly`**, allowing your frontend JavaScript to directly read this data for immediate UI updates without making an extra server request.

* **`getUser` Endpoint / Authentication Filter**:

    * This endpoint (or a server-side filter) will receive the `auth_token` cookie.

    * It will **verify the `session_id`'s signature** using your server-side secret.

    * It will then **look up the `session_id` in the `sessions` table** to validate its existence, `is_active` status, and expiration.

    * If the session is valid, it retrieves the `user_uuid` from the `sessions` table record.

    * It updates `last_accessed_at` in the `sessions` table to extend the session's life.

    * Finally, it fetches the full `User` object from the `users` table using the `user_uuid` and provides this to the "other app."

---

### 5. Account Information Page

This new page provides centralized user control over their account.

* **Purpose**: Allows users to view and manage their profile details and authentication methods.

* **Functionality**:

    * **Display Profile**: Shows `fullName`, `email`, `profilePic`, `acc_type`, and potentially `metadata`/`permissions`.

    * **Update Profile**: Allows changing `fullName`, `profilePic`, and other non-credential fields.

    * **Password Management**:

        * **For `email` accounts**: Allows users to change their password.

        * **For `google` accounts**: Provides an option to **add a password** to their account. If they set a password, their `acc_type` would be updated to `email_google`, and the `passwordHash` field in the `users` table would be populated. This allows them to log in via email/password in the future.

        * **For `email_google` accounts**: Allows changing their password.

    * **Link/Unlink Google Account**:

        * If the user has an `email` account type, they could initiate a Google login from this page to **link their Google ID** to their existing account, changing `acc_type` to `email_google`.

        * If the user has an `email_google` account type, they could have an option to **unlink their Google account**, setting `google_id` to `NULL` and changing `acc_type` back to `email` (as long as they have a password set).

* **Server-Side Control**: All changes made on this page would trigger backend API calls to update the `users` table (and potentially the `sessions` table for `lastLogin` or `updatedAt` timestamps).

---

### 6. Enhanced Logout and Security

* **Standard Logout**: The logout process will now explicitly **invalidate the specific `session_id` in the `sessions` table** by setting its `is_active` flag to `FALSE`, in addition to clearing the cookies on the client.

* **"Sign Out Other Devices"**: A new feature made possible by the `sessions` table. Users can click a button that calls a backend endpoint. This endpoint will invalidate (set `is_active = FALSE`) all `session_id`s in the `sessions` table associated with the user's `uuid`, *except* for the `session_id` of the current device. This immediately logs the user out from all other active sessions.

This refined architecture provides a powerful, flexible, and secure authentication system that gracefully handles multiple login methods and offers fine-grained control over user sessions.