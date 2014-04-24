package se.kth.csc.controller;

import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.api.AccountSnapshot;

import java.util.List;

public interface ApiProvider {
    Iterable<Account> findAccounts(boolean onlyAdmin, String query);

    Account fetchAccount(String userName) throws NotFoundException;

    Queue fetchQueue(String queueName) throws NotFoundException;

    Iterable<Queue> fetchAllQueues();

    QueuePosition fetchQueuePosition(String queueName, String userName);

    void createQueue(String queueName, Account owner, String title);

    void setAdmin(Account account, boolean admin);

    void deleteQueue(Queue queue);

    void addQueuePosition(Queue queue, Account account);

    void deleteQueuePosition(QueuePosition queuePosition);

    void setComment(QueuePosition queuePosition, String comment) throws TextLimitExceededException;

    void setLocation(QueuePosition queuePosition, String location);

    void clearQueue(Queue queue);

    void setActive(Queue queue, boolean active);

    void setLocked(Queue queue, boolean locked);

    void addOwner(Queue queue, Account owner);

    void removeOwner(Queue queue, Account owner);

    void addModerator(Queue queue, Account owner);

    void removeModerator(Queue queue, Account owner);
}
