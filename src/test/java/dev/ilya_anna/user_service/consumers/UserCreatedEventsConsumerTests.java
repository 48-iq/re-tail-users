package dev.ilya_anna.user_service.consumers;

import com.redis.testcontainers.RedisContainer;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.events.UserCreatedEvent;
import dev.ilya_anna.user_service.repositories.UserRepository;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
@DirtiesContext
@Slf4j
@SpringBootTest(
        properties = {
                "app.jwt.issuer=user_service",
                "app.jwt.subject=user_details",
                "app.jwt.access.duration=1000",
                "app.jwt.refresh.duration=2000",
                "app.jwt.access.secret=access_secret",
                "app.jwt.refresh.secret=refresh_secret",
                "eureka.client.enabled=false",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "app.uuid.seed=user_service",
                "app.minio.endpoint=http://localhost:9000",
                "app.minio.accessKey=minioadmin",
                "app.minio.secretKey=minioadmin",
                "app.gateway.uri=http://localhost:8080"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserCreatedEventsConsumerTests {
    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("user_service")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("init.sql");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        kafka.start();
    }

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:latest"));


    @AfterAll
    static void afterAll() {
        postgres.stop();
        kafka.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.database", () -> "0");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "password");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1/user";
        userRepository.deleteAll();
    }

    @Test
    void consumeUserCreatedEvent_CreatesUserInDB_WhenEventReceived() {
        await().atMost(300, TimeUnit.SECONDS).until(() -> kafka.isRunning());
        UserCreatedEvent userCreatedEvent = new UserCreatedEvent();
        userCreatedEvent.setId("123");
        userCreatedEvent.setUserId("userId");
        userCreatedEvent.setName("John");
        userCreatedEvent.setSurname("Doe");
        userCreatedEvent.setNickname("johndoe");
        userCreatedEvent.setPhone("+1234567890");
        userCreatedEvent.setEmail("john.doe@example.com");
        userCreatedEvent.setTime(ZonedDateTime.now(ZoneId.systemDefault()));

        try{
            kafkaTemplate.send("user-created-events-topic",
                    userCreatedEvent.getId(),
                    UserCreatedEvent.toMap(userCreatedEvent)).get();
        } catch (ExecutionException | InterruptedException e){
            log.error("An exception during sending user created event to kafka ", e);
        }


        await().atMost(300, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<User> userOptional = userRepository.findById("userId");
            assertThat(userOptional).isPresent();

            User user = userOptional.get();
            assertThat(user.getName()).isEqualTo("John");
            assertThat(user.getSurname()).isEqualTo("Doe");
            assertThat(user.getNickname()).isEqualTo("johndoe");
            assertThat(user.getPhone()).isEqualTo("+1234567890");
            assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        });
    }
}
