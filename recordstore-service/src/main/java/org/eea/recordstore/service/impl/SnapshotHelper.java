package org.eea.recordstore.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;

/**
 * The Class RestoreSnapshotHelper.
 */
@Component
public class SnapshotHelper implements DisposableBean {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SnapshotHelper.class);
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The recordStore service.
   */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * The max running tasks.
   */
  @Value("${snapshot.task.parallelism}")
  private int maxRunningTasks;

  /**
   * The restoration executor service.
   */
  private ExecutorService restorationExecutorService;

  /**
   * Instantiates a new file loader helper.
   */
  public SnapshotHelper() {
    super();
  }

  /**
   * Inits the executionservice.
   */
  @PostConstruct
  private void init() {
    restorationExecutorService = Executors.newFixedThreadPool(maxRunningTasks);
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
      DatasetTypeEnum datasetType, String user, Boolean isSchemaSnapshot, Boolean deleteData)
      throws EEAException {
    RestorationTask restorationTask = new RestorationTask(datasetId, idSnapshot, idPartition,
        datasetType, user, isSchemaSnapshot, deleteData);

    // first every task is always queued up to ensure the order

    if (((ThreadPoolExecutor) restorationExecutorService).getActiveCount() == maxRunningTasks) {
      LOG.info(
          "Snapshot {} will be queued up as there are no restoration threads available at the moment",
          idSnapshot);
    }

    this.restorationExecutorService.submit(new RestorationTasksExecutorThread(restorationTask));

  }

  /**
   * The Class RestorationTask.
   */

  @AllArgsConstructor
  private static class RestorationTask {

    /**
     * The dataset id.
     */
    Long datasetId;

    /**
     * The id snapshot.
     */
    Long idSnapshot;

    /**
     * The id partition.
     */
    Long idPartition;

    /**
     * The dataset type.
     */
    DatasetTypeEnum datasetType;

    /**
     * The user.
     */
    String user;

    /**
     * The is schema snapshot.
     */
    Boolean isSchemaSnapshot;

    /**
     * The delete data.
     */
    Boolean deleteData;
  }

  /**
   * The Class RestorationTasksExecutorThread.
   */
  private class RestorationTasksExecutorThread implements Runnable {

    /**
     * The Constant MILISECONDS.
     */
    private static final double MILISECONDS = 1000.0;
    /**
     * The restoration task.
     */
    private RestorationTask restorationTask;

    /**
     * Instantiates a new restoration tasks executor thread.
     *
     * @param restorationTask the restoration task
     */
    public RestorationTasksExecutorThread(RestorationTask restorationTask) {
      this.restorationTask = restorationTask;
    }

    /**
     * Run.
     */
    @Override
    public void run() {
      Long currentTime = System.currentTimeMillis();
      int workingThreads = ((ThreadPoolExecutor) restorationExecutorService).getActiveCount();

      LOG.info(
          "Executing restoration for snapshot {}. Working restoration threads {}, Available restoration threads {}",
          restorationTask.idSnapshot, workingThreads, maxRunningTasks - workingThreads);
      try {
        ThreadPropertiesManager.setVariable("user", restorationTask.user);
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
