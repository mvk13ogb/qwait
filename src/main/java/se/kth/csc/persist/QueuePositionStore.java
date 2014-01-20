package se.kth.csc.persist;

import se.kth.csc.model.QueuePosition;

public interface QueuePositionStore {

    public QueuePosition fetchQueuePositionWithId(int id);

    public void storeQueuePosition(QueuePosition queuePosition);

    public void removeQueuePosition(QueuePosition queuePosition);
}
