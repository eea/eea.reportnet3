package org.eea.ums.configuration;

import org.eea.security.jwt.configuration.EeaEnableSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * The Class UserManagementConfiguration.
 */
@Configuration
@EeaEnableSecurity
public class UserManagementConfiguration implements WebMvcConfigurer {


  /** The url. */
  @Value("${spring.datasource.metasource.url}")
  private String url;

  /** The username. */
  @Value("${spring.datasource.metasource.username}")
  private String username;

  /** The password. */
  @Value("${spring.datasource.metasource.password}")
  private String password;

  /** The driver. */
  @Value("${spring.datasource.metasource.driver-class-name}")
  private String driver;


  /**
   * Data source data source.
   *
   * @return the data source
   */
  @Bean
  @Primary
  public DataSource metabaseDatasource() {
    DriverManagerDataSource metaDataSource = new DriverManagerDataSource();
    metaDataSource.setDriverClassName(driver);
    metaDataSource.setUrl(url);
    metaDataSource.setUsername(username);
    metaDataSource.setPassword(password);

    return metaDataSource;
  }

  /**
   * Rest template.
   *
   * @return the rest template
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
