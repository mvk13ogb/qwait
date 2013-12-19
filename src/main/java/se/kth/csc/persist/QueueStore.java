package se.kth.csc.persist;

import se.kth.csc.model.Queue;

import java.util.List;

public interface QueueStore {

    public Queue fetchQueueWithId(int id);

    public List<Queue> fetchAllQueues();

    public void storeQueue(Queue queue);

    public void removeQueue(Queue queue);
}
