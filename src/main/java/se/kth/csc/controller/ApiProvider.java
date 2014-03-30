package se.kth.csc.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;

import java.util.List;

public interface ApiProvider {
    Account fetchAccount(String userName) throws NotFoundException;

    Queue fetchQueue(String queueName) throws NotFoundException;

    List<Queue> fetchAllQueues();

    QueuePosition fetchQueuePosition(String queueName, String userName);

    @PreAuthorize("hasRole('admin')")
    void createQueue(String queueName, Account owner, String title);

    @PreAuthorize("hasRole('admin')")
    void setAdmin(Account account, boolean admin);

    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    void deleteQueue(Queue queue);

    @PreAuthorize("!#queue.locked and #queue.active")
    void addQueuePosition(Queue queue, Account account);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name) or #queuePosition.queue.moderatorNames.contains(authentication.name) or #queuePosition.owner.principalName == authentication.name")
    void deleteQueuePosition(QueuePosition queuePosition);

    @PreAuthorize("hasRole('admin') or #queuePosition.owner.principalName == authentication.name")
    void setComment(QueuePosition queuePosition, String comment);

    @PreAuthorize("hasRole('admin') or #queuePosition.owner.principalName == authentication.name")
    void setLocation(QueuePosition queuePosition, String location);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name) or #queuePosition.queue.moderatorNames.contains(authentication.name)")
    void clearQueue(Queue queue);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name) or #queuePosition.queue.moderatorNames.contains(authentication.name)")
    void setActive(Queue queue, boolean active);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name) or #queuePosition.queue.moderatorNames.contains(authentication.name)")
    void setLocked(Queue queue, boolean locked);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name)")
    void addOwner(Queue queue, Account owner);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name)")
    void removeOwner(Queue queue, Account owner);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name)")
    void addModerator(Queue queue, Account owner);

    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name)")
    void removeModerator(Queue queue, Account owner);
}
