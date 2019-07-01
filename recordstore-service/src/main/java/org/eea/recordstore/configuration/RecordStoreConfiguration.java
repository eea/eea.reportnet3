package org.eea.recordstore.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class RecordStoreConfiguration {

  @Value("${spring.datasource.url}")
  private String connectionUrl;
  @Value("${spring.datasource.username}")
  private String connectionUsername;
  @Value("${spring.datasource.password}")
  private String connectionPassword;
  @Value("${spring.datasource.driverClassName}")
  private String connectionDriver;

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(connectionDriver);
    dataSource.setUrl(connectionUrl);
    dataSource.setUsername(connectionUsername);
    dataSource.setPassword(connectionPassword);

    return dataSource;
  }

  @Bean
  @Autowired
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    return jdbcTemplate;
  }
}
