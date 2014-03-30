package se.kth.csc.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueParameters {
    private final String title;

    @JsonCreator
    public QueueParameters(@JsonProperty("title") String title) {
        this.title = title;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }
}
