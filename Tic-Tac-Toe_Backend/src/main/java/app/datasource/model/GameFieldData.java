package app.datasource.model;

public class GameFieldData {
    private final int[][] cells;

    public GameFieldData(int[][] cells) {
        this.cells = copy(cells);
    }

    public int[][] getCells() {
        return copy(cells);
    }

    private static int[][] copy(int[][] source) {
        int[][] target = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            target[i] = source[i].clone();
        }
        return target;
    }
}
