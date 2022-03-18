package org.eea.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class JobScheduler.
 */
@Component
public class JobScheduler {

  /** The max running tasks. */
  @Value("${validation.tasks.parallelism}")
  private int maxRunningTasks;

  /** The scheduler. */
  private ScheduledExecutorService scheduler;


  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    scheduler = Executors.newScheduledThreadPool(maxRunningTasks + 1);
  }

  /**
   * Schedule.
   *
   * @param command the command
   * @param delay the delay
   * @param unit the unit
   * @return the scheduled future
   */
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return scheduler.schedule(command, delay, unit);
  }
}
