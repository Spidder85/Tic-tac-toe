package app.datasource.repository;

import app.domain.model.CurrentGame;

import java.util.UUID;

public interface CurrentGameRepository {
    CurrentGame save(CurrentGame currentGame);

    CurrentGame findById(UUID gameId);
}
