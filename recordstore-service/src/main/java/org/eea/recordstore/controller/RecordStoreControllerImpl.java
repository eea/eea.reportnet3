package org.eea.recordstore.controller;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class RecordStoreControllerImpl.
 */
@RestController
@RequestMapping("/recordstore")
public class RecordStoreControllerImpl implements RecordStoreController {

  /**
   * The record store service.
   */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RecordStoreControllerImpl.class);

  /**
   * Reste data set data base.
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/reset", method = RequestMethod.POST)
  public void resteDataSetDataBase() {
    try {
      recordStoreService.resetDatasetDatabase();
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
    }
  }


  /**
   * Creates the empty dataset.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/dataset/create/{datasetName}", method = RequestMethod.POST)
  public void createEmptyDataset(@PathVariable("datasetName") final String datasetName,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema) {
    // TODO neeed to create standar
    try {
      recordStoreService.createEmptyDataSet(datasetName, idDatasetSchema);
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
      // TODO Error control
    }
  }

  /**
   * Gets the connection to dataset.
   *
   * @param datasetName the dataset name
   *
   * @return the connection to dataset
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/connection/{datasetName}", method = RequestMethod.GET)
  public ConnectionDataVO getConnectionToDataset(
      @PathVariable("datasetName") final String datasetName) {
    ConnectionDataVO vo = null;
    try {
      vo = recordStoreService.getConnectionDataForDataset(datasetName);
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
    }
    return vo;
  }

  /**
   * Gets the connection to dataset.
   *
   * @return the connection to dataset
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/connections", method = RequestMethod.GET)
  public List<ConnectionDataVO> getDataSetConnections() {
    List<ConnectionDataVO> vo = null;
    try {
      vo = recordStoreService.getConnectionDataForDataset();
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
    }
    return vo;
  }


  /**
   * Creates the snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartitionDataset the id partition dataset
   */
  @Override
//  @HystrixCommand
  @RequestMapping(value = "/dataset/{datasetId}/snapshot/create", method = RequestMethod.POST)
  public void createSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "idPartitionDataset", required = true) Long idPartitionDataset) {
    try {
      recordStoreService.createDataSnapshot(datasetId, idSnapshot, idPartitionDataset);
      LOG.info("Snapshot created");
    } catch (SQLException | IOException | RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }


  /**
   * Restore snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartition the id partition
   * @param datasetType the dataset type
   */
  @Override
  @HystrixCommand
  @PostMapping("/dataset/{datasetId}/snapshot/restore")
  public void restoreSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "partitionId", required = true) Long idPartition,
      @RequestParam(value = "typeDataset", required = true) DatasetTypeEnum datasetType,
      @RequestParam(value = "user", required = true) String user,
      @RequestParam(value = "isSchemaSnapshot", required = true) Boolean isSchemaSnapshot,
      @RequestParam(value = "deleteData", defaultValue = "true") Boolean deleteData) {

    try {
      ThreadPropertiesManager.setVariable("user", user);
      recordStoreService.restoreDataSnapshot(datasetId, idSnapshot, idPartition, datasetType,
          isSchemaSnapshot, deleteData);
    } catch (SQLException | IOException | RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }

  /**
   * Restore snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartition the id partition
   * @param datasetType the dataset type
   */
  @PostMapping("/dataset/{datasetId}/snapshot/restore/poc")
  public void restoreSnapshotDataPOC(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "partitionId", required = true) Long idPartition,
      @RequestParam(value = "typeDataset", required = true) DatasetTypeEnum datasetType,
      @RequestParam(value = "user", required = true) String user,
      @RequestParam(value = "isSchemaSnapshot", required = true) Boolean isSchemaSnapshot,
      @RequestParam(value = "deleteData", defaultValue = "true") Boolean deleteData) {

    try {
      ThreadPropertiesManager.setVariable("user", user);
      recordStoreService.restoreDataSnapshotPoc(datasetId, idSnapshot, idPartition, datasetType,
          isSchemaSnapshot, deleteData);
    } catch (SQLException | IOException e) {
      LOG_ERROR.error(e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }

  /**
   * Delete snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/dataset/{datasetId}/snapshot/delete", method = RequestMethod.POST)
  public void deleteSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot) {

    try {
      recordStoreService.deleteDataSnapshot(datasetId, idSnapshot);
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }

  /**
   * Delete dataset.
   *
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/dataset/{datasetSchemaName}")
  public void deleteDataset(@PathVariable("datasetSchemaName") String datasetSchemaName) {
    recordStoreService.deleteDataset(datasetSchemaName);
  }

  /**
   * Creates a schema for each entry in the list. Also releases events to feed the new schemas.
   * <p>
   * <b>Note:</b> {@literal @}<i>Async</i> annotated method.
   * </p>
   *
   * @param datasetIdsAndSchemaIds Map matching datasetIds with datasetSchemaIds.
   * @param dataflowId The DataCollection's dataflow.
   * @param isCreation the is creation
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/dataset/create/dataCollection/{dataflowId}")
  public void createSchemas(@RequestBody Map<Long, String> datasetIdsAndSchemaIds,
      @PathVariable("dataflowId") Long dataflowId, @RequestParam("isCreation") boolean isCreation) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    recordStoreService.createSchemas(datasetIdsAndSchemaIds, dataflowId, isCreation);
  }
}
