package se.kth.csc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.servlet.ModelAndView;
import com.google.common.collect.ImmutableMap;

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
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(HttpServletRequest request) {

        String hostName = "";
        try {
            hostName = InetAddress.getByName(request.getRemoteHost()).getCanonicalHostName();
            log.info(hostName);
        } catch (UnknownHostException e){
            log.info("Hostname error:" + e.getMessage());
        }

        return new ModelAndView("index", ImmutableMap.of("hostName", hostName));
        //return "index";
    }

    /**
     * The debug page of the web application
     */
    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    public String debug() {
        return "debug";
    }

    @Transactional
    @RequestMapping(value = "/make-me-admin", method = RequestMethod.POST)
    public String makeMeAdmin(Principal principal) throws ForbiddenException {
        // Make user into an admin
        if (principal != null) {
            Account account = accountStore.fetchAccountWithPrincipalName(principal.getName());
            account.setAdmin(true);

            // Log out user to reload auth roles
            SecurityContextHolder.clearContext();

            log.info("User {} is now an admin and was logged out", account.getName());

            return "redirect:/debug";
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/make-me-not-admin", method = RequestMethod.POST)
    public String makeMeNotAdmin(Principal principal) throws ForbiddenException {
        // Make user into a non-admin
        if (principal != null) {
            Account account = accountStore.fetchAccountWithPrincipalName(principal.getName());
            account.setAdmin(false);

            // Log out user to reload auth roles
            SecurityContextHolder.clearContext();

            log.info("User {} is now not an admin and was logged out", account.getName());

            return "redirect:/debug";
        } else {
            throw new ForbiddenException();
        }
    }

    // Used to enforce authentication when logging in from welcome page
    @RequestMapping(value = "/login", method = {RequestMethod.GET})
    public String login() {
        return "redirect:/";
    }
}
