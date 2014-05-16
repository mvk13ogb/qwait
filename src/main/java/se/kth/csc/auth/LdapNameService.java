package se.kth.csc.auth;

/*
 * #%L
 * QWait
 * %%
 * Copyright (C) 2013 - 2014 KTH School of Computer Science and Communication
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
