package pe.edu.utp.cinestar.movie_service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@org.springframework.context.annotation.Import(AbstractIntegrationTest.TestCacheConfig.class)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15"
    )
    .withDatabaseName("test_db")
    .withUsername("postgres")
    .withPassword("root");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @org.springframework.boot.test.context.TestConfiguration
    public static class TestCacheConfig {
        @org.springframework.context.annotation.Bean
        public org.springframework.cache.CacheManager cacheManager() {
            return new org.springframework.cache.concurrent.ConcurrentMapCacheManager();
        }
    }
}
