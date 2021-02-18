package org.eea.recordstore.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;

/**
 * The Class RestoreSnapshotHelper.
 */
@Component
public class SnapshotHelper implements DisposableBean {

  private static final Logger LOG = LoggerFactory.getLogger(SnapshotHelper.class);

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Value("${snapshot.task.parallelism}")
  private int maxRunningTasks;

  @Autowired
  private RecordStoreService recordStoreService;

  private ExecutorService restorationExecutorService;

  public SnapshotHelper() {
    super();
  }

  @PostConstruct
  private void init() {
    restorationExecutorService =
        new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(maxRunningTasks));
  }

  /**
   * Destroy.
   *
   * @throws Exception the exception
   */
  @Override
  public void destroy() throws Exception {
    if (null != restorationExecutorService) {
      this.restorationExecutorService.shutdown();
    }
  }

  /**
   * Submits the restoration task to the restoration executor thread pool. If any thread is
   * available the task will start automatically. Otherwise it will wait in a FIFO queue
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartition the id partition
   * @param datasetType the dataset type
   * @param user the user
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   *
   * @throws EEAException the eea exception
   */
  public void processRestoration(Long datasetId, Long idSnapshot, Long idPartition,
      DatasetTypeEnum datasetType, Boolean isSchemaSnapshot, Boolean deleteData)
      throws EEAException {
    RestorationTask restorationTask = new RestorationTask(datasetId, idSnapshot, idPartition,
        datasetType, isSchemaSnapshot, deleteData);
    this.restorationExecutorService.submit(new RestorationTasksExecutorThread(restorationTask));
  }

  @AllArgsConstructor
  private static class RestorationTask {

    Long datasetId;

    Long idSnapshot;

    Long idPartition;

    DatasetTypeEnum datasetType;

    Boolean isSchemaSnapshot;

    Boolean deleteData;
  }

  private class RestorationTasksExecutorThread implements Runnable {

    private static final double MILISECONDS = 1000.0;

    private RestorationTask restorationTask;

    public RestorationTasksExecutorThread(RestorationTask restorationTask) {
      this.restorationTask = restorationTask;
    }

    @Override
    public void run() {
      Long currentTime = System.currentTimeMillis();
      try {
        recordStoreService.restoreDataSnapshot(restorationTask.datasetId,
            restorationTask.idSnapshot, restorationTask.idPartition, restorationTask.datasetType,
            restorationTask.isSchemaSnapshot, restorationTask.deleteData);
      } catch (SQLException | IOException | RecordStoreAccessException e) {
        LOG_ERROR.error("Error restoring snapshot for dataset {}", restorationTask.datasetId, e);
      } finally {
        Double totalTime = (System.currentTimeMillis() - currentTime) / MILISECONDS;
        LOG.info("Restoration task for snapshot {} finished, it has taken taken {} seconds",
            restorationTask.idSnapshot, totalTime);
      }
    }
  }

}
