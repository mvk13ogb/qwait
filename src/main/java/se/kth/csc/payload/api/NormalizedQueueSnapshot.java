package se.kth.csc.payload.api;

public class NormalizedQueueSnapshot {
    private final String name;
    private final String title;
    private final boolean hidden;
    private final boolean locked;

    public NormalizedQueueSnapshot(String name, String title, boolean hidden, boolean locked) {
        this.name = name;
        this.title = title;
        this.hidden = hidden;
        this.locked = locked;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isLocked() {
        return locked;
    }
}
