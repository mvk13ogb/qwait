package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueHiddenStatusChanged {
    private final String name;
    private final boolean hidden;

    public QueueHiddenStatusChanged(String name, boolean hidden) {
        this.name = name;
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public boolean isHidden() {
        return hidden;
    }
}
