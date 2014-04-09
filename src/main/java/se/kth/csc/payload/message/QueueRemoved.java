package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueRemoved {
    private final String name;

    public QueueRemoved(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
