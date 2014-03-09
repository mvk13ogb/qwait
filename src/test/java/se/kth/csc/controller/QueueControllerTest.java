package se.kth.csc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import se.kth.csc.auth.Role;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.QueueCreationInfo;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

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

    /*@Test
    public void testList() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.isUserInRole("admin")).thenReturn(true);

        Queue queue = mock(Queue.class);
        List<Queue> queues = mock(LinkedList.class);
        queues.add(queue);
        when(queueStore.fetchAllQueues()).thenReturn(queues);

        String queuesJson = null;
        ModelAndView result = null;
        try {
            queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);
            result = queueController.list(request);
        } catch (JsonProcessingException e) {
            // Do nothing
        }

        assertEquals(expected, result);
    }*/

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
