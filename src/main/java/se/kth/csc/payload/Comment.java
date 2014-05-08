package se.kth.csc.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment {
    private final String comment;

    @JsonCreator
    public Comment(@JsonProperty("comment") String comment) {
        this.comment = comment;
    }

    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }
}
