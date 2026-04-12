package app.datasource.repository;

import app.datasource.mapper.CurrentGameDataMapper;
import app.datasource.model.CurrentGameData;
import app.datasource.model.CurrentGameStorage;
import app.domain.model.CurrentGame;

import java.util.UUID;

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
}
