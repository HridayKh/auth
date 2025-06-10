# forgotten features
- password reset
- metadata private
- rate limit (blocks logins on first attempt in the last 60s even if correct pass to stop bruteforce as average user will just try again)

### 1. Google Sign-In Flow & Integration Logic

refer to google.md

---

### 2. Account Information Page

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