# Google Authentication System Design

This document outlines the current design and implementation of your Google OAuth authentication flow.

---

## 1. Flow Initiation

When a user initiates "Sign in with Google" from the frontend, their browser is **redirected directly to a backend endpoint** (e.g., `/google-login-initiate`). This endpoint can receive additional contextual parameters from the frontend (e.g., `redirect`) as query parameters.

### Example Frontend Redirection
```
https://your-backend.com/google-login-initiate?redirect=/user/accountInfoPage
```

---

## 2. Backend Redirect to Google

Upon receiving the request, the backend's `/google-login-initiate` endpoint performs the following:

* Parses any incoming **source redirect parameters** (e.g., `redirect`).
* Generates a unique **CSRF token**.
* Constructs the OAuth **`state` parameter** by combining the CSRF token and the source redirect parameters into a single, URL-encoded JSON string.
    * **Example `state` content (before URL-encoding):**
        ```json
        {
          "csrf": "your_unique_csrf_token_from_backend",
          "redirect": "/dashboard/products/123"
        }
        ```
* **Stores the original CSRF token** in the user's HTTP session.
* Performs a **server-side redirect** of the user's browser to Google's authentication page, including the `client_id`, `redirect_uri`, `scope`, and the constructed `state` parameter.

---

## 3. Google Callback

After the user grants consent on Google's authentication page, Google redirects the browser **back to your backend's `oauth2callback` servlet** (your configured `REDIRECT_URI`). This callback includes the `authorization code` and the `state` parameter originally provided by your backend.

---

## 4. State Verification & Token Exchange

The `oauth2callback` servlet processes the incoming request:

* **Decodes the `state` parameter**: It URL-decodes and parses the JSON string to retrieve both the original CSRF token and the source redirect parameters.
* **Verifies CSRF**: It compares the CSRF token extracted from the decoded `state` against the token stored in the user's HTTP session. If they do not match, the request is blocked to prevent CSRF attacks.
* **Exchanges Code for Tokens**: It exchanges the `authorization code` with Google for an `Access Token` and an `ID Token`.
* **Verifies ID Token**: It verifies the `ID Token`'s signature to confirm its authenticity and extracts user information (e.g., `google_id`, `email`, `fullName`, `profilePic`).

---

## 5. User Integration Logic

This is the core decision-making point for user identification and account management:

* **Check by `google_id`**: The system first attempts to find an existing user in the `users` table using the `google_id` from the `ID Token`.
    * If found, the user is recognized as a returning Google user, and their `lastLogin` timestamp is updated.
* **If no `google_id` match, check by `email`**: If no user is found by `google_id`, the system then checks for a user with the same email address (from the `ID Token`) in the `users` table.
    * **Account Linking Scenario**: If an existing user is found by email and they do not already have a `google_id` linked, the current authentication request is blocked. The user is typically directed to an account linking page. If the authentication was initially triggered from an account linking flow (indicated by a specific value in the `state` parameter), the system proceeds to link the existing account by updating the `google_id` field in the database.
    * **New Google User**: If no user is found by either `google_id` or `email`, a **new user record is created** in the `users` table. The `google_id`, `email`, `fullName`, and `profilePic` are populated from Google. The `passwordHash` is set to `NULL`, and the `acc_type` is set to `google`.

---

## 6. Session Creation

Once a user (either existing or newly created) is successfully identified, a **new session record** is created in the `sessions` table. This record includes `session_id`, `user_uuid`, `created_at`, `expires_at`,  `user_agent`, and `is_active` status.

---

## 7. Final Backend Redirect

After successful authentication and session creation, the backend performs a **server-side redirect** to the appropriate frontend page:

* It checks for a `returnTo` parameter within the previously decoded `state` information.
* If a `returnTo` URL is present and is a **valid, authorized URL** on your frontend domain (validated to prevent open redirect vulnerabilities), the user's browser is redirected to this specific URL.
* If `returnTo` is not present or is invalid, the user's browser is redirected to a default frontend page (e.g., your dashboard). The user can now securely assume they are logged in.