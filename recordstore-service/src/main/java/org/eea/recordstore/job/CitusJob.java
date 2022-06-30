package org.eea.recordstore.job;

import java.util.List;
import javax.annotation.PostConstruct;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(CitusJob.class);

  /** The record store service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /** The enable table distribution job. */
  @Value("${enableTableDistributionJob}")
  private String enableTableDistributionJob;


  /**
   * Inits the job.
   */
  @PostConstruct
  private void init() {
    if (!enableTableDistributionJob.isBlank()) {
      ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
      scheduler.initialize();
      scheduler.schedule(() -> executeTableDistribution(),
          new CronTrigger(enableTableDistributionJob));
    }
  }

  /**
   * Execute table distribution.
   */
  public void executeTableDistribution() {
    List<String> distributeDatasets = recordStoreService.getNotdistributedDatasets();
    for (String dataset : distributeDatasets) {
      String datasetAux = dataset.replace("dataset_", "");
      Long datasetLong = Long.parseLong(datasetAux);
      try {
        LOG.info("Distributing dataset {}", datasetLong);
        recordStoreService.distributeTablesJob(datasetLong);
      } catch (Exception e) {
        LOG.info("For any reason the distribution of the dataset {} failed", datasetLong, e);
      }
    }
  }
}
