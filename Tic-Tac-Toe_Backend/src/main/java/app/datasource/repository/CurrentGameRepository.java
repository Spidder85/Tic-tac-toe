package app.datasource.repository;

import app.domain.model.CurrentGame;

import java.util.List;
import java.util.UUID;

public interface CurrentGameRepository {
    CurrentGame save(CurrentGame currentGame);

    CurrentGame findById(UUID gameId);

    List<CurrentGame> findWaitingGames();
}
