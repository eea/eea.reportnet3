package org.eea.orchestrator.scheduling;

import lombok.RequiredArgsConstructor;
import org.eea.lock.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

//@Service
public class JobForRemovingOldDaysLocks {

  private final LockService lockService = null;

  private static final Logger LOG = LoggerFactory.getLogger(JobForRemovingOldDaysLocks.class);

  //@PostConstruct
  private void init() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.initialize();
    scheduler.schedule(this::deletePreviousDayLocks,
        new CronTrigger("0 0 8 * * *"));
  }

  @Transactional
  public void deletePreviousDayLocks() {
    try {
      int locksDeleted = lockService.deletePreviousDayLocks();
      LOG.info("Lock cleanup completed successfully. {} expired lock(s) removed.", locksDeleted);
    } catch (Exception ex) {
      LOG.error("Failed to delete previous day locks: {}", ex.getMessage(), ex);
    }
  }

}
