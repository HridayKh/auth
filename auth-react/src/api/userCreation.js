// User-related API: create, verification, password reset, get/update info
import { apiRequest } from './client';

// Create/register user
export function createUser({ email, password, fullName }) {
  return apiRequest({
    path: '/v1/users',
    method: 'POST',
    body: { email, password, fullName },
  });
}

// Resend verification email
export function resendVerification({ email }) {
  return apiRequest({
    path: '/v1/users/verify/resend',
    method: 'POST',
    body: { email },
  });
}

// Request password reset (send reset email)
export function requestPasswordReset({ email }) {
  return apiRequest({
    path: '/v1/users/password-resets',
    method: 'POST',
    body: { email },
  });
}

// Reset password with token
export function resetPassword({ token, newPassword }) {
  return apiRequest({
    path: '/v1/users/password-resets',
    method: 'PUT',
    body: { token, newPassword },
  });
}

