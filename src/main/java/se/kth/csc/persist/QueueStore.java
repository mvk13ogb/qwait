package se.kth.csc.persist;

import se.kth.csc.model.Queue;
import se.kth.csc.model.Account;

import java.util.List;

public interface QueueStore {

    public Queue fetchQueueWithId(int id);

    public Queue fetchQueueWithName(String name);

    public List<Queue> fetchAllQueues();

    public List<String> fetchAllQueueNames();

    public List<Queue> fetchAllActiveQueues();

    public List<Queue> fetchAllModeratedQueues(Account account);

    public List<Queue> fetchAllOwnedQueues(Account account);

    public void storeQueue(Queue queue);

    public void removeQueue(Queue queue);
}
