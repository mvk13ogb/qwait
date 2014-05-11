package se.kth.csc.payload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Location {
    private final String location;

    @JsonCreator
    public Location(@JsonProperty("location") String location) {
        this.location = location;
    }

    @JsonProperty("location")
    public String getLocation() {
        return location;
    }
}
