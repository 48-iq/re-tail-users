package dev.ilya_anna.user_service.controllers;

import com.redis.testcontainers.RedisContainer;
import dev.ilya_anna.user_service.dto.UserSettingsDto;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.repositories.UserRepository;
import dev.ilya_anna.user_service.repositories.UserSettingsRepository;
import dev.ilya_anna.user_service.services.JwtService;
import dev.ilya_anna.user_service.services.UserSettingsService;
import dev.ilya_anna.user_service.services.UuidService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
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
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

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
public class UserSettingsControllerTests {
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UuidService uuidService;

    @Autowired
    private JwtService jwtService;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void beforeEach() {
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1/user-settings";
        userRepository.deleteAll();
        userSettingsRepository.deleteAll();
    }

    @Test
    void getUserSettings_ReturnsUserSettings_WhenUserExistsAndDoesNotHasSettings() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .nickname("johndoe")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        userRepository.save(user);

        UserSettingsDto result = given()
                .when()
                .get("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.OK.value()).extract().as(UserSettingsDto.class);

        assertTrue(result.isNameVisibility());
        assertTrue(result.isSurnameVisibility());
        assertTrue(result.isPhoneVisibility());
        assertTrue(result.isEmailVisibility());
        assertTrue(result.isAddressVisibility());
        assertTrue(result.isAvatarVisibility());
    }

    @Test
    void getUserSettings_ReturnsUserSettings_WhenUserExistsAndHasSettings() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .nickname("johndoe")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
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

        UserSettingsDto result = given()
                .when()
                .get("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.OK.value()).extract().as(UserSettingsDto.class);

        assertTrue(result.isNameVisibility());
        assertFalse(result.isSurnameVisibility());
        assertTrue(result.isPhoneVisibility());
        assertTrue(result.isEmailVisibility());
        assertFalse(result.isAddressVisibility());
        assertFalse(result.isAvatarVisibility());
    }

    @Test
    void getUserSettings_ReturnsNotFound_WhenUserDoesNotExist() {
        String userId = uuidService.generate();

        given()
                .when()
                .get("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }
    @Test
    void updateUserSettings_UpdatesUserSettings_WhenUserExists() {
        String userId = uuidService.generate();
        User user = User.builder()
                .id(userId)
                .nickname("johndoe")
                .registeredAt(ZonedDateTime.now(ZoneId.systemDefault()))
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        UserSettingsDto result = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .body(Map.of(
                        "nameVisibility", false,
                        "surnameVisibility", false,
                        "emailVisibility", false,
                        "phoneVisibility", false,
                        "addressVisibility", false,
                        "avatarVisibility", false
                ))
                .put("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.OK.value()).extract().as(UserSettingsDto.class);

        assertFalse(result.isNameVisibility());
        assertFalse(result.isSurnameVisibility());
        assertFalse(result.isPhoneVisibility());
        assertFalse(result.isEmailVisibility());
        assertFalse(result.isAddressVisibility());
        assertFalse(result.isAvatarVisibility());
    }

    @Test
    void updateUserSettings_ReturnsNotFound_WhenUserDoesNotExist() {
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
                        "nameVisibility", false,
                        "surnameVisibility", false,
                        "emailVisibility", false,
                        "phoneVisibility", false,
                        "addressVisibility", false,
                        "avatarVisibility", false
                ))
                .put("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateUserSettings_ReturnsForbidden_WhenUpdatingAnotherUserSettings() {
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
                        "nameVisibility", false,
                        "surnameVisibility", false,
                        "emailVisibility", false,
                        "phoneVisibility", false,
                        "addressVisibility", false,
                        "avatarVisibility", false
                ))
                .put("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
