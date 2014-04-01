package se.kth.csc.payload.api;

import com.google.common.collect.ImmutableSet;
import se.kth.csc.payload.api.NormalizedAccountSnapshot;
import se.kth.csc.payload.api.NormalizedQueuePositionSnapshot;
import se.kth.csc.payload.api.NormalizedQueueSnapshot;

public class QueueSnapshot extends NormalizedQueueSnapshot {
    private final ImmutableSet<String> owners;
    private final ImmutableSet<String> moderators;
    private final ImmutableSet<QueuePositionInQueueSnapshot> positions;

    public QueueSnapshot(String name, String title, boolean active, boolean locked,
                         ImmutableSet<String> owners,
                         ImmutableSet<String> moderators,
                         ImmutableSet<QueuePositionInQueueSnapshot> positions) {
        super(name, title, active, locked);
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
