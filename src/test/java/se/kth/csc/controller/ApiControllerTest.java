package se.kth.csc.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.api.AccountSnapshot;
import se.kth.csc.payload.api.QueueParameters;
import se.kth.csc.payload.api.QueuePositionSnapshot;
import se.kth.csc.payload.api.QueueSnapshot;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

import java.security.Principal;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ApiControllerTest {
    private AccountStore accountStore;
    private QueueStore queueStore;
    private QueuePositionStore queuePositionStore;
    private SimpMessagingTemplate simpMessagingTemplate;
    private ApiController apiController;

    @Before
    public void setUp() {
        accountStore = mock(AccountStore.class);
        queueStore = mock(QueueStore.class);
        queuePositionStore = mock(QueuePositionStore.class);
        simpMessagingTemplate = mock(SimpMessagingTemplate.class);

        apiController = new ApiController(new ApiProviderImpl(accountStore, queueStore, queuePositionStore, simpMessagingTemplate));
    }

    @Test
    public void testGetUser() throws NotFoundException {
        String principalName = "testuser";
        Account expected = mock(Account.class, RETURNS_DEEP_STUBS);
        when(expected.getPrincipalName()).thenReturn(principalName);
        when(accountStore.fetchAccountWithPrincipalName(principalName)).thenReturn(expected);

        AccountSnapshot actual = apiController.getUser(principalName);

        assertEquals(principalName, actual.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNotFound() throws NotFoundException {
        String principalName = "testuser";
        Account account = null;
        when(accountStore.fetchAccountWithPrincipalName(principalName)).thenReturn(account);

        apiController.getUser(principalName);
    }

    @Test
    public void testGetUserRoleAdminTrue() throws NotFoundException {
        String principalName = "testuser";
        Account account = mock(Account.class);
        boolean expected = true;

        when(account.isAdmin()).thenReturn(expected);
        when(accountStore.fetchAccountWithPrincipalName(principalName)).thenReturn(account);

        boolean actual = apiController.getUserRoleAdmin(principalName);

        assertEquals(expected, actual);
    }

    @Test
    public void testGetUserRoleAdminFalse() throws NotFoundException {
        String principalName = "testuser";
        Account account = mock(Account.class);
        boolean expected = false;

        when(account.isAdmin()).thenReturn(expected);
        when(accountStore.fetchAccountWithPrincipalName(principalName)).thenReturn(account);

        boolean actual = apiController.getUserRoleAdmin(principalName);

        assertEquals(expected, actual);
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserRoleAdminNotFound() throws NotFoundException {
        String principalName = "testuser";
        Account account = null;

        when(accountStore.fetchAccountWithPrincipalName(principalName)).thenReturn(account);

        apiController.getUserRoleAdmin(principalName);
    }

    @Test
    public void testGetQueueList() {
        Queue queue1 = mock(Queue.class, RETURNS_DEEP_STUBS);
        Queue queue2 = mock(Queue.class, RETURNS_DEEP_STUBS);
        ImmutableList<Queue> expected = ImmutableList.of(queue1, queue2);

        when(queueStore.fetchAllQueues()).thenReturn(expected);

        Iterable<QueueSnapshot> actual = apiController.getQueueList();

        // TODO: compare more thoroughly
        assertEquals(expected.size(), Iterables.size(actual));
    }

    @Test
    public void testGetQueueListEmpty() {
        ImmutableList<Queue> expected = ImmutableList.of();

        when(queueStore.fetchAllQueues()).thenReturn(expected);

        Iterable<QueueSnapshot> actual = apiController.getQueueList();

        // TODO: compare more thoroughly
        assertEquals(expected.size(), Iterables.size(actual));
    }

    @Test
    public void testGetQueue() throws NotFoundException {
        String queueName = "testqueue";
        Queue expected = mock(Queue.class, RETURNS_DEEP_STUBS);
        when(expected.getName()).thenReturn(queueName);

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(expected);

        QueueSnapshot actual = apiController.getQueue(queueName);

        assertEquals(queueName, actual.getName());
    }


    @Test(expected = NotFoundException.class)
    public void testGetQueueNotFound() throws NotFoundException {
        String queueName = "testqueue";
        Queue expected = null;

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(expected);

        apiController.getQueue(queueName);
    }

    @Test
    public void testPutQueue() throws NotFoundException, ForbiddenException {
        String queueName = "testqueue";
        Principal principal = mock(Principal.class);
        Account account = mock(Account.class);

        String userName = "testuser";
        when(principal.getName()).thenReturn(userName);
        when(accountStore.fetchAccountWithPrincipalName(userName)).thenReturn(account);

        apiController.putQueue(queueName, new QueueParameters("Test queue"), principal);

        ArgumentCaptor<Queue> queueCaptor = ArgumentCaptor.forClass(Queue.class);

        verify(queueStore, times(1)).storeQueue(queueCaptor.capture());

        Queue queue = queueCaptor.getValue();

        assertEquals(queueName, queue.getName());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testPutQueueConflict() throws NotFoundException, ForbiddenException {
        final String queueName = "testqueue";
        Principal principal = mock(Principal.class);
        Account account = mock(Account.class);

        String userName = "testuser";
        when(principal.getName()).thenReturn(userName);
        when(accountStore.fetchAccountWithPrincipalName(userName)).thenReturn(account);

        // First do nothing, then throw exception, when storeQueue is called with an argument that both is a Queue and
        // has a name property equal to queueName
        doNothing().doThrow(DataIntegrityViolationException.class).when(queueStore).storeQueue(
                argThat(both(isA(Queue.class)).and(hasProperty("name", equalTo(queueName)))));

        apiController.putQueue(queueName, new QueueParameters("Test queue"), principal);

        // The queue already exists now, so this should lead to a conflict
        apiController.putQueue(queueName, new QueueParameters("Test queue"), principal);
    }

    @Test
    public void testDeleteQueue() throws NotFoundException {
        String queueName = "testqueue";
        Queue queue = mock(Queue.class);

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(queue);

        apiController.deleteQueue(queueName);

        verify(queueStore, atLeastOnce()).removeQueue(queue);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteQueueNotFound() throws NotFoundException {
        String queueName = "testqueue";
        Queue queue = null;

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(queue);

        apiController.deleteQueue(queueName);
    }

    @Test
    public void testGetQueuePosition() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";
        QueuePosition expected = mock(QueuePosition.class, RETURNS_DEEP_STUBS);
        when(expected.getAccount().getPrincipalName()).thenReturn(userName);
        when(expected.getQueue().getName()).thenReturn(queueName);

        when(queuePositionStore.fetchQueuePositionWithQueueAndUser(queueName, userName)).thenReturn(expected);

        QueuePositionSnapshot actual = apiController.getQueuePosition(queueName, userName);

        assertEquals(queueName, actual.getQueueName());
        assertEquals(userName, actual.getUserName());
    }

    @Test(expected = NotFoundException.class)
    public void testGetQueuePositionNotFound() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";
        QueuePosition expected = null;

        when(queuePositionStore.fetchQueuePositionWithQueueAndUser(queueName, userName)).thenReturn(expected);

        apiController.getQueuePosition(queueName, userName);
    }

    @Test
    public void testPutQueuePosition() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";

        Queue queue = mock(Queue.class);
        Account account = mock(Account.class);

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(queue);
        when(accountStore.fetchAccountWithPrincipalName(userName)).thenReturn(account);

        apiController.putQueuePosition(queueName, userName);

        ArgumentCaptor<QueuePosition> queuePositionCaptor = ArgumentCaptor.forClass(QueuePosition.class);

        verify(queuePositionStore, times(1)).storeQueuePosition(queuePositionCaptor.capture());

        QueuePosition queuePosition = queuePositionCaptor.getValue();

        assertEquals(queue, queuePosition.getQueue());
        assertEquals(account, queuePosition.getAccount());
    }

    @Test(expected = NotFoundException.class)
    public void testPutQueuePositionNotFoundUser() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";

        Queue queue = mock(Queue.class);
        Account account = null;

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(queue);
        when(accountStore.fetchAccountWithPrincipalName(userName)).thenReturn(account);

        apiController.putQueuePosition(queueName, userName);
    }

    @Test(expected = NotFoundException.class)
    public void testPutQueuePositionNotFoundQueue() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";

        Queue queue = null;
        Account account = mock(Account.class);

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(queue);
        when(accountStore.fetchAccountWithPrincipalName(userName)).thenReturn(account);

        apiController.putQueuePosition(queueName, userName);
    }

    @Test(expected = NotFoundException.class)
    public void testPutQueuePositionNotFoundBoth() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";

        Queue queue = null;
        Account account = null;

        when(queueStore.fetchQueueWithName(queueName)).thenReturn(queue);
        when(accountStore.fetchAccountWithPrincipalName(userName)).thenReturn(account);

        apiController.putQueuePosition(queueName, userName);
    }

    @Test
    public void testDeleteQueuePosition() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";
        QueuePosition queuePosition = mock(QueuePosition.class, RETURNS_DEEP_STUBS);
        when(queuePosition.getAccount().getPrincipalName()).thenReturn(userName);
        when(queuePosition.getQueue().getName()).thenReturn(queueName);

        when(queuePositionStore.fetchQueuePositionWithQueueAndUser(queueName, userName)).thenReturn(queuePosition);

        apiController.deleteQueuePosition(queueName, userName);

        verify(queuePositionStore, atLeastOnce()).removeQueuePosition(queuePosition);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteQueuePositionNotFound() throws NotFoundException {
        String queueName = "testqueue";
        String userName = "testuser";
        QueuePosition queuePosition = null;

        when(queuePositionStore.fetchQueuePositionWithQueueAndUser(queueName, userName)).thenReturn(queuePosition);

        apiController.deleteQueuePosition(queueName, userName);
    }
}
