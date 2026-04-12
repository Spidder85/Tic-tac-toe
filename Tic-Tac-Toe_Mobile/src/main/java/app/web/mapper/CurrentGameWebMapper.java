package app.web.mapper;

import app.domain.model.CurrentGame;
import app.domain.model.GameField;
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
                new GameField(currentGameDto.getGameField().getCells())
        );
    }

    public CurrentGameDto toDto(CurrentGame currentGame){
        return new CurrentGameDto(
                currentGame.getId(),
                new GameFieldDto(currentGame.getGameField().getCells())
        );
    }
}
