package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueLockedStatusChanged {
    private final String name;
    private final boolean locked;

    public QueueLockedStatusChanged(String name, boolean locked) {
        this.name = name;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return locked;
    }
}
