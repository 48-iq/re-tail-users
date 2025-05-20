package dev.ilya_anna.user_service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import com.redis.testcontainers.RedisContainer;

@Testcontainers
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
        }
)
class UserServiceApplicationTests {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
            .withDatabaseName("user_service")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("init.sql");

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:latest"))
            .withEnv("REDIS_PASSWORD", "password");

    @Container
    static MinIOContainer minio = new MinIOContainer(DockerImageName.parse("minio/minio:RELEASE.2023-09-04T19-57-37Z"))
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data");

    @BeforeAll
    static void beforeAll() {
        redis.start();
        postgres.start();
        kafka.start();
        minio.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
        postgres.stop();
        kafka.stop();
        minio.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.database", () -> "0");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redis.getFirstMappedPort()));
        registry.add("spring.data.redis.password", () -> "password");

        registry.add("app.minio.endpoint", () ->
                String.format("http://%s:%d", minio.getHost(), minio.getFirstMappedPort()));
        registry.add("app.minio.accessKey", () -> "minioadmin");
        registry.add("app.minio.secretKey", () -> "minioadmin");
    }

    @Test
    void contextLoads() {
    }
}