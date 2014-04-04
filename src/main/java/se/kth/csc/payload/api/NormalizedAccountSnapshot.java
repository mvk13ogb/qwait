package se.kth.csc.payload.api;

import com.google.common.collect.ImmutableSet;

public class NormalizedAccountSnapshot {
    private final String name;
    private final String readableName;
    private final boolean admin;
    private final boolean anonymous;
    private final ImmutableSet<String> roles;

    public NormalizedAccountSnapshot(String name, String readableName, boolean admin, boolean anonymous,
                                     ImmutableSet<String> roles) {
        this.name = name;
        this.readableName = readableName;
        this.admin = admin;
        this.anonymous = anonymous;
        this.roles = roles;
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

    public boolean isAnonymous() {
        return anonymous;
    }

    public ImmutableSet<String> getRoles() {
        return roles;
    }
}
