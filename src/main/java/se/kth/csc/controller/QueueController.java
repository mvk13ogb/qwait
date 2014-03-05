package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import java.net.InetAddress;
import java.net.UnknownHostException;

@Controller
@RequestMapping(value = "/queue")
public class QueueController {
    private static final Logger log = LoggerFactory.getLogger(QueueController.class);
    private final ObjectMapper objectMapper;
    private final QueueStore queueStore;
    private final AccountStore accountStore;
    private final QueuePositionStore queuePositionStore;
    private static final int MAX_LEN = 30; //max len for comment fields

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

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ModelAndView list(HttpServletRequest request) throws JsonProcessingException {
        List<Queue> queues;
        Principal user = request.getUserPrincipal();
        if (request.isUserInRole("admin")) {
            queues = queueStore.fetchAllQueues();
        } else {
            queues = queueStore.fetchAllActiveQueues();
            if (user != null) { // Anonymous user check
                List<Queue> modQueues = queueStore.fetchAllModeratedQueues(getCurrentAccount(user));
                List<Queue> ownQueues = queueStore.fetchAllOwnedQueues(getCurrentAccount(user));
                for(Queue q : modQueues){
                    if(!queues.contains(q)){
                        queues.add(q);
                    }
                }
                for(Queue q : ownQueues){
                    if(!queues.contains(q)){
                        queues.add(q);
                    }
                }
            }
        }

        String queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);

