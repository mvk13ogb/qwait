package se.kth.csc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Configuration
@Profile("test")
public class MockAuthConfig {
    @Bean
    @Autowired
    public AuthenticationProvider authenticationProvider(
            final AuthenticationUserDetailsService<Authentication> authenticationUserDetailsService) {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
                final UserDetails userDetails = authenticationUserDetailsService.loadUserDetails(authentication);
                return new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return userDetails.getAuthorities();
                    }

                    @Override
                    public Object getCredentials() {
                        return authentication.getCredentials();
                    }

                    @Override
                    public Object getDetails() {
                        return authentication.getDetails();
                    }

                    public UserDetails getUserDetails() {
                        return userDetails;
                    }

                    @Override
                    public Object getPrincipal() {
                        return userDetails;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return authentication.isAuthenticated();
                    }

                    @Override
                    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                        authentication.setAuthenticated(isAuthenticated);
                    }

                    @Override
                    public String getName() {
                        return authentication.getName();
                    }
                };
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return true;
            }
        };
    }
}
