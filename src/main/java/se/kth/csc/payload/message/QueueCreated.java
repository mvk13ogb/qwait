package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.kth.csc.payload.api.QueueSnapshot;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueCreated {
    private final String name;

    public QueueCreated(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
