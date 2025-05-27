package org.eea.orchestrator.scheduling;

import lombok.RequiredArgsConstructor;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class JobForRemovingOldDaysLocks {

  private final DatasetController datasetController;

  private static final Logger LOG = LoggerFactory.getLogger(JobForRemovingOldDaysLocks.class);

  @PostConstruct
  private void init() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.initialize();
    scheduler.schedule(this::deletePreviousDayLocks,
        new CronTrigger("0 0 8 * * *"));
  }

  @Transactional
  public void deletePreviousDayLocks() {
    try {
      int locksDeleted = datasetController.clearOldLocks();
      LOG.info("Lock cleanup completed successfully. {} expired lock(s) removed.", locksDeleted);
    } catch (Exception ex) {
      LOG.error("Failed to delete previous day locks: {}", ex.getMessage(), ex);
    }
  }

}
