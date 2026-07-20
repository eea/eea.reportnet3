package org.eea.orchestrator.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Spring configuration for the Dremio JDBC integration.
 *
 * <p>Registers the following beans:
 * <ul>
 *   <li>{@link DataSource} — a {@link DriverManagerDataSource} configured with the Dremio
 *       JDBC driver, URL, username, and password.</li>
 *   <li>{@link JdbcTemplate} — a JDBC template backed by the Dremio datasource, used for
 *       executing SQL queries against Dremio (e.g. the {@code SELECT 1} health check).</li>
 * </ul>
 *
 * <p>HTTP connectivity to Dremio is handled separately via the {@code DremioApiController}
 * Feign client, which reads its URL from
 * {@code spring.cloud.openfeign.client.config.dremioClient.url}. No {@link
 * org.springframework.web.client.RestTemplate} is needed here.
 *
 * <p>Required properties (set in {@code application.yml} or Consul):
 * <ul>
 *   <li>{@code dremio.url} — JDBC connection URL,
 *       e.g. {@code jdbc:dremio:direct=dremio-host:31010}</li>
 *   <li>{@code dremio.username} — Dremio username</li>
 *   <li>{@code dremio.password} — Dremio password</li>
 *   <li>{@code dremio.driver-class-name} — fully qualified JDBC driver class,
 *       e.g. {@code com.dremio.jdbc.Driver}</li>
 * </ul>
 */
@Configuration
public class DremioConfiguration {

    /**
     * JDBC connection URL for the Dremio instance.
     * Example: {@code jdbc:dremio:direct=dremio-host:31010}
     */
    @Value("${dremio.url}")
    private String url;

    /**
     * Dremio username for JDBC authentication.
     */
    @Value("${dremio.username}")
    private String username;

    /**
     * Dremio password for JDBC authentication.
     */
    @Value("${dremio.password}")
    private String password;

    /**
     * Fully qualified class name of the Dremio JDBC driver.
     * Example: {@code com.dremio.jdbc.Driver}
     */
    @Value("${dremio.driver-class-name}")
    private String driver;

    /**
     * Creates and configures the Dremio {@link DataSource}.
     *
     * <p>Uses {@link DriverManagerDataSource} (non-pooling), which is appropriate here
     * since connections are only opened during scheduled health-check runs rather than
     * continuously.
     *
     * <p>Named explicitly as {@code "dremioDatasource"} to avoid conflicts with any other
     * {@link DataSource} beans registered in the application context.
     *
     * @return a {@link DataSource} configured with the Dremio JDBC driver and credentials
     */
    @Bean
    public DataSource dremioDatasource() {
        DriverManagerDataSource dremioDataSource = new DriverManagerDataSource();
        dremioDataSource.setDriverClassName(driver);
        dremioDataSource.setUrl(url);
        dremioDataSource.setUsername(username);
        dremioDataSource.setPassword(password);
        return dremioDataSource;
    }
}