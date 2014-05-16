package se.kth.csc.controller;

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
