package org.eea.recordstore.job;

import java.util.List;
import javax.annotation.PostConstruct;
import org.eea.recordstore.service.RecordStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * The Class CitusJob.
 */
@RefreshScope
@Component
public class CitusJob {

  /** The record store service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /** The enable table distribution job. */
  @Value("${recordstore.enableTableDistributionJob}")
  private String enableTableDistributionJob;

  /**
   * Inits the job.
   */
  @PostConstruct
  private void init() {
    // cron = "0 0-6 * * *"
    String cronExpression = "0 0 * * * * ";
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.initialize();
    scheduler.schedule(() -> executeTableDistribution(), new CronTrigger(cronExpression));
  }

  /**
   * Execute table distribution.
   */
  public void executeTableDistribution() {
    if ("true".equals(enableTableDistributionJob)) {
      List<String> distributeDatasets = recordStoreService.getNotdistributedDatasets();
      for (String dataset : distributeDatasets) {
        String datasetAux = dataset.replace("dataset_", "");
        Long datasetLong = Long.parseLong(datasetAux);
        recordStoreService.distributeTablesJob(datasetLong);
      }
    }
  }
}
