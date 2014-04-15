package se.kth.csc.payload.api;

import org.joda.time.DateTime;
import se.kth.csc.payload.api.NormalizedAccountSnapshot;
import se.kth.csc.payload.api.NormalizedQueuePositionSnapshot;
import se.kth.csc.payload.api.NormalizedQueueSnapshot;

public class QueuePositionSnapshot extends NormalizedQueuePositionSnapshot {
    private final String queueName;
    private final String userName;

    public QueuePositionSnapshot(DateTime startTime, String location, String comment, String queueName, String userName) {
        super(startTime, location, comment);
        this.queueName = queueName;
        this.userName = userName;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getUserName() {
        return userName;
    }
}
