package de.ungefroren.KeyboardSnake.nativehook;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeInputEvent;

import java.lang.reflect.Field;

public class EventConsumer {

    public EventConsumer() {
        GlobalScreen.setEventDispatcher(new VoidDispatchService());
    }

    public void consumeEvent(NativeInputEvent event) throws UnconsumableEventException {
        try {
            final Field f = NativeInputEvent.class.getDeclaredField("reserved");
            f.setAccessible(true);
            f.setShort(event, (short) 0x01);
        } catch (Exception ex) {
            throw new UnconsumableEventException(event, ex);
        }
    }

    public void tryConsumeEvent(NativeInputEvent event) {
        try {
            consumeEvent(event);
        } catch (UnconsumableEventException e) {
            System.err.println(e.getMessage());
        }
    }

    public static class UnconsumableEventException extends Exception {

        public UnconsumableEventException(NativeInputEvent event, Throwable cause) {
            super("could not consume event (" + event.paramString() +")", cause);
        }
    }
}
