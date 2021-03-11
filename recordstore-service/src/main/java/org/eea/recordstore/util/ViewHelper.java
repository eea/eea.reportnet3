package org.eea.recordstore.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.thread.EEADelegatingSecurityContextExecutorService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class ViewHelper.
 */
@Component
public class ViewHelper implements DisposableBean {


  /** The max running tasks. */
  @Value("${recordstore.tasks.parallelism}")
  private int maxRunningTasks;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ViewHelper.class);

  /** The view executor service. */
  private ExecutorService viewExecutorService;

  /** The processes map. */
  private List<Long> processesList;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The record store service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    viewExecutorService = new EEADelegatingSecurityContextExecutorService(
        Executors.newFixedThreadPool(maxRunningTasks));
    processesList = new ArrayList<>();
  }

  /**
   * Insert view procces.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   * @param checkSQL the check SQL
   */
  public void insertViewProcces(Long datasetId, Boolean isMaterialized, Boolean checkSQL) {
    // Check the number of views per dataset in this moment queued
    switch (processesList.stream().filter(datasetId::equals).collect(Collectors.counting())
        .toString()) {
      case "0":
        // no processes running, then we should queue it
        viewExecutorService.execute(
            () -> executeCreateUpdateMaterializedQueryView(datasetId, isMaterialized, checkSQL));
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.INSERT_VIEW_PROCCES_EVENT, datasetId);
        break;
      case "1":
        // one process already queued so, we notify to every instance
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.INSERT_VIEW_PROCCES_EVENT, datasetId);
        break;
      default:
        // if there are 2 processes, one active, one in queue, no need to add more as the last
        // process will take all the changes happened before the second process starts
        break;
    }
  }

  /**
   * Insert procces list.
   *
   * @param datasetId the dataset id
   */
  public void insertProccesList(Long datasetId) {
    synchronized (processesList) {
      processesList.add(datasetId);
      LOG.info("Add process create Query views from dataset: {} ", datasetId);
    }
  }

  /**
   * Finish procces.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   * @param checkSQL the check SQL
   */
  public void finishProcces(Long datasetId, Boolean isMaterialized, Boolean checkSQL) {
    // If we hace two dataset view generating process we have to execute it again
    if (2 == processesList.stream().filter(datasetId::equals).collect(Collectors.counting())) {
      viewExecutorService.execute(
          () -> executeCreateUpdateMaterializedQueryView(datasetId, isMaterialized, checkSQL));
    }
    // update the processes list in every recordstore instance
    releaseDeleteViewProccesEvent(datasetId);
  }

  /**
   * Delete procces list.
   *
   * @param datasetId the dataset id
   */
  public void deleteProccesList(Long datasetId) {
    synchronized (processesList) {
      processesList.remove(datasetId);
      LOG.info("Delete process create Query views from dataset: {} ", datasetId);
    }
  }

  /**
   * Initialize create update materialized query view.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   * @param checkSQL the check SQL
   */
  public void executeCreateUpdateMaterializedQueryView(Long datasetId, Boolean isMaterialized,
      Boolean checkSQL) {
    recordStoreService.createUpdateQueryView(datasetId, isMaterialized);
    if (Boolean.TRUE.equals(checkSQL)) {
      releaseValidateManualQCEvent(datasetId, true);
    }
    releaseFinishViewProcessEvent(datasetId, isMaterialized, checkSQL);
  }


  /**
   * Release delete view procces event.
   *
   * @param datasetId the dataset id
   */
  private void releaseDeleteViewProccesEvent(Long datasetId) {
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.DELETE_VIEW_PROCCES_EVENT, datasetId);
  }


  /**
   * Release validate manual QC event.
   *
   * @param datasetId the dataset id
   * @param checkNoSQL the check no SQL
   */
  private void releaseValidateManualQCEvent(Long datasetId, boolean checkNoSQL) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put("checkNoSQL", checkNoSQL);
    kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATE_MANUAL_QC_COMMAND, result);
  }

  /**
   * Release finish view process event.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   * @param checkNoSQL the check no SQL
   */
  private void releaseFinishViewProcessEvent(Long datasetId, Boolean isMaterialized,
      boolean checkNoSQL) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put("isMaterialized", isMaterialized);
    result.put("checkNoSQL", checkNoSQL);
    kafkaSenderUtils.releaseKafkaEvent(EventType.FINISH_VIEW_PROCCES_EVENT, result);
  }

  /**
   * Destroy.
   *
   * @throws Exception the exception
   */
  @Override
  public void destroy() throws Exception {
    if (null != viewExecutorService) {
      this.viewExecutorService.shutdown();
    }
  }
}
