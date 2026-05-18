package com.project.likelion14thbe.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 실 DB 통합 테스트 공통 베이스.
 *
 * <p>기존 {@link RedisTestContainer}를 상속하여 Redis 싱글톤을 재사용하고, 여기에
 * MySQL 싱글톤 컨테이너를 추가한다. {@code @SpringBootTest}는 메인 컨텍스트를 로드하므로
 * Spring Data Redis 빈도 함께 기동되는데, 단일 클래스로 두 컨테이너를 모두 배선해
 * "기동 중 Redis 접근" 실패 위험과 Java 단일 상속 제약을 동시에 해소한다.
 *
 * <p>컨테이너는 {@code static} 싱글톤으로 한 번만 기동하여 테스트 클래스 간 재사용한다
 * ({@code @Container static}의 stale 포트 문제 회피). 스키마는 엔티티에서 새로 생성한다
 * ({@code ddl-auto=create-drop}) — {@code social_account}의 유니크 제약을 포함한다.
 */
public abstract class AbstractDbIntegrationTest extends RedisTestContainer {

    public static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0")).withDatabaseName("likelion_test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void mysqlProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
