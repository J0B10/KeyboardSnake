package de.ungefroren.KeyboardSnake.game;

import com.logitech.gaming.LogiLED;
import de.ungefroren.KeyboardSnake.keyboard.LogitechKeyboardLights;
import de.ungefroren.KeyboardSnake.nativehook.NativeKeyboardListener;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

public class Game extends Thread {

    private static final int[][] KEYS = new int[][]{
            {LogiLED.ONE, LogiLED.TWO, LogiLED.THREE, LogiLED.FOUR, LogiLED.FIVE, LogiLED.SIX, LogiLED.SEVEN, LogiLED.EIGHT, LogiLED.NINE, LogiLED.ZERO},
            {LogiLED.Q, LogiLED.W, LogiLED.E, LogiLED.R, LogiLED.T, LogiLED.Y, LogiLED.U, LogiLED.I, LogiLED.O, LogiLED.P},
            {LogiLED.A, LogiLED.S, LogiLED.D, LogiLED.F, LogiLED.G, LogiLED.H, LogiLED.J, LogiLED.K, LogiLED.L, LogiLED.SEMICOLON},
            {LogiLED.Z, LogiLED.X, LogiLED.C, LogiLED.V, LogiLED.B, LogiLED.N, LogiLED.M, LogiLED.COMMA, LogiLED.PERIOD, LogiLED.FORWARD_SLASH}
    };

    private static final Color
            BACKGROUND = Color.BLACK,
            FOOD = Color.GREEN,
            BUTTONS = Color.BLUE,
            DEATH = Color.RED,
            WIN = Color.GREEN;
    private final LogitechKeyboardLights lights;
    private final NativeKeyboardListener keyboardListener;
    private final Random rng = new Random();
    private final PlayingField field;
    private final Snake snake;
    private final long tickrate;
    private int hue = 0;
    private Pos food = null;
    private volatile Direction nextDirection = null;
    private Direction movementDirection;

    public Game(long tickrate, boolean hasBorder, LogitechKeyboardLights lights) {
        this.lights = lights;
        this.tickrate = tickrate;
        field = new PlayingField(hasBorder, KEYS);

        final Map<Integer, Runnable> inputListeners = new HashMap<>();
        inputListeners.put(NativeKeyEvent.VC_UP, this::inputUP);
        inputListeners.put(NativeKeyEvent.VC_DOWN, this::inputDown);
        inputListeners.put(NativeKeyEvent.VC_LEFT, this::inputLeft);
        inputListeners.put(NativeKeyEvent.VC_RIGHT, this::inputRight);
        inputListeners.put(NativeKeyEvent.VC_PAUSE, this::inputPause);
        keyboardListener = new NativeKeyboardListener(inputListeners, Level.OFF);

        movementDirection = rng.nextBoolean() ? Direction.LEFT : Direction.RIGHT;
        final Pos p = new Pos(field.getWidth() / 2 - 2, field.getHeight() / 2 - 1);
        final List<Pos> body = (movementDirection == Direction.LEFT)
                ? Arrays.asList(p, p.plusX(1), p.plusX(2))
                : Arrays.asList(p.plusX(2), p.plusX(1), p);
        snake = new Snake(body, field);
        this.start();
    }

    @Override
    public void run() {
        while (!interrupted()) {
            if (nextDirection != null && !nextDirection.opposite(movementDirection)) {
                movementDirection = nextDirection;
                nextDirection = null;
            }
            if (food == null) spawnFood();
            final boolean alive = snake.move(movementDirection);
            if (alive) {
                if (food.equals(snake.getHead())) {
                    food = null;
                    snake.eat();
                }
                updateLights();
            } else {
                onDeath();
            }
            if (snake.getBody().size() == field.getHeight() * field.getWidth()) {
                onWin();
            }
            try {
                Thread.sleep(tickrate);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void updateLights() {
        lights.setLightning(BACKGROUND);
        lights.setLightning(LogiLED.ARROW_UP, BUTTONS);
        lights.setLightning(LogiLED.ARROW_DOWN, BUTTONS);
        lights.setLightning(LogiLED.ARROW_LEFT, BUTTONS);
        lights.setLightning(LogiLED.ARROW_RIGHT, BUTTONS);
        lights.setLightning(LogiLED.PAUSE_BREAK, BUTTONS);
        if (food != null) lights.setLightning(field.getKeyCode(food), FOOD);
        snake.getBody().forEach(p -> lights.setLightning(field.getKeyCode(p), rainbowSnake()));
    }

    private void onWin() {
        try {
            for (int i = 0; i < 5; i++) {
                lights.setLightning(WIN);
                snake.getBody().forEach(p -> lights.setLightning(field.getKeyCode(p), rainbowSnake()));
                Thread.sleep(200);
                lights.setLightning(Color.BLACK);
                snake.getBody().forEach(p -> lights.setLightning(field.getKeyCode(p), rainbowSnake()));
                Thread.sleep(200);
            }
            exit();
        } catch (InterruptedException ignored) {
        }
    }

    private void onDeath() {
        try {
            for (int i = 0; i < 5; i++) {
                lights.setLightning(DEATH);
                snake.getBody().forEach(p -> lights.setLightning(field.getKeyCode(p), rainbowSnake()));
                Thread.sleep(200);
                lights.setLightning(Color.BLACK);
                snake.getBody().forEach(p -> lights.setLightning(field.getKeyCode(p), rainbowSnake()));
                Thread.sleep(200);
            }
            exit();
        } catch (InterruptedException ignored) {
        }
    }

    public synchronized void inputUP() {
        nextDirection = Direction.UP;
    }

    public synchronized void inputDown() {
        nextDirection = Direction.DOWN;
    }

    public synchronized void inputLeft() {
        nextDirection = Direction.LEFT;
    }

    public synchronized void inputRight() {
        nextDirection = Direction.RIGHT;
    }

    public synchronized void inputPause() {
        exit();
    }

    public void exit() {
        new Thread(() -> {
            this.interrupt();
            keyboardListener.close();
            lights.close();
            System.exit(0);
        }).start();
    }

    private void spawnFood() {
        if (snake.getBody().size() == field.getHeight() * field.getWidth())
            throw new IllegalStateException("Cannot spawn food as snake covers entire field");
        do {
            food = new Pos(rng.nextInt(field.getWidth()), rng.nextInt(field.getHeight()));
        } while (snake.getBody().contains(food));
    }

    private Color rainbowSnake() {
        if (hue++ > 64) hue = 0;
        return Color.getHSBColor(hue / 64f, 1, 1);
    }
}
