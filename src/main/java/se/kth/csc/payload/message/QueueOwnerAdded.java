package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueueOwnerAdded {
    private final String queueName;
    private final String userName;

    public QueueOwnerAdded(String queueName, String userName) {
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
