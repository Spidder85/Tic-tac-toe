package app.domain.service;

import app.domain.model.CurrentGame;

public interface CurrentGameService {
    CurrentGame getNextMove(CurrentGame currentGame);

    void validateGameField(CurrentGame currentGame);

    boolean isGameOver(CurrentGame currentGame);
}
