package se.kth.csc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.kth.csc.auth.Role;
import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;
import org.junit.Before;
import se.kth.csc.persist.QueueStore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AdminControllerTest {

    // SUT
    private AdminController adminController;
    // Dependencies
    private AccountStore accountStore;

    @Before
    public void setUp() throws Exception {
        accountStore = mock(AccountStore.class);
        adminController = new AdminController(accountStore);
    }

    @Test
    public void testMakeUserAdmin() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.isUserInRole(Role.ADMIN.getAuthority())).thenReturn(true);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        Account account = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(account);

        String result = adminController.makeUserAdmin("testuser", request);
        verify(account, atLeastOnce()).setAdmin(true);
        assertEquals("redirect:/admin", result);
    }

    @Test
    public void testRemoveUserAdmin() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.isUserInRole(Role.ADMIN.getAuthority())).thenReturn(true);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        Account account = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(account);

        String result = adminController.removeUserAdmin("testuser", request);
        verify(account, atLeastOnce()).setAdmin(false);
        assertEquals("redirect:/admin", result);
    }
}
