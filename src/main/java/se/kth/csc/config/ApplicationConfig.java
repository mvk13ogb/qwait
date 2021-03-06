package se.kth.csc.config;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Controller;
import se.kth.csc.Application;

import javax.naming.ldap.LdapContext;

import static org.springframework.context.annotation.ComponentScan.Filter;

/**
 * Configures the whole application; scans for components in the entire package tree and sets up DI accordingly.
 * <p/>
 * This does NOT include set-up of web application components such as controllers.
 */
@Configuration
@ComponentScan(basePackageClasses = Application.class, excludeFilters = @Filter(Controller.class))
class ApplicationConfig {
    private static final Logger log = LoggerFactory.getLogger(ApplicationConfig.class);

    /**
     * The provider for placeholders that lets us use {@link org.springframework.beans.factory.annotation.Value}
     * annotations elsewhere in the application.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer(Environment environment) {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();

        // Load properties from "settings.properties"
        ppc.setLocation(new ClassPathResource("/settings.properties"));

        log.info("Creating placeholder configurer based on \"settings.properties\" file");
        return ppc;
    }

    @Bean
    public LdapContextSource ldapContextSource(
            @Value("${security.ldap.url}") String url,
            @Value("${security.ldap.base}") String base,
            @Value("${security.ldap.userDn}") String userDn,
            @Value("${security.ldap.password}") String password,
            @Value("${security.ldap.anonymousReadOnly}") boolean ro) {
        LdapContextSource ldapContextSource = new LdapContextSource();

        ldapContextSource.setUrl(url);
        ldapContextSource.setBase(base);
        ldapContextSource.setUserDn(userDn);
        ldapContextSource.setPassword(password);
        ldapContextSource.setAnonymousReadOnly(ro);

        return ldapContextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        LdapTemplate ldapTemplate = new LdapTemplate();

        ldapTemplate.setContextSource(ldapContextSource);

        return ldapTemplate;
    }

    /**
     * Provider for Jackson serialization
     */
    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        ObjectMapper result = new ObjectMapper();
        result.registerModule(new JodaModule());

        log.info("Creating object mapper with Joda support");
        return result;
    }
}
