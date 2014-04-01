package se.kth.csc.payload.api;

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
