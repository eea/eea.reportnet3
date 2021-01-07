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
    viewExecutorService = Executors.newFixedThreadPool(maxRunningTasks);
    processesList = new ArrayList<>();
  }


  /**
   * Insert view procces.
   * 
   * @param datasetId the dataset id
   * @param checkSQL
   * @param user
   * @param isMaterialized
   */
  public void insertViewProcces(Long datasetId, Boolean isMaterialized, String user,
      Boolean checkSQL) {
    switch (processesList.stream().filter(x -> datasetId.equals(x)).collect(Collectors.counting())
        .toString()) {
      case "0":
        viewExecutorService.execute(() -> executeCreateUpdateMaterializedQueryView(datasetId,
            isMaterialized, user, checkSQL));
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.INSERT_VIEW_PROCCES_EVENT, datasetId);
        break;
      case "1":
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.INSERT_VIEW_PROCCES_EVENT, datasetId);
        break;
      default:
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
   */
  public void finishProcces(Long datasetId, Boolean isMaterialized, String user, Boolean checkSQL) {
    if (2 == processesList.stream().filter(x -> datasetId.equals(x))
        .collect(Collectors.counting())) {
      viewExecutorService.execute(() -> executeCreateUpdateMaterializedQueryView(datasetId,
          isMaterialized, user, checkSQL));
    }
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
   * @param isMaterialized
   * @param checkSQL
   * @param user
   */
  public void executeCreateUpdateMaterializedQueryView(Long datasetId, Boolean isMaterialized,
      String user, Boolean checkSQL) {
    recordStoreService.createUpdateQueryView(datasetId, isMaterialized);
    if (checkSQL) {
      releaseValidateManualQCEvent(datasetId, user, true);
    }
    releaseFinishViewProcessEvent(datasetId, isMaterialized, user, checkSQL);
  }


  /**
   * Release delete view procces event.
   *
   * @param datasetId the dataset id
   */
  private void releaseDeleteViewProccesEvent(Long datasetId) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    kafkaSenderUtils.releaseKafkaEvent(EventType.DELETE_VIEW_PROCCES_EVENT, result);
  }


  /**
   * Release validate manual QC event.
   *
   * @param datasetId the dataset id
   * @param checkNoSQL the check no SQL
   */
  private void releaseValidateManualQCEvent(Long datasetId, String user, boolean checkNoSQL) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put("checkNoSQL", checkNoSQL);
    result.put(LiteralConstants.USER, user);
    kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATE_MANUAL_QC_COMMAND, result);
  }


  /**
   * Release finish view process event.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   * @param user the user
   * @param checkNoSQL the check no SQL
   */
  private void releaseFinishViewProcessEvent(Long datasetId, Boolean isMaterialized, String user,
      boolean checkNoSQL) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put("isMaterialized", isMaterialized);
    result.put("checkNoSQL", checkNoSQL);
    result.put(LiteralConstants.USER, user);
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
