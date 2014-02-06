package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
   @RequestMapping(value="/makeadmin", method= RequestMethod.POST)
   public void makeUserAdmin(Account acc) {
       acc.setAdmin(true);
   }
}
