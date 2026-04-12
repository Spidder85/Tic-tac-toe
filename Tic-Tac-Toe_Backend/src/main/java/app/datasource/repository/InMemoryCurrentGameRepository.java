package app.datasource.repository;

import app.datasource.mapper.CurrentGameDataMapper;
import app.datasource.model.CurrentGameData;
import app.datasource.model.CurrentGameStorage;
import app.domain.model.GameStatus;
import app.domain.model.CurrentGame;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InMemoryCurrentGameRepository implements CurrentGameRepository {
    private final CurrentGameStorage storage;
    private final CurrentGameDataMapper mapper;

    public InMemoryCurrentGameRepository(CurrentGameStorage storage, CurrentGameDataMapper mapper) {
        this.storage = storage;
        this.mapper = mapper;
    }

    @Override
    public CurrentGame save(CurrentGame currentGame) {
        CurrentGameData savedData = storage.save(mapper.toData(currentGame));
        return mapper.toDomain(savedData);
    }

    @Override
    public CurrentGame findById(UUID gameId) {
        CurrentGameData currentGameData = storage.findById(gameId);
        if (currentGameData == null) {
            return null;
        }
        return mapper.toDomain(currentGameData);
    }

    @Override
    public List<CurrentGame> findWaitingGames() {
        return storage.findAll().stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING_FOR_PLAYERS)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
