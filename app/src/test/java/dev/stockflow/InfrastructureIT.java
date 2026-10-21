package dev.stockflow;

import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** Infrastructure contract, not an example business implementation. Requires Docker. */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InfrastructureIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            System.getProperty("stockflow.postgres.image",
                    "postgres@sha256:97ff59a4e30e08d1c11bdcd9455e7832368c0572b576c9092cde2df4ae5552a3"))
            .withDatabaseName("stockflow_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    DataSource dataSource;

    @Value("${local.server.port}")
    int port;

    @Test
    void migrationCreatesTechnicalMarkerInRealPostgres() {
        assertThat(jdbc.queryForObject("select description from platform_marker where id = 1", String.class))
                .isEqualTo("Local learning platform is ready");
    }

    @Test
    void httpReadinessIncludesWorkingDatabase() throws Exception {
        try (var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/actuator/health/readiness"))
                    .timeout(Duration.ofSeconds(5)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\":\"UP\"");
        }
    }

    @Test
    void runningMigrationsAgainDoesNotDuplicateDataOrHistory() throws Exception {
        var before = jdbc.queryForObject("select count(*) from databasechangelog", Long.class);
        var migration = new SpringLiquibase();
        migration.setDataSource(dataSource);
        migration.setChangeLog("classpath:db/changelog/db.changelog-master.yaml");
        migration.setResourceLoader(new DefaultResourceLoader());
        migration.afterPropertiesSet();

        assertThat(jdbc.queryForObject("select count(*) from databasechangelog", Long.class)).isEqualTo(before);
        assertThat(jdbc.queryForObject("select count(*) from platform_marker", Long.class)).isEqualTo(1L);
    }
}
