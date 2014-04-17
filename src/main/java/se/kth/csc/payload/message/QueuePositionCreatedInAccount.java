package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.api.QueuePositionInAccountSnapshot;
import se.kth.csc.payload.api.Snapshotters;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueuePositionCreatedInAccount {
    private final QueuePositionInAccountSnapshot position;
    private final String queueName;

    public QueuePositionCreatedInAccount(QueuePositionInAccountSnapshot queuePosition, String queueName) {
        this.position = queuePosition;
        this.queueName = queueName;
    }

    public QueuePositionInAccountSnapshot getQueuePosition() {
        return position;
    }
    public String getQueueName() {
        return queueName;
    }
}
