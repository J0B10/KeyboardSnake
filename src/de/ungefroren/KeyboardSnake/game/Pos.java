package de.ungefroren.KeyboardSnake.game;

import java.util.Objects;

public class Pos {
    public final int x;
    public final int y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pos pos = (Pos) o;
        return x == pos.x &&
                y == pos.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public Pos plusX(int amount) {
        return new Pos(x + amount, y);
    }

    public Pos plusY(int amount) {
        return new Pos(x, y + amount);
    }

}
