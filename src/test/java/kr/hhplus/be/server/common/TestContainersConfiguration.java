package kr.hhplus.be.server.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {
    @Bean
    @ServiceConnection
    public MySQLContainer<?> mySqlContainer() {
        MySQLContainer<?> container = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                // 초기화 대기전략 설정
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
                // 컨테이너 재사용으로 속도 향상
                .withReuse(true)
                // 연결 타임아웃 설정
                .withConnectTimeoutSeconds(30)
                // MySQL 설정 최적화
                .withCommand(
                        "--character-set-server=utf8mb4",
                        "--collation-server=utf8mb4_unicode_ci",
                        "--max_connections=1000",  // 동시 연결 수 증가
                        "--innodb_lock_wait_timeout=50"  // 락 대기 시간 50초
                );
        container.start();

        log.info("===========================================");
        log.info("🐳 Testcontainers MySQL 시작 완료");
        log.info("===========================================");
        log.info("JDBC URL: {}", container.getJdbcUrl());
        log.info("Username: {}", container.getUsername());
        log.info("Database: {}", container.getDatabaseName());
        log.info("Container ID: {}", container.getContainerId());
        log.info("===========================================");

        return container;
    }

    // Redis 컨테이너 추가
    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(6379)
                .withReuse(true);  // 컨테이너 재사용
    }
}
