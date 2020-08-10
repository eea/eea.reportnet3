package org.eea.dataflow.configuration;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The Class SpringAsyncConfig.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class SpringAsyncConfig {

  /** The timeout. */
  @Value("${stream.download.timeout}")
  private Integer timeout;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(SpringAsyncConfig.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the async executor.
   *
   * @return the async executor
   */
  @Bean(name = "taskExecutor")
  public AsyncTaskExecutor getAsyncExecutor() {
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
  @Bean
  public WebMvcConfigurer webMvcConfigurerConfigurer(AsyncTaskExecutor taskExecutor,
      CallableProcessingInterceptor callableProcessingInterceptor) {
    return new WebMvcConfigurer() {
      @Override
      public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(timeout).setTaskExecutor(taskExecutor);
        configurer.registerCallableInterceptors(callableProcessingInterceptor);
        WebMvcConfigurer.super.configureAsyncSupport(configurer);
      }
    };
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
        LOG_ERROR.error("timeout!");
        return super.handleTimeout(request, task);
      }
    };
  }

  /**
   * Gets the async uncaught exception handler.
   *
   * @return the async uncaught exception handler
   */
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new SimpleAsyncUncaughtExceptionHandler();
  }


}
