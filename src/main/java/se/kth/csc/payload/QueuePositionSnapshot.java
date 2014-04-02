package se.kth.csc.payload;

import org.joda.time.DateTime;

public class QueuePositionSnapshot extends NormalizedQueuePositionSnapshot {
    private final NormalizedQueueSnapshot queue;
    private final NormalizedAccountSnapshot user;

    public QueuePositionSnapshot(DateTime startTime, String location, String comment, NormalizedQueueSnapshot queue,
                                 NormalizedAccountSnapshot user) {
        super(startTime, location, comment);
        this.queue = queue;
        this.user = user;
    }

    public NormalizedQueueSnapshot getQueue() {
        return queue;
    }

    public NormalizedAccountSnapshot getUser() {
        return user;
    }
}
