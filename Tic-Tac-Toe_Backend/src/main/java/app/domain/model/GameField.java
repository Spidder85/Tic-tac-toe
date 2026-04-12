package app.domain.model;

public class GameField {
    private static final int SIZE = 3;

    private final int[][] cells;

    public GameField() {
        this.cells = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this.cells[i][j] = -1;
            }
        }
    }

    public GameField(int[][] boards) {
        validateSize(boards);
        this.cells = copy(boards);
    }

    public int[][] getCells() {
        return copy(cells);
    }

    private void validateSize(int[][] boards) {
        if (boards == null || boards.length != SIZE) {
            throw new IllegalArgumentException("Game board size is not " + SIZE + "x" + SIZE + ".");
        }
        for (int[] row : boards) {
            if (row == null || row.length != SIZE) {
                throw new IllegalArgumentException("Game board size is not " + SIZE + "x" + SIZE + ".");
            }
        }
    }

    private int[][] copy(int[][] source) {
        int[][] target = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            target[i] = source[i].clone();
        }
        return target;
    }
}
