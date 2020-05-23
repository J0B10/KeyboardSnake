package de.ungefroren.KeyboardSnake.game;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public boolean opposite(Direction movementDirection) {
        return (this == UP && movementDirection == DOWN)
                || (this == DOWN && movementDirection == UP)
                || (this == LEFT && movementDirection == RIGHT)
                || (this == RIGHT && movementDirection == LEFT);
    }
}
