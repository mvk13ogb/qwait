package se.kth.csc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.auth.Role;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.payload.QueueCreationInfo;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class QueueControllerTest {

    // System under test (SUT):
    private QueueController queueController;

    // Dependencies of this unit
    private ObjectMapper objectMapper;
    private QueueStore queueStore;
    private AccountStore accountStore;
    private QueuePositionStore queuePositionStore;

    @Before
    public void setUp() throws Exception {
        // Mock dependencies
        objectMapper = mock(ObjectMapper.class);
        queueStore = mock(QueueStore.class);
        accountStore = mock(AccountStore.class);
        queuePositionStore = mock(QueuePositionStore.class);

        // Create SUT
        queueController = new QueueController(objectMapper, queueStore, accountStore, queuePositionStore);
    }

    /**
     * Only test if any ModelAndView is returned and that something has been written
     * to the ObjectMapper.
     */
    @Test
    public void testList() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.isUserInRole("admin")).thenReturn(true);

        List<Queue> queues = mock(LinkedList.class);
        when(queueStore.fetchAllQueues()).thenReturn(queues);
        when(queues.contains(any(Queue.class))).thenReturn(true); // Test when

        ModelAndView expected = any(ModelAndView.class);
        ModelAndView result = null;
        try {
            result = queueController.list(request);
            verify(objectMapper, atLeastOnce()).writerWithView(Queue.class).writeValueAsString(queues);
        } catch (Exception e) {
            // Do nothing
        }

        assertEquals(expected, result);
    }

    /**
     * Only test if a ModelAndView is returned and that a queue has been written
     * to the ObjectMapper.
     */
    @Test
    public void testShow() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account account = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(account);

        HttpServletRequest request = mock(HttpServletRequest.class);

        ModelAndView expected = any(ModelAndView.class);
        ModelAndView result = null;
        try {
            result = queueController.show(10, principal, request);
            verify(objectMapper, atLeastOnce()).writerWithView(Queue.class).writeValueAsString(any(Queue.class));
        } catch (Exception e) {
            // Do nothing
        }

        assertEquals(expected, result);
    }

    /**
     * Only tests if _a_ queue is stored due to object creation in method.
     */
    @Test
    public void testCreate() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account owner = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(owner);

        Queue queue = mock(Queue.class);
        when(queueStore.fetchQueueWithId(anyInt())).thenReturn(queue); // Ignore the id value

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.isUserInRole(Role.ADMIN.getAuthority())).thenReturn(true); // Test when user is admin

        QueueCreationInfo queueCreationInfo = mock(QueueCreationInfo.class);
        when(queueCreationInfo.getName()).thenReturn("testqueue");

        String result = "";
        try {
            result = queueController.create(queueCreationInfo, request, principal);
        } catch (Exception e) {
            // Do nothing
        }
        verify(queueStore, atLeastOnce()).storeQueue(any(Queue.class));

        assertEquals("redirect:/queue/list", result);
    }

    @Test
    public void testOpen() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account owner = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(owner);

        Queue queue = mock(Queue.class);
        when(queueStore.fetchQueueWithId(anyInt())).thenReturn(queue); // Ignore the id value

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(owner.canModerateQueue(queue)).thenReturn(true);

        String result = "";
        try {
            result = queueController.openQueue(10, request);
        } catch (Exception e) {
            // Do nothing
        }

        verify(queue, atLeastOnce()).setActive(true);
        assertEquals("redirect:/queue/10", result);
    }

    @Test
    public void testClose() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account owner = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(owner);

        Queue queue = mock(Queue.class);
        when(queueStore.fetchQueueWithId(anyInt())).thenReturn(queue); // Ignore the id value

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(owner.canModerateQueue(queue)).thenReturn(true);

        String result = "";
        try {
            result = queueController.closeQueue(10, request);
        } catch (Exception e) {
            // Do nothing
        }

        verify(queue, atLeastOnce()).setActive(false);
        assertEquals("redirect:/queue/10", result);
    }

    @Test
    public void testLock() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account owner = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(owner);

        Queue queue = mock(Queue.class);
        when(queueStore.fetchQueueWithId(anyInt())).thenReturn(queue); // Ignore the id value

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(owner.canModerateQueue(queue)).thenReturn(true);

        String result = "";
        try {
            result = queueController.lockQueue(10, request);
        } catch (Exception e) {
            // Do nothing
        }

        verify(queue, atLeastOnce()).setLocked(true);
        assertEquals("redirect:/queue/10", result);
    }

    @Test
    public void testUnlock() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account owner = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(owner);

        Queue queue = mock(Queue.class);
        when(queueStore.fetchQueueWithId(anyInt())).thenReturn(queue); // Ignore the id value

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(owner.canModerateQueue(queue)).thenReturn(true);

        String result = "";
        try {
            result = queueController.unlockQueue(10, request);
        } catch (Exception e) {
            // Do nothing
        }

        verify(queue, atLeastOnce()).setLocked(false);
        assertEquals("redirect:/queue/10", result);
    }
}
