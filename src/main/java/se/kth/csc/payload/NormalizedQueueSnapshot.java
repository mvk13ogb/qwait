package se.kth.csc.payload;

public class NormalizedQueueSnapshot {
    private final String name;
    private final boolean active;
    private final boolean locked;

    public NormalizedQueueSnapshot(String name, boolean active, boolean locked) {
        this.name = name;
        this.active = active;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLocked() {
        return locked;
    }
}
