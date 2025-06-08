# Your Current Authentication System: Design Overview
### designed by me, turned into a proper md doc by gemini

Your current authentication system is designed as a standalone service, intended to be consumed by your "other app." It focuses on email and password-based authentication with a stateless session approach leveraging signed cookies.

---

### 1. User Data Storage (`users` Table)

At the core, your system manages user information in a MySQL database. Each user record is defined by the following key fields:

* **`uuid`**: This is the **primary, unique identifier** for each user within your system. It's an internal ID, central to how your application identifies and references users.
* **`email`**: The user's primary email address.
* **`passwordHash`**: Stores a securely hashed version of the user's password. This is the credential used for email/password logins.
* **`isVerified`**: A boolean flag indicating if the user's email address has been verified (e.g., via a link sent after registration).
* **`createdAt`, `updatedAt`, `lastLogin`**: Timestamps for auditing and user activity tracking. `lastLogin` is nullable.
* **`profilePic`, `fullName`, `metadata`, `permissions`**: These are optional fields that hold additional user-specific data, such as their display name, profile image URL, and flexible JSON structures for arbitrary attributes (`metadata`) and access controls (`permissions`).

Currently, only the `passwordHash` can be updated, suggesting a focus on basic credential management.

---

### 2. Authentication Flow (Email & Password)

* **Registration**: Users provide an email and password. Your system hashes the password and stores the user record in the `users` table, generating a `uuid`. Email verification is typically initiated.
* **Login**: Users provide an email and password. Your system verifies the password hash against the stored one.

---

### 3. Session Management (Stateless via Signed UUID Cookie)

Your current session management relies on a client-side cookie without maintaining active session state on the server.

* **Cookie Creation**: Upon successful login (email/password), your server generates a **signed cookie**. This cookie contains:
    * The user's **`uuid`**.
    * A **signature** (`sign`) of that `uuid`, created by encrypting the `uuid` using a server-side secret password/key.
* **Client-Side Storage**: The browser receives this signed cookie and stores it.
* **Authentication for "Other App"**: When your "other app" needs user information or wants to verify the user, it sends this signed cookie back to your authentication service.
* **`getUser` Endpoint**: Your authentication service has a dedicated `getUser` endpoint. When it receives the cookie:
    * It **verifies the `sign`** using the same server-side secret to ensure the `uuid` hasn't been tampered with and was issued by your server.
    * If the signature is valid, it extracts the `uuid`.
    * It then queries the `users` table in MySQL using this `uuid` to retrieve all the user's information (`email`, `fullName`, `profilePic`, `metadata`, `permissions`, etc.).
    * This information is then returned to the "other app."
* **Logout**: Invalidation likely involves instructing the client to delete the cookie, as the server doesn't hold any session state to explicitly invalidate.

---

### 4. Integration with Your "Other App"

Your authentication app acts as an independent service. The "other app" offloads all authentication and user data retrieval to this service by sending the `uuid` cookie. This separation allows for modularity and potentially reuse of the authentication service across multiple applications.