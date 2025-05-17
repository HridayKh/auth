package entities;

public record User(String uuid, String email, String password_hash, Boolean is_verified, long created_at,
		long updated_at, long last_login) {
}
