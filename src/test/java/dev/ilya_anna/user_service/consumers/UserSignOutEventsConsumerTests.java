package dev.ilya_anna.user_service.consumers;

import com.redis.testcontainers.RedisContainer;
import dev.ilya_anna.user_service.entities.SignOutMark;
import dev.ilya_anna.user_service.events.UserSignOutEvent;
import dev.ilya_anna.user_service.repositories.SignOutMarkRepository;
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

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
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
public class UserSignOutEventsConsumerTests {
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
    private SignOutMarkRepository signOutMarkRepository;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1/user";
        signOutMarkRepository.deleteAll();
    }

    @Test
    void consumeUserCreatedEvent_CreatesUserInDB_WhenEventReceived() throws ExecutionException, InterruptedException {
        await().atMost(300, TimeUnit.SECONDS).until(() -> kafka.isRunning());
        UserSignOutEvent userSignOutEvent = new UserSignOutEvent();
        userSignOutEvent.setId("123");
        userSignOutEvent.setUserId("userId");
        userSignOutEvent.setTime(ZonedDateTime.now());

        kafkaTemplate.send("user-sign-out-events-topic",
                userSignOutEvent.getId(),
                UserSignOutEvent.toMap(userSignOutEvent)).get();

        await().atMost(300, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<SignOutMark> signOutMarkOptional = signOutMarkRepository.findById("123");
            assertThat(signOutMarkOptional).isPresent();

            SignOutMark signOutMark = signOutMarkOptional.get();
            assertThat(signOutMark.getUserId()).isEqualTo("userId");
        });
    }
}
