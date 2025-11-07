import { apiRequest } from './client';

// Get user info (profile)
export function getUserInfo() {
  return apiRequest({
    path: '/v1/users/me',
    method: 'GET',
  });
}

// Update user info (profile)
export function updateProfile(fields) {
  return apiRequest({
    path: '/v1/users/me',
    method: 'PATCH',
    body: fields,
  });
}

// Change password
export function changePassword({ currentPassword, newPassword }) {
  return apiRequest({
    path: '/v1/users/me/password',
    method: 'POST',
    body: { currentPassword, newPassword },
  });
}