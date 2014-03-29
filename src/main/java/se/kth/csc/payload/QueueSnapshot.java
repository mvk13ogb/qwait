package se.kth.csc.payload;

import com.google.common.collect.ImmutableSet;

public class QueueSnapshot extends NormalizedQueueSnapshot {
    private final ImmutableSet<NormalizedAccountSnapshot> owners;
    private final ImmutableSet<NormalizedAccountSnapshot> moderators;
    private final ImmutableSet<NormalizedQueuePositionSnapshot> positions;

    public QueueSnapshot(String name, boolean active, boolean locked,
                         ImmutableSet<NormalizedAccountSnapshot> owners,
                         ImmutableSet<NormalizedAccountSnapshot> moderators,
                         ImmutableSet<NormalizedQueuePositionSnapshot> positions) {
        super(name, active, locked);
        this.owners = owners;
        this.moderators = moderators;
        this.positions = positions;
    }

    public ImmutableSet<NormalizedAccountSnapshot> getOwners() {
        return owners;
    }

    public ImmutableSet<NormalizedAccountSnapshot> getModerators() {
        return moderators;
    }

    public ImmutableSet<NormalizedQueuePositionSnapshot> getPositions() {
        return positions;
    }
}
