package app.web.mapper;

import app.domain.model.CurrentGame;
import app.domain.model.GameField;
import app.domain.model.GameStatus;
import app.web.model.CurrentGameDto;
import app.web.model.GameFieldDto;

public class CurrentGameWebMapper {
    public CurrentGame toDomain(CurrentGameDto currentGameDto){
        if (currentGameDto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (currentGameDto.getGameField() == null) {
            throw new IllegalArgumentException("Game field is required");
        }
        if (currentGameDto.getGameField().getCells() == null) {
            throw new IllegalArgumentException("Game field cells are required");
        }
        return new CurrentGame(
                currentGameDto.getId(),
                new GameField(currentGameDto.getGameField().getCells()),
                currentGameDto.getFirstPlayerId(),
                currentGameDto.getSecondPlayerId(),
                currentGameDto.getCurrentTurnPlayerId(),
                currentGameDto.getWinnerPlayerId(),
                toStatus(currentGameDto.getStatus()),
                currentGameDto.isComputerOpponent()
        );
    }

    public CurrentGameDto toDto(CurrentGame currentGame){
        return new CurrentGameDto(
                currentGame.getId(),
                new GameFieldDto(currentGame.getGameField().getCells()),
                currentGame.getFirstPlayerId(),
                currentGame.getSecondPlayerId(),
                currentGame.getCurrentTurnPlayerId(),
                currentGame.getWinnerPlayerId(),
                currentGame.getStatus().name(),
                currentGame.isComputerOpponent()
        );
    }

    private GameStatus toStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return GameStatus.valueOf(status);
    }
}
