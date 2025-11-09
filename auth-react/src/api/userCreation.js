// User-related API: create, verification, password reset, get/update info
import { apiRequest } from './client';

// Create/register user
export function createUser({ email, password, fullName, redirect }) {
  return apiRequest({
    path: '/v1/users',
    method: 'POST',
    body: { email, pass: password, fullName,redirect },
  });
}

// Resend verification email
export function resendVerification({ email, redirect }) {
  return apiRequest({
    path: '/v1/users/verify/resend',
    method: 'POST',
    body: { email, redirect },
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

