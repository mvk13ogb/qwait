package se.kth.csc.controller;

/*
 * #%L
 * QWait
 * %%
 * Copyright (C) 2013 - 2014 KTH School of Computer Science and Communication
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
    public void createQueue(String queueName, String title) {
        Queue queue = new Queue();
        queue.setName(queueName);
        queue.setTitle(title);
        queue.setHidden(false);
        queue.setLocked(false);
        queueStore.storeQueue(queue);

        messageBus.convertAndSend("/topic/queue", new QueueCreated(queueName));
    }

    @Override
    @PreAuthorize("hasRole('admin')")
    public void setAdmin(Account account, boolean admin) throws ForbiddenException {
        if (!admin && Iterables.size(findAccounts(true, null)) < 2) { // Trying to remove an admin and less than two admins left
            throw new ForbiddenException("Can not remove the last admin");
        } else {
            account.setAdmin(admin);

            messageBus.convertAndSend("/topic/user/" + account.getPrincipalName(),
                    new UserAdminStatusChanged(account.getPrincipalName(), admin));
        }
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeQueue(Queue queue) {
        QueueRemoved message = new QueueRemoved(queue.getName());

        for (Account owner : ImmutableSet.copyOf(queue.getOwners())) {
            removeOwner(queue, owner);
        }

        for (Account moderator : ImmutableSet.copyOf(queue.getModerators())) {
            removeModerator(queue, moderator);
        }

        for (QueuePosition queuePosition : queue.getPositions()) {
            removeQueuePosition(queuePosition);
        }

        queueStore.removeQueue(queue);

        messageBus.convertAndSend("/topic/queue", message);
    }

    @Override
    @PreAuthorize("!#queue.locked and !#queue.hidden and #account.principalName == authentication.name")
    public void addQueuePosition(Queue queue, Account account) {
        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setAccount(account);
        queuePosition.setReadableName(account.getName());
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
    public void removeQueuePosition(QueuePosition queuePosition) {
        QueuePositionRemoved message = new QueuePositionRemoved(queuePosition.getQueue().getName(),
                queuePosition.getAccount().getPrincipalName());

        messageBus.convertAndSend("/topic/queue/" + queuePosition.getQueue().getName(), message);
        queuePosition.getQueue().getPositions().remove(queuePosition);
        messageBus.convertAndSend("/topic/user/" + queuePosition.getAccount().getPrincipalName(), message);
        queuePosition.getAccount().getPositions().remove(queuePosition);

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
            removeQueuePosition(position);
        }
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void setHidden(Queue queue, boolean hidden) {
        queue.setHidden(hidden);
        setLocked(queue, hidden);
        if (hidden) {
            clearQueue(queue);
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
        owner.getOwnedQueues().add(queue);

        QueueOwnerAdded message = new QueueOwnerAdded(queue.getName(), owner.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + owner.getPrincipalName(), message);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeOwner(Queue queue, Account owner) {
        queue.getOwners().remove(owner);
        owner.getOwnedQueues().remove(queue);

        QueueOwnerRemoved message = new QueueOwnerRemoved(queue.getName(), owner.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + owner.getPrincipalName(), message);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void addModerator(Queue queue, Account moderator) {
        queue.getModerators().add(moderator);
        moderator.getModeratedQueues().add(queue);

        QueueModeratorAdded message = new QueueModeratorAdded(queue.getName(), moderator.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + moderator.getPrincipalName(), message);
    }

    @Override
    @PreAuthorize("hasRole('admin') or #queue.ownerNames.contains(authentication.name)")
    public void removeModerator(Queue queue, Account moderator) {
        queue.getModerators().remove(moderator);
        moderator.getModeratedQueues().remove(queue);

        QueueModeratorRemoved message = new QueueModeratorRemoved(queue.getName(), moderator.getPrincipalName());
        messageBus.convertAndSend("/topic/queue/" + queue.getName(), message);
        messageBus.convertAndSend("/topic/user/" + moderator.getPrincipalName(), message);
    }
}
