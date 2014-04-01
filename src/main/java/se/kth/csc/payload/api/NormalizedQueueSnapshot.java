package se.kth.csc.payload.api;

public class NormalizedQueueSnapshot {
    private final String name;
    private final String title;
    private final boolean active;
    private final boolean locked;

    public NormalizedQueueSnapshot(String name, String title, boolean active, boolean locked) {
        this.name = name;
        this.title = title;
        this.active = active;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isLocked() {
        return locked;
    }
}
