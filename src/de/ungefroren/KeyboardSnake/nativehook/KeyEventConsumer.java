package de.ungefroren.KeyboardSnake.nativehook;

import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.util.Set;

public class KeyEventConsumer extends EventConsumer {

    private final Set<Integer> blacklist;

    public KeyEventConsumer(Set<Integer> blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public void consumeEvent(NativeInputEvent event) throws UnconsumableEventException {
        if (!(event instanceof NativeKeyEvent)) {
            return;
        }
        if (blacklist.contains(((NativeKeyEvent) event).getKeyCode())) {
            super.consumeEvent(event);
        }
    }

    public Set<Integer> getBlacklist() {
        return blacklist;
    }
}
