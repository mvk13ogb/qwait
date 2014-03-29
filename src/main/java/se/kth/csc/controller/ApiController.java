package se.kth.csc.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

/**
 * A portable REST API for QWait. This class bears some similarities to other controllers in the project, but the
 * important difference is that this class communicates via pure JSON and other machine-readable representations.
 */
@Controller
@RequestMapping("/api")
public class ApiController {
    private final AccountStore accountStore;
    private final QueueStore queueStore;
    private final QueuePositionStore queuePositionStore;

    protected ApiController() {
        accountStore = null;
        queueStore = null;
        queuePositionStore = null;
    }

    @Autowired
    public ApiController(AccountStore accountStore, QueueStore queueStore, QueuePositionStore queuePositionStore) {
        this.accountStore = accountStore;
        this.queueStore = queueStore;
        this.queuePositionStore = queuePositionStore;
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
     * /queue/{queueName}/closed GET PUT
     * /queue/{queueName}/locked GET PUT
     * /queue/{queueName}/owner/{userName} PUT GET DELETE
     * /queue/{queueName}/moderator/{userName} PUT GET DELETE
     */

    private Account fetchAccountOr404(String userName) throws NotFoundException {
        Account account = accountStore.fetchAccountWithPrincipalName(userName);

        if (account == null) {
            throw new NotFoundException(String.format("User %s not found", userName));
        } else {
            return account;
        }
    }

    private Queue fetchQueueOr404(String queueName) throws NotFoundException {
        Queue queue = queueStore.fetchQueueWithName(queueName);

        if (queue == null) {
            throw new NotFoundException(String.format("Queue %s not found", queueName));
        } else {
            return queue;
        }
    }

    private QueuePosition fetchQueuePositionOr404(String queueName, String userName) throws NotFoundException {
        QueuePosition queuePosition = queuePositionStore.fetchQueuePositionWithQueueAndUser(queueName, userName);

        if (queuePosition == null) {
            throw new NotFoundException(String.format("Queue position for queue %s and user %s not found", queueName, userName));
        } else {
            return queuePosition;
        }
    }

    @RequestMapping(value = "/user/{userName}", method = RequestMethod.GET)
    @JsonView(Account.class)
    @ResponseBody
    public Account getUser(@PathVariable("userName") String userName) throws NotFoundException {
        return fetchAccountOr404(userName);
    }

    @RequestMapping(value = "/user/{userName}/role/admin", method = RequestMethod.GET)
    @ResponseBody
    public boolean getUserRoleAdmin(@PathVariable("userName") String userName) throws NotFoundException {
        return fetchAccountOr404(userName).isAdmin();
    }

    @RequestMapping(value = "/user/{userName}/role/admin", method = RequestMethod.PUT)
    @Transactional
    public void putUserRoleAdmin(@PathVariable("userName") String userName, @RequestBody boolean admin) throws NotFoundException {
        fetchAccountOr404(userName).setAdmin(admin);
    }

    @RequestMapping(value = "/queue/list", method = RequestMethod.GET)
    @JsonView(Queue.class)
    @ResponseBody
    public Iterable<Queue> getQueueList() {
        return queueStore.fetchAllQueues();
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.GET)
    @JsonView(Queue.class)
    @ResponseBody
    public Queue getQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        return fetchQueueOr404(queueName);
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.PUT)
    @Transactional
    public void putQueue(@PathVariable("queueName") String queueName) throws ConflictException {
        Queue queue = new Queue();
        queue.setName(queueName);
        queueStore.storeQueue(queue);
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.DELETE)
    @Transactional
    public void deleteQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        queueStore.removeQueue(fetchQueueOr404(queueName));
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}", method = RequestMethod.GET)
    @JsonView(Queue.class)
    @ResponseBody
    public QueuePosition getQueuePosition(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        return fetchQueuePositionOr404(queueName, userName);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}", method = RequestMethod.PUT)
    @Transactional
    public void putQueuePosition(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setAccount(account);

        queuePositionStore.storeQueuePosition(queuePosition);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}", method = RequestMethod.DELETE)
    @Transactional
    public void deleteQueuePosition(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        queuePositionStore.removeQueuePosition(fetchQueuePositionOr404(queueName, userName));
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
    public void putQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName, @RequestBody String location) throws NotFoundException {
        fetchQueuePositionOr404(queueName, userName).setLocation(location);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/location", method = RequestMethod.DELETE)
    @Transactional
    public void deleteQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        fetchQueuePositionOr404(queueName, userName).setLocation(null);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.GET)
    @JsonView(Queue.class)
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
    public void putQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName, @RequestBody String comment) throws NotFoundException {
        fetchQueuePositionOr404(queueName, userName).setComment(comment);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.DELETE)
    @Transactional
    public void deleteQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        fetchQueuePositionOr404(queueName, userName).setComment(null);
    }

    @RequestMapping(value = "/queue/{queueName}/clear", method = RequestMethod.POST)
    @Transactional
    public void clearQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        fetchQueueOr404(queueName).getPositions().clear();
    }

    @RequestMapping(value = "/queue/{queueName}/closed", method = RequestMethod.GET)
    @ResponseBody
    public boolean getQueueClosed(@PathVariable("queueName") String queueName) throws NotFoundException {
        return !fetchQueueOr404(queueName).isActive();
    }

    @RequestMapping(value = "/queue/{queueName}/closed", method = RequestMethod.PUT)
    @Transactional
    public void putQueueClosed(@PathVariable("queueName") String queueName, @RequestBody boolean closed) throws NotFoundException {
        fetchQueueOr404(queueName).setActive(!closed);
    }

    @RequestMapping(value = "/queue/{queueName}/locked", method = RequestMethod.GET)
    @ResponseBody
    public boolean getQueueLocked(@PathVariable("queueName") String queueName) throws NotFoundException {
        return fetchQueueOr404(queueName).isLocked();
    }

    @RequestMapping(value = "/queue/{queueName}/locked", method = RequestMethod.PUT)
    @Transactional
    public void putQueueLocked(@PathVariable("queueName") String queueName, @RequestBody boolean locked) throws NotFoundException {
        fetchQueueOr404(queueName).setLocked(locked);
    }

    @RequestMapping(value = "/queue/{queueName}/owner/{userName}", method = RequestMethod.GET)
    @JsonView(Account.class)
    @ResponseBody
    public Account getQueueOwner(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        if (queue.getOwners().contains(account)) {
            return account;
        } else {
            throw new NotFoundException(String.format("Not an owner for queue %s: %s", queueName, userName));
        }
    }

    @RequestMapping(value = "/queue/{queueName}/owner/{userName}", method = RequestMethod.PUT)
    @Transactional
    public void putQueueOwner(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) {
        Queue queue = queueStore.fetchQueueWithName(queueName);
        Account account = accountStore.fetchAccountWithPrincipalName(userName);

        queue.getOwners().add(account);
    }

    @RequestMapping(value = "/queue/{queueName}/owner/{userName}", method = RequestMethod.DELETE)
    @Transactional
    public void deleteQueueOwner(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) {
        Queue queue = queueStore.fetchQueueWithName(queueName);
        Account account = accountStore.fetchAccountWithPrincipalName(userName);

        queue.getOwners().remove(account);
    }

    @RequestMapping(value = "/queue/{queueName}/moderator/{userName}", method = RequestMethod.GET)
    @JsonView(Account.class)
    @ResponseBody
    public Account getQueueModerator(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        Queue queue = fetchQueueOr404(queueName);
        Account account = fetchAccountOr404(userName);

        if (queue.getModerators().contains(account)) {
            return account;
        } else {
            throw new NotFoundException(String.format("Not a moderator for queue %s: %s", queueName, userName));
        }
    }

    @RequestMapping(value = "/queue/{queueName}/moderator/{userName}", method = RequestMethod.PUT)
    @Transactional
    public void putQueueModerator(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) {
        Queue queue = queueStore.fetchQueueWithName(queueName);
        Account account = accountStore.fetchAccountWithPrincipalName(userName);

        queue.getModerators().add(account);
    }

    @RequestMapping(value = "/queue/{queueName}/moderator/{userName}", method = RequestMethod.DELETE)
    @Transactional
    public void deleteQueueModerator(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) {
        Queue queue = queueStore.fetchQueueWithName(queueName);
        Account account = accountStore.fetchAccountWithPrincipalName(userName);

        queue.getModerators().remove(account);
    }

}
