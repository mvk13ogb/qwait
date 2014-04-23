package se.kth.csc.payload.api;

import org.joda.time.DateTime;

public class QueuePositionInQueueSnapshot extends NormalizedQueuePositionSnapshot {
    private final String userName;

    public QueuePositionInQueueSnapshot(DateTime startTime, String location, String comment, String userName) {
        super(startTime, location, comment);
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
