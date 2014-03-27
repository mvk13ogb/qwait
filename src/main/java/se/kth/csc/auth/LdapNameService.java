package se.kth.csc.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;

@Service
@Profile("ldap")
public class LdapNameService implements NameService {
    private final LdapOperations ldapOperations;

    @Autowired
    public LdapNameService(LdapOperations ldapOperations) {
        this.ldapOperations = ldapOperations;
    }

    @Override
    public String nameUser(String userName) {
        List<String> names = ldapOperations.search(
                LdapQueryBuilder.query().where("ugKthid").is(userName),
                new AttributesMapper<String>() {
                    @Override
                    public String mapFromAttributes(Attributes attributes) throws NamingException {
                        return attributes.get("cn").get().toString();
                    }
                });

        if (names.isEmpty()) {
            throw new IllegalArgumentException("No user with user name " + userName);
        } else {
            return names.get(0);
        }
    }
}
