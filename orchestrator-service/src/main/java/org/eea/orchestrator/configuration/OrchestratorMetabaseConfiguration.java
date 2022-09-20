/**
 *
 */
package org.eea.orchestrator.configuration;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * The Class OrchestratorMetabaseConfiguration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(entityManagerFactoryRef = "orchestratorEntityManagerFactory",
    transactionManagerRef = "orchestratorTransactionManager",
    basePackages = "org.eea.orchestrator.persistence.repository")
@EnableCaching
public class OrchestratorMetabaseConfiguration implements WebMvcConfigurer {

  /** The dll. */
  @Value("${spring.jpa.hibernate.metabase.ddl-auto}")
  private String dll;

  /** The dialect. */
  @Value("${spring.jpa.properties.hibernate.dialect}")
  private String dialect;

  /** The create clob propertie. */
  @Value("${spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation}")
  private String createClobPropertie;

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
   * Metabase datasource.
   *
   * @return the data source
   */
  @Bean
  public DataSource metabaseDatasource() {
    DriverManagerDataSource metaDataSource = new DriverManagerDataSource();
    metaDataSource.setDriverClassName(driver);
    metaDataSource.setUrl(url);
    metaDataSource.setUsername(username);
    metaDataSource.setPassword(password);

    return metaDataSource;
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
    LocalContainerEntityManagerFactoryBean orchestratorEM =
        new LocalContainerEntityManagerFactoryBean();
    orchestratorEM.setDataSource(metabaseDatasource());
    orchestratorEM.setPackagesToScan("org.eea.orchestrator.persistence.domain");
    JpaVendorAdapter vendorMetabaseAdapter = new HibernateJpaVendorAdapter();
    orchestratorEM.setJpaVendorAdapter(vendorMetabaseAdapter);
    orchestratorEM.setJpaProperties(additionalMetaProperties());
    return orchestratorEM;
  }

  /**
   * Additional meta properties.
   *
   * @return the properties
   */
  private Properties additionalMetaProperties() {
    Properties metaProperties = new Properties();
    metaProperties.setProperty("hibernate.hbm2ddl.auto", dll);
    metaProperties.setProperty("hibernate.dialect", dialect);
    metaProperties.setProperty("hibernate.jdbc.lob.non_contextual_creation", createClobPropertie);
    return metaProperties;
  }

  /**
   * Orchestrator transaction manager.
   *
   * @return the platform transaction manager
   */
  @Bean
  @Primary
  public PlatformTransactionManager orchestratorTransactionManager() {

    JpaTransactionManager orchestratorTransactionManager = new JpaTransactionManager();
    orchestratorTransactionManager
        .setEntityManagerFactory(orchestratorEntityManagerFactory().getObject());
    return orchestratorTransactionManager;
  }


}
