package org.eea.orchestrator.configuration;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * The Class OrchestratorConfiguration.
 */
@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "orchestratorEntityManagerFactory",
        transactionManagerRef = "orchestratorTransactionManager",
        basePackages = "org.eea.orchestrator.persistence.repository")
@EnableTransactionManagement
@EntityScan(basePackages = "org.eea.orchestrator.persistence.domain")
public class OrchestratorConfiguration {

  /**
   * The url.
   */
  @Value("${spring.datasource.orchestratorDb.url}")
  private String url;

  /**
   * The username.
   */
  @Value("${spring.datasource.orchestratorDb.username}")
  private String username;

  /**
   * The password.
   */
  @Value("${spring.datasource.orchestratorDb.password}")
  private String password;

  /**
   * The driver.
   */
  @Value("${spring.datasource.orchestratorDb.driver-class-name}")
  private String driver;

  /** The dll. */
  @Value("${spring.jpa.hibernate.ddl-auto}")
  private String dll;

  /** The dialect. */
  @Value("${spring.jpa.properties.hibernate.dialect}")
  private String dialect;

  /** The show sql. */
  @Value("${spring.jpa.hibernate.show-sql}")
  private String showSql;

  /** The flush mode. */
  @Value("${spring.jpa.hibernate.flushMode}")
  private String flushMode;

  /**
   * Data source data source.
   *
   * @return the data source
   */
  @Bean
  public DataSource orchestratorDatasource() {
    DriverManagerDataSource orchestratorDataSource = new DriverManagerDataSource();
    orchestratorDataSource.setDriverClassName(driver);
    orchestratorDataSource.setUrl(url);
    orchestratorDataSource.setUsername(username);
    orchestratorDataSource.setPassword(password);

    return orchestratorDataSource;
  }

  /**
   * Orchestrator entity manager factory.
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Primary
  @Qualifier("orchestratorEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean orchestratorEntityManagerFactory() {
    final LocalContainerEntityManagerFactoryBean orchestratorEM =
            new LocalContainerEntityManagerFactoryBean();
    orchestratorEM.setDataSource(orchestratorDatasource());
    orchestratorEM.setPackagesToScan("org.eea.orchestrator.persistence.domain");
    orchestratorEM.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    orchestratorEM.setJpaProperties(additionalProperties());
    final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    orchestratorEM.setJpaVendorAdapter(vendorAdapter);

    return orchestratorEM;
  }

  /**
   * Orchestrator transaction manager.
   *
   * @param entityManagerFactory the entity manager factory
   *
   * @return the platform transaction manager
   */
  @Bean
  @Primary
  public PlatformTransactionManager orchestratorTransactionManager(
          @Autowired EntityManagerFactory entityManagerFactory) {
    final JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(orchestratorEntityManagerFactory().getObject());
    return transactionManager;
  }

  /**
   * Additional properties.
   *
   * @return the properties
   */
  private Properties additionalProperties() {
    final Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", dll);
    properties.setProperty("hibernate.dialect", dialect);
    properties.setProperty("hibernate.show_sql", showSql);
    properties.setProperty("hibernate.flushMode", flushMode);
    return properties;
  }
}
