package dev.ilya_anna.user_service.consumers;

import dev.ilya_anna.user_service.events.UserSignOutEvent;
import dev.ilya_anna.user_service.services.BlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class UserSignOutEventsConsumer {

    @Autowired
    private BlacklistService blacklistService;

    @KafkaListener(topics = "user-sign-out-events-topic")
    public void consumeUserSignOutEvent(Map<String, Object> eventData) {
        log.info("Received user sign out event: {}", eventData);

        UserSignOutEvent userSignOutEvent = UserSignOutEvent.fromMap(eventData);

        blacklistService.addToBlacklist(userSignOutEvent);
    }
}
