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

import org.jasig.cas.client.util.CommonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FilteredCasAuthEntryPoint implements AuthenticationEntryPoint, InitializingBean {

    private ServiceProperties serviceProperties;

    private String loginUrl;

    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(this.loginUrl, "loginUrl must be specified");
        Assert.notNull(this.serviceProperties, "serviceProperties must be specified");
    }

    public final void commence(final HttpServletRequest servletRequest, final HttpServletResponse response,
                               final AuthenticationException authenticationException) throws IOException, ServletException {

        final String urlEncodedService = CommonUtils.constructServiceUrl(
                null, response, this.serviceProperties.getService(), null, this.serviceProperties.getArtifactParameter(), true);
        final String redirectUrl = CommonUtils.constructRedirectUrl(
                this.loginUrl, this.serviceProperties.getServiceParameter(), urlEncodedService, this.serviceProperties.isSendRenew(), false);

        String accept = servletRequest.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            response.sendRedirect(redirectUrl);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access to this resource requires authentication");
        }
    }

    public final String getLoginUrl() {
        return this.loginUrl;
    }

    public final ServiceProperties getServiceProperties() {
        return this.serviceProperties;
    }

    public final void setLoginUrl(final String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public final void setServiceProperties(final ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }
}
