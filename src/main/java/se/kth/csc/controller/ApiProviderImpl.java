package se.kth.csc.controller;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

import java.util.List;

/**
 * An implementation of all the actions that can be performed via the API. These are collected into a single class to
 * make security easier to handle. All of the security lock-downs can happen in this single class.
 */
@Component
public class ApiProviderImpl implements ApiProvider {
    private final AccountStore accountStore;
    private final QueueStore queueStore;
    private final QueuePositionStore queuePositionStore;

    @Autowired
    public ApiProviderImpl(AccountStore accountStore, QueueStore queueStore, QueuePositionStore queuePositionStore) {
        this.accountStore = accountStore;
        this.queueStore = queueStore;
        this.queuePositionStore = queuePositionStore;
    }

    @Override
    public Account fetchAccount(String userName) throws NotFoundException {
        return accountStore.fetchAccountWithPrincipalName(userName);
    }

    @Override
    public Queue fetchQueue(String queueName) throws NotFoundException {
        return queueStore.fetchQueueWithName(queueName);
    }

    @Override
    public List<Queue> fetchAllQueues() {
        return queueStore.fetchAllQueues();
    }

    @Override
    public QueuePosition fetchQueuePosition(String queueName, String userName) {
        return queuePositionStore.fetchQueuePositionWithQueueAndUser(queueName, userName);
    }

    @Override
    @PreAuthorize("hasRole('admin')")
    public void createQueue(String queueName, Account owner, String title) {
        Queue queue = new Queue();
        queue.setName(queueName);
        queue.setTitle(title);
        queue.setActive(true);
        queue.setLocked(false);
        queue.getOwners().add(owner);
        queueStore.storeQueue(queue);
    }

    @Override
    @PreAuthorize("hasRole('admin')")
    public void setAdmin(Account account, boolean admin) {
        account.setAdmin(admin);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void deleteQueue(Queue queue) {
        queueStore.removeQueue(queue);
    }

    @Override
    @PreAuthorize("!#queue.locked and #queue.active")
    public void addQueuePosition(Queue queue, Account account) {
        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setAccount(account);
        queuePosition.setStartTime(DateTime.now());

        queuePositionStore.storeQueuePosition(queuePosition);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name) or #queuePosition.queue.moderatorNames.contains(authentication.name) or #queuePosition.owner.principalName == authentication.name")
    public void deleteQueuePosition(QueuePosition queuePosition) {
        queuePositionStore.removeQueuePosition(queuePosition);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queuePosition.account.principalName == authentication.name")
    public void setComment(QueuePosition queuePosition, String comment) {
        queuePosition.setComment(comment);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queuePosition.account.principalName == authentication.name")
    public void setLocation(QueuePosition queuePosition, String location) {
        queuePosition.setComment(location);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name) or #queue.moderatorNames.contains(authentication.name)")
    public void clearQueue(Queue queue) {
        queue.getPositions().clear();
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name) or #queue.moderatorNames.contains(authentication.name)")
    public void setActive(Queue queue, boolean active) {
        queue.setActive(active);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name) or #queue.moderatorNames.contains(authentication.name)")
    public void setLocked(Queue queue, boolean locked) {
        queue.setLocked(locked);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void addOwner(Queue queue, Account owner) {
        queue.getOwners().add(owner);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeOwner(Queue queue, Account owner) {
        queue.getOwners().remove(owner);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void addModerator(Queue queue, Account owner) {
        queue.getModerators().add(owner);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeModerator(Queue queue, Account owner) {
        queue.getModerators().remove(owner);
    }
}
