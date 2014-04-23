package se.kth.csc.payload.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public class QueuePositionCommentChanged {
    private final String queueName;
    private final String userName;
    private final String comment;

    public QueuePositionCommentChanged(String queueName, String userName, String comment) {
        this.queueName = queueName;
        this.userName = userName;
        this.comment = comment;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getUserName() {
        return userName;
    }

    public String getComment() {
        return comment;
    }
}
