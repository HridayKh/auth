// Session management API
import { apiRequest } from './client';

// Create Session (login)
export function createSession({ email, password }) {
  return apiRequest({
    path: '/v1/users/sessions',
    method: 'POST',
    body: { email, pass: password },
  });
}

// List all sessions for the current user
export function getSessions() {
  return apiRequest({
    path: '/v1/users/sessions/me',
    method: 'GET',
  });
}

// Delete a specific session by sessionId
export function deleteSession(sessionId) {
  return apiRequest({
    path: `/v1/users/sessions/${encodeURIComponent(sessionId)}`,
    method: 'DELETE',
  });
}

// Delete the current session (logout)
export function deleteCurrentSession() {
  return apiRequest({
    path: '/v1/users/sessions/current',
    method: 'DELETE',
  });
}
