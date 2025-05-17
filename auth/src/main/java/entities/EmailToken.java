package entities;

public record EmailToken(String user_uuid, String token, long expires_at) {
}