package app.domain.service;

import app.datasource.repository.CurrentGameRepository;
import app.domain.model.CurrentGame;
import app.domain.model.GameField;

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
        validateGameField(currentGame);
        if (isGameOver(currentGame)) {
            return repository.save(currentGame);
        }

        int[][] field = currentGame.getGameField().getCells();
        int[] bestMove = findBestMove(field);
        if (bestMove[0] != -1) {
            field[bestMove[0]][bestMove[1]] = COMPUTER;
        }

        CurrentGame updateGame = new CurrentGame(currentGame.getId(), new GameField(field));
        return repository.save(updateGame);
    }

    @Override
    public void validateGameField(CurrentGame currentGame) {
        int[][] currentField = currentGame.getGameField().getCells();
        validateValues(currentField);

        CurrentGame savedGame = repository.findById(currentGame.getId());
        if (savedGame == null) {
            throw new IllegalArgumentException("Game not found");
        }

        if (isGameOver(currentGame)) {
            throw new IllegalArgumentException("Game is allready finished");
        }

        int[][] previousField = savedGame.getGameField().getCells();

        int userMoves = 0;
        for (int row = 0; row < currentField.length; row++) {
            for (int column = 0; column < currentField[row].length; column++) {
                if (previousField[row][column] == COMPUTER && currentField[row][column] != COMPUTER) {
                    throw new IllegalArgumentException("Previous computer moves were changed");
                }
                if (previousField[row][column] == USER && currentField[row][column] != USER) {
                    throw new IllegalArgumentException("Previous user moves were changed");
                }
                if (previousField[row][column] == EMPTY && currentField[row][column] == COMPUTER) {
                    throw new IllegalArgumentException("Computer move cannot be sent by user");
                }
                if (previousField[row][column] == EMPTY && currentField[row][column] == USER) {
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
        int[][] field = currentGame.getGameField().getCells();
        return hasWinner(field, USER) || hasWinner(field, COMPUTER) || isBoardFull(field);
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
