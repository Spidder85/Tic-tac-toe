package app.datasource.repository;

import app.domain.model.User;

import java.util.UUID;

public interface UserRepository {
    User save(User user);

    User findById(UUID id);

    User findByLogin(String login);

    boolean existsByLogin(String login);
}
