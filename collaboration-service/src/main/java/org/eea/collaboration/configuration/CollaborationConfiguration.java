package org.eea.collaboration.configuration;

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
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
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class CollaborationConfiguration.
 */
@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EntityScan(basePackages = "org.eea.collaboration.persistence.domain")
@EnableJpaRepositories(entityManagerFactoryRef = "collaborationEntityManagerFactory",
    transactionManagerRef = "collaborationTransactionManager",
    basePackages = "org.eea.collaboration.persistence.repository")
public class CollaborationConfiguration implements WebMvcConfigurer {

  /** The driver. */
  @Value("${spring.datasource.metasource.driver-class-name}")
  private String driver;

  /** The url. */
  @Value("${spring.datasource.metasource.url}")
  private String url;

  /** The username. */
  @Value("${spring.datasource.metasource.username}")
  private String username;

  /** The password. */
  @Value("${spring.datasource.metasource.password}")
  private String password;

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

  /** The max file size. */
  @Value("${spring.servlet.multipart.max-file-size}")
  private Long maxFileSize;

  /** The max request size. */
  @Value("${spring.servlet.multipart.max-request-size}")
  private Long maxRequestSize;

  /**
   * Collaboration entity manager factory.
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Primary
  @Qualifier("collaborationEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean collaborationEntityManagerFactory() {
    LocalContainerEntityManagerFactoryBean collaborationEntityManagerFactory =
        new LocalContainerEntityManagerFactoryBean();
    collaborationEntityManagerFactory.setDataSource(metabaseDatasource());
    collaborationEntityManagerFactory.setPackagesToScan("org.eea.collaboration.persistence.domain");
    collaborationEntityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    collaborationEntityManagerFactory.setJpaProperties(additionalProperties());
    return collaborationEntityManagerFactory;
  }

  /**
   * Collaboration transaction manager.
   *
   * @param entityManagerFactory the entity manager factory
   * @return the platform transaction manager
   */
  @Bean
  @Primary
  public PlatformTransactionManager collaborationTransactionManager(
      @Autowired EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
    jpaTransactionManager.setEntityManagerFactory(collaborationEntityManagerFactory().getObject());
    return jpaTransactionManager;
  }

  /**
   * Metabase datasource.
   *
   * @return the data source
   */
  @Bean
  public DataSource metabaseDatasource() {
    DriverManagerDataSource metabaseDatasource = new DriverManagerDataSource();
    metabaseDatasource.setDriverClassName(driver);
    metabaseDatasource.setUrl(url);
    metabaseDatasource.setUsername(username);
    metabaseDatasource.setPassword(password);
    return metabaseDatasource;
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

  /**
   * Multipart resolver.
   *
   * @return the multipart resolver
   */
  @Bean
  public MultipartResolver multipartResolver() {
    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    multipartResolver.setMaxUploadSize(maxFileSize);
    multipartResolver.setMaxUploadSizePerFile(maxRequestSize);
    return multipartResolver;
  }
}
