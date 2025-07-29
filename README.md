# Hriday.Tech Authentication Service API Documentation

# the following is outdated as the entire api keys system, route and servlet controller amd some other things have been completely rewritten, redesigned and rebuilt
this doc will be updatee by july 30 2025 4pm ist

## Overview

This is a comprehensive Java servlet-based authentication service built with Jakarta EE, providing secure user management, session handling, Google OAuth integration, and role-based access control. The service supports both traditional email/password authentication and Google OAuth2 authentication.

## Table of Contents

1. [Architecture](#architecture)
2. [Database Schema](#database-schema)
3. [Authentication & Security](#authentication--security)
4. [API Endpoints](#api-endpoints)
5. [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
6. [Error Handling](#error-handling)
7. [Environment Configuration](#environment-configuration)
8. [Deployment](#deployment)

## Architecture

### Technology Stack
- **Backend Framework**: Jakarta EE (Servlet API 5.0.0)
- **Database**: MySQL with JDBC
- **JSON Processing**: org.json library
- **Email Service**: Mailgun
- **OAuth**: Google OAuth2 API
- **Logging**: Apache Log4j2
- **Build Tool**: Maven

### Project Structure
```
src/main/java/
├── auth/                    # Authentication filters and test apps
├── db/                      # Database access layer (DAOs)
├── dtos/                    # Data Transfer Objects
├── entities/                # Domain entities
├── google/                  # Google OAuth implementation
├── servlets/                # HTTP request handlers
│   ├── authentication/     # Login/logout handlers
│   ├── profile/            # User profile management
│   ├── registration/       # User registration and verification
│   └── security/           # Session and password management
└── utils/                   # Utility classes
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    uuid VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    last_login BIGINT,
    profile_pic TEXT,
    full_name VARCHAR(255),
    metadata JSON,
    permissions JSON,
    google_id VARCHAR(255),
    acc_type VARCHAR(50) NOT NULL,
    refresh_token TEXT,
    refresh_token_expires_at BIGINT
);
```

### Sessions Table
```sql
CREATE TABLE sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    created_at BIGINT NOT NULL,
    last_accessed_at BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
);
```

### Email Tokens Table
```sql
CREATE TABLE email_tokens (
    token VARCHAR(255) PRIMARY KEY,
    user_uuid VARCHAR(36) NOT NULL,
    expires_at BIGINT NOT NULL,
    FOREIGN KEY (user_uuid) REFERENCES users(uuid) ON DELETE CASCADE
);
```

## Authentication & Security

### Session Management
- **Cookie-based sessions**: Uses signed JWT-like tokens stored in HTTP-only cookies
- **Session expiry**: Configurable session timeout (default: 7 days)
- **Multi-device support**: Users can have multiple active sessions
- **Secure cookies**: HTTPS-only in production with proper domain settings

### API Key Authentication
The service implements a multi-tier authentication system:

1. **Public Paths**: No authentication required
2. **Frontend Paths**: Require client ID header for identification
3. **Admin Paths**: Require secret API keys for backend services

#### API Key Configuration
The system uses environment variables for API key configuration. These keys provide different levels of access:

| Environment Variable | Client ID | Access Level | Usage |
|---------------------|-----------|--------------|-------|
| `MY_WEB_APP_AUTH_API_KEY` | `my-web-app` | Standard Backend | Web application backend services |
| `MY_MOBILE_APP_AUTH_API_KEY` | `my-mobile-app` | Standard Backend | Mobile application backend services |
| `MY_ADMIN_PANEL_AUTH_API_KEY` | `my-admin-panel` | **Admin Privileges** | Admin operations, user metadata management |

#### API Key Headers
- **Secret API Key**: `X-Hriday-Tech-App-Key` (for backend services)
- **Public Client ID**: `X-Hriday-Tech-Client-ID` (for frontend identification)

#### Access Control Matrix
| Endpoint Category | Public Client ID | Standard API Key | Admin API Key |
|------------------|------------------|------------------|---------------|
| Authentication | ✅ | ✅ | ✅ |
| Registration | ✅ | ✅ | ✅ |
| Profile Management | ✅ | ✅ | ✅ |
| Security Operations | ✅ | ✅ | ✅ |
| **Admin Profile Operations** | ❌ | ❌ | ✅ |
| **Admin Metadata Updates** | ❌ | ❌ | ✅ |

### Password Security
- **SHA-256 hashing**: All passwords are hashed using SHA-256
- **Password validation**: Server-side validation for password changes
- **Account type checking**: Prevents password operations on Google-authenticated accounts

## API Endpoints

### Authentication Endpoints

#### POST /v1/auth/login
**Description**: Authenticate user with email and password

**Request Body**:
```json
{
    "email": "user@example.com",
    "pass": "userpassword"
}
```

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "Logged In Successfully, Redirecting...."
}
```

**Response Errors**:
- `400`: Invalid email/password, unverified account, or Google account
- `500`: Internal server error

---

#### GET /v1/auth/logout
**Description**: Logout current user session

**Response Success (200)**:
```json
{
    "type": "success", 
    "message": "Logged out successfully"
}
```

### Registration Endpoints

#### POST /v1/register/register-user
**Description**: Register a new user account

**Request Body**:
```json
{
    "email": "user@example.com",
    "pass": "userpassword",
    "fullName": "John Doe",
    "redirect": "https://yourapp.com/dashboard"
}
```

**Response Success (201)**:
```json
{
    "type": "success",
    "message": "User registered successfully. Please check your email account verification link."
}
```

**Response Errors**:
- `409`: User already exists (verified or unverified)
- `500`: Registration failed

---

#### POST /v1/register/re-verify
**Description**: Resend verification email for unverified accounts

**Request Body**:
```json
{
    "email": "user@example.com",
    "redirect": "https://yourapp.com/dashboard"
}
```

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "A new verification email has been sent."
}
```

---

#### GET /v1/register/verify
**Description**: Verify user email with token (URL-based)

**Query Parameters**:
- `token`: Email verification token
- `redirect`: Redirect URL after verification

**Response**: HTTP redirect to success/error page

### Profile Management Endpoints

#### GET /v1/profile/get-user
**Description**: Get current user's profile information

**Authentication**: Required (session cookie)

**Response Success (200)**:
```json
{
    "uuid": "user-uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "profilePic": "https://example.com/pic.jpg",
    "isVerified": true,
    "accType": "password",
    "createdAt": 1640995200,
    "lastLogin": 1640995200,
    "metadata": {...},
    "permissions": {...}
}
```

---

#### POST /v1/profile/update-profile
**Description**: Update user's basic profile information

**Authentication**: Required (session cookie)

**Request Body** (all fields optional):
```json
{
    "email": "newemail@example.com",
    "profile_pic": "https://example.com/newpic.jpg",
    "full_name": "Jane Doe"
}
```

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "Profile updated successfully."
}
```

**Response Errors**:
- `400`: Invalid email format
- `403`: Email change not allowed for Google accounts
- `409`: Email already in use

---

#### GET /v1/profile/admin-profile
**Description**: Get user profile with metadata/permissions for admin operations

**Authentication**: Required (Basic Auth + session cookie)

**Special Requirements**: 
- Requires `auth` header with hashed database password
- Admin-level API key required

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "Metadata and permissions updated successfully."
}
```

---

#### POST /v1/profile/update-admin-metadata
**Description**: Update user's metadata and permissions (admin operation)

**Authentication**: Required (session cookie)

**Request Body**:
```json
{
    "metadata": {
        "department": "engineering",
        "role": "senior-developer"
    },
    "permissions": {
        "canAccessAdmin": true,
        "canDeleteUsers": false
    },
    "metadataMerge": true,
    "permissionsMerge": false
}
```

**Merge Logic**:
- `metadataMerge: true`: Merge with existing metadata
- `metadataMerge: false`: Replace entire metadata object
- Same logic applies to permissions

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "Metadata and permissions updated successfully."
}
```

### Security Endpoints

#### GET /v1/users/{userId}/sessions
**Description**: Get all active sessions for current user

**Authentication**: Required (session cookie)

**Response Success (200)**:
```json
{
    "type": "success",
    "sessions": [
        {
            "session_id": "session-uuid",
            "user_uuid": "user-uuid",
            "created_at": 1640995200,
            "last_accessed_at": 1640995300,
            "expires_at": 1641600000,
            "user_agent": "Mozilla/5.0...",
            "is_active": true
        }
    ]
}
```

---

#### POST /v1/users/{userId}/sessions/{sessionId}
**Description**: Remove/invalidate a specific user session

**Authentication**: Required (session cookie)

**Request Body**:
```json
{
    "session_id": "session-uuid-to-remove"
}
```

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "Session Removed Successfully"
}
```

---

#### POST /v1/users/{userId}/password
**Description**: Update user's password

**Authentication**: Required (session cookie)

**Request Body**:
```json
{
    "old": "currentpassword",
    "new": "newpassword"
}
```

**Response Success (200)**:
```json
{
    "type": "success",
    "message": "Password is updated!"
}
```

**Response Errors**:
- `401`: Invalid old password or empty passwords
- `500`: Update failed

### Google OAuth Endpoints

#### GET /googleLoginInitiate
**Description**: Initiate Google OAuth flow

**Query Parameters**:
- `redirect`: Post-authentication redirect URL
- `source`: Source application identifier

**Response**: HTTP redirect to Google OAuth consent screen

---

#### GET /oauth2callback
**Description**: Handle Google OAuth callback

**Internal Use**: Processes OAuth response and creates user session

## Data Transfer Objects (DTOs)

### UserProfileUpdateDTO
```java
public class UserProfileUpdateDTO {
    private String email;
    private String profilePic;
    private String fullName;
    // Getters and setters...
}
```

### UserMetadataPermissionsUpdateDTO
```java
public class UserMetadataPermissionsUpdateDTO {
    private JSONObject metadata;
    private JSONObject permissions;
    private boolean metadataMerge;
    private boolean permissionsMerge;
    // Getters and setters...
}
```

## Error Handling

### Standard Error Response Format
```json
{
    "type": "error",
    "message": "Descriptive error message"
}
```

### Common HTTP Status Codes
- **200**: Success
- **201**: Created (registration)
- **400**: Bad Request (validation errors)
- **401**: Unauthorized (authentication required)
- **403**: Forbidden (insufficient permissions)
- **404**: Not Found (resource doesn't exist)
- **409**: Conflict (duplicate resource)
- **500**: Internal Server Error

### Transaction Management
- All database operations use transactions
- Automatic rollback on errors
- Atomic operations for multi-table updates

## Environment Configuration

### Required Environment Variables

```bash
# Database Configuration
AUTH_DB_USER=your_db_username
AUTH_DB_PASSWORD=your_db_password

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Service URLs
VITE_AUTH_BACKEND=https://auth.yoursite.com
VITE_AUTH_FRONTEND=https://app.yoursite.com

# Email Service
MAILGUN_KEY=your_mailgun_api_key

# Environment
VITE_PROD=yes  # "yes" for production, anything else for development

# API Keys for Client Authentication
# These should be long, random, secure strings (minimum 32 characters)
# Generate using: openssl rand -hex 32
MY_WEB_APP_AUTH_API_KEY=your_secure_web_app_api_key_here
MY_MOBILE_APP_AUTH_API_KEY=your_secure_mobile_app_api_key_here
MY_ADMIN_PANEL_AUTH_API_KEY=your_secure_admin_panel_api_key_here
```

#### API Key Security Guidelines
- **Key Length**: Minimum 32 characters, recommended 64+ characters
- **Key Generation**: Use cryptographically secure random generation
- **Key Rotation**: Regularly rotate API keys (recommended: every 90 days)
- **Environment Separation**: Use different keys for development, staging, and production
- **Storage**: Store in secure environment variable management systems
- **Never commit**: API keys should never be committed to version control

#### Example Key Generation
```bash
# Generate secure API keys using OpenSSL
openssl rand -hex 32  # Generates 64-character hex string
openssl rand -base64 32  # Generates base64-encoded string

# Or using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

### Database Connection
- **URL**: `jdbc:mysql://db.hriday.tech:3306/Auth_Db`
- **Driver**: MySQL Connector/J 9.3.0
- **Connection pooling**: Basic JDBC connection management

## Deployment

### Build Configuration
```xml
<!-- pom.xml -->
<groupId>tech.hriday</groupId>
<artifactId>auth</artifactId>
<packaging>war</packaging>
<version>0.0.1-SNAPSHOT</version>
```

### Servlet Container
- Compatible with Jakarta EE servlet containers
- Requires Servlet API 5.0.0+ support
- Configured via `web.xml` and annotations

### Security Considerations
- HTTPS enforced in production
- Secure cookie settings
- CORS filtering implemented
- API key validation
- SQL injection prevention via prepared statements
- XSS protection through proper JSON handling

### Monitoring & Logging
- Apache Log4j2 for application logging
- Database transaction logging
- Error tracking and debugging support

### API Key Troubleshooting

#### Common Issues
1. **401 Unauthorized: Invalid or missing API Key/Client ID**
   - Check if `X-Hriday-Tech-App-Key` header is set correctly
   - Verify API key matches environment variable value
   - Ensure no extra whitespace or encoding issues

2. **403 Forbidden: Insufficient application privileges**
   - Admin endpoints require `MY_ADMIN_PANEL_AUTH_API_KEY`
   - Check if correct API key is being used for admin operations
   - Verify client ID mapping in `ApiKeyManager.isAdminApp()`

3. **Environment Variable Not Set Errors**
   ```
   MY_WEB_APP_AUTH_API_KEY environment variable not set!
   MY_MOBILE_APP_AUTH_API_KEY environment variable not set!
   MY_ADMIN_PANEL_AUTH_API_KEY environment variable not set!
   ```
   - Set environment variables before application startup
   - Verify environment variable names match exactly
   - Check deployment configuration

#### Debug Steps
1. **Check Server Logs**: Look for API key validation messages
2. **Verify Headers**: Use browser dev tools or API testing tools
3. **Test Environment Variables**: Use `System.getenv()` debug output
4. **Validate Key Format**: Ensure keys are properly encoded strings