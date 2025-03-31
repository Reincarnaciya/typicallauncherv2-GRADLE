package space.typro.typicallauncher.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import space.typro.typicallauncher.models.Account;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class AuthManager {
    private static final String AUTH_URL = "http://localhost:8080/api/auth/login";
    private static volatile AuthManager instance;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static Account currentAcc;

    private AuthManager() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Получение экземпляра AuthManager (Singleton)
     */
    public static AuthManager getInstance() {
        if (instance == null) {
            synchronized (AuthManager.class) {
                if (instance == null) {
                    instance = new AuthManager();
                }
            }
        }
        return instance;
    }

    /**
     * Аутентифицирует пользователя на сервере
     */
    public Account authenticate(Account.User user) throws AuthenticationException {
        try {
            String requestBody = objectMapper.writeValueAsString(new AuthRequest(
                    user.getUsername(),
                    user.getPassword().toString()
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(AUTH_URL))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, BodyHandlers.ofString());

            if (response.statusCode() == 200) {

                JsonNode responseJson = objectMapper.readTree(response.body());
                boolean success = responseJson.has("success") && responseJson.get("success").asBoolean();

                if (!success) {
                    throw new AuthenticationException("AuthError: %s".formatted(responseJson.get("message").asText()));
                }


                boolean isModer = responseJson.has("isModer") && responseJson.get("isModer").asBoolean();
                String userRang = responseJson.has("userRang") ?
                        responseJson.get("userRang").asText() :
                        Account.defaultUserRang;
                currentAcc = new Account(userRang, isModer, success, user);
                return currentAcc;
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Authentication error: " + e.getMessage());
            throw new AuthenticationException(e.getMessage());
        }
        return null;
    }

    public Account getCurrentAccount(){
        return currentAcc;
    }


    @Getter
    private static class AuthRequest {
        private final String username;
        private final String password;

        public AuthRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}