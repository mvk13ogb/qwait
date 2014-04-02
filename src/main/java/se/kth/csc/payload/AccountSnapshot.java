package se.kth.csc.payload;

import com.google.common.collect.ImmutableSet;

public class AccountSnapshot extends NormalizedAccountSnapshot {
    private final ImmutableSet<NormalizedQueuePositionSnapshot> queuePositions;
    private final ImmutableSet<NormalizedQueueSnapshot> ownedQueues;
    private final ImmutableSet<NormalizedQueueSnapshot> moderatedQueues;

    public AccountSnapshot(String name, String readableName, boolean admin,
                           ImmutableSet<NormalizedQueuePositionSnapshot> queuePositions,
                           ImmutableSet<NormalizedQueueSnapshot> ownedQueues,
                           ImmutableSet<NormalizedQueueSnapshot> moderatedQueues) {
        super(name, readableName, admin);
        this.queuePositions = queuePositions;
        this.ownedQueues = ownedQueues;
        this.moderatedQueues = moderatedQueues;
    }

    public ImmutableSet<NormalizedQueuePositionSnapshot> getQueuePositions() {
        return queuePositions;
    }

    public ImmutableSet<NormalizedQueueSnapshot> getOwnedQueues() {
        return ownedQueues;
    }

    public ImmutableSet<NormalizedQueueSnapshot> getModeratedQueues() {
        return moderatedQueues;
    }
}
