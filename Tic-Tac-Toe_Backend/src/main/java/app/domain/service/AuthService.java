package app.domain.service;

import app.web.model.SignUpRequest;

import java.util.UUID;

public interface AuthService {
    boolean signUp(SignUpRequest request);

    UUID signIn(String authorizationHeader);

    UUID authenticate(String authorizationHeader);
}
