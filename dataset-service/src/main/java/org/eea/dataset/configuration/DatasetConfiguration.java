package org.eea.dataset.configuration;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.eea.dataset.multitenancy.MultiTenantDataSource;
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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * The type Dataset configuration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(entityManagerFactoryRef = "dataSetsEntityManagerFactory",
    transactionManagerRef = "dataSetsTransactionManager",
    basePackages = "org.eea.dataset.persistence.repository")
public class DatasetConfiguration implements WebMvcConfigurer {

  @Value("${spring.jpa.hibernate.ddl-auto}")
  private String dll;
  @Value("${spring.jpa.properties.hibernate.dialect}")
  private String dialect;
  @Value("${spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation}")
  private String createClobPropertie;


  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;


  /**
   * Data source data source.
   *
   * @return the data source
   */

  @Bean
  @Primary
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
      targetDataSources.put(connectionDataVO.getSchema(), dataSetsDataSource(connectionDataVO));
    }
    return targetDataSources;
  }

  @Primary
  private static DataSource dataSetsDataSource(final ConnectionDataVO connectionDataVO) {
    final DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setUrl(connectionDataVO.getConnectionString());
    ds.setUsername(connectionDataVO.getUser());
    ds.setPassword(connectionDataVO.getPassword());
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setSchema(connectionDataVO.getSchema());
    return ds;
  }


  @Bean
  @Autowired
  @Primary
  @Qualifier("dataSetsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean dataSetsEntityManagerFactory(
      DataSource datasource) {
    LocalContainerEntityManagerFactoryBean dataSetsEM =
        new LocalContainerEntityManagerFactoryBean();
    dataSetsEM.setDataSource(datasource);
    dataSetsEM.setPackagesToScan("org.eea.dataset.persistence.domain");
    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    dataSetsEM.setJpaVendorAdapter(vendorAdapter);
    dataSetsEM.setJpaProperties(additionalProperties());
    return dataSetsEM;
  }

  Properties additionalProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.hbm2ddl.auto", dll);
    properties.setProperty("hibernate.dialect", dialect);
    properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", createClobPropertie);
    return properties;
  }

  @Bean
  @Autowired
  @Primary
  public PlatformTransactionManager dataSetsTransactionManager(
      @Qualifier("dataSetsEntityManagerFactory") LocalContainerEntityManagerFactoryBean dataSetsEntityManagerFactory) {

    JpaTransactionManager schemastransactionManager = new JpaTransactionManager();
    schemastransactionManager.setEntityManagerFactory(dataSetsEntityManagerFactory.getObject());
    return schemastransactionManager;
  }


}
