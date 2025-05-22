package dev.ilya_anna.user_service.producers;

import dev.ilya_anna.user_service.events.UserChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class UserChangedEventsProducer {
    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;


    public void sendUserCreatedEvent(UserChangedEvent userChangedEvent) {
        try {
            SendResult<String, Map<String, Object>> result = kafkaTemplate.send("user-changed-events-topic",
                    userChangedEvent.getId(),
                    UserChangedEvent.toMap(userChangedEvent)).get();
            log.info("Message sent successfully: {}", result.getRecordMetadata());

        } catch (InterruptedException e) {
            log.error("Interrupted while sending message", e);

        } catch (ExecutionException e) {
            log.error("Failed to send message", e.getCause());
        }
    }
}
