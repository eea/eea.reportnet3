package org.eea.recordstore.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * The Class RecordStoreConfiguration.
 */
@Configuration
public class RecordStoreConfiguration {

  /**
   * The connection url.
   */
  @Value("${spring.datasource.url}")
  private String connectionUrl;

  /**
   * The connection username.
   */
  @Value("${spring.datasource.dataset.username}")
  private String connectionUsername;

  /**
   * The connection password.
   */
  @Value("${spring.datasource.dataset.password}")
  private String connectionPassword;

  /**
   * The connection driver.
   */
  @Value("${spring.datasource.driverClassName}")
  private String connectionDriver;

  /**
   * Data source.
   *
   * @return the data source
   */
  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(connectionDriver);
    dataSource.setUrl(connectionUrl);
    dataSource.setUsername(connectionUsername);
    dataSource.setPassword(connectionPassword);

    return dataSource;
  }

  /**
   * Jdbc template.
   *
   * @param dataSource the data source
   *
   * @return the jdbc template
   */
  @Bean
  @Autowired
  public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}
