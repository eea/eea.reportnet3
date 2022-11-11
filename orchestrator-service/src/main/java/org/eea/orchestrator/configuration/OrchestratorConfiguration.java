package org.eea.orchestrator.configuration;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ISOLATION_LEVEL_CONFIG;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eea.kafka.domain.EEAEventVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
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
    properties.setProperty("hibernate.hbm2ddl.auto", "update");
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
    properties.setProperty("hibernate.show_sql", "false");
    properties.setProperty("hibernate.flushMode", "commit");
    return properties;
  }
}
