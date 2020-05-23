package de.ungefroren.KeyboardSnake;

import de.ungefroren.KeyboardSnake.game.Game;
import de.ungefroren.KeyboardSnake.keyboard.LogitechKeyboardLights;

import java.io.IOException;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        final boolean monochrome = Arrays.stream(args).anyMatch(s -> s.equals("-m") || s.equals("--monochrome"));
        final boolean border = Arrays.stream(args).anyMatch(s -> s.equals("-b") || s.equals("--border"));
        final long speed = Arrays.stream(args).filter(s -> s.matches("(-s=|--speed=)\\d+"))
                .map(s -> Long.valueOf(s.substring(s.indexOf("=") + 1)))
                .findFirst().orElse(500L);
        try {
            final LogitechKeyboardLights lights = new LogitechKeyboardLights(!monochrome);
            Game g = new Game(speed, border, lights);
        } catch (IOException e) {
            if (e.getMessage().equals("Could not initialize Logitech Light SDK")) {
                System.out.println("Could not initialize Logitech Light SDK");
                System.exit(1);
            } else {
                throw e;
            }
        }
    }
}
