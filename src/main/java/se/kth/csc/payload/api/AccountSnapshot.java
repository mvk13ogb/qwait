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

import com.google.common.collect.ImmutableSet;

public class AccountSnapshot extends NormalizedAccountSnapshot {
    private final ImmutableSet<QueuePositionInAccountSnapshot> queuePositions;
    private final ImmutableSet<String> ownedQueues;
    private final ImmutableSet<String> moderatedQueues;

    public AccountSnapshot(String name, String readableName, boolean admin, boolean anonymous,
                           ImmutableSet<String> roles,
                           ImmutableSet<QueuePositionInAccountSnapshot> queuePositions,
                           ImmutableSet<String> ownedQueues,
                           ImmutableSet<String> moderatedQueues) {
        super(name, readableName, admin, anonymous, roles);
        this.queuePositions = queuePositions;
        this.ownedQueues = ownedQueues;
        this.moderatedQueues = moderatedQueues;
    }

    public ImmutableSet<QueuePositionInAccountSnapshot> getQueuePositions() {
        return queuePositions;
    }

    public ImmutableSet<String> getOwnedQueues() {
        return ownedQueues;
    }

    public ImmutableSet<String> getModeratedQueues() {
        return moderatedQueues;
    }
}
