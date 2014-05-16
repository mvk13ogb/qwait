package se.kth.csc.payload.api;

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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import se.kth.csc.auth.Role;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;

public final class Snapshotters {
    private Snapshotters() {
        // Prevent instantiation
    }

    private static <A, B> ImmutableSet<B> transformSet(Iterable<A> iterable, Function<? super A, ? extends B> function) {
        return ImmutableSet.copyOf(Iterables.transform(iterable, function));
    }

    private static ImmutableSet<String> getRoles(Account account) {
        if (account.isAdmin()) {
            return ImmutableSet.of(Role.ADMIN.getAuthority(), Role.USER.getAuthority());
        } else {
            return ImmutableSet.of(Role.USER.getAuthority());
        }
    }

    public enum AccountSnapshotter implements Function<Account, AccountSnapshot> {
        INSTANCE;

        @Override
        public AccountSnapshot apply(Account account) {
            return account == null ? null : new AccountSnapshot(
                    account.getPrincipalName(), account.getName(), account.isAdmin(), false, getRoles(account),
                    transformSet(account.getPositions(), QueuePositionInAccountSnapshotter.INSTANCE),
                    transformSet(account.getOwnedQueues(), QueueNamer.INSTANCE),
                    transformSet(account.getModeratedQueues(), QueueNamer.INSTANCE));
        }
    }

    public enum AccountNamer implements Function<Account, String> {
        INSTANCE;

        @Override
        public String apply(Account account) {
            return account.getPrincipalName();
        }
    }

    public enum QueueSnapshotter implements Function<Queue, QueueSnapshot> {
        INSTANCE;

        @Override
        public QueueSnapshot apply(Queue queue) {
            return queue == null ? null : new QueueSnapshot(queue.getName(), queue.getTitle(), queue.isHidden(), queue.isLocked(),
                    transformSet(queue.getOwners(), AccountNamer.INSTANCE),
                    transformSet(queue.getModerators(), AccountNamer.INSTANCE),
                    transformSet(queue.getPositions(), QueuePositionInQueueSnapshotter.INSTANCE));
        }
    }

    public enum QueueNamer implements Function<Queue, String> {
        INSTANCE;

        @Override
        public String apply(Queue queue) {
            return queue.getName();
        }
    }

    public enum QueuePositionSnapshotter implements Function<QueuePosition, QueuePositionSnapshot> {
        INSTANCE;

        @Override
        public QueuePositionSnapshot apply(QueuePosition queuePosition) {
            return queuePosition == null ? null : new QueuePositionSnapshot(queuePosition.getStartTime(), queuePosition.getLocation(),
                    queuePosition.getComment(),
                    QueueNamer.INSTANCE.apply(queuePosition.getQueue()),
                    AccountNamer.INSTANCE.apply(queuePosition.getAccount()));
        }
    }

    public enum QueuePositionInQueueSnapshotter implements Function<QueuePosition, QueuePositionInQueueSnapshot> {
        INSTANCE;

        @Override
        public QueuePositionInQueueSnapshot apply(QueuePosition queuePosition) {
            return queuePosition == null ? null : new QueuePositionInQueueSnapshot(
                    queuePosition.getStartTime(), queuePosition.getLocation(),
                    queuePosition.getComment(), AccountNamer.INSTANCE.apply(queuePosition.getAccount()));
        }
    }

    public enum QueuePositionInAccountSnapshotter implements Function<QueuePosition, QueuePositionInAccountSnapshot> {
        INSTANCE;

        @Override
        public QueuePositionInAccountSnapshot apply(QueuePosition queuePosition) {
            return queuePosition == null ? null : new QueuePositionInAccountSnapshot(
                    queuePosition.getStartTime(), queuePosition.getLocation(),
                    queuePosition.getComment(), QueueNamer.INSTANCE.apply(queuePosition.getQueue()));
        }
    }
}
