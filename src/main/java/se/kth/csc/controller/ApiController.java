package se.kth.csc.controller;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.*;

import java.security.Principal;

/**
 * A portable REST API for QWait. This class bears some similarities to other controllers in the project, but the
 * important difference is that this class communicates via pure JSON and other machine-readable representations.
 */
@Controller
@RequestMapping("/api")
public class ApiController {
    private final ApiProvider apiProvider;

    @Autowired
    public ApiController(ApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    /* API:
     * /user/{userName} GET
     * /user/{userName}/role/admin GET PUT
     * /queue/list GET
     * /queue/{queueName} GET PUT DELETE
     * /queue/{queueName}/position/{userName} GET PUT DELETE
     * /queue/{queueName}/position/{userName}/location GET PUT DELETE
     * /queue/{queueName}/position/{userName}/comment GET PUT DELETE
     * /queue/{queueName}/clear POST
     * /queue/{queueName}/active GET PUT
     * /queue/{queueName}/locked GET PUT
     * /queue/{queueName}/owner/{userName} PUT GET DELETE
     * /queue/{queueName}/moderator/{userName} PUT GET DELETE
     */

    @RequestMapping(value = "/user/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public AccountSnapshot getUser(@PathVariable("userName") String userName) throws NotFoundException {
        return AccountSnapshotter.INSTANCE.apply(fetchAccountOr404(userName));
    }

    @RequestMapping(value = "/user/{userName}/role/admin", method = RequestMethod.GET)
    @ResponseBody
    public boolean getUserRoleAdmin(@PathVariable("userName") String userName) throws NotFoundException {
        return fetchAccountOr404(userName).isAdmin();
    }

    @RequestMapping(value = "/user/{userName}/role/admin", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putUserRoleAdmin(@PathVariable("userName") String userName, @RequestBody boolean admin) throws NotFoundException {
        apiProvider.setAdmin(fetchAccountOr404(userName), admin);
    }

    @RequestMapping(value = "/queue/list", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<QueueSnapshot> getQueueList() {
        return transformSet(apiProvider.fetchAllQueues(), QueueSnapshotter.INSTANCE);
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.GET)
    @ResponseBody
    public QueueSnapshot getQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        return QueueSnapshotter.INSTANCE.apply(fetchQueueOr404(queueName));
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueue(@PathVariable("queueName") String queueName, @RequestBody QueueParameters queueParameters, Principal principal) throws NotFoundException, ForbiddenException {
        if (principal == null) {
            throw new ForbiddenException();
        }
        apiProvider.createQueue(queueName, fetchAccountOr404(principal.getName()), queueParameters.getTitle());
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        apiProvider.deleteQueue(fetchQueueOr404(queueName));
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public QueuePositionSnapshot getQueuePosition(@PathVariable("queueName") String queueName,
                                                  @PathVariable("userName") String userName) throws NotFoundException {
        return QueuePositionSnapshotter.INSTANCE.apply(fetchQueuePositionOr404(queueName, userName));
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueuePosition(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        apiProvider.addQueuePosition(queue, account);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueuePosition(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        apiProvider.deleteQueuePosition(fetchQueuePositionOr404(queueName, userName));
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/location", method = RequestMethod.GET)
    @ResponseBody
    public String getQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        String location = fetchQueuePositionOr404(queueName, userName).getLocation();
        if (location == null) {
            throw new NotFoundException(String.format("Queue position for queue %s and user %s doesn't have a location", queueName, userName));
        } else {
            return location;
        }
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/location", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName, @RequestBody String location) throws NotFoundException {
        apiProvider.setLocation(fetchQueuePositionOr404(queueName, userName), location);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/location", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        apiProvider.setLocation(fetchQueuePositionOr404(queueName, userName), null);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.GET)
    @ResponseBody
    public String getQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        String comment = fetchQueuePositionOr404(queueName, userName).getComment();
        if (comment == null) {
            throw new NotFoundException(String.format("Queue position for queue %s and user %s doesn't have a comment", queueName, userName));
        } else {
            return comment;
        }
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName, @RequestBody String comment) throws NotFoundException {
        apiProvider.setComment(fetchQueuePositionOr404(queueName, userName), comment);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        apiProvider.setComment(fetchQueuePositionOr404(queueName, userName), null);
    }

    @RequestMapping(value = "/queue/{queueName}/clear", method = RequestMethod.POST)
    @Transactional
    @ResponseBody
    public void clearQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        apiProvider.clearQueue(fetchQueueOr404(queueName));
    }

    @RequestMapping(value = "/queue/{queueName}/active", method = RequestMethod.GET)
    @ResponseBody
    public boolean getQueueActive(@PathVariable("queueName") String queueName) throws NotFoundException {
        return fetchQueueOr404(queueName).isActive();
    }

    @RequestMapping(value = "/queue/{queueName}/active", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueueActive(@PathVariable("queueName") String queueName, @RequestBody boolean active) throws NotFoundException {
        apiProvider.setActive(fetchQueueOr404(queueName), active);
    }

    @RequestMapping(value = "/queue/{queueName}/locked", method = RequestMethod.GET)
    @ResponseBody
    public boolean getQueueLocked(@PathVariable("queueName") String queueName) throws NotFoundException {
        return fetchQueueOr404(queueName).isLocked();
    }

    @RequestMapping(value = "/queue/{queueName}/locked", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueueLocked(@PathVariable("queueName") String queueName, @RequestBody boolean locked) throws NotFoundException {
        apiProvider.setLocked(fetchQueueOr404(queueName), locked);
    }

    @RequestMapping(value = "/queue/{queueName}/owner/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public AccountSnapshot getQueueOwner(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        if (queue.getOwners().contains(account)) {
            return AccountSnapshotter.INSTANCE.apply(account);
        } else {
            throw new NotFoundException(String.format("Not an owner for queue %s: %s", queueName, userName));
        }
    }

    @RequestMapping(value = "/queue/{queueName}/owner/{userName}", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueueOwner(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        apiProvider.addOwner(queue, account);
    }

    @RequestMapping(value = "/queue/{queueName}/owner/{userName}", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueueOwner(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        apiProvider.removeOwner(queue, account);
    }

    @RequestMapping(value = "/queue/{queueName}/moderator/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public AccountSnapshot getQueueModerator(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        if (queue.getModerators().contains(account)) {
            return AccountSnapshotter.INSTANCE.apply(account);
        } else {
            throw new NotFoundException(String.format("Not a moderator for queue %s: %s", queueName, userName));
        }
    }

    @RequestMapping(value = "/queue/{queueName}/moderator/{userName}", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueueModerator(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        apiProvider.addModerator(queue, account);
    }

    @RequestMapping(value = "/queue/{queueName}/moderator/{userName}", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueueModerator(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        apiProvider.removeModerator(queue, account);
    }

    public QueuePosition fetchQueuePositionOr404(String queueName, String userName) throws NotFoundException {
        QueuePosition queuePosition = apiProvider.fetchQueuePosition(queueName, userName);

        if (queuePosition == null) {
            throw new NotFoundException(String.format("Queue position for queue %s and user %s not found", queueName, userName));
        } else {
            return queuePosition;
        }
    }


    public Account fetchAccountOr404(String userName) throws NotFoundException {
        Account account = apiProvider.fetchAccount(userName);

        if (account == null) {
            throw new NotFoundException(String.format("User %s not found", userName));
        } else {
            return account;
        }
    }

    public Queue fetchQueueOr404(String queueName) throws NotFoundException {
        Queue queue = apiProvider.fetchQueue(queueName);

        if (queue == null) {
            throw new NotFoundException(String.format("Queue %s not found", queueName));
        } else {
            return queue;
        }
    }

    private static <A, B> ImmutableSet<B> transformSet(Iterable<A> iterable, Function<? super A, ? extends B> function) {
        return ImmutableSet.copyOf(Iterables.transform(iterable, function));
    }

    private enum AccountSnapshotter implements Function<Account, AccountSnapshot> {
        INSTANCE;

        @Override
        public AccountSnapshot apply(Account account) {
            return account == null ? null : new AccountSnapshot(account.getPrincipalName(), account.getName(), account.isAdmin(),
                    transformSet(account.getPositions(), NormalizedQueuePositionSnapshotter.INSTANCE),
                    transformSet(account.getOwnedQueues(), NormalizedQueueSnapshotter.INSTANCE),
                    transformSet(account.getModeratedQueues(), NormalizedQueueSnapshotter.INSTANCE));
        }
    }

    private enum QueueSnapshotter implements Function<Queue, QueueSnapshot> {
        INSTANCE;

        @Override
        public QueueSnapshot apply(Queue queue) {
            return queue == null ? null : new QueueSnapshot(queue.getName(), queue.getTitle(), queue.isActive(), queue.isLocked(),
                    transformSet(queue.getOwners(), NormalizedAccountSnapshotter.INSTANCE),
                    transformSet(queue.getModerators(), NormalizedAccountSnapshotter.INSTANCE),
                    transformSet(queue.getPositions(), NormalizedQueuePositionSnapshotter.INSTANCE));
        }
    }

    private enum QueuePositionSnapshotter implements Function<QueuePosition, QueuePositionSnapshot> {
        INSTANCE;

        @Override
        public QueuePositionSnapshot apply(QueuePosition queuePosition) {
            return queuePosition == null ? null : new QueuePositionSnapshot(queuePosition.getStartTime(), queuePosition.getLocation(),
                    queuePosition.getComment(),
                    NormalizedQueueSnapshotter.INSTANCE.apply(queuePosition.getQueue()),
                    NormalizedAccountSnapshotter.INSTANCE.apply(queuePosition.getAccount()));
        }
    }

    private enum NormalizedAccountSnapshotter implements Function<Account, NormalizedAccountSnapshot> {
        INSTANCE;

        @Override
        public NormalizedAccountSnapshot apply(Account account) {
            return account == null ? null : new NormalizedAccountSnapshot(account.getPrincipalName(), account.getName(), account.isAdmin());
        }
    }

    private enum NormalizedQueueSnapshotter implements Function<Queue, NormalizedQueueSnapshot> {
        INSTANCE;

        @Override
        public NormalizedQueueSnapshot apply(Queue queue) {
            return queue == null ? null : new NormalizedQueueSnapshot(queue.getName(), queue.getTitle(), queue.isActive(), queue.isLocked());
        }
    }

    private enum NormalizedQueuePositionSnapshotter implements Function<QueuePosition, NormalizedQueuePositionSnapshot> {
        INSTANCE;

        @Override
        public NormalizedQueuePositionSnapshot apply(QueuePosition queuePosition) {
            return queuePosition == null ? null : new NormalizedQueuePositionSnapshot(queuePosition.getStartTime(), queuePosition.getLocation(), queuePosition.getComment());
        }
    }
}
