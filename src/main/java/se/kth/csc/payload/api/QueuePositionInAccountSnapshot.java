package se.kth.csc.payload.api;

import org.joda.time.DateTime;

public class QueuePositionInAccountSnapshot extends NormalizedQueuePositionSnapshot {
    private final String queueName;

    public QueuePositionInAccountSnapshot(DateTime startTime, String location, String comment, String queueName) {
        super(startTime, location, comment);
        this.queueName = queueName;
    }

    public String getUserName() {
        return queueName;
    }
}
