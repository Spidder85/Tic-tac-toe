package app.domain.service;

import app.domain.model.CurrentGame;

import java.util.List;
import java.util.UUID;

public interface CurrentGameService {
    CurrentGame getNextMove(CurrentGame currentGame);

    CurrentGame getNextMove(CurrentGame currentGame, UUID playerId);

    CurrentGame createGame(UUID ownerId, boolean computerOpponent);

    List<CurrentGame> findAvailableGames();

    CurrentGame joinGame(UUID gameId, UUID playerId);

    CurrentGame findById(UUID gameId);

    void validateGameField(CurrentGame currentGame);

    boolean isGameOver(CurrentGame currentGame);
}
