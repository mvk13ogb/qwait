package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastMessage {
    private final String message;

    @JsonCreator
    public BroadcastMessage(@JsonProperty("message") String message) {
        this.message = message;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
}
