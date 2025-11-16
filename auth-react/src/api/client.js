import { AUTH_BACKEND } from "@/vars";
const CLIENT_ID = 'f1';

export async function apiRequest({ path, method = 'GET', body, headers = {} }) {
	const url = AUTH_BACKEND + path;
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

