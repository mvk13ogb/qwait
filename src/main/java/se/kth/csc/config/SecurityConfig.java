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

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import se.kth.csc.auth.FilteredCasAuthEntryPoint;

@Configuration
@ImportResource("classpath:spring-security-context.xml")
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public ServiceProperties serviceProperties(@Value("${security.cas.service}") String service) {
        ServiceProperties serviceProperties = new ServiceProperties();

        serviceProperties.setService(service);
        serviceProperties.setSendRenew(false);

        log.info("Creating CAS service properties with service \"{}\" and no renewal requirement", service);
        return serviceProperties;
    }

    @Autowired
    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager authenticationManager) {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();

        casAuthenticationFilter.setAuthenticationManager(authenticationManager);
        casAuthenticationFilter.setFilterProcessesUrl("/authenticate");

        log.info("Creating CAS authentication filter with process URL \"/authenticate\"");
        return casAuthenticationFilter;
    }

    @Autowired
    @Bean
    public AuthenticationEntryPoint casAuthenticationEntryPoint(@Value("${security.cas.loginUrl}") String loginUrl,
                                                                ServiceProperties serviceProperties) {
        FilteredCasAuthEntryPoint casAuthenticationEntryPoint = new FilteredCasAuthEntryPoint();

        casAuthenticationEntryPoint.setLoginUrl(loginUrl);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties);

        log.info("Creating CAS authentication entry point with login url {}", loginUrl);
        return casAuthenticationEntryPoint;
    }

    @Autowired
    @Bean(name = "authenticationProvider")
    @Profile("default")
    public CasAuthenticationProvider authenticationProvider(
            AuthenticationUserDetailsService authenticationUserDetailsService,
            ServiceProperties serviceProperties,
            @Value("${security.cas.ticketValidator}") String ticketValidator,
            @Value("${security.cas.authProviderKey}") String authProviderKey) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();

        casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService);
        casAuthenticationProvider.setServiceProperties(serviceProperties);
        casAuthenticationProvider.setTicketValidator(new Cas20ServiceTicketValidator(ticketValidator));
        casAuthenticationProvider.setKey(authProviderKey);

        log.info("Creating CAS authentication provider using {} as the ticket validator and a secret provider key",
                ticketValidator);
        return casAuthenticationProvider;
    }
}
