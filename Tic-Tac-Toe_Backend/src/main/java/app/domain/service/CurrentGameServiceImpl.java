package app.domain.service;

import app.datasource.repository.CurrentGameRepository;
import app.domain.model.CurrentGame;
import app.domain.model.GameField;
import app.domain.model.GameStatus;

import java.util.List;
import java.util.UUID;

public class CurrentGameServiceImpl implements CurrentGameService {
    private static final int EMPTY = 0;
    private static final int USER = 1;
    private static final int COMPUTER = 2;

    private final CurrentGameRepository repository;

    public CurrentGameServiceImpl(CurrentGameRepository repository) {
        this.repository = repository;
    }

    @Override
    public CurrentGame getNextMove(CurrentGame currentGame) {
        CurrentGame savedGame = repository.findById(currentGame.getId());
        if (savedGame == null) {
            throw new IllegalArgumentException("Game not found");
        }
        return getNextMove(currentGame, savedGame.getFirstPlayerId());
    }

    @Override
    public CurrentGame getNextMove(CurrentGame currentGame, UUID playerId) {
        CurrentGame savedGame = repository.findById(currentGame.getId());
        if (savedGame == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (savedGame.getStatus() != GameStatus.TURN) {
            throw new IllegalArgumentException("Game is not active");
        }
        if (!playerId.equals(savedGame.getCurrentTurnPlayerId())) {
            throw new IllegalArgumentException("It is not this player's turn");
        }

        CurrentGame gameForValidation = new CurrentGame(
                currentGame.getId(),
                currentGame.getGameField(),
                savedGame.getFirstPlayerId(),
                savedGame.getSecondPlayerId(),
                savedGame.getCurrentTurnPlayerId(),
                savedGame.getWinnerPlayerId(),
                savedGame.getStatus(),
                savedGame.isComputerOpponent()
        );
        validateGameField(gameForValidation, savedGame, playerId);

        int[][] field = currentGame.getGameField().getCells();
        int playerSymbol = getPlayerSymbol(savedGame, playerId);
        GameStatus status = resolveStatus(field);
        UUID winnerPlayerId = status == GameStatus.WIN ? playerId : null;
        UUID nextTurnPlayerId = null;

        if (status == GameStatus.TURN && savedGame.isComputerOpponent()) {
            int[] bestMove = findBestMove(field);
            if (bestMove[0] != -1) {
                field[bestMove[0]][bestMove[1]] = COMPUTER;
            }
            status = resolveStatus(field);
            winnerPlayerId = status == GameStatus.WIN ? null : winnerPlayerId;
            nextTurnPlayerId = status == GameStatus.TURN ? savedGame.getFirstPlayerId() : null;
        } else if (status == GameStatus.TURN) {
            nextTurnPlayerId = playerSymbol == USER ? savedGame.getSecondPlayerId() : savedGame.getFirstPlayerId();
        }

        return repository.save(new CurrentGame(
                savedGame.getId(),
                new GameField(field),
                savedGame.getFirstPlayerId(),
                savedGame.getSecondPlayerId(),
                nextTurnPlayerId,
                winnerPlayerId,
                status,
                savedGame.isComputerOpponent()
        ));
    }

    @Override
    public CurrentGame createGame(UUID ownerId, boolean computerOpponent) {
        GameStatus status = computerOpponent ? GameStatus.TURN : GameStatus.WAITING_FOR_PLAYERS;
        UUID currentTurnPlayerId = computerOpponent ? ownerId : null;
        return repository.save(new CurrentGame(
                UUID.randomUUID(),
                new GameField(),
                ownerId,
                null,
                currentTurnPlayerId,
                null,
                status,
                computerOpponent
        ));
    }

    @Override
    public List<CurrentGame> findAvailableGames() {
        return repository.findWaitingGames();
    }

    @Override
    public CurrentGame joinGame(UUID gameId, UUID playerId) {
        CurrentGame game = repository.findById(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found");
        }
        if (game.isComputerOpponent()) {
            throw new IllegalArgumentException("Computer game cannot be joined");
        }
        if (game.getStatus() != GameStatus.WAITING_FOR_PLAYERS || game.getSecondPlayerId() != null) {
            throw new IllegalArgumentException("Game is not available");
        }
        if (playerId.equals(game.getFirstPlayerId())) {
            throw new IllegalArgumentException("Game owner cannot join the same game");
        }
        return repository.save(new CurrentGame(
                game.getId(),
                game.getGameField(),
                game.getFirstPlayerId(),
                playerId,
                game.getFirstPlayerId(),
                null,
                GameStatus.TURN,
                false
        ));
    }

    @Override
    public CurrentGame findById(UUID gameId) {
        return repository.findById(gameId);
    }

    @Override
    public void validateGameField(CurrentGame currentGame) {
        CurrentGame savedGame = repository.findById(currentGame.getId());
        if (savedGame == null) {
            throw new IllegalArgumentException("Game not found");
        }
        validateGameField(currentGame, savedGame, savedGame.getCurrentTurnPlayerId());
    }

    private void validateGameField(CurrentGame currentGame, CurrentGame savedGame, UUID playerId) {
        int[][] currentField = currentGame.getGameField().getCells();
        validateValues(currentField);
        if (savedGame.getStatus() != GameStatus.TURN) {
            throw new IllegalArgumentException("Game is already finished or waiting for players");
        }

        int[][] previousField = savedGame.getGameField().getCells();
        int playerSymbol = getPlayerSymbol(savedGame, playerId);

        int userMoves = 0;
        for (int row = 0; row < currentField.length; row++) {
            for (int column = 0; column < currentField[row].length; column++) {
                if (previousField[row][column] != EMPTY && currentField[row][column] != previousField[row][column]) {
                    throw new IllegalArgumentException("Previous moves were changed");
                }
                if (previousField[row][column] == EMPTY && currentField[row][column] != EMPTY
                        && currentField[row][column] != playerSymbol) {
                    throw new IllegalArgumentException("Player can use only their own symbol");
                }
                if (previousField[row][column] == EMPTY && currentField[row][column] == playerSymbol) {
                    userMoves++;
                }
            }
        }

        if (userMoves != 1) {
            throw new IllegalArgumentException("Exactly one user move is required");
        }
    }

    @Override
    public boolean isGameOver(CurrentGame currentGame) {
        if (currentGame == null) {
            return false;
        }
        return resolveStatus(currentGame.getGameField().getCells()) != GameStatus.TURN;
    }

    private int getPlayerSymbol(CurrentGame currentGame, UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player is required");
        }
        if (playerId.equals(currentGame.getFirstPlayerId())) {
            return USER;
        }
        if (!currentGame.isComputerOpponent() && playerId.equals(currentGame.getSecondPlayerId())) {
            return COMPUTER;
        }
        throw new IllegalArgumentException("Player is not a participant of this game");
    }

