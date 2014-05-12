package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.kth.csc.model.QueuePosition;

import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueCleared {
    private final String name;
    private Set<QueuePosition> positions;

    public QueueCleared(String name, Set<QueuePosition> positions) {
        this.name = name;
        this.positions = positions;
    }

    public String getName() {
        return name;
    }

    public Set<QueuePosition> getPositions() {
      return positions;
    }
}
