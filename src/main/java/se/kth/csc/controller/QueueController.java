package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/queue")
public class QueueController {
    private static final Logger log = LoggerFactory.getLogger(QueueController.class);
    private final ObjectMapper objectMapper;
    private final QueueStore queueStore;
    private final AccountStore accountStore;
    private final QueuePositionStore queuePositionStore;

    protected QueueController() {
        // Needed for injection
        objectMapper = null;
        queueStore = null;
        accountStore = null;
        queuePositionStore = null;
    }

    @Autowired
    public QueueController(
            ObjectMapper objectMapper,
            QueueStore queueStore,
            AccountStore accountStore,
            QueuePositionStore queuePositionStore) {
        this.objectMapper = objectMapper;
        this.queueStore = queueStore;
        this.accountStore = accountStore;
        this.queuePositionStore = queuePositionStore;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request) throws JsonProcessingException {
        List<Queue> queues;
        if (request.isUserInRole("admin")) {
            queues = queueStore.fetchAllQueues();
        } else {
            queues = queueStore.fetchAllActiveQueues();
        }

        String queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);

        return new ModelAndView("queue/list", ImmutableMap.of("queues", queues, "queuesJson", queuesJson));
    }

    private Account getCurrentAccount(Principal principal) {
        return accountStore.fetchAccountWithPrincipalName(principal.getName());
    }

    @Transactional
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("queueCreationInfo") QueueCreationInfo queueCreationInfo,
                         HttpServletRequest request,
                         Principal principal)
            throws ForbiddenException {
        if (request.isUserInRole(Role.SUPER_ADMIN.getAuthority())) {
            Queue queue = new Queue();
            queue.setName(queueCreationInfo.getName());
            queue.addOwner(getCurrentAccount(principal));

            queue.setActive(true);
            queue.setLocked(false);

            queueStore.storeQueue(queue);

            return "redirect:/queue/list";
        } else {
            throw new ForbiddenException();
        }
    }

    @RequestMapping(value = "/{id}/addOwner", method = RequestMethod.POST)
    public String addOwner(@PathVariable("id") int id, String newOwner) {
        Queue queue = queueStore.fetchQueueWithId(id);
        Account account = accountStore.fetchAccountWithPrincipalName(newOwner);
        queue.addOwner(account);
        return "redirect:/queue/{id}";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("id") int id, Principal principal)
            throws NotFoundException, JsonProcessingException {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
        }

        String queueJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queue);
        return new ModelAndView("queue/show", ImmutableMap.of("queue", queue, "queueJson", queueJson,
                "account", getCurrentAccount(principal)));
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove", method = RequestMethod.POST)
    public String remove(@PathVariable("id") int id, HttpServletRequest request)
            throws NotFoundException, ForbiddenException {
        if (request.isUserInRole(Role.SUPER_ADMIN.getAuthority())) {
            Queue queue = queueStore.fetchQueueWithId(id);

            if (queue == null) {
                throw new NotFoundException();
            }

            queueStore.removeQueue(queue);

            return "redirect:/queue/list";
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/create", method = RequestMethod.POST)
    public String createPosition(@PathVariable("id") int id, Principal principal) throws Exception {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
        }

        // Users not logged in won't have a principal
        if (!queue.isActive() || queue.isLocked() || principal == null) {
            throw new ForbiddenException();
        } else {
            // Check if user already in queue. If so, throw exception.
            for (QueuePosition queuePos : queue.getPositions()) {
                if (queuePos.getAccount().getPrincipalName().equals(principal.getName())) {
                    throw new ForbiddenException();
                }
            }
        }

        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setAccount(getCurrentAccount(principal));
        queuePosition.setStartTime(DateTime.now());
        queuePositionStore.storeQueuePosition(queuePosition);

        queue.getPositions().add(queuePosition);

        return "redirect:/queue/" + id;
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/{positionId}/remove", method = RequestMethod.POST)
    public String deletePosition(@PathVariable("id") int id, @PathVariable("positionId") int positionId,
                                 HttpServletRequest request, Principal principal) throws Exception {
        Account account = getCurrentAccount(principal);
        QueuePosition queuePosition = queuePositionStore.fetchQueuePositionWithId(positionId);

        if (queuePosition == null) {
            throw new NotFoundException();
        }

        if (request.isUserInRole(Role.ADMIN.getAuthority()) || queuePosition.getAccount().equals(account)) {
            Queue queue = queueStore.fetchQueueWithId(id);

            if (queue == null) {
                throw new NotFoundException();
            }
            queue.getPositions().remove(queuePosition);
            queuePositionStore.removeQueuePosition(queuePosition);

            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/{positionId}/comment", method = RequestMethod.POST)
    public String updateComment(@PathVariable("id") int id, @PathVariable("positionId") int positionId, String comment)
            throws NotFoundException {
        QueuePosition queuePosition = queuePositionStore.fetchQueuePositionWithId(positionId);
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queuePosition == null || queue == null) {
            throw new NotFoundException();
        }

        queuePosition.setComment(comment);

        return "redirect:/queue/" + id;
    }

    @Transactional
    @RequestMapping(value = "/{id}/close", method = RequestMethod.POST)
    public String closeQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        if (request.isUserInRole("admin")) {
            Queue queue = queueStore.fetchQueueWithId(id);
            queue.setActive(false);
            for (QueuePosition pos : queue.getPositions ()) {
                queuePositionStore.removeQueuePosition(queuePositionStore.fetchQueuePositionWithId(pos.getId()));
            }
            queue.getPositions().clear();

           return "redirect:/queue/list";
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/open", method = {RequestMethod.POST})
    public String openQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        if (request.isUserInRole("admin")) {
            Queue queue = queueStore.fetchQueueWithId(id);
            queue.setActive(true);

            return "redirect:/queue/list";
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/lock", method = {RequestMethod.POST})
    public String lockQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        if (request.isUserInRole("admin")) {
            Queue queue = queueStore.fetchQueueWithId(id);
            queue.setLocked(true);

            return "redirect:/queue/list";
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/unlock", method = {RequestMethod.POST})
    public String unlockQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        if (request.isUserInRole("admin")) {
            Queue queue = queueStore.fetchQueueWithId(id);
            queue.setLocked(false);

            return "redirect:/queue/list";
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/add-queue-owner", method = {RequestMethod.POST})
    public String addQueueOwner(@RequestParam("name") String newQueueOwner,
                                @PathVariable("id") int id)
                                throws ForbiddenException {
        Account account = accountStore.fetchAccountWithPrincipalName(newQueueOwner);
        if(account == null) {
            log.info("Account " + newQueueOwner + " could not be found");
            return "redirect:/queue/" + id;
        }
        Queue queue = queueStore.fetchQueueWithId(id);
        queue.addOwner(account);
        log.info("Queue with id " + id + " now has " + newQueueOwner
                + " as a queue owner");
        return "redirect:/queue/" + id;
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove-queue-owner", method = {RequestMethod.POST})
    public String removeQueueOwner(@RequestParam("name") String oldOwnerName,
                                   @PathVariable("id") int id) {
        Account account = accountStore.fetchAccountWithPrincipalName(oldOwnerName);
        if(account == null) {
            log.info("Account " + oldOwnerName + " could not be found");
            return "redirect:/queue/" + id;
        }
        Queue queue = queueStore.fetchQueueWithId(id);
        queue.removeOwner(account);
        log.info(oldOwnerName + " remove from ownerlist of queue with id " + id);
        return "redirect:/queue/" + id;
    }
}