        return new ModelAndView("queue/list", ImmutableMap.of("queues", queues, "queuesJson", queuesJson));
    }

    /**
     * @param principal
     * @return the account of the given principal, return null if the principal is null
     */
    private Account getCurrentAccount(Principal principal) {
        if (principal != null) // Anonymous users won't ha a principal
            return accountStore.fetchAccountWithPrincipalName(principal.getName());
        else {
            return null;
        }
    }

    @Transactional
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("queueCreationInfo") QueueCreationInfo queueCreationInfo,
                         HttpServletRequest request,
                         Principal principal)
            throws ForbiddenException, BadNameException {
        if (principal == null) // Anonymous user
            throw new ForbiddenException();

        if (request.isUserInRole(Role.ADMIN.getAuthority())) {
            String queueName = queueCreationInfo.getName();
            if (queueName.trim().length() > 0) {
                Queue queue = new Queue();
                queue.setName(queueName);
                queue.addOwner(getCurrentAccount(principal));

                queue.setActive(true);
                queue.setLocked(false);
                queueStore.storeQueue(queue);

                return "redirect:/queue";
            } else {
                throw new BadNameException();
            }
        } else {
            throw new ForbiddenException();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("id") int id, Principal principal, HttpServletRequest request)
            throws NotFoundException, JsonProcessingException {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
        }

        String queueJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queue);
        String hostName = "";
        try{
            hostName = InetAddress.getByName(request.getRemoteHost()).getCanonicalHostName();
        } catch (UnknownHostException e){
            log.debug(e.getMessage());
        }

        Account account = getCurrentAccount(principal);
        if (account != null) {
            return new ModelAndView("queue/show", ImmutableMap.of("queue", queue, "queueJson", queueJson,
                    "account", account, "hostName", hostName));
        } else { // Null account not allowed, checks needed before method calling in view
            return new ModelAndView("queue/show", ImmutableMap.of("queue", queue, "queueJson", queueJson,
                    "hostName", hostName));
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove", method = RequestMethod.POST)
    public String remove(@PathVariable("id") int id, HttpServletRequest request)
            throws NotFoundException, ForbiddenException {
        Account account = getCurrentAccount(request.getUserPrincipal());
        if (account == null) // Anonymous user
            throw new ForbiddenException();

        Queue queue = queueStore.fetchQueueWithId(id);
        if (account.canEditQueue(queue)) {

            if (queue == null) {
                throw new NotFoundException();
            }

            queueStore.removeQueue(queue);

            return "redirect:/queue";
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
        } else if (account == null) { // Anonymous user
            throw new ForbiddenException();
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
    @RequestMapping(value = "/{id}/position/{positionId}/location", method = {RequestMethod.POST})
    public String updateLocation(@PathVariable("id") int id, @PathVariable("positionId") int positionId, String location,
                                 Principal principal)
            throws NotFoundException, ForbiddenException {
        QueuePosition queuePosition = queuePositionStore.fetchQueuePositionWithId(positionId);
        Queue queue = queueStore.fetchQueueWithId(id);

        // Null if anonymous
        if (principal == null || !getCurrentAccount(principal).equals(queuePosition.getAccount()))
            throw new ForbiddenException();

        if (queuePosition == null || queue == null) {
            throw new NotFoundException();
        }

        int length = Math.min(location.length(), MAX_LEN);

        queuePosition.setLocation(location.substring(0, length));

        return "redirect:/queue/" + id;
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/{positionId}/comment", method = {RequestMethod.POST})
    public String updateComment(@PathVariable("id") int id, @PathVariable("positionId") int positionId, String comment,
                                Principal principal)
            throws NotFoundException, ForbiddenException {
        QueuePosition queuePosition = queuePositionStore.fetchQueuePositionWithId(positionId);
        Queue queue = queueStore.fetchQueueWithId(id);

        // Null if anonymous
        if (principal == null || !getCurrentAccount(principal).equals(queuePosition.getAccount()))
            throw new ForbiddenException();

        if (queuePosition == null || queue == null) {
            throw new NotFoundException();
        }

        int length = Math.min(comment.length(), MAX_LEN);

        queuePosition.setComment(comment.substring(0, length));

        return "redirect:/queue/" + id;
    }

    @Transactional
    @RequestMapping(value = "/{id}/close", method = RequestMethod.POST)
    public String closeQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        Account account = getCurrentAccount(request.getUserPrincipal());
        if (account == null) // Anonymous user
            throw new ForbiddenException();

        Queue queue = queueStore.fetchQueueWithId(id);
        if (account.canModerateQueue(queue)) {
            queue.setActive(false);
            for (QueuePosition pos : queue.getPositions ()) {
                queuePositionStore.removeQueuePosition(queuePositionStore.fetchQueuePositionWithId(pos.getId()));
            }
            queue.getPositions().clear();

           return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/open", method = RequestMethod.POST)
    public String openQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        Account account = getCurrentAccount(request.getUserPrincipal());
        if (account == null) // Anonymous user
            throw new ForbiddenException();

        Queue queue = queueStore.fetchQueueWithId(id);
        if (account.canModerateQueue(queue)) {
            queue.setActive(true);

            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/lock", method = RequestMethod.POST)
    public String lockQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        Account account = getCurrentAccount(request.getUserPrincipal());
        if (account == null) // Anonymous user
            throw new ForbiddenException();

        Queue queue = queueStore.fetchQueueWithId(id);
        if (account.canModerateQueue(queue)) {
            queue.setLocked(true);

            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/unlock", method = RequestMethod.POST)
    public String unlockQueue(@PathVariable("id") int id, HttpServletRequest request)
            throws ForbiddenException {
        Account account = getCurrentAccount(request.getUserPrincipal());
        if (account == null) // Anonymous user
            throw new ForbiddenException();

        Queue queue = queueStore.fetchQueueWithId(id);
        if (account.canModerateQueue(queue)) {
            queue.setLocked(false);

            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/add-owner", method = RequestMethod.POST)
    public String addQueueOwner(@RequestParam("name") String newQueueOwner,
                                @PathVariable("id") int id, HttpServletRequest request)
                                throws NotFoundException, ForbiddenException {
        Account accountOfAdder = getCurrentAccount(request.getUserPrincipal());
        if (accountOfAdder == null) // Anonymous user
            throw new ForbiddenException();

        Account accountToAdd = accountStore.fetchAccountWithPrincipalName(newQueueOwner);
        Queue queue = queueStore.fetchQueueWithId(id);
        if(accountOfAdder.canEditQueue(queue)) {
            if(accountToAdd == null) {
                log.info("Account " + newQueueOwner + " could not be found");
                throw new NotFoundException("Could not find the account " + newQueueOwner);
            }
            queue.addOwner(accountToAdd);
            log.info("Queue with id " + id + " now has " + newQueueOwner
                    + " as a queue owner");

            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove-owner", method = RequestMethod.POST)
    public String removeQueueOwner(@RequestParam("name") String oldOwnerName,
                                   @PathVariable("id") int id, HttpServletRequest request)
                                   throws NotFoundException, ForbiddenException {
        Account accountOfRemover = getCurrentAccount(request.getUserPrincipal());
        if (accountOfRemover == null) // Anonymous user
            throw new ForbiddenException();

        Account accountToRemove = accountStore.fetchAccountWithPrincipalName(oldOwnerName);
        Queue queue = queueStore.fetchQueueWithId(id);
        if(accountOfRemover.canEditQueue(queue)) {
            if(accountToRemove == null) {
                log.info("Account " + oldOwnerName + " could not be found");
                throw new NotFoundException("Couldn't find the owner " + oldOwnerName);
            }
            queue.removeOwner(accountToRemove);
            log.info(oldOwnerName + " remove from ownerlist of queue with id " + id);
            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/add-moderator", method = RequestMethod.POST)
    public String addQueueModerator(@RequestParam("name") String newQueueModerator,
                                @PathVariable("id") int id, HttpServletRequest request)
            throws NotFoundException, ForbiddenException {
        Account accountOfAdder = getCurrentAccount(request.getUserPrincipal());
        if (accountOfAdder == null) // Anonymous user
            throw new ForbiddenException();

        Account accountToAdd = accountStore.fetchAccountWithPrincipalName(newQueueModerator);
        Queue queue = queueStore.fetchQueueWithId(id);
        if(accountOfAdder.canEditQueue(queue)) {
            if(accountToAdd == null) {
                log.info("Account " + newQueueModerator + " could not be found");
                throw new NotFoundException("Could not find the account " + newQueueModerator);
            }
            queue.addModerator(accountToAdd);
            log.info("Queue with id " + id + " now has " + newQueueModerator
                    + " as a queue moderator");

            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove-moderator", method = RequestMethod.POST)
    public String removeQueueModerator(@RequestParam("name") String oldModeratorName,
                                   @PathVariable("id") int id, HttpServletRequest request)
            throws NotFoundException, ForbiddenException {
        Account accountOfRemover = getCurrentAccount(request.getUserPrincipal());
        if (accountOfRemover == null) // Anonymous user
            throw new ForbiddenException();

        Account accountToRemove = accountStore.fetchAccountWithPrincipalName(oldModeratorName);
        Queue queue = queueStore.fetchQueueWithId(id);
        if(accountOfRemover.canEditQueue(queue)) {
            if(accountToRemove == null) {
                log.info("Account " + oldModeratorName + " could not be found");
                throw new NotFoundException("Couldn't find the moderator " + oldModeratorName);
            }
            queue.removeModerator(accountToRemove);
            log.info(oldModeratorName + " remove from moderatorlist of queue with id " + id);
            return "redirect:/queue/" + id;
        } else {
            throw new ForbiddenException();
        }
    }
}
