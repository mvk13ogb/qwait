package se.kth.csc.config;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;

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
    public CasAuthenticationEntryPoint casAuthenticationEntryPoint(@Value("${security.cas.loginUrl}") String loginUrl,
                                                                   ServiceProperties serviceProperties) {
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();

        casAuthenticationEntryPoint.setLoginUrl(loginUrl);
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties);

        log.info("Creating CAS authentication entry point with login url {}", loginUrl);
        return casAuthenticationEntryPoint;
    }

    @Autowired
    @Bean(name = "casAuthenticationProvider")
    public CasAuthenticationProvider casAuthenticationProvider(
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
