package app.datasource.model;

import app.domain.model.GameStatus;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "current_games")
public class CurrentGameData {
    @Id
    private UUID gameId;

    @Embedded
    private GameFieldData gameField;

    private UUID firstPlayerId;
    private UUID secondPlayerId;
    private UUID currentTurnPlayerId;
    private UUID winnerPlayerId;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private boolean computerOpponent;

    protected CurrentGameData() {
    }

    public CurrentGameData(UUID gameId, GameFieldData gameField) {
        this(gameId, gameField, null, null, null, null, GameStatus.WAITING_FOR_PLAYERS, true);
    }

    public CurrentGameData(
            UUID gameId,
            GameFieldData gameField,
            UUID firstPlayerId,
            UUID secondPlayerId,
            UUID currentTurnPlayerId,
            UUID winnerPlayerId,
            GameStatus status,
            boolean computerOpponent
    ) {
        this.gameId = gameId;
        this.gameField = gameField;
        this.firstPlayerId = firstPlayerId;
        this.secondPlayerId = secondPlayerId;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.winnerPlayerId = winnerPlayerId;
        this.status = status;
        this.computerOpponent = computerOpponent;
    }

    public UUID getGameId() {
        return gameId;
    }

    public GameFieldData getGameField() {
        return gameField;
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
