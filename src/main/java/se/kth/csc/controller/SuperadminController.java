package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;

import java.security.Principal;

@Controller
@RequestMapping (value="/superadmin")
public class SuperadminController {
    private static final Logger log = LoggerFactory.getLogger(SuperadminController.class);
    private final AccountStore accountStore;

    protected SuperadminController() {
        accountStore = null;
    }

    @Autowired
    public SuperadminController(
            AccountStore accountStore
    ) {
        this.accountStore = accountStore;
    }

    @RequestMapping(value = "/superadmin", method = RequestMethod.GET)
    public ModelAndView superadminsettings() {
        return new ModelAndView();
    }
    private Account getCurrentAccount(Principal principal) {
        return accountStore.fetchAccountWithPrincipalName(principal.getName());
    }

    @Transactional
    @RequestMapping(value="/makeadmin", method = RequestMethod.POST)
    public String makeUserAdmin(@RequestParam("name") String adminName) {
        Account account = accountStore.fetchAccountWithPrincipalName(adminName);
        if(account == null) {
            log.info("Account " + adminName + " could not be found");
            return "redirect:/superadmin/superadmin";
        }
        account.setAdmin(true);
        log.info(adminName + " made admin");

        SecurityContextHolder.clearContext();
        return "redirect:/superadmin/superadmin";
    }

    @Transactional
    @RequestMapping(value="/removeadmin", method = RequestMethod.POST)
    public String removeUserAdmin(@RequestParam("name") String adminName) {
        Account account = accountStore.fetchAccountWithPrincipalName(adminName);
        if(account == null) {
            log.info("Account " + adminName + " could not be found");
            return "redirect:/superadmin/superadmin";
        }
        account.setAdmin(false);
        log.info(adminName + " removed from admin");

        SecurityContextHolder.clearContext();
        return "redirect:/superadmin/superadmin";
    }
}
