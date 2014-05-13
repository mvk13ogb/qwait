package se.kth.csc.controller;

import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;

public interface ApiProvider {
    Iterable<Account> findAccounts(boolean onlyAdmin, String query);

    Account fetchAccount(String userName) throws NotFoundException;

    Queue fetchQueue(String queueName) throws NotFoundException;

    Iterable<Queue> fetchAllQueues();

    QueuePosition fetchQueuePosition(String queueName, String userName);

    void createQueue(String queueName, String title);

    void setAdmin(Account account, boolean admin) throws ForbiddenException;

    void deleteQueue(Queue queue);

    void addQueuePosition(Queue queue, Account account);

    void deleteQueuePosition(QueuePosition queuePosition);

    void setComment(QueuePosition queuePosition, String comment) throws BadRequestException;

    void setLocation(QueuePosition queuePosition, String location) throws BadRequestException;

    void clearQueue(Queue queue);

    void setHidden(Queue queue, boolean hidden);

    void setLocked(Queue queue, boolean locked);

    void addOwner(Queue queue, Account owner);

    void removeOwner(Queue queue, Account owner);

    void addModerator(Queue queue, Account owner);

    void removeModerator(Queue queue, Account owner);
}
