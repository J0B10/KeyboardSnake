package de.ungefroren.KeyboardSnake.keyboard;

import com.logitech.gaming.LogiLED;

import java.awt.Color;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

public class LogitechKeyboardLights implements Closeable {

    private final boolean rgbSupport;

    public LogitechKeyboardLights(boolean rgbSupport) throws IOException {
        this.rgbSupport = rgbSupport;
        if (!LogiLED.LogiLedInitWithName("KeyboardSnake".toCharArray())) {
            throw new IOException("Could not initialize Logitech Light SDK");
        }
        LogiLED.LogiLedSaveCurrentLighting();
    }

    public void setLightning(Color color) {
        if (rgbSupport) LogiLED.LogiLedSetLighting(
                (color.getRed() * 100) / 256,
                (color.getGreen() * 100) / 256,
                (color.getBlue() * 100) / 256
        );
        else LogiLED.LogiLedSetLighting(
                (grayTones(color).getRed() * 100) / 256,
                (grayTones(color).getGreen() * 100) / 256,
                (grayTones(color).getBlue() * 100) / 256
        );
    }

    public void setLightning(int keyCode, Color color) {
        if (rgbSupport) LogiLED.LogiLedSetLightingForKeyWithKeyName(
                keyCode,
                (color.getRed() * 100) / 256,
                (color.getGreen() * 100) / 256,
                (color.getBlue() * 100) / 256
        );
        else LogiLED.LogiLedSetLightingForKeyWithKeyName(
                keyCode,
                (grayTones(color).getRed() * 100) / 256,
                (grayTones(color).getGreen() * 100) / 256,
                (grayTones(color).getBlue() * 100) / 256
        );
    }

    public static Color grayTones(Color c) {
        //((Red value X 299) + (Green value X 587) + (Blue value X 114)) / 1000
        final int brightness = ((c.getRed() * 299) + (c.getGreen() * 587) + (c.getBlue() *114)) / 1000;
        return new Color(brightness, brightness, brightness);
    }

    @Override
    public void close() {
        LogiLED.LogiLedRestoreLighting();
        LogiLED.LogiLedShutdown();
    }

    public boolean hasRGBSupport() {
        return rgbSupport;
    }
}
