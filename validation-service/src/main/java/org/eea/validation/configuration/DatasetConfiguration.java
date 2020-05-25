package org.eea.validation.configuration;

import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.validation.configuration.util.EeaDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * The type Dataset configuration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(entityManagerFactoryRef = "dataSetsEntityManagerFactory",
    transactionManagerRef = "dataSetsTransactionManager",
    basePackages = "org.eea.validation.persistence.data.repository")
@EnableWebMvc
public class DatasetConfiguration implements WebMvcConfigurer {

  /**
   * The dll.
   */
  @Value("${spring.jpa.hibernate.ddl-auto}")
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
   * The batch size.
   */
  @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
  private String batchSize;

  /**
   * The show sql propertie
   */
  @Value("${spring.jpa.hibernate.flushMode}")
  private String flushMode;

  /**
   * The show sql propertie
   */
  @Value("${spring.jpa.hibernate.show-sql}")
  private String showSql;

  /**
   * The stats.
   */
  @Value("${spring.jpa.properties.hibernate.order_updates}")
  private String orderUpdates;

  /**
   * The order.
   */
  @Value("${spring.jpa.properties.hibernate.order_inserts}")
  private String orderInserts;
  /**
   * /** The username.
   */
  @Value("${spring.datasource.dataset.username}")
  private String username;

  /**
   * The password.
   */
  @Value("${spring.datasource.dataset.password}")
  private String password;

  /**
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;


  /**
   * Data source data source.
   *
   * @return the data source
   */


  @Bean
  public DataSource datasetDataSource() {
    final List<ConnectionDataVO> connections = recordStoreControllerZull.getDataSetConnections();
    DataSource dataSource = null;
    if (null != connections && !connections.isEmpty()) {
      dataSource = dataSetsDataSource(connections.get(0));
    }
    return dataSource;
  }

  /**
   * Target data sources.
   *
   * @return the map
   */


  /**
   * Data sets data source.
   *
   * @param connectionDataVO the connection data VO
   *
   * @return the data source
   */
  private DataSource dataSetsDataSource(final ConnectionDataVO connectionDataVO) {

    EeaDataSource ds = new EeaDataSource();
    ds.setUrl(connectionDataVO.getConnectionString());
    // set validation microservice credentials
    ds.setUsername(this.username);
    ds.setPassword(this.password);
    ds.setDriverClassName("org.postgresql.Driver");

    return ds;
  }


  /**
   * Data sets entity manager factory.
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Primary
  @Qualifier("dataSetsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean dataSetsEntityManagerFactory() {
    final LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean =
        new LocalContainerEntityManagerFactoryBean();
    localContainerEntityManagerFactoryBean.setDataSource(datasetDataSource());
    localContainerEntityManagerFactoryBean
        .setPackagesToScan("org.eea.validation.persistence.data.domain");
    final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    localContainerEntityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

    localContainerEntityManagerFactoryBean.setJpaProperties(additionalProperties());
    return localContainerEntityManagerFactoryBean;
  }

  /**
   * Additional properties.
   *
   * @return the properties
   */
  Properties additionalProperties() {
    final Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", dll);
    properties.setProperty("hibernate.dialect", dialect);
    properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", createClobPropertie);
    properties.setProperty("hibernate.jdbc.batch_size", batchSize);
    properties.setProperty("hibernate.show_sql", showSql);
    properties.setProperty("hibernate.flushMode", flushMode);
    properties.setProperty("hibernate.order_updates", orderUpdates);
    properties.setProperty("hibernate.order_inserts", orderInserts);
    return properties;
  }

  /**
   * Data sets transaction manager.
   *
   * @return the platform transaction manager
   */
  @Bean
  @Primary
  public PlatformTransactionManager dataSetsTransactionManager() {

    final JpaTransactionManager schemastransactionManager = new JpaTransactionManager();
    schemastransactionManager.setEntityManagerFactory(dataSetsEntityManagerFactory().getObject());
    return schemastransactionManager;
  }

  @Bean
  public LocalSessionFactoryBean sessionFactory() {
    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
    sessionFactory.setDataSource(datasetDataSource());
    sessionFactory.setPackagesToScan("org.eea.validation.persistence.data.domain");
    sessionFactory.setHibernateProperties(additionalProperties());

    return sessionFactory;
  }
}
