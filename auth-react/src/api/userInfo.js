import { apiRequest } from './client';

export function getUserInfo() {
	return apiRequest({
		path: '/v1/users/me',
		method: 'GET',
	});
}

export function updateProfile(fields) {
	return apiRequest({
		path: '/v1/users/me',
		method: 'PATCH',
		body: fields,
	});
}

export function changePassword({ old, newPass }) {
	return apiRequest({
		path: '/v1/users/me/password',
		method: 'POST',
		body: {old: old, new: newPass },
	});
}

export function linkPassword({ newPass }) {
	return apiRequest({
		path: '/v1/users/me/password',
		method: 'POST',
		body: { new: newPass },
	});
}

export function unlinkGoogle() {
	return apiRequest({
		path: '/v1/users/google/unlink',
		method: 'DELETE'
	});
}