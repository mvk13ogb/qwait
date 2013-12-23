package se.kth.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import se.kth.csc.model.Queue;
import se.kth.csc.persist.QueueStore;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueueControllerTest {

    @Test
    public void testList() throws JsonProcessingException {
        QueueStore queueStore = mock(QueueStore.class);

        Queue queue1 = new Queue();
        Queue queue2 = new Queue();

        when(queueStore.fetchAllQueues()).thenReturn(ImmutableList.of(queue1, queue2));

        QueueController queueController = new QueueController(null, queueStore, null, null);

        ModelAndView result = queueController.list();

        assertEquals("queue/list", result.getViewName());
        assertTrue(result.getModel().containsKey("queues"));
        assertEquals(queue1, ((List<Queue>) result.getModel().get("queues")).get(0));
    }


}
