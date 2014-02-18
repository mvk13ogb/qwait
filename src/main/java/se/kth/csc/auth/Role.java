package se.kth.csc.auth;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER("user"), ADMIN("admin"), SUPER_ADMIN("super_admin"), QUEUE_OWNER("queue_owner");
    private final String authority;

    private Role(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
