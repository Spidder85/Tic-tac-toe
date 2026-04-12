package app.web.model;

public class GameFieldDto {
    private int[][] cells;

    public GameFieldDto() {
    }
    public GameFieldDto(int[][] cells) {
        this.cells = copy(cells);
    }

    public int[][] getCells() {
        return copy(cells);
    }

    public void setCells(int[][] cells) {
        this.cells = copy(cells);
    }

    private static int[][] copy(int[][] source) {
        if (source == null) {
            return null;
        }
        int[][] target = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            target[i] = source[i].clone();
        }
        return target;
    }
}
