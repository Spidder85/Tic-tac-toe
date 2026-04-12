package app.domain.model;

import java.util.UUID;

public class CurrentGame {
    private final UUID id;
    private final GameField gameField;
    private final UUID firstPlayerId;
    private final UUID secondPlayerId;
    private final UUID currentTurnPlayerId;
    private final UUID winnerPlayerId;
    private final GameStatus status;
    private final boolean computerOpponent;

    public CurrentGame(UUID id, GameField gameField) {
        this(id, gameField, null, null, null, null, GameStatus.WAITING_FOR_PLAYERS, true);
    }

    public CurrentGame(
            UUID id,
            GameField gameField,
            UUID firstPlayerId,
            UUID secondPlayerId,
            UUID currentTurnPlayerId,
            UUID winnerPlayerId,
            GameStatus status,
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

    public GameField getGameField() {
        return gameField;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFirstPlayerId() {
        return firstPlayerId;
    }

    public UUID getSecondPlayerId() {
        return secondPlayerId;
    }

    public UUID getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public UUID getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public boolean isComputerOpponent() {
        return computerOpponent;
    }
}
