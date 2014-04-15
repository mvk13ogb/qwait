package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.api.QueuePositionInQueueSnapshot;
import se.kth.csc.payload.api.Snapshotters;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueuePositionCreatedInQueue {
    private final QueuePositionInQueueSnapshot position;
    private final String queueName;

    public QueuePositionCreatedInQueue(QueuePosition queuePosition, String queueName) {
        this.position = Snapshotters.QueuePositionInQueueSnapshotter.INSTANCE.apply(queuePosition);
        this.queueName = queueName;
    }

    public QueuePositionInQueueSnapshot getQueuePosition() {
        return position;
    }
    public String getQueueName() {
        return queueName;
    }
}
