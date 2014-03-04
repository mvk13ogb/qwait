package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import se.kth.csc.auth.Role;
import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueueStore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AccountStore accountStore;
    private final QueueStore queueStore;
    private final ObjectMapper objectMapper;

    protected AdminController() {
        accountStore = null;
        objectMapper = null;
        queueStore = null;
    }

    @Autowired
    public AdminController(
            AccountStore accountStore,
            ObjectMapper objectMapper,
            QueueStore queueStore
    ) {
        this.accountStore = accountStore;
        this.objectMapper = objectMapper;
        this.queueStore = queueStore;
    }

    @RequestMapping(value = "")
    public ModelAndView adminSettings(Principal principal) throws JsonProcessingException {
        return new ModelAndView("admin/settings");
    }

    private Account getCurrentAccount(Principal principal) {
        return accountStore.fetchAccountWithPrincipalName(principal.getName());
    }

    @Transactional
    @RequestMapping(value="/make-admin", method = RequestMethod.POST)
    public String makeUserAdmin(@RequestParam("name") String adminName, HttpServletRequest request)
            throws NotFoundException, ForbiddenException {
        if(request.isUserInRole(Role.ADMIN.getAuthority())) {
            Account account = accountStore.fetchAccountWithPrincipalName(adminName);
            if(account == null) {
                log.info("Account " + adminName + " could not be found");
                throw new NotFoundException("Could not find account " + adminName);
            }
            account.setAdmin(true);
            log.info(adminName + " made admin");

            SecurityContextHolder.clearContext();
            return "redirect:/admin";
        }
        else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value="/remove-admin", method = RequestMethod.POST)
    public String removeUserAdmin(@RequestParam("name") String adminName, HttpServletRequest request)
            throws NotFoundException, ForbiddenException {
        if(request.isUserInRole(Role.ADMIN.getAuthority())) {
            Account account = accountStore.fetchAccountWithPrincipalName(adminName);
            if(account == null) {
                log.info("Account " + adminName + " could not be found");
                throw new NotFoundException("Could not find admin " + adminName);
            }
            account.setAdmin(false);
            log.info(adminName + " removed from admin");

            SecurityContextHolder.clearContext();
            return "redirect:/admin";
        }
        else {
            throw new ForbiddenException();
        }
    }
}
