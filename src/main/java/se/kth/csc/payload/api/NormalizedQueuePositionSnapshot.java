package se.kth.csc.payload.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.deser.DateTimeDeserializer;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import org.joda.time.DateTime;

public class NormalizedQueuePositionSnapshot {
    private final DateTime startTime;
    private final String location;
    private final String comment;

    public NormalizedQueuePositionSnapshot(DateTime startTime, String location, String comment) {
        this.startTime = startTime;
        this.location = location;
        this.comment = comment;
    }

    @JsonSerialize(using = DateTimeSerializer.class)
    @JsonDeserialize(using = DateTimeDeserializer.class)
    public DateTime getStartTime() {
        return startTime;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }
}
