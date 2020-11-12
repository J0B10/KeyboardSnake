package de.ungefroren.KeyboardSnake.game;

import com.logitech.gaming.LogiLED;
import de.ungefroren.KeyboardSnake.keyboard.LogitechKeyboardLights;
import de.ungefroren.KeyboardSnake.nativehook.NativeKeyboardListener;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Game implements Runnable {

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
    private final ScheduledExecutorService executor;
    private final Random rng = new Random();
    private final PlayingField field;
    private final Snake snake;
    private final long tickrate;
    private final Queue<Direction> nextDirections;
    private Pos food = null;
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
        nextDirections = new ConcurrentLinkedQueue<>();

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this, 0, tickrate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        while (!nextDirections.isEmpty()) {
            Direction next = nextDirections.poll();
            if (next != movementDirection && !movementDirection.opposite(next)) {
                movementDirection = next;
                break;
            }
        }
        if (food == null) spawnFood();
        final boolean alive = snake.move(movementDirection);
        if (alive) {
            if (food.equals(snake.getHead())) {
                food = null;
                snake.eat();
            }
            snake.shiftColor();
            updateLights();
        } else {
            onDeath();
        }
        if (snake.getBody().size() == field.getHeight() * field.getWidth()) {
            onWin();
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

        snake.forEach((pos, color) -> lights.setLightning(field.getKeyCode(pos), color));
    }

    private void onWin() {
        try {
            for (int i = 0; i < 5; i++) {
                lights.setLightning(WIN);
                snake.forEach((pos, color) -> lights.setLightning(field.getKeyCode(pos), color));
                Thread.sleep(200);
                lights.setLightning(Color.BLACK);
                snake.forEach((pos, color) -> lights.setLightning(field.getKeyCode(pos), color));
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
                snake.forEach((pos, color) -> lights.setLightning(field.getKeyCode(pos), color));
                Thread.sleep(200);
                lights.setLightning(Color.BLACK);
                snake.forEach((pos, color) -> lights.setLightning(field.getKeyCode(pos), color));
                Thread.sleep(200);
            }
            exit();
        } catch (InterruptedException ignored) {
        }
    }

    public synchronized void inputUP() {
        nextDirections.add(Direction.UP);
    }

    public synchronized void inputDown() {
        nextDirections.add(Direction.DOWN);
    }

    public synchronized void inputLeft() {
        nextDirections.add(Direction.LEFT);
    }

    public synchronized void inputRight() {
        nextDirections.add(Direction.RIGHT);
    }

    public synchronized void inputPause() {
        exit();
    }

    public void exit() {
        new Thread(() -> {
            executor.shutdownNow();
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

}
