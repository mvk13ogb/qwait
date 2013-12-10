package se.kth.csc.controller;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

