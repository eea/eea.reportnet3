package org.eea.recordstore.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.eea.kafka.utils.KafkaAdminUtils;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.recordstore.util.model.ManageViewProcessVO;
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
  private Map<String, ManageViewProcessVO> processesMap;

  /** The kafka admin utils. */
  @Autowired
  private KafkaAdminUtils kafkaAdminUtils;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  private String selfUUID;

  /**
   * Inits the.
   */
  @PostConstruct
  private void init() {
    viewExecutorService = Executors.newFixedThreadPool(maxRunningTasks);
    selfUUID = UUID.randomUUID().toString();
  }

  /**
   * Initialize create update materialized query view.
   * 
   * @param datasetId the dataset id
   * @param user the user
   * @param released the released
   * @param idProcess the id process
   */
  public void executeCreateUpdateMaterializedQueryView(Long datasetId, boolean isMaterialized) {
    // TODO Lanza mensaje kafka con insertar en la lista el proceso


  }

  public void insertProccesList(Long datasetId, String uuid) {
    // TODO comprobar si se puede insertar en la lista de procesos
    // TODO caso 1 = no hay nada inserto, y compruebo si soy el dueño, (uuid recibida y asignada son
    // la misma) si son la misma ejecuto el proceso.

    // TODO caso 2 = hay uno e inserto
    // TODO caso 3 = hay dos no hago nada

  }

  public void deleteProccesList(Long datasetId, String uuid) {
    // TODO Borrar el proceso terminado.
    // TODO hay mas peticiones para este dataset id?
    // TODO caso 2 = hay alguno, compruebo si me pertenece(UUID) y ejecuto.

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
