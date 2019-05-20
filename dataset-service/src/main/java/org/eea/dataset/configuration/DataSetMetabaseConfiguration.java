/**
 * 
 */
package org.eea.dataset.configuration;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
 * @author Mario Severa
 *
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(entityManagerFactoryRef = "metadataSetsEntityManagerFactory",
    transactionManagerRef = "metabaseDataSetsTransactionManager",
    basePackages = "org.eea.dataset.metabase.repository")

public class DataSetMetabaseConfiguration implements WebMvcConfigurer {

  @Value("${spring.jpa.hibernate.ddl-auto}")
  private String dll;
  @Value("${spring.jpa.properties.hibernate.dialect}")
  private String dialect;
  @Value("${spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation}")
  private String createClobPropertie;
  @Value("${spring.datasource.metasource.url}")
  private String url;
  @Value("${spring.datasource.metasource.username}")
  private String username;
  @Value("${spring.datasource.metasource.password}")
  private String password;
  @Value("${spring.datasource.metasource.driver-class-name}")
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

  @Bean
  @Autowired
  @Qualifier("metadataSetsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean metadataSetsEntityManagerFactory() {
    LocalContainerEntityManagerFactoryBean metadataSetsEM =
        new LocalContainerEntityManagerFactoryBean();
    metadataSetsEM.setDataSource(metaBaseDataSource());
    metadataSetsEM.setPackagesToScan("org.eea.dataset.metabase.domain");
    JpaVendorAdapter vendorMetabaseAdapter = new HibernateJpaVendorAdapter();
    metadataSetsEM.setJpaVendorAdapter(vendorMetabaseAdapter);
    metadataSetsEM.setJpaProperties(additionalMetaProperties());
    return metadataSetsEM;
  }

  private Properties additionalMetaProperties() {
    Properties metaProperties = new Properties();
    metaProperties.setProperty("hibernate.hbm2ddl.auto", dll);
    metaProperties.setProperty("hibernate.dialect", dialect);
    metaProperties.setProperty("hibernate.jdbc.lob.non_contextual_creation", createClobPropertie);
    return metaProperties;
  }

  @Bean
  @Autowired
  public PlatformTransactionManager metabaseDataSetsTransactionManager(
      @Qualifier("metadataSetsEntityManagerFactory") LocalContainerEntityManagerFactoryBean metadataSetsEntityManagerFactory) {

    JpaTransactionManager metabasetransactionManager = new JpaTransactionManager();
    metabasetransactionManager
        .setEntityManagerFactory(metadataSetsEntityManagerFactory().getObject());
    return metabasetransactionManager;
  }


}
