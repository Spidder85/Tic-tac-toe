package app.datasource.model;

import java.util.UUID;

public class CurrentGameData {
    private final UUID gameId;
    private final GameFieldData gameField;

    public CurrentGameData(UUID gameId, GameFieldData gameField) {
        this.gameId = gameId;
        this.gameField = gameField;
    }

    public UUID getGameId() {
        return gameId;
    }

    public GameFieldData getGameField() {
        return gameField;
    }
}
