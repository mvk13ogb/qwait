package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
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
import se.kth.csc.persist.QueueStore;

import java.security.Principal;

@Controller
@RequestMapping(value="/superadmin")
public class SuperadminController {
    private static final Logger log = LoggerFactory.getLogger(SuperadminController.class);
    private final AccountStore accountStore;
    private final QueueStore queueStore;
    private final ObjectMapper objectMapper;

    protected SuperadminController() {
        accountStore = null;
        objectMapper = null;
        queueStore = null;
    }

    @Autowired
    public SuperadminController(
            AccountStore accountStore,
            ObjectMapper objectMapper,
            QueueStore queueStore
    ) {
        this.accountStore = accountStore;
        this.objectMapper = objectMapper;
        this.queueStore = queueStore;
    }

    @RequestMapping(value = "/settings")

    public ModelAndView superadminsettings(Principal principal) throws JsonProcessingException {
        Account account = getCurrentAccount(principal);

        //A superadmin should be able to add and remove people on EVERY queue
        //but should not keep owner priviliges for every queue if demoted from superadmin.
        //Might be changed into making every superadmin an actual queueowner.
        if(account.isSuperAdmin()) {
            account = new Account();
            account.setQueues(Sets.newHashSet(queueStore.fetchAllQueues()));
        }
        String accountJson = objectMapper.writerWithView(Account.class).writeValueAsString(account);
        return new ModelAndView("superadmin/settings", ImmutableMap.of("account", account, "accountJson", accountJson));
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
            return "redirect:/superadmin/settings";
        }
        account.setAdmin(true);
        log.info(adminName + " made admin");

        SecurityContextHolder.clearContext();
        return "redirect:/superadmin/settings";
    }


    @Transactional
    @RequestMapping(value="/removeadmin", method = RequestMethod.POST)
    public String removeUserAdmin(@RequestParam("name") String adminName) {
        Account account = accountStore.fetchAccountWithPrincipalName(adminName);
        if(account == null) {
            log.info("Account " + adminName + " could not be found");
            return "redirect:/superadmin/settings";
        }
        account.setAdmin(false);
        log.info(adminName + " removed from admin");

        SecurityContextHolder.clearContext();
        return "redirect:/superadmin/settings";
    }
}
