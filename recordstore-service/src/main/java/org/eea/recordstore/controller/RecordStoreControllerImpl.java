package org.eea.recordstore.controller;


import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.recordstore.service.impl.SnapshotHelper;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class RecordStoreControllerImpl.
 */
@RestController
@RequestMapping("/recordstore")
@ApiIgnore
public class RecordStoreControllerImpl implements RecordStoreController {

  /**
   * The record store service.
   */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * The restore snapshot helper.
   */
  @Autowired
  private SnapshotHelper restoreSnapshotHelper;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RecordStoreControllerImpl.class);

  /**
   * Creates the empty dataset.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/dataset/create/{datasetName}")
  @ApiOperation(value = "Creates an empty Dataset with the following parameters", hidden = true)
  @ApiResponse(code = 500, message = "Couldn't create a new empty Dataset.")
  public void createEmptyDataset(
      @ApiParam(value = "Dataset name",
          example = "Dataset displayed name") @PathVariable("datasetName") final String datasetName,
      @ApiParam(value = "Dataset Id schema", example = "5cf0e9b3b793310e9ceca190",
          required = false) @RequestParam(value = "idDatasetSchema",
              required = false) String idDatasetSchema) {
    try {
      recordStoreService.createEmptyDataSet(datasetName, idDatasetSchema);
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(
          "Error creating an empty dataset: Dataset Name {}. idDatasetSchema {}. Message: {}",
          datasetName, idDatasetSchema, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.CREATING_EMPTY_DATASET);
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
  @GetMapping("/private/connection")
  @ApiOperation(value = "Gets connection to a dataset based on a Dataset name",
      response = ConnectionDataVO.class, hidden = true)
  public ConnectionDataVO getConnectionToDataset(@ApiParam(value = "Dataset name",
      example = "Dataset displayed name") @RequestParam String datasetName) {
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
  @GetMapping(value = "/private/connections")
  @ApiOperation(value = "Gets all the dataset connections", response = ConnectionDataVO.class,
      responseContainer = "List", hidden = true)
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
   * @param dateRelease the date release
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @PostMapping(value = "/dataset/{datasetId}/snapshot/create")
  @ApiOperation(value = "Creates snapshot data for a given Dataset", hidden = true)
  @ApiResponse(code = 500, message = "Could not create the snapshot data.")
  public void createSnapshotData(
      @ApiParam(value = "Dataset Id", example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(value = "Snapshot Id", example = "0",
          required = true) @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @ApiParam(value = "Dataset Partition Id", example = "0", required = true) @RequestParam(
          value = "idPartitionDataset", required = true) Long idPartitionDataset,
      @ApiParam(value = "Release date", example = "YYYY-MM-DD", required = false) @RequestParam(
          value = "dateRelease", required = false) String dateRelease,
      @ApiParam(value = "Prefilling reference", example = "false", required = false) @RequestParam(
          value = "prefillingReference", required = false,
          defaultValue = "false") Boolean prefillingReference) {
    try {
      ThreadPropertiesManager.setVariable("user",
          SecurityContextHolder.getContext().getAuthentication().getName());
      LOG.info(
          "The user invoking RecordStoreControllerImpl.createSnapshotData is {} and the datasetId {}",
          SecurityContextHolder.getContext().getAuthentication().getName(), datasetId);
      LOG.info("The user set on threadPropertiesManager is {}",
          ThreadPropertiesManager.getVariable("user"));
      if (StringUtils.isNotBlank(dateRelease)) {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateRelease);
      }
      recordStoreService.createDataSnapshot(datasetId, idSnapshot, idPartitionDataset, dateRelease,
          prefillingReference);
      LOG.info("Snapshot created");
    } catch (SQLException | IOException | RecordStoreAccessException | EEAException
        | ParseException e) {
      LOG_ERROR.error(
          "Error creating a snapshot for the dataset: DatasetId {}. idSnapshot {}. Message: {}",
          datasetId, idSnapshot, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.CREATING_SNAPSHOT);
    }

  }


  /**
   * Restore snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartition the id partition
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   */
  @Override
  @HystrixCommand
  @PostMapping("/dataset/{datasetId}/snapshot/restore")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Restores snapshot data for a given Dataset", hidden = true)
  @ApiResponse(code = 500, message = "Could not restore the snapshot data.")
  public void restoreSnapshotData(@PathVariable("datasetId") Long datasetId,
      @ApiParam(value = "Snapshot Id", example = "0",
          required = true) @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @ApiParam(value = "Partition Id", example = "0",
          required = true) @RequestParam(value = "partitionId", required = true) Long idPartition,
      @ApiParam(value = "Dataset type", example = "REPORTING", required = true) @RequestParam(
          value = "typeDataset", required = true) DatasetTypeEnum datasetType,
      @ApiParam(value = "Is it a schema snapshot?", example = "true",
          required = true) @RequestParam(value = "isSchemaSnapshot",
              required = true) Boolean isSchemaSnapshot,
      @ApiParam(value = "Should prior data be erased?", example = "true",
          defaultValue = "true") @RequestParam(value = "deleteData",
              defaultValue = "true") Boolean deleteData,
      @ApiParam(value = "Prefilling reference", example = "false", required = false) @RequestParam(
          value = "prefillingReference", required = false,
          defaultValue = "false") Boolean prefillingReference) {

    try {
      // TO DO Status will be updated based on the running process in the dataset, this call will be
      // changed when processes table is implemented
      datasetMetabaseControllerZuul.updateDatasetRunningStatus(datasetId,
          DatasetRunningStatusEnum.RESTORING_SNAPSHOT);
      restoreSnapshotHelper.processRestoration(datasetId, idSnapshot, idPartition, datasetType,
          isSchemaSnapshot, deleteData, prefillingReference);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error restoring a snapshot for the dataset: DatasetId {}. idSnapshot {}. Message: {}",
          datasetId, idSnapshot, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.RESTORING_SNAPSHOT);
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
  @PostMapping(value = "/dataset/{datasetId}/snapshot/delete")
  @ApiOperation(value = "Delete snapshot data for a given Dataset", hidden = true)
  @ApiResponse(code = 500, message = "Could not delete the snapshot data")
  public void deleteSnapshotData(
      @ApiParam(value = "Dataset Id", example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(value = "Snapshot Id", example = "0",
          required = true) @RequestParam(value = "idSnapshot", required = true) Long idSnapshot) {

    try {
      recordStoreService.deleteDataSnapshot(datasetId, idSnapshot);
    } catch (IOException e) {
      LOG_ERROR.error(
          "Error deleting a snapshot in the dataset: DatasetId {}. idSnapshot {}. Message: {}",
          datasetId, idSnapshot, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.DELETING_SNAPSHOT);
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
  @ApiOperation(value = "Delete dataset data for a given dataset schema name", hidden = true)
  public void deleteDataset(@ApiParam(value = "Dataset schema name",
      example = "Dataset schema displayed name") @PathVariable("datasetSchemaName") String datasetSchemaName) {
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
   * @param isMaterialized the is materialized
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/dataset/create/dataCollection/{dataflowId}")
  @ApiOperation(value = "Creates a dataset schema for each entry in the map", hidden = true)
  public void createSchemas(@ApiParam(
      value = "Map containing associations between datasetIds and schemaIds") @RequestBody Map<Long, String> datasetIdsAndSchemaIds,
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(value = "Is creating dataset schemas from scratch?",
          example = "true") @RequestParam("isCreation") boolean isCreation,
      @ApiParam(value = "Is the schema view going to be materialized?",
          example = "true") @RequestParam("isMaterialized") boolean isMaterialized) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    recordStoreService.createSchemas(datasetIdsAndSchemaIds, dataflowId, isCreation,
        isMaterialized);
  }

  /**
   * Distribute tables.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/dataset/create/dataCollection/finish/{datasetId}")
  @ApiOperation(value = "Distribute into reference tables", hidden = true)
  public void distributeTables(
      @ApiParam(value = "Dataset Id", example = "0") @PathVariable("datasetId") Long datasetId) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    recordStoreService.distributeTables(datasetId);
  }


  /**
   * Creates the update query view.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  @Override
  @PutMapping("/private/createUpdateQueryView")
  @ApiOperation(value = "Creates or updates a View", hidden = true)
  public void createUpdateQueryView(
      @ApiParam(value = "Dataset Id", example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Is the schema going to be materialized?",
          example = "true") @RequestParam("isMaterialized") boolean isMaterialized) {
    recordStoreService.createUpdateQueryView(datasetId, isMaterialized);
  }

  /**
   * Refresh materialized view.
   *
   * @param datasetId the dataset id
   * @param processId the process id
   */
  @Override
  @PutMapping("/private/refreshMaterializedView")
  @ApiOperation(value = "Refreshes a materialized view", hidden = true)
  public void refreshMaterializedView(
      @ApiParam(value = "Dataset Id", example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "ProcessId", example = "0") @RequestParam(value = "processId",
          required = false) String processId) {

    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    recordStoreService.refreshMaterializedQuery(Arrays.asList(datasetId), false, false, datasetId,
        processId);
  }

  /**
   * Clone data.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param originDataset the origin dataset
   * @param targetDataset the target dataset
   * @param partitionDatasetTarget the partition dataset target
   * @param tableSchemasIdPrefill the table schemas id prefill
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/cloneData/origin/{originDataset}/target/{targetDataset}")
  @ApiOperation(value = "Private operation to copy data between two datasets", hidden = true)
  public void cloneData(@RequestBody Map<String, String> dictionaryOriginTargetObjectId,
      @PathVariable("originDataset") Long originDataset,
      @PathVariable("targetDataset") Long targetDataset,
      @RequestParam("partitionDatasetTarget") Long partitionDatasetTarget,
      @RequestParam("tableSchemasId") List<String> tableSchemasIdPrefill) {
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    recordStoreService.createSnapshotToClone(originDataset, targetDataset,
        dictionaryOriginTargetObjectId, partitionDatasetTarget, tableSchemasIdPrefill);
  }

  /**
   * Update snapshot disabled.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/updateSnapshotDisabled/{datasetId}")
  @ApiOperation(value = "Private operation to update snapshot, disable and move the files",
      hidden = true)
  public void updateSnapshotDisabled(@PathVariable("datasetId") Long datasetId) {
    recordStoreService.updateSnapshotDisabled(datasetId);
  }

}
