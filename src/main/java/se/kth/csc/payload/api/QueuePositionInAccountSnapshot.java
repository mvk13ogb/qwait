package se.kth.csc.payload.api;

import org.joda.time.DateTime;

public class QueuePositionInAccountSnapshot extends NormalizedQueuePositionSnapshot {
    private final String userName;

    public QueuePositionInAccountSnapshot(DateTime startTime, String location, String comment, String userName) {
        super(startTime, location, comment);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
