package org.eea.dataset.configuration;

import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.eea.dataset.configuration.util.EeaDataSource;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
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
    basePackages = "org.eea.dataset.persistence.data.repository")
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
   * The max file size.
   */
  @Value("${spring.servlet.multipart.max-file-size}")
  private Long maxFileSize;


  /**
   * The max request size.
   */
  @Value("${spring.servlet.multipart.max-request-size}")
  private Long maxRequestSize;

  /**
   * The username.
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
  @Qualifier("datasetDataSource")
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
    //set validation microservice credentials
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
    final LocalContainerEntityManagerFactoryBean dataSetsEM =
        new LocalContainerEntityManagerFactoryBean();
    dataSetsEM.setDataSource(datasetDataSource());
    dataSetsEM.setPackagesToScan("org.eea.dataset.persistence.data.domain");
    final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    dataSetsEM.setJpaVendorAdapter(vendorAdapter);

    dataSetsEM.setJpaProperties(additionalProperties());
    return dataSetsEM;
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
