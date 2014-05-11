package se.kth.csc.auth;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!ldap")
public class IdNameService implements NameService {
    @Override
    public String nameUser(String userName) {
        return userName + " (Readable name)"; // Do nothing
    }
}
