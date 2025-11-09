package entities;

public record Session(String sessionId, String userUuid, long createdAt, long lastAccessedAt, long expiresAt,
		String userAgent) {
}