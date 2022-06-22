package org.eea.recordstore.job;

import java.util.List;
import org.eea.recordstore.service.RecordStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class CitusJob {

  @Autowired
  RecordStoreService recordStoreService;

  @Scheduled(cron = "0 0 */2 * * *")
  public void executeTableDistribution() {
    List<String> distributeDatasets = recordStoreService.getNotdistributedDatasets();
    for (String dataset : distributeDatasets) {
      String datasetAux = dataset.replace("dataset_", "");
      Long datasetLong = Long.parseLong(datasetAux);
      recordStoreService.distributeTablesJob(datasetLong);
    }
  }
}
