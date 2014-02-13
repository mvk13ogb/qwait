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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.model.Account;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.payload.QueueCreationInfo;
import se.kth.csc.persist.AccountStore;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.acl.NotOwnerException;
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
    public ModelAndView list() throws JsonProcessingException {
        // Get all available queues
        List<Queue> queues = queueStore.fetchAllQueues();

        String queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);

        return new ModelAndView("queue/list", ImmutableMap.of("queues", queues, "queuesJson", queuesJson));
    }

    private Account getCurrentAccount(Principal principal) {
        return accountStore.fetchAccountWithPrincipalName(principal.getName());
    }

    @Transactional
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("queueCreationInfo") QueueCreationInfo queueCreationInfo, HttpServletRequest request,
        Principal principal)
        throws NotOwnerException {
        if (request.isUserInRole("super_admin")) {
            Queue queue = new Queue();
            queue.setName(queueCreationInfo.getName());
            Set<Account> ownerSet = Sets.newHashSet();
            ownerSet.add(getCurrentAccount(principal));
            queue.setOwner(ownerSet);

            queueStore.storeQueue(queue);

            return "redirect:/queue/list";
        } else {
            throw new NotOwnerException();
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("id") int id) throws NotFoundException, JsonProcessingException {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
        }

        String queueJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queue);

        return new ModelAndView("queue/show", ImmutableMap.of("queue", queue, "queueJson", queueJson));
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove", method = RequestMethod.POST)
    public String remove(@PathVariable("id") int id, HttpServletRequest request) throws Exception {
        if (request.isUserInRole("super_admin")) {
            Queue queue = queueStore.fetchQueueWithId(id);

            if (queue == null) {
                throw new NotFoundException();
            }

            queueStore.removeQueue(queue);

            return "redirect:/queue/list";
        } else {
            throw new NotOwnerException();
        }
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/create", method = RequestMethod.POST)
    public String createPosition(@PathVariable("id") int id, Principal principal) throws NotFoundException {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
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
    @RequestMapping(value = "/{id}/position/{positionId}/remove", method = {RequestMethod.POST})
    public String deletePosition(@PathVariable("id") int id, @PathVariable("positionId") int positionId) throws NotFoundException {
        QueuePosition queuePosition = queuePositionStore.fetchQueuePositionWithId(positionId);
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queuePosition == null || queue == null) {
            throw new NotFoundException();
        }

        queue.getPositions().remove(queuePosition);
        queuePositionStore.removeQueuePosition(queuePosition);

        return "redirect:/queue/" + id;
    }
}
