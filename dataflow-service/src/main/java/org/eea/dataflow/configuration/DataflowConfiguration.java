package org.eea.dataflow.configuration;

import java.util.concurrent.Callable;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class DataflowConfiguration.
 */
@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "dataFlowsEntityManagerFactory",
    transactionManagerRef = "dataFlowsTransactionManager",
    basePackages = "org.eea.dataflow.persistence.repository")
@EnableTransactionManagement
@EntityScan(basePackages = "org.eea.dataflow.persistence.domain")
@EnableWebMvc
@EnableAsync
@EnableScheduling
public class DataflowConfiguration implements WebMvcConfigurer {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataflowConfiguration.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The timeout. */
  @Value("${stream.download.timeout}")
  private Integer timeout;

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
   * Data source data source.
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
   * Data flows entity manager factory.
   *
   * @return the local container entity manager factory bean
   */
  @Bean
  @Primary
  @Qualifier("dataFlowsEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean dataFlowsEntityManagerFactory() {
    final LocalContainerEntityManagerFactoryBean dataFlowEM =
        new LocalContainerEntityManagerFactoryBean();
    dataFlowEM.setDataSource(metabaseDatasource());
    dataFlowEM.setPackagesToScan("org.eea.dataflow.persistence.domain");
    final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    dataFlowEM.setJpaVendorAdapter(vendorAdapter);

    return dataFlowEM;
  }

  /**
   * Data flows transaction manager.
   *
   * @param entityManagerFactory the entity manager factory
   *
   * @return the platform transaction manager
   */
  @Bean
  @Primary
  public PlatformTransactionManager dataFlowsTransactionManager(
      @Autowired EntityManagerFactory entityManagerFactory) {
    final JpaTransactionManager schemastransactionManager = new JpaTransactionManager();
    schemastransactionManager.setEntityManagerFactory(dataFlowsEntityManagerFactory().getObject());
    return schemastransactionManager;
  }

  /**
   * Rest template.
   *
   * @return the rest template
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * Gets the async executor.
   *
   * @return the async executor
   */
  @Bean
  public AsyncTaskExecutor streamTaskExecutor() {
    LOG.info("Creating Async Task Executor");
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(25);
    return executor;
  }

  /**
   * Web mvc configurer configurer.
   *
   * @param taskExecutor the task executor
   * @param callableProcessingInterceptor the callable processing interceptor
   * @return the web mvc configurer
   */

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setDefaultTimeout(timeout).setTaskExecutor(streamTaskExecutor());
    configurer.registerCallableInterceptors(callableProcessingInterceptor());
  }

  /**
   * Callable processing interceptor.
   *
   * @return the callable processing interceptor
   */
  @Bean
  public CallableProcessingInterceptor callableProcessingInterceptor() {
    return new TimeoutCallableProcessingInterceptor() {
      @Override
      public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
        LOG_ERROR.error("Stream download failed by timeout");
        return super.handleTimeout(request, task);
      }
    };
  }

}
