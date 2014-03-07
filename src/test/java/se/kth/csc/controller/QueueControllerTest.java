package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
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
import static org.junit.Assert.assertTrue;
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
        ModelAndView expected = mock(ModelAndView.class);
        try {
            queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);
            result = queueController.list(request);
        } catch (JsonProcessingException e) {
            // Do nothing
        }

        ImmutableMap<String, Object> modelViewMap = ImmutableMap.of("queues", queues, "queuesJson", queuesJson);
        when(expected.getModel()).thenReturn(modelViewMap);

        assertEquals(expected, result);
    }*/

    /*@Test
    public void testCreate() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");

        Account owner = mock(Account.class);
        when(accountStore.fetchAccountWithPrincipalName("testuser")).thenReturn(owner);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.isUserInRole(Role.ADMIN.getAuthority())).thenReturn(true); // Test when user is admin

        QueueCreationInfo queueCreationInfo = mock(QueueCreationInfo.class);
        when(queueCreationInfo.getName()).thenReturn("testqueue");

//        queueStore = spy(queueStore); // Should call the methods on the real object

        String result = "";
        try {
            result = queueController.create(queueCreationInfo, request, principal);
        } catch (Exception e) {
            // Do nothing
        }
        //verify(queuePositionStore, atLeastOnce()).storeQueuePosition(null);

        List<Queue> queues = queueStore.fetchAllActiveQueues();
        Queue queue = queues.get(0);
        assertEquals(queues.size(), 1);
        assertEquals(queue.getName(), "testqueue");
        assertTrue(queue.isActive());
        assertFalse(queue.isLocked());
        assertTrue(queue.getOwners().contains(owner));

        assertEquals("redirect:/queue/list", result);
    }*/

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
        assertTrue(queue.isActive()); // TODO Fails
        assertEquals("redirect:/queue/10", result);
    }
}
