package se.kth.csc.auth;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;

import java.util.Collection;

@Service
public class UserService implements AuthenticationUserDetailsService<Authentication> {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final AccountStore accountStore;
    private final NameService nameService;

    @Autowired
    public UserService(AccountStore accountStore,
                       NameService nameService) {
        this.accountStore = accountStore;
        this.nameService = nameService;
    }

    @Transactional
    @Override
    public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
        Account account = accountStore.fetchAccountWithPrincipalName(token.getName());

        if (account == null) {
            account = new Account();
            account.setPrincipalName(token.getName());
            for (GrantedAuthority grantedAuthority : token.getAuthorities()) {
                if (Role.ADMIN.getAuthority().equals(grantedAuthority.getAuthority())) {
                    account.setAdmin(true);
                    break;
                }
            }
            accountStore.storeAccount(account);

            log.info("Created user called \"{}\" with id {} and principal {}",
                    account.getName(), account.getId(), account.getPrincipalName());
        }
        String name = nameService.nameUser(token.getName());

        if (account.getName() == null || !account.getName().equals(name)) {
            account.setName(name);
            log.info("User with id {} and principal {} is now called \"{}\"",
                    account.getId(), account.getPrincipalName(), name);
        }

        return createUser(account);
    }

    private UserDetails createUser(Account account) {
        if (account.isAdmin()) {
            return new Details(account.getPrincipalName(), ImmutableSet.of(Role.USER, Role.ADMIN), account.getName());
        } else { // Regular user
            return new Details(account.getPrincipalName(), ImmutableSet.of(Role.USER), account.getName());
        }
    }

    public static class Details implements UserDetails, CredentialsContainer {
        private final String principalName;
        private final ImmutableSet<? extends GrantedAuthority> authorities;
        private final String name;

        public Details(String principalName, ImmutableSet<? extends GrantedAuthority> authorities, String name) {
            this.principalName = principalName;
            this.authorities = authorities;
            this.name = name;
        }

        @Override
        public void eraseCredentials() {

        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return null;
        }

        public String getName() {
            return name;
        }

        @Override
        public String getUsername() {
            return principalName;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
