package app.web.model;

import java.util.UUID;

public class CurrentGameDto {
    private UUID id;
    private GameFieldDto gameField;
    private UUID firstPlayerId;
    private UUID secondPlayerId;
    private UUID currentTurnPlayerId;
    private UUID winnerPlayerId;
    private String status;
    private boolean computerOpponent;

    public CurrentGameDto() {
    }

    public CurrentGameDto(UUID id, GameFieldDto gameField) {
        this.id = id;
        this.gameField = gameField;
    }

    public CurrentGameDto(
            UUID id,
            GameFieldDto gameField,
            UUID firstPlayerId,
            UUID secondPlayerId,
            UUID currentTurnPlayerId,
            UUID winnerPlayerId,
            String status,
            boolean computerOpponent
    ) {
        this.id = id;
        this.gameField = gameField;
        this.firstPlayerId = firstPlayerId;
        this.secondPlayerId = secondPlayerId;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.winnerPlayerId = winnerPlayerId;
        this.status = status;
        this.computerOpponent = computerOpponent;
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

    public UUID getFirstPlayerId() {
        return firstPlayerId;
    }

    public void setFirstPlayerId(UUID firstPlayerId) {
        this.firstPlayerId = firstPlayerId;
    }

    public UUID getSecondPlayerId() {
        return secondPlayerId;
    }

    public void setSecondPlayerId(UUID secondPlayerId) {
        this.secondPlayerId = secondPlayerId;
    }

    public UUID getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(UUID currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public UUID getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public void setWinnerPlayerId(UUID winnerPlayerId) {
        this.winnerPlayerId = winnerPlayerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isComputerOpponent() {
        return computerOpponent;
    }

    public void setComputerOpponent(boolean computerOpponent) {
        this.computerOpponent = computerOpponent;
    }
}
