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
import se.kth.csc.payload.api.NormalizedAccountSnapshot;
import se.kth.csc.payload.api.NormalizedQueuePositionSnapshot;
import se.kth.csc.payload.api.NormalizedQueueSnapshot;

public class QueueSnapshot extends NormalizedQueueSnapshot {
    private final ImmutableSet<String> owners;
    private final ImmutableSet<String> moderators;
    private final ImmutableSet<QueuePositionInQueueSnapshot> positions;

    public QueueSnapshot(String name, String title, boolean hidden, boolean locked,
                         ImmutableSet<String> owners,
                         ImmutableSet<String> moderators,
                         ImmutableSet<QueuePositionInQueueSnapshot> positions) {
        super(name, title, hidden, locked);
        this.owners = owners;
        this.moderators = moderators;
        this.positions = positions;
    }

    public ImmutableSet<String> getOwners() {
        return owners;
    }

    public ImmutableSet<String> getModerators() {
        return moderators;
    }

    public ImmutableSet<QueuePositionInQueueSnapshot> getPositions() {
        return positions;
    }
}
