package ru.jumatiy.trackersupervisor.event;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 23.09.13 13:27.
 */
public class Event {

    private int id;
    private Object data;

    public Event(int id) {
        this.id = id;
    }

    public Event(int id, Object data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public Object getData() {
        return data;
    }
}
