package de.ungefroren.KeyboardSnake.game;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Snake {

    private final LinkedList<Pos> body;
    private final LinkedList<Color> colors;
    private final PlayingField playingField;

    private int hue = (int) (Math.random() * 64);
    private Pos last = null;

    public Snake(List<Pos> body, PlayingField playingField) {
        this.playingField = playingField;
        this.body = new LinkedList<>(body);
        this.colors = body.stream().map(pos -> rainbowColors()).collect(Collectors.toCollection(LinkedList::new));
        if (body.isEmpty())
            throw new IllegalArgumentException("Body is not allowed to be empty");
        if (!body.stream().allMatch(playingField::inside))
            throw new IllegalArgumentException("Body must be inside playing field");
    }

    public List<Pos> getBody() {
        return Collections.unmodifiableList(body);
    }

    public List<Color> getColors() {
        return colors;
    }

    public void forEach(BiConsumer<Pos, Color> consumer) {
        for (int i = 0; i < body.size(); i++) {
            consumer.accept(body.get(i), colors.get(i));
        }
    }

    public Pos getHead() {
        return body.peekFirst();
    }

    public void eat() {
        if (last == null) throw new IllegalStateException("Snake must move before it can be fed");
        body.addLast(last);
        colors.addLast(rainbowColors());
        last = null;
    }

    private boolean move(Pos to, Pos throughBorder) {
        Pos newHead = to;
        if (!playingField.inside(newHead)) {
            if (playingField.hasBorder()) {
                return false;
            } else {
                newHead = throughBorder;
            }
        }
        if (body.contains(newHead)) {
            return false;
        } else {
            body.addFirst(newHead);
            last = body.pollLast();
            return true;
        }
    }

    public boolean move(Direction direction) {
        switch (direction) {
            case UP:
                return move(
                        getHead().plusY(-1),
                        new Pos(getHead().x, playingField.getHeight() - 1)
                );
            case DOWN:
                return move(
                        getHead().plusY(1),
                        new Pos(getHead().x, 0)
                );
            case LEFT:
                return move(
                        getHead().plusX(-1),
                        new Pos(playingField.getWidth() - 1, getHead().y)
                );
            case RIGHT:
                return move(
                        getHead().plusX(1),
                        new Pos(0, getHead().y)
                );
            default:
                throw new NullPointerException();
        }
    }

    public void shiftColor() {
        colors.addLast(rainbowColors());
        colors.pollFirst();
    }

    private Color rainbowColors() {
        if (hue++ > 64) hue = 0;
        return Color.getHSBColor(hue / 64f, 1, 1);
    }


}
