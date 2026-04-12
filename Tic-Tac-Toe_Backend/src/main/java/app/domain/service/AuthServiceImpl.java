package app.domain.service;

import app.domain.model.User;
import app.web.model.SignUpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {
    private static final String BASIC_PREFIX = "Basic ";

    private final UserService userService;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean signUp(SignUpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Sign up request is required");
        }
        userService.register(request.getLogin(), request.getPassword());
        return true;
    }

    @Override
    public UUID signIn(String authorizationHeader) {
        return authenticate(authorizationHeader);
    }

    @Override
    public UUID authenticate(String authorizationHeader) {
        Credentials credentials = parseAuthorizationHeader(authorizationHeader);
        User user = userService.findByLogin(credentials.login());
        if (user == null || !user.getPassword().equals(credentials.password())) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        return user.getId();
    }

    private Credentials parseAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
            throw new IllegalArgumentException("Basic authorization header is required");
        }
        String encodedCredentials = authorizationHeader.substring(BASIC_PREFIX.length());
        String decodedCredentials;
        try {
            decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Authorization header has invalid base64 value", exception);
        }
        int delimiterIndex = decodedCredentials.indexOf(':');
        if (delimiterIndex <= 0 || delimiterIndex == decodedCredentials.length() - 1) {
            throw new IllegalArgumentException("Authorization header must contain login and password");
        }
        return new Credentials(
                decodedCredentials.substring(0, delimiterIndex),
                decodedCredentials.substring(delimiterIndex + 1)
        );
    }

    private record Credentials(String login, String password) {
    }
}
