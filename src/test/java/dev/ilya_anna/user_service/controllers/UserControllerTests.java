package dev.ilya_anna.user_service.controllers;

import com.redis.testcontainers.RedisContainer;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.events.UserChangedEvent;
import dev.ilya_anna.user_service.events.UserSignOutEvent;
import dev.ilya_anna.user_service.repositories.UserRepository;
import dev.ilya_anna.user_service.repositories.UserSettingsRepository;
import dev.ilya_anna.user_service.services.BlacklistService;
import dev.ilya_anna.user_service.services.JwtService;
import dev.ilya_anna.user_service.services.UserSettingsService;
import dev.ilya_anna.user_service.services.UuidService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static io.restassured.RestAssured.*;

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
                "app.uuid.seed=user_service",
                "app.minio.endpoint=http://localhost:9000",
                "app.minio.accessKey=minioadmin",
                "app.minio.secretKey=minioadmin",
                "app.gateway.uri=http://localhost:8080"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {

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
        redis.start();
        redis.withEnv("REDIS_PASSWORD", "password");
        postgres.start();
    }

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:latest"));


    @AfterAll
    static void afterAll() {
        redis.stop();
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.database", () -> "0");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "password");
    }

    @Getter
    @Setter
    static class TestConsumer {
        private List<Map<String, Object>> userChangedEventMessages = new ArrayList<>();

        @KafkaListener(topics = "user-changed-events-topic")
        public void onUserCreatedEvent(Map<String, Object> message) {
            userChangedEventMessages.add(message);
        }

        public void clear() {
            userChangedEventMessages.clear();
        }
    }

    @TestConfiguration
    static class Config {
        @Bean
        public TestConsumer testConsumer() {
            return new TestConsumer();
        }
    }

    @Autowired
    private TestConsumer testConsumer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UuidService uuidService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BlacklistService blacklistService;

    @MockitoBean
    private RestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1/user";
        userRepository.deleteAll();
        userSettingsRepository.deleteAll();
    }

    @Test
    void getUserAllInfo_ReturnsUserInfo_WhenUserExists() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .nickname("johndoe")
                .email("john@example.com")
                .phone("+1234567890")
                .address("123 Main St")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .about("About John")
                .avatarImageId("avatar123")
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(Integer.class),
                Optional.ofNullable(ArgumentMatchers.any())))
                .thenReturn(5);

        UserDto result = given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/all-info/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.OK.value()).extract().as(UserDto.class);

        assertEquals(user.getName(), result.getName());
        assertEquals(user.getSurname(), result.getSurname());
        assertEquals(user.getNickname(), result.getNickname());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPhone(), result.getPhone());
        assertEquals(user.getAddress(), result.getAddress());
        assertEquals(user.getAbout(), result.getAbout());
        assertEquals(user.getAvatarImageId(), result.getAvatarImageId());
        assertEquals(5, result.getAnnouncementsCount());
    }

    @Test
    void getUserAllInfo_ReturnsNotFound_WhenUserDoesNotExist() {
        String currentUserId = uuidService.generate();
        User currentUser = User.builder()
                .id(currentUserId)
                .nickname("currentUser")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        userRepository.save(currentUser);

        String accessToken = jwtService.generateAccess(currentUser);

        userRepository.delete(currentUser);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/all-info/{userId}", currentUserId)
                .then()
                .log().body()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getUserAllInfo_ReturnsForbidden_WhenAccessingAnotherUserInfo() {
        String userId = uuidService.generate();

        String currentUserId = uuidService.generate();
        User currentUser = User.builder()
                .id(currentUserId)
                .nickname("currentUser")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        userRepository.save(currentUser);

        String accessToken = jwtService.generateAccess(currentUser);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/all-info/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getUserAllInfo_ReturnsForbidden_WhenUserJwtIsInBlacklist() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .nickname("johndoe")
                .email("john@example.com")
                .phone("+1234567890")
                .address("123 Main St")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .about("About John")
                .avatarImageId("avatar123")
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        UserSignOutEvent userSignOutEvent = new UserSignOutEvent(uuidService.generate(), userId, ZonedDateTime.now(ZoneId.systemDefault()));
        blacklistService.addToBlacklist(userSignOutEvent);

        when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(Integer.class),
                Optional.ofNullable(ArgumentMatchers.any())))
                .thenReturn(5);

        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/all-info/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void getUser_ReturnsVisibleUserInfo_WhenUserExists() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .nickname("johndoe")
                .email("john@example.com")
                .phone("+1234567890")
                .address("123 Main St")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .about("About John")
                .avatarImageId("avatar123")
                .build();
        userRepository.save(user);

        UserSettingsDto userSettingsDto = UserSettingsDto.builder()
                .nameVisibility(true)
                .surnameVisibility(false)
                .phoneVisibility(true)
                .emailVisibility(true)
                .addressVisibility(false)
                .avatarVisibility(false)
                .build();

        userSettingsService.updateUserSettings(userId, userSettingsDto);

        when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(Integer.class),
                Optional.ofNullable(ArgumentMatchers.any())))
                .thenReturn(5);

        UserDto result = given()
                .when()
                .get("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.OK.value()).extract().as(UserDto.class);

        assertEquals(user.getName(), result.getName());
        assertNull(result.getSurname());
        assertEquals(user.getNickname(), result.getNickname());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPhone(), result.getPhone());
        assertNull(result.getAddress());
        assertEquals(user.getAbout(), result.getAbout());
        assertNull(result.getAvatarImageId());
        assertEquals(5, result.getAnnouncementsCount());
    }

    @Test
    void getUser_ReturnsNotFound_WhenUserDoesNotExist() {
        String userId = uuidService.generate();

        given()
                .when()
                .get("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }



    @Test
    void updateUser_UpdatesUserAndSendsEvent_WhenUserExists() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .nickname("johndoe")
                .email("john@example.com")
                .phone("+1234567890")
                .address("123 Main St")
                .registeredAt(ZonedDateTime.now())
                .about("About John")
                .avatarImageId("avatar123")
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(Integer.class),
                Optional.ofNullable(ArgumentMatchers.any())))
                .thenReturn(5);

        UserDto result = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(Map.of(
                        "name", "newName",
                        "surname", "newSurname",
                        "nickname", "new-nickname",
                        "email", "new@mail.ru",
                        "phone", "+7777777777",
                        "address", "new street",
                        "about", "new about"
                ))
                .put("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.OK.value()).extract().as(UserDto.class);

        assertEquals("newName", result.getName());
        assertEquals("newSurname", result.getSurname());
        assertEquals("new-nickname", result.getNickname());
        assertEquals("new@mail.ru", result.getEmail());
        assertEquals("+7777777777", result.getPhone());
        assertEquals("new street", result.getAddress());
        assertEquals("new about", result.getAbout());
        assertEquals(user.getAvatarImageId(), result.getAvatarImageId());
        assertEquals(5, result.getAnnouncementsCount());

        UserChangedEvent userChangedEvent = UserChangedEvent.fromMap(testConsumer.userChangedEventMessages.getFirst());
        assertEquals(userId, userChangedEvent.getId());
        assertEquals(userId, userChangedEvent.getUserId());
        assertEquals("newName", userChangedEvent.getName());
        assertEquals("newSurname", userChangedEvent.getSurname());
        assertEquals("new-nickname", userChangedEvent.getNickname());
        assertEquals("new@mail.ru", userChangedEvent.getEmail());
        assertEquals("+7777777777", userChangedEvent.getPhone());
        assertEquals("new street", userChangedEvent.getAddress());
        assertEquals("new about", userChangedEvent.getAbout());
    }

    @Test
    void updateUser_ReturnsNotFound_WhenUserDoesNotExist() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .nickname("johndoe")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        userRepository.delete(user);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(Map.of(
                        "name", "newName",
                        "surname", "newSurname",
                        "nickname", "new-nickname",
                        "email", "new@mail.ru",
                        "phone", "+7777777777",
                        "address", "new street",
                        "about", "new about"
                ))
                .put("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateUser_ReturnsForbidden_WhenUpdatingAnotherUser() {
        String userId = uuidService.generate();

        String currentUserId = uuidService.generate();
        User currentUser = User.builder()
                .id(currentUserId)
                .nickname("currentUser")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        userRepository.save(currentUser);

        String accessToken = jwtService.generateAccess(currentUser);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(Map.of(
                        "name", "newName",
                        "surname", "newSurname",
                        "nickname", "new-nickname",
                        "email", "new@mail.ru",
                        "phone", "+7777777777",
                        "address", "new street",
                        "about", "new about"
                ))
                .put("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}