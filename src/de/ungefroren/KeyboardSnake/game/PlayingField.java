package de.ungefroren.KeyboardSnake.game;

import java.util.Arrays;
import java.util.stream.IntStream;

public class PlayingField {
    private final int[][] keys;
    private final boolean hasBorder;

    public PlayingField(boolean hasBorder, int[][] keys) {
        this.hasBorder = hasBorder;
        this.keys = keys;
    }

    public int getWidth() {
        return keys[0].length;
    }

    public int getHeight() {
        return keys.length;
    }

    public boolean hasBorder() {
        return hasBorder;
    }

    public int getKeyCode(Pos position) {
        return keys[position.y][position.x];
    }

    public boolean inside(Pos position) {
        return position.x < getWidth() && position.x >= 0
                && position.y < getHeight() && position.y >= 0;
    }

    public IntStream keyCodeStream() {
        return Arrays.stream(keys).flatMapToInt(Arrays::stream);
    }
}
