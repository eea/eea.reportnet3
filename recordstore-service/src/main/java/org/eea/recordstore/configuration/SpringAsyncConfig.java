package org.eea.recordstore.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * The Class SpringAsyncConfig.
 */
@Configuration
@EnableAsync
public class SpringAsyncConfig implements AsyncConfigurer {

  @Override
  public Executor getAsyncExecutor() {
    SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
    executor.setThreadNamePrefix("asynchronous-taskExecutor-thread-");

    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
  }
}
