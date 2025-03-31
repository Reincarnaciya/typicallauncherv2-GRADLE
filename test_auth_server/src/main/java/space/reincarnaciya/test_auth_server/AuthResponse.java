package space.reincarnaciya.test_auth_server;

public record AuthResponse(
        boolean success,
        String message,
        String username,
        boolean isModer,
        String userRang
) {
}