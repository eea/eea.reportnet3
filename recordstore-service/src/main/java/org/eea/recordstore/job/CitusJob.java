package org.eea.recordstore.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
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

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

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
      Long datasetId = Long.parseLong(dataset.replace("dataset_", ""));
      Map<String, Object> values = new HashMap<>();
      values.put(LiteralConstants.DATASET_ID, datasetId);
      LOG.info("Distributing dataset {}", datasetId);
      kafkaSenderUtils.releaseKafkaEvent(EventType.DISTRIBUTE_DATASET_EVENT, values);
    }
  }
}
