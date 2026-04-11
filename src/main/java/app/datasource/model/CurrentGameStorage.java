package app.datasource.model;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CurrentGameStorage {
    private final ConcurrentMap<UUID, CurrentGameData> games = new ConcurrentHashMap<>();

    public CurrentGameData save(CurrentGameData currentGame) {
        games.put(currentGame.getGameId(), currentGame);
        return currentGame;
    }

    public CurrentGameData findById(UUID gameId) {
        return games.get(gameId);
    }
}
