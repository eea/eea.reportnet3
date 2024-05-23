package org.eea.dataset.configuration;

import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.TimeoutCallableProcessingInterceptor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The type Dataset cors config.
 */
@EnableWebMvc
@Configuration
public class DatasetMvcConfig implements WebMvcConfigurer {
  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetMvcConfig.class);

  /** The profile. */
  @Value("${spring.profiles.active}")
  private String[] profile;

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
   * Adds the cors mappings.
   *
   * @param registry the registry
   */
  @Override
  public void addCorsMappings(final CorsRegistry registry) {

    if (profile != null && profile.length > 0 && !"local".equals(profile[0])) {
      registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE");
    }

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

  /**
   * Configure async support.
   *
   * @param configurer the configurer
   */
  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    configurer.setDefaultTimeout(7200000).setTaskExecutor(streamTaskExecutor());
    configurer.registerCallableInterceptors(callableProcessingInterceptor());
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
   * Callable processing interceptor.
   *
   * @return the callable processing interceptor
   */
  @Bean
  public CallableProcessingInterceptor callableProcessingInterceptor() {
    return new TimeoutCallableProcessingInterceptor() {
      @Override
      public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
        LOG.error("Stream download failed by timeout");
        return super.handleTimeout(request, task);
      }
    };
  }
}
