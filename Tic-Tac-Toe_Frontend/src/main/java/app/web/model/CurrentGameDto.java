package app.web.model;

import java.util.UUID;

public class CurrentGameDto {
    private UUID id;
    private GameFieldDto gameField;

    public CurrentGameDto() {
    }

    public CurrentGameDto(UUID id, GameFieldDto gameField) {
        this.id = id;
        this.gameField = gameField;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GameFieldDto getGameField() {
        return gameField;
    }

    public void setGameField(GameFieldDto gameField) {
        this.gameField = gameField;
    }
}
