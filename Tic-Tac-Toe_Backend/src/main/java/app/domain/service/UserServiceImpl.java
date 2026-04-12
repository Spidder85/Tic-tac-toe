package app.domain.service;

import app.datasource.repository.UserRepository;
import app.domain.model.User;

import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User register(String login, String password) {
        validateCredentials(login, password);
        if (repository.existsByLogin(login)) {
            throw new IllegalArgumentException("Login is already used");
        }
        return repository.save(new User(UUID.randomUUID(), login, password));
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public User findByLogin(String login) {
        return repository.findByLogin(login);
    }

    @Override
    public boolean hasCredentials(String login, String password) {
        User user = repository.findByLogin(login);
        return user != null && user.getPassword().equals(password);
    }

    private void validateCredentials(String login, String password) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
