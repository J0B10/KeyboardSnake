package de.ungefroren.KeyboardSnake.nativehook;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeKeyboardListener extends NativeKeyAdapter implements Runnable, Closeable {

    private static final Logger LOGGER = Logger.getLogger(GlobalScreen.class.getPackage().getName());

    private final KeyEventConsumer consumer;
    private final Map<Integer,Runnable> keyListeners;

    public NativeKeyboardListener(Map<Integer,Runnable> keyListeners, Level logLvl) {
        this.keyListeners = keyListeners;
        LOGGER.setLevel(logLvl);
        consumer = new KeyEventConsumer(Collections.unmodifiableSet(keyListeners.keySet()));
        new Thread(this, "NativeKeyboardListener").start();
    }

    @Override
    public void run() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (Exception e) {
            throw new RuntimeException("Uncaught exception for NativeKeyboard listener", e);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        consumer.tryConsumeEvent(event);
        if (!keyListeners.containsKey(event.getKeyCode())) return;
        keyListeners.get(event.getKeyCode()).run();
    }

    @Override
    public void close() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }
}
