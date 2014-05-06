package se.kth.csc.controller;

import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.api.Snapshotters;
import se.kth.csc.payload.message.*;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

import java.util.Iterator;

/**
 * An implementation of all the actions that can be performed via the API. These are collected into a single class to
 * make security easier to handle. All of the security lock-downs can happen in this single class.
 */
@Component
public class ApiProviderImpl implements ApiProvider {
    private final AccountStore accountStore;
    private final QueueStore queueStore;
    private final QueuePositionStore queuePositionStore;
    private final SimpMessagingTemplate messageBus;

    @Autowired
    public ApiProviderImpl(AccountStore accountStore, QueueStore queueStore, QueuePositionStore queuePositionStore,
                           SimpMessagingTemplate messageBus) {
        this.accountStore = accountStore;
        this.queueStore = queueStore;
        this.queuePositionStore = queuePositionStore;
        this.messageBus = messageBus;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Iterable<Account> findAccounts(boolean onlyAdmin, String query) {
        return accountStore.findAccounts(onlyAdmin, query);
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
    public Iterable<Queue> fetchAllQueues() {
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
        queue.setHidden(false);
        queue.setLocked(false);
        queue.getOwners().add(owner);
        queueStore.storeQueue(queue);

        messageBus.convertAndSend("/topic/queue", new QueueCreated(queueName));
        messageBus.convertAndSend("/topic/user/" + owner.getPrincipalName(),
                new QueueOwnerAdded(queueName, owner.getPrincipalName()));
    }

    @Override
    @PreAuthorize("hasRole('admin')")
    public void setAdmin(Account account, boolean admin) throws BadRequestException {
        // Make sure there is not only one admin left
        int adminCount = 0;
        if (admin == false) { // Only run if trying to remove an admin
            Iterator adminIterator = findAccounts(true, "").iterator();
            while (adminIterator.hasNext() && adminCount < 2) {
                adminIterator.next();
                adminCount++;
            }
        }
        if (admin == false && adminCount < 2) { // Trying to remove an admin and less than two admins left
            throw new BadRequestException("Can not remove the last admin!");
        } else {
            account.setAdmin(admin);

            messageBus.convertAndSend("/topic/user/" + account.getPrincipalName(),
                    new UserAdminStatusChanged(account.getPrincipalName(), admin));
        }
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void deleteQueue(Queue queue) {
        QueueRemoved message = new QueueRemoved(queue.getName());
        queueStore.removeQueue(queue);

        messageBus.convertAndSend("/topic/queue", message);
    }

    @Override
    @PreAuthorize("!#queue.locked and !#queue.hidden")
    public void addQueuePosition(Queue queue, Account account) {
        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setAccount(account);
        queuePosition.setStartTime(DateTime.now());

        queuePositionStore.storeQueuePosition(queuePosition);

        QueuePositionCreatedInQueue message1 = new QueuePositionCreatedInQueue(
                Snapshotters.QueuePositionInQueueSnapshotter.INSTANCE.apply(queuePosition), queue.getName());
        QueuePositionCreatedInAccount message2 = new QueuePositionCreatedInAccount(
                Snapshotters.QueuePositionInAccountSnapshotter.INSTANCE.apply(queuePosition), account.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message1);
        messageBus.convertAndSend("/topic/user/" + account.getPrincipalName(), message2);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queuePosition.queue.ownerNames.contains(authentication.name) or #queuePosition.queue.moderatorNames.contains(authentication.name) or #queuePosition.account.principalName == authentication.name")
    public void deleteQueuePosition(QueuePosition queuePosition) {
        QueuePositionRemoved message = new QueuePositionRemoved(queuePosition.getQueue().getName(),
                queuePosition.getAccount().getPrincipalName());

        messageBus.convertAndSend("/topic/queue/" + queuePosition.getQueue().getName(), message);
        messageBus.convertAndSend("/topic/user/" + queuePosition.getAccount().getPrincipalName(), message);
        queuePositionStore.removeQueuePosition(queuePosition);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queuePosition.account.principalName == authentication.name")
    public void setComment(QueuePosition queuePosition, String comment) throws BadRequestException {
        if (comment != null && comment.length() > 20) {
            throw new BadRequestException("Comment length cannot exceed 20 characters");
        } else {
            queuePosition.setComment(comment);
            QueuePositionCommentChanged message = new QueuePositionCommentChanged(queuePosition.getQueue().getName(),
                    queuePosition.getAccount().getPrincipalName(), comment);
            messageBus.convertAndSend("/topic/queue/" + queuePosition.getQueue().getName(), message);
            messageBus.convertAndSend("/topic/user/" + queuePosition.getAccount().getPrincipalName(), message);
        }
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queuePosition.account.principalName == authentication.name")
    public void setLocation(QueuePosition queuePosition, String location) throws BadRequestException {
        if (location != null && location.length() > 20) {
            throw new BadRequestException("Location length cannot exceed 20 characters");
        } else {
            queuePosition.setLocation(location);
            QueuePositionLocationChanged message = new QueuePositionLocationChanged(queuePosition.getQueue().getName(),
                    queuePosition.getAccount().getPrincipalName(), location);
            messageBus.convertAndSend("/topic/queue/" + queuePosition.getQueue().getName(), message);
            messageBus.convertAndSend("/topic/user/" + queuePosition.getAccount().getPrincipalName(), message);
        }
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name) or #queue.moderatorNames.contains(authentication.name)")
    public void clearQueue(Queue queue) {
        for (QueuePosition position : ImmutableSet.copyOf(queue.getPositions())) {
            queuePositionStore.removeQueuePosition(position);
        }
        queue.getPositions().clear();

        messageBus.convertAndSend("/topic/queue/" + queue.getName(), new QueueCleared(queue.getName()));
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name) or #queue.moderatorNames.contains(authentication.name)")
    public void setHidden(Queue queue, boolean hidden) {
        queue.setHidden(hidden);
        if (hidden) {
            clearQueue(queue);
            setLocked(queue, true);
        }
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), new QueueHiddenStatusChanged(queue.getName(), hidden));
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name) or #queue.moderatorNames.contains(authentication.name)")
    public void setLocked(Queue queue, boolean locked) {
        queue.setLocked(locked);

        messageBus.convertAndSend("/topic/queue/" + queue.getName(), new QueueLockedStatusChanged(queue.getName(), locked));
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void addOwner(Queue queue, Account owner) {
        queue.getOwners().add(owner);

        QueueOwnerAdded message = new QueueOwnerAdded(queue.getName(), owner.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + owner.getPrincipalName(), message);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeOwner(Queue queue, Account owner) {
        queue.getOwners().remove(owner);

        QueueOwnerRemoved message = new QueueOwnerRemoved(queue.getName(), owner.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + owner.getPrincipalName(), message);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void addModerator(Queue queue, Account moderator) {
        queue.getModerators().add(moderator);

        QueueModeratorAdded message = new QueueModeratorAdded(queue.getName(), moderator.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + moderator.getPrincipalName(), message);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeModerator(Queue queue, Account moderator) {
        queue.getModerators().remove(moderator);

        QueueModeratorRemoved message = new QueueModeratorRemoved(queue.getName(), moderator.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + moderator.getPrincipalName(), message);
    }
}
