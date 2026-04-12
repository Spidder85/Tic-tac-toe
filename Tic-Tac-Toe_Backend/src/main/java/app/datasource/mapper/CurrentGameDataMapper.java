package app.datasource.mapper;

import app.datasource.model.CurrentGameData;
import app.datasource.model.GameFieldData;
import app.domain.model.GameField;
import app.domain.model.CurrentGame;

public class CurrentGameDataMapper {
    public CurrentGameData toData (CurrentGame currentGame) {
        return new CurrentGameData(
                currentGame.getId(),
                new GameFieldData(currentGame.getGameField().getCells())
        );
    }

    public CurrentGame toDomain (CurrentGameData currentGameData) {
        return new CurrentGame(
                currentGameData.getGameId(),
                new GameField(currentGameData.getGameField().getCells())
        );
    }
}
