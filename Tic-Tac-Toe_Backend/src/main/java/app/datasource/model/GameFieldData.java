package app.datasource.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GameFieldData {
    @Column(name = "cells", nullable = false, length = 32)
    private String cells;

    protected GameFieldData() {
    }

    public GameFieldData(int[][] cells) {
        this.cells = serialize(cells);
    }

    public int[][] getCells() {
        return deserialize(cells);
    }

    private static String serialize(int[][] source) {
        StringBuilder builder = new StringBuilder(9);
        for (int[] row : source) {
            for (int cell : row) {
                builder.append(cell);
            }
        }
        return builder.toString();
    }

    private static int[][] deserialize(String source) {
        if (source == null || source.length() != 9) {
            return new int[3][3];
        }
        int[][] target = new int[3][3];
        for (int index = 0; index < source.length(); index++) {
            target[index / 3][index % 3] = Character.digit(source.charAt(index), 10);
        }
        return target;
    }
}
