package org.eea.validation.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The Class SpringAsyncConfig.
 */
@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer {

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("asynchronous-taskExecutor-thread-");
    executor.initialize();
    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }

}
