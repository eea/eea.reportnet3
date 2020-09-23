package org.eea.lock.configuration;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
 * The Class LockMetabaseConfiguration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(entityManagerFactoryRef = "lockEntityManagerFactory",
    transactionManagerRef = "lockTransactionManager",
    basePackages = "org.eea.lock.persistence.repository")
public class LockMetabaseConfiguration implements WebMvcConfigurer {

  /**
   * The dll.
   */
  @Value("${spring.jpa.hibernate.metabase.ddl-auto}")
  private String dll;

  /**
   * The dialect.
   */
  @Value("${spring.jpa.properties.hibernate.dialect}")
  private String dialect;

  /**
   * The create clob propertie.
   */
  @Value("${spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation}")
  private String createClobPropertie;

  /**
   * The url.
   */
  @Value("${spring.datasource.metasource.url}")
  private String url;

  /**
   * The username.
   */
  @Value("${spring.datasource.metasource.username}")
  private String username;

  /**
   * The password.
   */
  @Value("${spring.datasource.metasource.password}")
  private String password;

  /**
   * The driver.
   */
  @Value("${spring.datasource.metasource.driver-class-name}")
  private String driver;


  /**
   * Creates a data source to the database where Lock entities are created. This datasource will be
   * created only if there are not a metabase datasource already created
   *
   * @return the data source
   */
  @Bean
  @ConditionalOnMissingBean(name = {"metabaseDatasource"})
  public DataSource metabaseDatasource() {
    DriverManagerDataSource lockDataSource = new DriverManagerDataSource();
    lockDataSource.setDriverClassName(driver);
    lockDataSource.setUrl(url);
    lockDataSource.setUsername(username);
    lockDataSource.setPassword(password);

    return lockDataSource;
  }

  /**
   * Lock entity manager factory.
   *
   * @param dataSource the data source
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Qualifier("lockEntityManagerFactory")
  @Autowired
  public LocalContainerEntityManagerFactoryBean lockEntityManagerFactory(
      @Qualifier("metabaseDatasource") DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean lcemfb = new LocalContainerEntityManagerFactoryBean();
    lcemfb.setDataSource(dataSource);
    lcemfb.setPackagesToScan("org.eea.lock.persistence.domain");
    JpaVendorAdapter vendorMetabaseAdapter = new HibernateJpaVendorAdapter();
    lcemfb.setJpaVendorAdapter(vendorMetabaseAdapter);
    lcemfb.setJpaProperties(additionalMetaProperties());
    return lcemfb;
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
   * Lock transaction manager.
   *
   * @param dataSource the data source
   *
   * @return the platform transaction manager
   */
  @Bean
  @Autowired
  public PlatformTransactionManager lockTransactionManager(
      @Qualifier("metabaseDatasource") DataSource dataSource) {

    JpaTransactionManager locktransactionManager = new JpaTransactionManager();
    locktransactionManager
        .setEntityManagerFactory(lockEntityManagerFactory(dataSource).getObject());
    return locktransactionManager;
  }
}
