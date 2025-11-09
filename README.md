# HridayKh.in Authentication Service API Documentation

*Last Updated: November 7, 2025*

## Overview

This is a comprehensive Java servlet-based authentication service built with Jakarta EE, providing secure user
management, session handling, Google OAuth integration, and role-based access control. The service features a completely
redesigned and rebuilt architecture with a modern API key system, centralized routing, and modular servlet controller
design.

## Table of Contents

1. [Architecture](#architecture)
2. [Database Schema](#database-schema)
3. [Authentication & Security](#authentication-and-security)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Environment Configuration](#environment-configuration)
7. [Deployment](#deployment)

## Architecture

### Technology Stack

- **Backend Framework**: Jakarta EE (Servlet API 5.0.0)
- **Database**: MySQL with JDBC
- **JSON Processing**: org.json library
- **Email Service**: Mailgun
- **OAuth**: Google OAuth2 API
- **Logging**: Apache Log4j2
- **Build Tool**: Maven

### Modern Architecture Components

#### 1. Centralized Routing System (`ApiServlet`)

- **Single Entry Point**: All API requests route through `/v1/*` via `ApiServlet`
- **Route-to-Handler Mapping**: Declarative routing with path parameter support
- **Pattern Matching**: Supports parameterized routes like `/v1/users/sessions/{sessionId}`
- **Method-based Routing**: HTTP methods mapped to specific handlers

#### 2. Multi-tier Authentication Filter (`ApiKeyFilter`)

- **Strategy Pattern**: Modular authentication strategies for different access levels
- **Path-based Access Control**: Automatic access level determination based on request path
- **Role-based Security**: Support for PUBLIC, FRONTEND, BACKEND, and ADMIN access levels

#### 3. Flexible API Key Management (`ApiKeyManager`)

- **Environment-driven Configuration**: Comma-separated API keys via environment variables
- **Role-based Key Mapping**: Keys automatically mapped to roles (ADMIN, BACKEND, FRONTEND)
- **Runtime Key Validation**: Efficient key-to-role lookup

### Project Structure

```
src/main/java/
├── auth/                  # Authentication filters and test apps
│   ├── ApiKeyFilter.java      # Multi-tier authentication filter
│   ├── CORSFilter.java        # Cross-origin request handling
│   └── TestApp.java           # Authentication testing endpoint
├── db/                    # Database access layer (DAOs)
├── entities/              # Domain entities
├── google/                # Google OAuth implementation
├── servlets/              # HTTP request handlers
│   ├── ApiServlet.java        # Central routing servlet
│   ├── ApiConstants.java      # Endpoint URL constants
│   ├── userPasswords/         # Password Update and reset servlets
│   ├── userSessions/          # Sessions related servlets
│   ├── usersCreate/           # User creation and email verification
│   └── usersInfo/             # User info related
└── utils/                 # Utility classes
    ├── ApiKeyManager.java 	# API key management and validation
    ├── AuthUtil.java      	# Authentication utilities
    └── ...                	# Other utilities
```

## Database Schema

### Users Table

| Field                    | Type         | Null | Key | Default |
|--------------------------|--------------|------|-----|---------|
| uuid                     | varchar(36)  | NO   | PRI | NULL    |
| email                    | varchar(255) | NO   | UNI | NULL    |
| password_hash            | varchar(255) | YES  |     | NULL    |
| is_verified              | tinyint(1)   | YES  |     | 0       |
| created_at               | bigint       | NO   |     | NULL    |
| updated_at               | bigint       | NO   |     | NULL    |
| last_login               | bigint       | YES  |     | NULL    |
| profile_pic              | varchar(255) | YES  |     | NULL    |
| full_name                | varchar(100) | YES  |     | NULL    |
| metadata                 | json         | YES  |     | NULL    |
| permissions              | json         | YES  |     | NULL    |
| acc_type                 | varchar(50)  | NO   |     | both    |
| google_id                | varchar(255) | YES  |     | NULL    |
| refresh_token            | text         | YES  |     | NULL    |
| refresh_token_expires_at | bigint       | YES  |     | NULL    |
| internal                 | json         | YES  |     | NULL    |

### Sessions Table

| Field            | Type         | Null | Key | Default |
|------------------|--------------|------|-----|---------|
| session_id       | varchar(36)  | NO   | PRI | NULL    |
| user_uuid        | varchar(36)  | NO   |     | NULL    |
| created_at       | bigint       | NO   |     | NULL    |
| last_accessed_at | bigint       | NO   |     | NULL    |
| expires_at       | bigint       | NO   |     | NULL    |
| user_agent       | varchar(255) | YES  |     | NULL    |

### Email Tokens Table

| Field      | Type         | Null | Key | Default |
|------------|--------------|------|-----|---------|
| token      | varchar(255) | NO   | PRI | NULL    |
| user_uuid  | varchar(36)  | NO   | MUL | NULL    |
| expires_at | bigint       | NO   |     | NULL    |

## Authentication and Security

### Session Management

- **Cookie-based sessions**: Uses signed JWT-like tokens stored in HTTP-only cookies
- **Session expiry**: Configurable session timeout (default: 7 days)
- **Multi-device support**: Users can have multiple sessions
- **Secure cookies**: HTTPS-only in production with proper domain settings

### Modern API Key Authentication System

The service implements a completely redesigned multi-tier authentication system using the Strategy Pattern:

#### Access Levels

1. **PUBLIC**: No authentication required
2. **FRONTEND**: Requires client ID header for frontend applications
3. **BACKEND**: Requires secret API keys for backend services
4. **ADMIN**: Requires admin-level API keys for privileged operations

#### New API Key Architecture

**Environment Variable Structure:**

- `ADMIN_API_KEYS`: Comma-separated admin-level keys
- `BACKEND_API_KEYS`: Comma-separated backend service keys
- `FRONTEND_CLIENT_IDS`: Comma-separated frontend client identifiers

**Headers:**

- `X-HridayKh-In-Auth-Key`: Secret API key for backend/admin operations
- `X-HridayKh-In-Client-ID`: Public client identifier for frontend requests

#### Path-based Access Control

The system automatically determines required access level based on request paths:

| Path Category       | Access Level | Examples                                                         |
|---------------------|--------------|------------------------------------------------------------------|
| Authentication      | FRONTEND     | `/v1/auth/login`, `/v1/auth/logout`                              |
| Registration        | FRONTEND     | `/v1/register/*`                                                 |
| Profile Management  | FRONTEND     | `/v1/profile/get-user`, `/v1/profile/update-profile`             |
| Admin Operations    | BACKEND      | `/v1/profile/admin-profile`, `/v1/profile/update-admin-metadata` |
| Security Operations | FRONTEND     | `/v1/users/{userId}/sessions/*`, `/v1/users/{userId}/password`   |

#### Authentication Strategies

**PublicAuthStrategy**: No validation required

```java
// Automatically allows access for public endpoints
```

**FrontendAuthStrategy**: Validates client ID

```java
// Requires: X-HridayKh-In-Client-ID header
// Validates against FRONTEND_CLIENT_IDS environment variable
```

**BackendAuthStrategy**: Validates backend API keys

```java
// Requires: X-HridayKh-In-Auth-Key header
// Validates against BACKEND_API_KEYS environment variable
```

**AdminAuthStrategy**: Validates admin API keys

```java
// Requires: X-HridayKh-In-Auth-Key header
// Validates against ADMIN_API_KEYS environment variable
```

### Password Security

- **SHA-256 hashing**: All passwords are hashed using SHA-256
- **Password validation**: Server-side validation for password changes
- **Account type checking**: Prevents password operations on Google-authenticated accounts


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

### Required Environment Variables [TO BE UPDATED]

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

# Admin keys for privileged operations
ADMIN_API_KEYS=admin_key_1,admin_key_2,admin_key_3

# Backend service keys for backend-to-backend communication
BACKEND_API_KEYS=backend_key_1,backend_key_2,backend_key_3

# Frontend client identifiers for frontend applications
FRONTEND_CLIENT_IDS=web_app_client_id,mobile_app_client_id,dashboard_client_id
``` 

### Modern API Key Configuration [TO BE VERIFIED]

#### Key Structure

The new system uses comma-separated environment variables to support multiple keys per role:

**ADMIN_API_KEYS Example:**

```bash
ADMIN_API_KEYS=adm_live_sk_1234567890abcdef,adm_live_sk_abcdef1234567890,adm_live_sk_fedcba0987654321
```

**BACKEND_API_KEYS Example:**

```bash
BACKEND_API_KEYS=bck_live_sk_1234567890abcdef,bck_live_sk_abcdef1234567890
```

**FRONTEND_CLIENT_IDS Example:**

```bash
FRONTEND_CLIENT_IDS=web_app_v1,mobile_app_v1,admin_panel_v1
```

#### API Key Security Guidelines

- **Key Length**: Minimum 32 characters, recommended 64+ characters
- **Key Format**: Use prefixes to identify key types (e.g., `adm_`, `bck_`, `web_`)
- **Key Generation**: Use cryptographically secure random generation
- **Key Rotation**: Regularly rotate API keys (recommended: every 90 days)
- **Environment Separation**: Use different keys for development, staging, and production
- **Storage**: Store in secure environment variable management systems
- **Never commit**: API keys should never be committed to version control

### Key Validation Flow

1. **Request Received**: `ApiKeyFilter` intercepts all `/v1/*` requests
2. **Path Analysis**: System determines required access level based on request path
3. **Strategy Selection**: Appropriate authentication strategy is selected
4. **Key Validation**: `ApiKeyManager` validates the provided key against environment variables
5. **Role Assignment**: Valid keys are mapped to their corresponding roles
6. **Request Processing**: Authorized requests proceed to `ApiServlet` for routing

### Database Connection

- **URL**: `jdbc:mysql://db.HridayKh.in:3306/Auth_Db`
- **Driver**: MySQL Connector/J 9.3.0
- **Connection pooling**: Basic JDBC connection management

## Deployment

### Build Configuration

```xml
<!-- pom.xml -->
<groupId>in.hridaykh</groupId>
<artifactId>auth</artifactId>
<packaging>war</packaging>
<version>0.0.1-SNAPSHOT</version>
```

### Servlet Container

- Compatible with Jakarta EE servlet containers
- Requires Servlet API 5.0.0+ support
- Configured via `web.xml` and annotations

### Modern Architecture Benefits

- **Centralized Routing**: Single entry point reduces complexity and improves maintainability
- **Modular Authentication**: Strategy pattern allows easy addition of new authentication methods
- **Environment-driven Configuration**: Flexible API key management without code changes
- **Path-based Security**: Automatic access control based on request patterns
- **Type Safety**: Strongly-typed constants and enums prevent configuration errors

### Security Considerations

- HTTPS enforced in production
- Secure cookie settings
- CORS filtering implemented
- Multi-tier API key validation
- SQL injection prevention via prepared statements
- XSS protection through proper JSON handling
- Path traversal protection in routing system

### Monitoring & Logging

- Apache Log4j2 for application logging
- Database transaction logging
- Error tracking and debugging support
- Authentication event logging

### New API Key System Troubleshooting

#### Common Issues

1. **401 Unauthorized: Invalid or missing API Key/Client ID**
    - Check if `X-HridayKh-In-Auth-Key` or `X-HridayKh-In-Client-ID` header is set correctly
    - Verify key exists in the corresponding environment variable list
    - Ensure no extra whitespace or encoding issues

2. **403 Forbidden: Insufficient application privileges**
    - Verify the correct access level for the endpoint
    - Backend endpoints require `X-HridayKh-In-Auth-Key` from `BACKEND_API_KEYS`
    - Admin operations require keys from `ADMIN_API_KEYS`

3. **Environment Variable Configuration Errors**
   ```
   CRITICAL: No API keys were loaded. Check environment variables.
   Environment variable for API keys 'ADMIN_API_KEYS' is not set.
   Environment variable for API keys 'BACKEND_API_KEYS' is not set.
   Environment variable for API keys 'FRONTEND_CLIENT_IDS' is not set.
   ```
    - Set environment variables before application startup
    - Verify environment variable names match exactly: `ADMIN_API_KEYS`, `BACKEND_API_KEYS`, `FRONTEND_CLIENT_IDS`
    - Check that values are comma-separated with no extra spaces

4. **Route Not Found (404 Errors)**
    - Verify the endpoint URL matches the patterns in `ApiConstants.java`
    - Check if path parameters are properly formatted (e.g., `/v1/users/{userId}/sessions`)
    - Ensure HTTP method matches the route definition

#### Debug Steps

1. **Check Server Logs**: Look for API key validation and routing messages
2. **Verify Headers**: Use browser dev tools or API testing tools to confirm headers
3. **Test Environment Variables**: Check `ApiKeyManager` debug output for loaded keys
4. **Validate Key Format**: Ensure keys are properly encoded strings with no special characters
5. **Path Analysis**: Verify request paths match the patterns in `PathAccessControl`