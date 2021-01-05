package org.eea.recordstore.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    // 1º
    // inicio el proceso
    checkProccesList(datasetId);
  }

  /**
   * Check procces list.
   *
   * @param datasetId the dataset id
   */
  private void checkProccesList(Long datasetId) {
    // TODO comprobar si se puede insertar en la lista de procesos
    // TODO caso 1 = no hay nada inserto,y ejecuto el proceso.
    // Launch Broadcast - insert in list
    // TODO caso 2 = hay uno e inserto
    // Launch Broadcast - insert in list
    // TODO caso 3 = hay dos no hago nada
  }

  /**
   * Insert procces list.
   *
   * @param datasetId the dataset id
   */
  public void insertProccesList(Long datasetId) {
    // recibo broadcast
    // apunta id en la lista.
  }

  /**
   * Finish procces.
   *
   * @param datasetId the dataset id
   */
  public void finishProcces(Long datasetId) {
    // recibo he terminado de trabajar
    // TODO = hay 2
    // ejecuto.
    // Lanzo borrado en broadcast.
    releaseDeleteViewProccesEvent(datasetId, "");
  }

  /**
   * Delete procces list.
   *
   * @param datasetId the dataset id
   */
  public void deleteProccesList(Long datasetId) {
    // recibo broadcast de borrado.
    // borro proceso el primero que encuentre.
  }



  /**
   * Initialize create update materialized query view.
   *
   * @param datasetId the dataset id
   */
  public void executeCreateUpdateMaterializedQueryView(Long datasetId) {
    // TODO llama al service
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
