package org.eea.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

  @Value("${validation.tasks.parallelism}")
  private int maxRunningTasks;

  private ScheduledExecutorService scheduler;


  @PostConstruct
  private void init() {
    scheduler = Executors.newScheduledThreadPool(maxRunningTasks + 1);
  }

  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return scheduler.schedule(command, delay, unit);
  }
}
