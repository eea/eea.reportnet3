package org.eea.recordstore.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DremioConfiguration {

    @Value("${dremio.url}")
    private String url;

    /**
     * The username.
     */
    @Value("${dremio.username}")
    private String username;

    /**
     * The password.
     */
    @Value("${dremio.password}")
    private String password;

    /**
     * The driver.
     */
    @Value("${dremio.driver-class-name}")
    private String driver;

    @Bean
    public DataSource dremioDatasource() {
        DriverManagerDataSource dremioDataSource = new DriverManagerDataSource();
        dremioDataSource.setDriverClassName(driver);
        dremioDataSource.setUrl(url);
        dremioDataSource.setUsername(username);
        dremioDataSource.setPassword(password);
        return dremioDataSource;
    }

    @Bean
    public JdbcTemplate dremioJdbcTemplate() {
        return new JdbcTemplate(dremioDatasource());
    }
}

























