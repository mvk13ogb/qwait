package se.kth.csc.controller;

/*
 * #%L
 * QWait
 * %%
 * Copyright (C) 2013 - 2014 KTH School of Computer Science and Communication
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
import se.kth.csc.payload.Comment;
import se.kth.csc.payload.Location;
import se.kth.csc.payload.api.*;

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
     * /users?role=admin&query=abc123 GET
     * /user/{userName} GET
     * /user/{userName}/role/admin GET PUT DELETE
     * /queues GET
     * /queue/{queueName} GET PUT DELETE
     * /queue/{queueName}/position/{userName} GET PUT DELETE
     * /queue/{queueName}/position/{userName}/location GET PUT DELETE
     * /queue/{queueName}/position/{userName}/comment GET PUT DELETE
     * /queue/{queueName}/clear POST
     * /queue/{queueName}/hidden GET PUT
     * /queue/{queueName}/locked GET PUT
     * /queue/{queueName}/owner/{userName} PUT GET DELETE
     * /queue/{queueName}/moderator/{userName} PUT GET DELETE
     */

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<AccountSnapshot> getUsers(@RequestParam(value = "role", required = false) String role,
                                              @RequestParam(value = "query", required = false) String query) {
        final boolean onlyAdmin;

        if ("admin".equalsIgnoreCase(role)) {
            onlyAdmin = true;
        } else if (role == null) {
            onlyAdmin = false;
        } else {
            return ImmutableSet.of();
        }

        return transformSet(apiProvider.findAccounts(onlyAdmin, query), Snapshotters.AccountSnapshotter.INSTANCE);
    }

    @RequestMapping(value = "/user/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public AccountSnapshot getUser(@PathVariable("userName") String userName) throws NotFoundException {
        return Snapshotters.AccountSnapshotter.INSTANCE.apply(fetchAccountOr404(userName));
    }

    @RequestMapping(value = "/user/{userName}/role/admin", method = RequestMethod.GET)
    @ResponseBody
    public boolean getUserRoleAdmin(@PathVariable("userName") String userName) throws NotFoundException {
        return fetchAccountOr404(userName).isAdmin();
    }

    @RequestMapping(value = "/user/{userName}/role/admin", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putUserRoleAdmin(@PathVariable("userName") String userName, @RequestBody boolean admin) throws NotFoundException, ForbiddenException {
        apiProvider.setAdmin(fetchAccountOr404(userName), admin);
    }

    @RequestMapping(value = "/queues", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<QueueSnapshot> getQueueList() {
        return transformSet(apiProvider.fetchAllQueues(), Snapshotters.QueueSnapshotter.INSTANCE);
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.GET)
    @ResponseBody
    public QueueSnapshot getQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        return Snapshotters.QueueSnapshotter.INSTANCE.apply(fetchQueueOr404(queueName));
    }

    @RequestMapping(value = "/queue/{queueName}", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueue(@PathVariable("queueName") String queueName, @RequestBody QueueParameters queueParameters, Principal principal) throws NotFoundException, ForbiddenException {
        if (principal == null) {
            throw new ForbiddenException();
        }
        apiProvider.createQueue(queueName, queueParameters.getTitle());
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
        return Snapshotters.QueuePositionSnapshotter.INSTANCE.apply(fetchQueuePositionOr404(queueName, userName));
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
    public Location getQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        String location = fetchQueuePositionOr404(queueName, userName).getLocation();
        if (location == null) {
            throw new NotFoundException(String.format("Queue position for queue %s and user %s doesn't have a location", queueName, userName));
        } else {
            return new Location(location);
        }
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/location", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueuePositionLocation(
            @PathVariable("queueName") String queueName,
            @PathVariable("userName") String userName,
            @RequestBody Location location) throws NotFoundException, BadRequestException {
        apiProvider.setLocation(fetchQueuePositionOr404(queueName, userName), location.getLocation());
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/location", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueuePositionLocation(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException, BadRequestException {
        apiProvider.setLocation(fetchQueuePositionOr404(queueName, userName), null);
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.GET)
    @ResponseBody
    public Comment getQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException {
        String comment = fetchQueuePositionOr404(queueName, userName).getComment();
        if (comment == null) {
            throw new NotFoundException(String.format("Queue position for queue %s and user %s doesn't have a comment", queueName, userName));
        } else {
            return new Comment(comment);
        }
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueuePositionComment(
            @PathVariable("queueName") String queueName,
            @PathVariable("userName") String userName,
            @RequestBody Comment comment) throws NotFoundException, BadRequestException {
        apiProvider.setComment(fetchQueuePositionOr404(queueName, userName), comment.getComment());
    }

    @RequestMapping(value = "/queue/{queueName}/position/{userName}/comment", method = RequestMethod.DELETE)
    @Transactional
    @ResponseBody
    public void deleteQueuePositionComment(@PathVariable("queueName") String queueName, @PathVariable("userName") String userName) throws NotFoundException, BadRequestException {
        apiProvider.setComment(fetchQueuePositionOr404(queueName, userName), null);
    }

    @RequestMapping(value = "/queue/{queueName}/clear", method = RequestMethod.POST)
    @Transactional
    @ResponseBody
    public void clearQueue(@PathVariable("queueName") String queueName) throws NotFoundException {
        apiProvider.clearQueue(fetchQueueOr404(queueName));
    }

    @RequestMapping(value = "/queue/{queueName}/hidden", method = RequestMethod.GET)
    @ResponseBody
    public boolean getQueueHidden(@PathVariable("queueName") String queueName) throws NotFoundException {
        return fetchQueueOr404(queueName).isHidden();
    }

    @RequestMapping(value = "/queue/{queueName}/hidden", method = RequestMethod.PUT)
    @Transactional
    @ResponseBody
    public void putQueueHidden(@PathVariable("queueName") String queueName, @RequestBody boolean hidden) throws NotFoundException {
        apiProvider.setHidden(fetchQueueOr404(queueName), hidden);
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
            return Snapshotters.AccountSnapshotter.INSTANCE.apply(account);
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
            return Snapshotters.AccountSnapshotter.INSTANCE.apply(account);
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
}
