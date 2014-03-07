package se.kth.csc.controller;

import org.junit.Before;
import org.junit.Test;

import se.kth.csc.model.Account;
import se.kth.csc.persist.AccountStore;

import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HomeControllerTest {

  // System under test (SUT):
  private HomeController homeController;

  // Dependencies of this unit
  private AccountStore accountStore;

  @Before
  public void setUp() throws Exception {
    // Mock dependencies
    accountStore = mock(AccountStore.class);

    // Create SUT
    homeController = new HomeController(accountStore);
  }

  @Test
  public void testMakeMeAdmin() {
    // Set up expected behavior of mocks
    Principal principal = mock(Principal.class);
    when(principal.getName()).thenReturn("testuser");

    Account account = mock(Account.class);
    when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(account);

    // Run the system under test
    String result = null;
      try {
        result = homeController.makeMeAdmin(principal);
      } catch (ForbiddenException e) {
        // Do nothing
      }

    // Verify that interactions with mocks happened
    verify(account, atLeastOnce()).setAdmin(true);

    // Verify actual returned results
    assertEquals("redirect:/debug", result);
  }
}
