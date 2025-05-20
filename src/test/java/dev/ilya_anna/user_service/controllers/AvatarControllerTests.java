package dev.ilya_anna.user_service.controllers;

import com.redis.testcontainers.RedisContainer;
import dev.ilya_anna.user_service.dto.UserDto;
import dev.ilya_anna.user_service.entities.AvatarMetadata;
import dev.ilya_anna.user_service.entities.User;
import dev.ilya_anna.user_service.repositories.AvatarMetadataRepository;
import dev.ilya_anna.user_service.repositories.UserRepository;
import dev.ilya_anna.user_service.services.JwtService;
import dev.ilya_anna.user_service.services.UuidService;
import io.minio.*;
import io.minio.errors.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
public class AvatarControllerTests {
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

    @Container
    static MinIOContainer minio = new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2023-09-04T19-57-37Z"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data");

    @BeforeAll
    static void beforeAll() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        redis.start();
        redis.withEnv("REDIS_PASSWORD", "password");
        postgres.start();
        minio.start();

        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://" + minio.getHost() + ":" + minio.getFirstMappedPort())
                .credentials("minioadmin", "minioadmin")
                .build();

        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket("avatars").build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("avatars").build());
        }
    }

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:latest"));


    @AfterAll
    static void afterAll() {
        redis.stop();
        postgres.stop();
        minio.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.database", () -> "0");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.data.redis.password", () -> "password");
        registry.add("app.minio.endpoint", () ->
                String.format("http://%s:%d", minio.getHost(), minio.getFirstMappedPort()));
        registry.add("app.minio.accessKey", () -> "minioadmin");
        registry.add("app.minio.secretKey", () -> "minioadmin");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AvatarMetadataRepository avatarMetadataRepository;

    @Autowired
    private UuidService uuidService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MinioClient minioClient;

    @MockitoBean
    private RestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void beforeEach(){
        RestAssured.baseURI = "http://localhost:" + port + "/api/v1/user-avatars";
        userRepository.deleteAll();
        avatarMetadataRepository.deleteAll();
    }

    @Test
    void getUserAvatar_ReturnsUserAvatar_WhenAvatarExists() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String avatarId = uuidService.generate();
        String avatarPath = "users/123/avatar/" + avatarId + ".jpg";

        AvatarMetadata avatarMetadata = AvatarMetadata.builder()
                .id(avatarId)
                .avatarPath(avatarPath)
                .build();
        avatarMetadataRepository.save(avatarMetadata);

        byte[] testImage = Files.readAllBytes(Paths.get("src/test/resources/test-avatar.jpg"));
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("avatars")
                        .object(avatarPath)
                        .stream(new ByteArrayInputStream(testImage), testImage.length, -1)
                        .build());

        byte[] result = given()
                .when()
                .get("/{avatarId}", avatarId)
                .then()
                .log().headers()
                .statusCode(HttpStatus.OK.value()).extract().body().asByteArray();

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(image).isNotNull();
    }

    @Test
    void getUserAvatar_ReturnsNotFound_WhenAvatarDoesNotExist(){
        given()
                .when()
                .get("/{avatarId}", "nonexistentAvatarId")
                .then()
                .log().headers()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateUserAvatar_UpdatesUserAvatar_WhenUserExistsAndDoesNotHaveAvatar() throws IOException {
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
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(Integer.class),
                Optional.ofNullable(ArgumentMatchers.any())))
                .thenReturn(5);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test-avatar.jpg"));

        UserDto result = given()
                .contentType(ContentType.MULTIPART)
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("avatar", "test-avatar.jpg", imageBytes, "image/jpeg")
                .when()
                .post("/{userId}", userId)
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
        assertNotNull(result.getAvatarImageId());
        assertEquals(5, result.getAnnouncementsCount());
    }

    @Test
    void updateUserAvatar_UpdatesUserAvatar_WhenUserExistsAndHasAvatar() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String userId = uuidService.generate();

        String avatarId = uuidService.generate();
        String avatarPath = "users/" + userId+ "/avatar/" + avatarId + ".jpg";

        AvatarMetadata avatarMetadata = AvatarMetadata.builder()
                .id(avatarId)
                .avatarPath(avatarPath)
                .build();
        avatarMetadataRepository.save(avatarMetadata);

        byte[] testImage = Files.readAllBytes(Paths.get("src/test/resources/test-avatar.jpg"));
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("avatars")
                        .object(avatarPath)
                        .stream(new ByteArrayInputStream(testImage), testImage.length, -1)
                        .build());

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
                .avatarImageId(avatarId)
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        when(restTemplate.getForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(Integer.class),
                Optional.ofNullable(ArgumentMatchers.any())))
                .thenReturn(5);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test-avatar2.jpg"));

        UserDto result = given()
                .contentType(ContentType.MULTIPART)
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("avatar", "test-avatar.jpg", imageBytes, "image/jpeg")
                .when()
                .post("/{userId}", userId)
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
        assertNotEquals(avatarId, result.getAvatarImageId());
        assertEquals(5, result.getAnnouncementsCount());
    }

    @Test
    void updateUserAvatar_ReturnsNotFound_WhenUserDoesNotExist() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
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
                .build();
        userRepository.save(user);

        String accessToken = jwtService.generateAccess(user);

        userRepository.delete(user);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test-avatar.jpg"));

        given()
                .contentType(ContentType.MULTIPART)
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("avatar", "test-avatar.jpg", imageBytes, "image/jpeg")
                .when()
                .post("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateUserAvatar_ReturnsForbidden_WhenUpdatingAnotherUserAvatar() throws IOException {
        String userId = uuidService.generate();

        String currentUserId = uuidService.generate();
        User currentUser = User.builder()
                .id(currentUserId)
                .nickname("currentUser")
                .registeredAt(ZonedDateTime.now())
                .build();
        userRepository.save(currentUser);

        String accessToken = jwtService.generateAccess(currentUser);

        byte[] imageBytes = Files.readAllBytes(Paths.get("src/test/resources/test-avatar.jpg"));

        given()
                .contentType(ContentType.MULTIPART)
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("avatar", "test-avatar.jpg", imageBytes, "image/jpeg")
                .when()
                .post("/{userId}", userId)
                .then()
                .log().body()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
