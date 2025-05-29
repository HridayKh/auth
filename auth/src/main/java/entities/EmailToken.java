package entities;

public record EmailToken(String token, String user_uuid, long expires_at) {
}