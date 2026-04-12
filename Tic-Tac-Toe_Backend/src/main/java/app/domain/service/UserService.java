package app.domain.service;

import app.domain.model.User;

import java.util.UUID;

public interface UserService {
    User register(String login, String password);

    User findById(UUID id);

    User findByLogin(String login);

    boolean hasCredentials(String login, String password);
}
