package app.datasource.repository;

import app.datasource.model.UserData;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends CrudRepository<UserData, UUID> {
    Optional<UserData> findByLogin(String login);

    boolean existsByLogin(String login);
}
