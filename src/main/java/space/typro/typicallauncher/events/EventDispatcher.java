package space.typro.typicallauncher.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventDispatcher {
    private static final ConcurrentHashMap<EventType, List<EventListener<? extends EventData>>> subscribers =
            new ConcurrentHashMap<>();

    public static <T extends EventData> void subscribe(EventType type, EventListener<T> listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public static <T extends EventData> void unsubscribe(EventType type, EventListener<T> listener) {
        List<EventListener<? extends EventData>> listeners = subscribers.get(type);
        if (listeners != null) {
            listeners.remove(listener);

            if (listeners.isEmpty()) {
                subscribers.remove(type);
            }
        }
    }

    public static <T extends EventData> void throwEvent(EventType type, T eventData) {
        List<EventListener<? extends EventData>> listeners = subscribers.get(type);
        if (listeners != null) {
            for (EventListener<? extends EventData> listener : listeners) {
                try {
                    @SuppressWarnings("unchecked")
                    EventListener<T> typedListener = (EventListener<T>) listener;
                    typedListener.onEvent(eventData);
                } catch (ClassCastException e) {
                    System.err.println("Type mismatch in event listener: " + e.getMessage());
                }
            }
        }
    }

    public enum EventType {
        INTERNET_SENSITIVITY, USER_EVENT, SETTINGS_EVENT, DOWNLOAD_EVENT
    }
}