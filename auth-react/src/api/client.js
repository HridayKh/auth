const API_BASE_URL = import.meta.env.VITE_AUTH_BACKEND || '';
const CLIENT_ID = 'f1';

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
		return { ok: false, status: 0, type: 'error', message: 'Network error' };
	}

	let data = null;
	try {
		data = await response.json();
	} catch (e) { }

	// Just return backend's type/message/data, always
	return {
		ok: response.ok,
		status: response.status,
		type: data && data.type,
		message: data && data.message,
		data,
	};
}

export const env = {
	API_BASE_URL,
	CLIENT_ID
}