    private GameStatus resolveStatus(int[][] field) {
        if (hasWinner(field, USER) || hasWinner(field, COMPUTER)) {
            return GameStatus.WIN;
        }
        if (isBoardFull(field)) {
            return GameStatus.DRAW;
        }
        return GameStatus.TURN;
    }

    private void validateValues(int[][] field) {
        for (int[] row : field) {
            for (int cell : row) {
                if (cell != EMPTY && cell != USER && cell != COMPUTER) {
                    throw new IllegalArgumentException("Game field contains unsupported values");
                }
            }
        }
    }

    private int[] findBestMove(int[][] field) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};

        for (int row = 0; row < field.length; row++) {
            for (int column = 0; column < field[row].length; column++) {
                if (field[row][column] == EMPTY) {
                    field[row][column] = COMPUTER;
                    int score = minimax(field, false);
                    field[row][column] = EMPTY;
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove[0] = row;
                        bestMove[1] = column;
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int[][] field, boolean maximizing) {
        if (hasWinner(field, COMPUTER)) {
            return 1;
        }
        if (hasWinner(field, USER)) {
            return -1;
        }
        if (isBoardFull(field)) {
            return 0;
        }

        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        int player = maximizing ? COMPUTER : USER;

        for (int row = 0; row < field.length; row++) {
            for (int column = 0; column < field[row].length; column++) {
                if (field[row][column] == EMPTY) {
                    field[row][column] = player;
                    int score = minimax(field, !maximizing);
                    field[row][column] = EMPTY;
                    if (maximizing) {
                        bestScore = Math.max(score, bestScore);
                    } else {
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
        }
        return bestScore;
    }

    private boolean hasWinner(int[][] field, int player) {
        for (int i = 0; i < 3; i++) {
            if (field[i][0] == player && field[i][1] == player && field[i][2] == player) {
                return true;
            }
            if (field[0][i] == player && field[1][i] == player && field[2][i] == player) {
                return true;
            }
        }

        return (field[0][0] == player && field[1][1] == player && field[2][2] == player)
                || (field[0][2] == player && field[1][1] == player && field[2][0] == player);
    }

    private boolean isBoardFull(int[][] field) {
        for (int[] row : field) {
            for (int cell : row) {
                if (cell == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
}
