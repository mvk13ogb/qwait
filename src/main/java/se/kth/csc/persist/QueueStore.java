package se.kth.csc.persist;

import se.kth.csc.model.Queue;
import se.kth.csc.model.Account;

import java.util.List;

public interface QueueStore {

    public Queue fetchQueueWithName(String name);

    public List<Queue> fetchAllQueues();

    public void storeQueue(Queue queue);

    public void removeQueue(Queue queue);
}
