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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * The type Dataset configuration.
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EnableJpaRepositories(basePackages = "org.eea.dataset.persistence.repository")
@EntityScan(basePackages = "org.eea.dataset.persistence.domain")
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

    properties.setProperty("spring.jpa.database", "postgresql");
    properties.setProperty("spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation",
        "true");
    return properties;
  }


}
