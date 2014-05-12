package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.kth.csc.model.QueuePosition;

import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueCleared {
    private final String name;
    private Set<QueuePosition> positions;

    public QueueCleared(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
