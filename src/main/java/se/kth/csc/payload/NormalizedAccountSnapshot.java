package se.kth.csc.payload;

public class NormalizedAccountSnapshot {
    private final String name;
    private final String readableName;
    private final boolean admin;

    public NormalizedAccountSnapshot(String name, String readableName, boolean admin) {
        this.name = name;
        this.readableName = readableName;
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public String getReadableName() {
        return readableName;
    }

    public boolean isAdmin() {
        return admin;
    }
}
