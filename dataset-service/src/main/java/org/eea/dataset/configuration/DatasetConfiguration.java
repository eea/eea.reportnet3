package org.eea.dataset.configuration;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.eea.dataset.multitenancy.MultiTenantDataSource;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * The type Dataset configuration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = "org.eea.dataset.persistence.repository")
public class DatasetConfiguration implements WebMvcConfigurer {


  private final String PROPERTY_DRIVER = "driver";
  private final String PROPERTY_URL = "url";
  private final String PROPERTY_USERNAME = "user";
  private final String PROPERTY_PASSWORD = "password";
  private final String PROPERTY_SHOW_SQL = "hibernate.show_sql";
  private final String PROPERTY_DIALECT = "hibernate.dialect";
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * Entity manager factory bean local container entity manager factory bean.
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() {
    final LocalContainerEntityManagerFactoryBean lfb = new LocalContainerEntityManagerFactoryBean();
    lfb.setDataSource(dataSource());
    lfb.setPackagesToScan("org.eea.dataset.persistence.domain");
    lfb.setJpaProperties(hibernateProps());
    lfb.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    return lfb;
  }

  /**
   * Data source data source.
   *
   * @return the data source
   */
  @Bean
  public DataSource dataSource() {
    final AbstractRoutingDataSource ds = new MultiTenantDataSource();

    ds.setTargetDataSources(targetDataSources());
    return ds;
  }

  @Bean
  @Qualifier("targetDataSources")
  public Map<Object, Object> targetDataSources() {
    final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();

    final List<ConnectionDataVO> connections = recordStoreControllerZull.getConnectionToDataset();
    for (final ConnectionDataVO connectionDataVO : connections) {
      targetDataSources.put(connectionDataVO.getSchema(), dataSource(connectionDataVO));
    }
    return targetDataSources;
  }

  private static DataSource dataSource(final ConnectionDataVO connectionDataVO) {
    final DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setUrl(connectionDataVO.getConnectionString());
    ds.setUsername(connectionDataVO.getUser());
    ds.setPassword(connectionDataVO.getPassword());
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setSchema(connectionDataVO.getSchema());
    return ds;
  }


  private Properties hibernateProps() {
    final Properties properties = new Properties();
    properties.setProperty(PROPERTY_DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
    properties
        .setProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation", "true");
    properties.setProperty("spring.jpa.database", "postgresql");
    return properties;
  }

  /**
   * Entity manager factory entity manager factory.
   *
   * @return the entity manager factory
   */
  @Bean
  public EntityManagerFactory entityManagerFactory() {
    return entityManagerFactoryBean().getObject();
  }

  /**
   * Transaction manager jpa transaction manager.
   *
   * @return the jpa transaction manager
   */
  @Bean
  public JpaTransactionManager transactionManager() {
    final JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory());
    return transactionManager;
  }
}
