package org.eea.recordstore.util;

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
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * The Class ViewHelper.
 */
@Component
@RefreshScope
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
    processesList.stream().filter(datasetId::equals).forEach(id -> LOG.info("VAGOS inside insertViewProcces filter datasetIds current datasetId {}", id));

    switch (processesList.stream().filter(datasetId::equals).collect(Collectors.counting())
        .toString()) {
      case "0":
        // no processes running, then we should queue it
        LOG.info("VAGOS inside insertViewProcces, CASE 0, datasetId : {}, isMaterialized: {}, checkSQL: {}", datasetId, isMaterialized, checkSQL);
        viewExecutorService.execute(
            () -> executeCreateUpdateMaterializedQueryView(datasetId, isMaterialized, checkSQL));
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.INSERT_VIEW_PROCCES_EVENT, datasetId);
        break;
      case "1":
        LOG.info("VAGOS inside insertViewProcces, CASE 1, datasetId : {}, isMaterialized: {}, checkSQL: {}", datasetId, isMaterialized, checkSQL);
        // one process already queued so, we notify to every instance
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.INSERT_VIEW_PROCCES_EVENT, datasetId);
        break;
      default:
        LOG.info("VAGOS inside insertViewProcces, CASE default, datasetId : {}, isMaterialized: {}, checkSQL: {}", datasetId, isMaterialized, checkSQL);
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
      LOG.info("VAGOS inside insertProccesList before adding dataset with id {}", datasetId);
      processesList.add(datasetId);
      processesList.stream().filter(datasetId::equals).forEach(id -> LOG.info("after adding dataset id, List contains ids : {} ", id));
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
    LOG.info("VAGOS inside finishProcces with datasetID : {}", datasetId);
    if (2 == processesList.stream().filter(datasetId::equals).collect(Collectors.counting())) {
      LOG.info("VAGOS inside finishProcces when we have 2 datasets with same id : {}", datasetId);
      viewExecutorService.execute(
          () -> executeCreateUpdateMaterializedQueryView(datasetId, isMaterialized, checkSQL));
    }
    // update the processes list in every recordstore instance
    LOG.info("VAGOS inside finishProcces before releaseDeleteViewProccesEvent : {}", datasetId);
    releaseDeleteViewProccesEvent(datasetId);
  }

  /**
   * Delete procces list.
   *
   * @param datasetId the dataset id
   */
  public void deleteProccesList(Long datasetId) {
    synchronized (processesList) {
      LOG.info("VAGOS inside deleteProccesList before remove : datasetId: {}", datasetId);
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
    LOG.info("VAGOS inside executeCreateUpdateMaterializedQueryView before createUpdateQueryView with dataset id : {}", datasetId);
    recordStoreService.createUpdateQueryView(datasetId, isMaterialized);
    if (Boolean.TRUE.equals(checkSQL)) {
      releaseValidateManualQCEvent(datasetId, true);
    }
    LOG.info("VAGOS inside executeCreateUpdateMaterializedQueryView before releaseFinishViewProcessEvent with dataset id : {}", datasetId);
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
