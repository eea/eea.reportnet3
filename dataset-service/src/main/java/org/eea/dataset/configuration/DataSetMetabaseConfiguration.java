/**
 *
 */
package org.eea.dataset.configuration;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
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
 * The Class DataSetMetabaseConfiguration.
 *
 *
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(entityManagerFactoryRef = "metadataSetsEntityManagerFactory",
    transactionManagerRef = "metabaseDataSetsTransactionManager",
    basePackages = "org.eea.dataset.persistence.metabase.repository")
@EnableCaching
public class DataSetMetabaseConfiguration implements WebMvcConfigurer {

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
   * Metadata sets entity manager factory.
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Qualifier("metadataSetsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean metadataSetsEntityManagerFactory() {
    LocalContainerEntityManagerFactoryBean metadataSetsEM =
        new LocalContainerEntityManagerFactoryBean();
    metadataSetsEM.setDataSource(metabaseDatasource());
    metadataSetsEM.setPackagesToScan("org.eea.dataset.persistence.metabase.domain");
    JpaVendorAdapter vendorMetabaseAdapter = new HibernateJpaVendorAdapter();
    metadataSetsEM.setJpaVendorAdapter(vendorMetabaseAdapter);
    metadataSetsEM.setJpaProperties(additionalMetaProperties());
    return metadataSetsEM;
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
   * Metabase data sets transaction manager.
   *
   * @return the platform transaction manager
   */
  @Bean
  public PlatformTransactionManager metabaseDataSetsTransactionManager() {

    JpaTransactionManager metabasetransactionManager = new JpaTransactionManager();
    metabasetransactionManager
        .setEntityManagerFactory(metadataSetsEntityManagerFactory().getObject());
    return metabasetransactionManager;
  }


}
