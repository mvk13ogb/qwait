package se.kth.csc.auth;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;

@Service
public class UserService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final GrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("user");
    private static final GrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("admin");
    private final AccountStore accountStore;

    @Autowired
    public UserService(AccountStore accountStore) {
        this.accountStore = accountStore;
    }

    @Transactional
    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        Account account = accountStore.fetchAccountWithPrincipalName(token.getName());

        if (account == null) {
            account = new Account();
            account.setPrincipalName(token.getName());
            account.setName(token.getName());
            accountStore.storeAccount(account);

            log.info("Created user called \"{}\" with id {}", account.getName(), account.getId());
        }

        return createUser(account);
    }

    private UserDetails createUser(Account account) {
        return new User(account.getPrincipalName(), "",
                account.isAdmin() ? ImmutableSet.of(USER_AUTHORITY, ADMIN_AUTHORITY) : ImmutableSet.of(USER_AUTHORITY));
    }
}
