package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.kth.csc.payload.api.AccountSnapshot;
import se.kth.csc.payload.api.QueueSnapshot;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueModeratorAdded {
    private final String queueName;
    private final String userName;

    public QueueModeratorAdded(String queueName, String userName) {
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
