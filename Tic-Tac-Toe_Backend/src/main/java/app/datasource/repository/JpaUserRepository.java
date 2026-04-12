package app.datasource.repository;

import app.datasource.mapper.UserDataMapper;
import app.domain.model.User;

import java.util.UUID;

public class JpaUserRepository implements UserRepository {
    private final UserJpaRepository repository;
    private final UserDataMapper mapper;

    public JpaUserRepository(UserJpaRepository repository, UserDataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toData(user)));
    }

    @Override
    public User findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public User findByLogin(String login) {
        return repository.findByLogin(login)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByLogin(String login) {
        return repository.existsByLogin(login);
    }
}
