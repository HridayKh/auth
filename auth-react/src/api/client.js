// Central API client for all requests
// Handles env variables, headers, credentials, error normalization

// Import env variables (Vite exposes import.meta.env)
const API_BASE_URL = import.meta.env.VITE_AUTH_BACKEND || '';
const CLIENT_ID = import.meta.env.REACT_APP_CLIENT_ID || import.meta.env.VITE_CLIENT_ID || '';

/**
 * Generic API request function
 * @param {Object} options
 * @param {string} options.path - API path (e.g. '/v1/users/me')
 * @param {string} [options.method] - HTTP method (default 'GET')
 * @param {Object} [options.body] - Request body (will be JSON.stringified)
 * @param {Object} [options.headers] - Additional headers
 * @returns {Promise<{ok: boolean, status: number, data?: any, error?: string}>}
 */
export async function apiRequest({ path, method = 'GET', body, headers = {} }) {
  const url = API_BASE_URL + path;
  const fetchOptions = {
    method,
    headers: {
      'Content-Type': 'application/json',
      'X-HridayKh-In-Client-ID': CLIENT_ID,
      ...headers,
    },
    credentials: 'include',
  };
  if (body) fetchOptions.body = JSON.stringify(body);

  let response;
  try {
    response = await fetch(url, fetchOptions);
  } catch (err) {
    return { ok: false, status: 0, error: 'Network error' };
  }

  let data;
  try {
    data = await response.json();
  } catch (e) {
    data = null;
  }

  if (response.ok) {
    return { ok: true, status: response.status, data };
  } else {
    // Try to extract error message from backend
    const error = (data && (data.message || data.error)) || response.statusText || 'Unknown error';
    return { ok: false, status: response.status, error };
  }
}

// Export env for use in other modules
export const env = {
  API_BASE_URL,
  CLIENT_ID,
};
