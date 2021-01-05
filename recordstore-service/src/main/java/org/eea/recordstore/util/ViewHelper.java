package org.eea.recordstore.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaAdminUtils;
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

  /** The initial tax. */
  @Value("${validation.tasks.initial.tax}")
  private int initialTax;

  /** The task released tax. */
  @Value("${validation.tasks.release.tax}")
  private int taskReleasedTax;

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ViewHelper.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The view executor service. */
  private ExecutorService viewExecutorService;

  /** The processes map. */
  private List<Long> processesList;

  /** The kafka admin utils. */
  @Autowired
  private KafkaAdminUtils kafkaAdminUtils;

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
  }


  /**
   * Insert view procces.
   *
   * @param datasetId the dataset id
   */
  public void insertViewProcces(Long datasetId) {
    switch (processesList.stream().filter(x -> datasetId.equals(x)).collect(Collectors.counting())
        .toString()) {
      case "0":
        viewExecutorService.execute(() -> executeCreateUpdateMaterializedQueryView(datasetId));
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
        break;
      case "1":
        kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
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
    // recibo broadcast
    processesList.add(datasetId);
  }

  /**
   * Finish procces.
   *
   * @param datasetId the dataset id
   */
  public void finishProcces(Long datasetId) {
    // recibo he terminado de trabajar
    if (2 == processesList.stream().filter(x -> datasetId.equals(x))
        .collect(Collectors.counting())) {
      // ejecuto.
      viewExecutorService.execute(() -> executeCreateUpdateMaterializedQueryView(datasetId));
    }
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
  }

  /**
   * Delete procces list.
   *
   * @param datasetId the dataset id
   */
  public void deleteProccesList(Long datasetId) {
    // recibo broadcast de borrado.
    processesList.remove(datasetId);
  }



  /**
   * Initialize create update materialized query view.
   *
   * @param datasetId the dataset id
   */
  public void executeCreateUpdateMaterializedQueryView(Long datasetId) {
    recordStoreService.createUpdateQueryView(datasetId, false);
  }


  private void releaseDeleteViewProccesEvent(Long datasetId, String user) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put(LiteralConstants.USER, user);
    kafkaSenderUtils.releaseKafkaEvent(EventType.DELETE_VIEW_PROCCES_EVENT, result);
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
