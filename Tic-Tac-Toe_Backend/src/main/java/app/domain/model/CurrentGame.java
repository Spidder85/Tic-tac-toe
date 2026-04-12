package app.domain.model;

import java.util.UUID;

public class CurrentGame {
    private final UUID id;
    private final GameField gameField;

    public CurrentGame(UUID id, GameField gameField) {
        this.id = id;
        this.gameField = gameField;
    }

    public GameField getGameField() {
        return gameField;
    }

    public UUID getId() {
        return id;
    }
}
