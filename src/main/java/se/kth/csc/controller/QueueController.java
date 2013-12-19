package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.model.Queue;
import se.kth.csc.model.QueuePosition;
import se.kth.csc.model.User;
import se.kth.csc.payload.QueueCreationInfo;
import se.kth.csc.persist.QueuePositionStore;
import se.kth.csc.persist.QueueStore;
import se.kth.csc.persist.UserStore;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping(value = "/queue")
public class QueueController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private QueueStore queueStore;

    @Inject
    private UserStore userStore;

    @Inject
    private QueuePositionStore queuePositionStore;

    @Transactional
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() throws JsonProcessingException {
        // Get all available queues
        List<Queue> queues = queueStore.fetchAllQueues();

        String queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);

        return new ModelAndView("queue/list", ImmutableMap.of("queues", queues, "queuesJson", queuesJson));
    }

    private User getCurrentUser() {

        // TODO: add login support and use the currently logged in user instead
        User user = userStore.fetchNewestUser();
        if (user == null) {
            user = new User();
            user.setName("Test User");
            user.setEmail("test@kth.se");
            userStore.storeUser(user);
        }
        return user;
    }

    @Transactional
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("queueCreationInfo") QueueCreationInfo queueCreationInfo) {
        Queue queue = new Queue();
        queue.setName(queueCreationInfo.getName());
        queue.setOwner(getCurrentUser());

        queueStore.storeQueue(queue);

        return "redirect:/queue/" + queue.getId();
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
    public String remove(@PathVariable("id") int id) throws NotFoundException {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
        }

        queueStore.removeQueue(queue);

        return "redirect:/queue/list";
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/create", method = RequestMethod.POST)
    public String createPosition(@PathVariable("id") int id) throws NotFoundException {
        Queue queue = queueStore.fetchQueueWithId(id);

        if (queue == null) {
            throw new NotFoundException();
        }

        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setUser(getCurrentUser());
        queuePosition.setStartTime(DateTime.now());
        queuePositionStore.storeQueuePosition(queuePosition);

        queue.getPositions().add(queuePosition);

        log.info("Saved queue position with id {}", queuePosition.getId());

        return "redirect:/queue/" + queue.getId();
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
