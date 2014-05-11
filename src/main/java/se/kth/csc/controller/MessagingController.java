package se.kth.csc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import se.kth.csc.payload.message.BroadcastMessage;

@Controller
public class MessagingController {
    private static final Logger log = LoggerFactory.getLogger(MessagingController.class);

    @MessageMapping("/broadcast")
    @SendTo("/topic/broadcast")
    public BroadcastMessage broadcast(BroadcastMessage message) {
        log.info("Got broadcast {}", message.getMessage());
        return message;
    }
}
