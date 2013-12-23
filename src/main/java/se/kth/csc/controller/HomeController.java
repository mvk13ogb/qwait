package se.kth.csc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controls the home page.
 */
@Controller
public class HomeController {

    /**
     * The index page of the web application.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "home";
    }
}

