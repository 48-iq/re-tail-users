package dev.ilya_anna.user_service.consumers;

import dev.ilya_anna.user_service.events.UserCreatedEvent;
import dev.ilya_anna.user_service.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class UserCreatedEventsConsumer {

    @Autowired
    private UserService userService;

    @KafkaListener(topics = "user-created-events-topic")
    public void consumeUserCreatedEvent(Map<String, Object> eventData) {
            log.info("Received user created event: {}", eventData);

            UserCreatedEvent userCreatedEvent = UserCreatedEvent.fromMap(eventData);

            userService.createUser(userCreatedEvent);
    }
}