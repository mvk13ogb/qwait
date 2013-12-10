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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

@Controller
@RequestMapping(value = "/queue")
public class QueueController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ObjectMapper objectMapper;

    @Transactional
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() throws JsonProcessingException {
        // Get all available queues
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Queue> query = cb.createQuery(Queue.class);
        List<Queue> queues = entityManager.createQuery(query.select(query.from(Queue.class))).getResultList();

        String queuesJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queues);

        return new ModelAndView("queue/list", ImmutableMap.of("queues", queues, "queuesJson", queuesJson));
    }

    private User getCurrentUser() {

        // TODO: add login support and use the currently logged in user instead
        User user = entityManager.find(User.class, 1);
        if (user == null) {
            user = new User();
            user.setName("Test User");
            user.setEmail("test@kth.se");
            entityManager.persist(user);
        }
        return user;
    }

    @Transactional
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("queueCreationInfo") QueueCreationInfo queueCreationInfo) {
        Queue queue = new Queue();
        queue.setName(queueCreationInfo.getName());
        queue.setOwner(getCurrentUser());

        entityManager.persist(queue);

        return "redirect:/queue/" + queue.getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ModelAndView show(@PathVariable("id") int id) throws NotFoundException, JsonProcessingException {
        Queue queue = entityManager.find(Queue.class, id);

        if (queue == null) {
            throw new NotFoundException();
        }

        String queueJson = objectMapper.writerWithView(Queue.class).writeValueAsString(queue);

        return new ModelAndView("queue/show", ImmutableMap.of("queue", queue, "queueJson", queueJson));
    }

    @Transactional
    @RequestMapping(value = "/{id}/remove", method = RequestMethod.POST)
    public String remove(@PathVariable("id") int id) throws NotFoundException {
        Queue queue = entityManager.find(Queue.class, id);

        if (queue == null) {
            throw new NotFoundException();
        }

        entityManager.remove(queue);

        return "redirect:/queue/list";
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/create", method = RequestMethod.POST)
    public String createPosition(@PathVariable("id") int id) throws NotFoundException {
        Queue queue = entityManager.find(Queue.class, id);

        if (queue == null) {
            throw new NotFoundException();
        }

        QueuePosition queuePosition = new QueuePosition();
        queuePosition.setQueue(queue);
        queuePosition.setUser(getCurrentUser());
        queuePosition.setStartTime(DateTime.now());
        entityManager.persist(queuePosition);
        queue.getPositions().add(queuePosition);
        log.info("Saved queue position with id {}", queuePosition.getId());

        return "redirect:/queue/" + queue.getId();
    }

    @Transactional
    @RequestMapping(value = "/{id}/position/{positionId}/remove", method = {RequestMethod.POST})
    public String deletePosition(@PathVariable("id") int id, @PathVariable("positionId") int positionId) throws NotFoundException {
        QueuePosition queuePosition = entityManager.find(QueuePosition.class, positionId);

        if (queuePosition == null) {
            throw new NotFoundException();
        }

        entityManager.remove(queuePosition);

        return "redirect:/queue/" + id;
    }
}
