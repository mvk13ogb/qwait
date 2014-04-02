package se.kth.csc.config;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.inject.Inject;
import java.util.List;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public abstract class WebSecurityConfigurationAware extends WebAppConfigurationAware {

    @Inject
    private FilterChainProxy springSecurityFilterChain;

    @Before
    public void before() {
        this.mockMvc = webAppContextSetup(this.wac)
                .addFilters(this.springSecurityFilterChain).build();
    }

    public MockHttpSession signInAs(final String principalName, final String... roles) {
        MockHttpSession session = new MockHttpSession();

        final List<GrantedAuthority> authorities = Lists.newArrayList();

        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new MockSecurityContext(new AbstractAuthenticationToken(authorities) {
                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getPrincipal() {
                        return principalName;
                    }
                })
        );

        return session;
    }

    public class MockSecurityContext implements SecurityContext {
        private Authentication authentication;

        public MockSecurityContext(Authentication authentication) {
            this.authentication = authentication;
        }

        @Override
        public Authentication getAuthentication() {
            return authentication;
        }

        @Override
        public void setAuthentication(Authentication authentication) {
            this.authentication = authentication;
        }
    }
}
