package se.kth.csc.controller;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.persist.AccountStore;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Controls the home page.
 */
@Controller
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final AccountStore accountStore;

    protected HomeController() {
        // Needed for injection
        accountStore = null;
    }

    @Autowired
    public HomeController(AccountStore accountStore) {
        this.accountStore = accountStore;
    }

    /**
     * The welcome page of the web application
     */
    @Order(Ordered.LOWEST_PRECEDENCE - 1000)
    @RequestMapping(value = {"/", "/about", "/help", "/queue/**", "/admin"}, method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request) {

        String hostname;
        try {
            hostname = InetAddress.getByName(request.getRemoteHost()).getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error("Hostname error", e);
            hostname = null;
        }

        return new ModelAndView("index", "hostname", hostname);
    }

    // Used to enforce authentication when logging in from welcome page
    @RequestMapping(value = "/login", method = {RequestMethod.GET})
    public String login(@RequestParam("target") String target) {
        return "redirect:" + target;
    }
}
