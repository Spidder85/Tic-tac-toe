package app.datasource.repository;

import app.datasource.mapper.CurrentGameDataMapper;
import app.domain.model.CurrentGame;
import app.domain.model.GameStatus;

import java.util.List;
import java.util.UUID;

public class JpaCurrentGameRepository implements CurrentGameRepository {
    private final CurrentGameJpaRepository repository;
    private final CurrentGameDataMapper mapper;

    public JpaCurrentGameRepository(CurrentGameJpaRepository repository, CurrentGameDataMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CurrentGame save(CurrentGame currentGame) {
        return mapper.toDomain(repository.save(mapper.toData(currentGame)));
    }

    @Override
    public CurrentGame findById(UUID gameId) {
        return repository.findById(gameId)
                .map(mapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<CurrentGame> findWaitingGames() {
        return repository.findByStatusAndComputerOpponent(GameStatus.WAITING_FOR_PLAYERS, false).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
