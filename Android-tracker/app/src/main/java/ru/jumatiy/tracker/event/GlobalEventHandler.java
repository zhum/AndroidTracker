package ru.jumatiy.tracker.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 23.09.13 9:00.
 */
public class GlobalEventHandler {

    private static GlobalEventHandler instance;
    private Map<Integer, List<EventListener>> eventListeners = new HashMap<Integer, List<EventListener>>();

    public static void initInstance() {
        instance = new GlobalEventHandler();
    }

    public static GlobalEventHandler getInstance() {
        return instance;
    }

    public void addListener(int id, EventListener listener) {
        List<EventListener> listeners;
        if(eventListeners.containsKey(id) && eventListeners.get(id) != null) {
            listeners = eventListeners.get(id);
        } else {
            listeners = new ArrayList<EventListener>();
            eventListeners.put(id, listeners);
        }

        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void addEventListeners(EventListener listener, int... ids) {
        for (int id : ids) {
            addListener(id, listener);
        }
    }

    public void removeEventListener(int id, EventListener listener) {
        List<EventListener> listeners;
        if(eventListeners.containsKey(id) && eventListeners.get(id) != null) {
            listeners = eventListeners.get(id);
            if(listeners.contains(listener)) {
                listeners.remove(listener);
            }
        }
    }

    public void removeListeners(EventListener listener, int... ids) {
        for(int id : ids) {
            removeEventListener(id, listener);
        }
    }

    public void removeAllOccurency(EventListener listener) {
        for (Integer key : eventListeners.keySet()) {
            List<EventListener> list = eventListeners.get(key);
            if(list.contains(listener)) {
                list.remove(listener);
            }
        }
    }

    public void dispatchEvent(Event event) {
        int eventId = event.getId();
        if(eventListeners.containsKey(eventId) && eventListeners.get(eventId) != null) {
            List<EventListener> listeners = eventListeners.get(eventId);
            for(EventListener listener : listeners) {
                if(listener != null) {
                    listener.onEvent(event);
                }
            }
        }
    }

    public void dispatchEvent(int eventId) {
        dispatchEvent(new Event(eventId));
    }

}
