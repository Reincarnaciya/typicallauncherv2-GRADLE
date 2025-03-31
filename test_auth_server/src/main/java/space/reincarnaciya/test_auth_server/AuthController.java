package space.reincarnaciya.test_auth_server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        log.info("Catch request {}", request);

        if ("username".equals(request.username()) && PasswordHasher.hashPassword("password").equals(request.password())) {
            return new AuthResponse(
                    true,
                    "Авторизация прошла успешно",
                    request.username(),
                    true,
                    "[" + "ADMIN" + "]"
            );
        }
        return new AuthResponse(
                false,
                "Данные введены неверно",
                request.username(),
                false,
                null
        );
    }
}