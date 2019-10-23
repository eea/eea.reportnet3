package org.eea.dataflow.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class DataflowConfiguration.
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.eea.dataflow.persistence.repository")
@EnableTransactionManagement
@EntityScan(basePackages = "org.eea.dataflow.persistence.domain")
@EnableWebMvc
public class DataflowConfiguration implements WebMvcConfigurer {


  /**
   * The url.
   */
  @Value("${spring.datasource.url}")
  private String url;

  /**
   * The username.
   */
  @Value("${spring.datasource.username}")
  private String username;

  /**
   * The password.
   */
  @Value("${spring.datasource.password}")
  private String password;

  /**
   * The driver.
   */
  @Value("${spring.datasource.driver-class-name}")
  private String driver;

  /**
   * Data source data source.
   *
   * @return the data source
   */
  @Bean
  public DataSource metaBaseDataSource() {
    DriverManagerDataSource metaDataSource = new DriverManagerDataSource();
    metaDataSource.setDriverClassName(driver);
    metaDataSource.setUrl(url);
    metaDataSource.setUsername(username);
    metaDataSource.setPassword(password);

    return metaDataSource;
  }
}
