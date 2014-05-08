package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueuePositionLocationChanged {
    private final String queueName;
    private final String userName;
    private final String location;

    public QueuePositionLocationChanged(String queueName, String userName, String location) {
        this.queueName = queueName;
        this.userName = userName;
        this.location = location;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getUserName() {
        return userName;
    }

    public String getLocation() {
        return location;
    }
}
