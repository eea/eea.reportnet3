package org.eea.recordstore.controller;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.interfaces.vo.recordstore.ProcessVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessTypeEnum;
import org.eea.interfaces.vo.validation.ProcessTaskVO;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.ProcessService;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.recordstore.service.TaskService;
import org.eea.recordstore.service.impl.SnapshotHelper;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class RecordStoreControllerImpl.
 */
@RestController
@RequestMapping("/recordstore")
@ApiIgnore
public class RecordStoreControllerImpl implements RecordStoreController {

  @Value("${pathSnapshot}")
  private String pathSnapshot;

  /**
   * The record store service.
   */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * The process service
   */
  @Autowired
  private ProcessService processService;

  /**
   * The task service
   */
  @Autowired
  private TaskService taskService;

  /**
   * The restore snapshot helper.
   */
  @Autowired
  private SnapshotHelper restoreSnapshotHelper;

  /** The dataset metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul datasetMetabaseControllerZuul;

  /**
   * The dataset controller zuul
   */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

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
      LOG.error(
              "Error creating an empty dataset: Dataset Name {}. idDatasetSchema {}. Message: {}",
              datasetName, idDatasetSchema, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.CREATING_EMPTY_DATASET);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating empty dataset {} with dataSchemaId {}. Message: {}", datasetName, idDatasetSchema, e.getMessage());
      throw e;
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
      LOG.error(e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving connection to dataset {}. Message: {}", datasetName, e.getMessage());
      throw e;
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
      LOG.error(e.getMessage(), e);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error retrieving dataset connection. Message: {}", e.getMessage());
      throw e;
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
                  defaultValue = "false") Boolean prefillingReference,
          @ApiParam(value = "ProcessId", example = "5eb5a2a9-c53f-4192", required = false) @RequestParam(
                  value = "processId", required = false) String processId) {
    try {
      ProcessVO processVO = null;
      if (processId!=null) {
        processVO = processService.getByProcessId(processId);
      }
      String user = processVO!=null ? processVO.getUser() : SecurityContextHolder.getContext().getAuthentication().getName();
      ThreadPropertiesManager.setVariable("user", user);
      LOG.info(
              "The user invoking RecordStoreControllerImpl.createSnapshotData is {} and the datasetId {} with processId {}", user, datasetId, processId);
      LOG.info("The user set on threadPropertiesManager is {}", ThreadPropertiesManager.getVariable("user"));
      if (StringUtils.isNotBlank(dateRelease)) {
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateRelease);
      }
      recordStoreService.createDataSnapshot(datasetId, idSnapshot, idPartitionDataset, dateRelease,
              prefillingReference, processId);
      LOG.info("Snapshot created");
    } catch (SQLException | IOException | RecordStoreAccessException | EEAException
             | ParseException e) {
      LOG.error(
              "Error creating a snapshot for the dataset: DatasetId {}. idSnapshot {}. processId {} Message: {}",
              datasetId, idSnapshot, processId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.CREATING_SNAPSHOT);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating snapshot data with id {} for datasetId {}, processId {}. Message: {}", idSnapshot, datasetId, processId, e.getMessage());
      throw e;
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
                                          defaultValue = "false") Boolean prefillingReference,
                                  @ApiParam(value = "Process Id", example = "5eb5a2a9-c53f-4192", required = false) @RequestParam(
                                          value = "processId", required = false) String processId) {

    try {
      // TO DO Status will be updated based on the running process in the dataset, this call will be
      // changed when processes table is implemented
      datasetMetabaseControllerZuul.updateDatasetRunningStatus(datasetId,
              DatasetRunningStatusEnum.RESTORING_SNAPSHOT);
      restoreSnapshotHelper.processRestoration(datasetId, idSnapshot, idPartition, datasetType,
              isSchemaSnapshot, deleteData, prefillingReference, processId);
    } catch (EEAException e) {
      LOG.error(
              "Error restoring a snapshot for the dataset: DatasetId {}. idSnapshot {}. processId {}. Message: {}",
              datasetId, idSnapshot, processId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.RESTORING_SNAPSHOT);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error restoring snapshot data with id {} for datasetId {} and processId {}. Message: {}", idSnapshot, datasetId, processId, e.getMessage());
      throw e;
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
      LOG.error(
              "Error deleting a snapshot in the dataset: DatasetId {}. idSnapshot {}. Message: {}",
              datasetId, idSnapshot, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              EEAErrorMessage.DELETING_SNAPSHOT);
    }
    catch (Exception e) {
      LOG.error("Unexpected error! Error removing snapshot data with id {} for datasetId {}. Message: {}", idSnapshot, datasetId, e.getMessage());
      throw e;
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
    try {
//      recordStoreService.deleteDataset(datasetSchemaName);
      LOG.info("Deleted dataset with name {}", datasetSchemaName);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error deleting dataset {}. Message: {}", datasetSchemaName, e.getMessage());
      throw e;
    }
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

    try {
      // This method will release the lock
      recordStoreService.createSchemas(datasetIdsAndSchemaIds, dataflowId, isCreation,
              isMaterialized);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating schemas for dataflowId {}. Message: {}", dataflowId, e.getMessage());
      throw e;
    }
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
    try {
      recordStoreService.distributeTables(datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error distributing tables for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
    }
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
    try {
      recordStoreService.createUpdateQueryView(datasetId, isMaterialized);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating update query view for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
    }
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
    try {
      recordStoreService.refreshMaterializedQuery(Arrays.asList(datasetId), false, false, datasetId,
              processId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error refreshing materialized view for datasetId {} and processId {}. Message: {}", datasetId, processId, e.getMessage());
      throw e;
    }
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
    try {
      recordStoreService.createSnapshotToClone(originDataset, targetDataset,
              dictionaryOriginTargetObjectId, partitionDatasetTarget, tableSchemasIdPrefill);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error cloning data from datasetId {} to datasetId {}. Message: {}", originDataset, targetDataset, e.getMessage());
      throw e;
    }
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
    try {
      recordStoreService.updateSnapshotDisabled(datasetId);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error updating snapshot disabled for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }


  /**
   /**
   * Restore specific file snapshot data.
   *
   * @param datasetId
   * @param idSnapshot
   * @param startingNumber
   * @param endingNumber
   * @param processId
   * @throws SQLException
   * @throws IOException
   */
  @HystrixCommand
  @PostMapping(value = "/restoreSpecificFileSnapshotData")
  @ApiOperation(value = "Restore specific snapshot data", hidden = true)
  @ApiResponse(code = 500, message = "Could not restore the specific snapshot data")
  public void restoreSpecificFileSnapshotData(
          @ApiParam(value = "Dataset Id", example = "0", required = true)
          @RequestParam("datasetId") Long datasetId,
          @ApiParam(value = "Snapshot Id", example = "0", required = true)
          @RequestParam("idSnapshot") Long idSnapshot,
          @ApiParam(value = "Starting number", example = "0", required = true)
          @RequestParam("startingNumber") int startingNumber,
          @ApiParam(value = "Ending number", example = "0", required = true)
          @RequestParam("endingNumber") int endingNumber,
          @ApiParam(value = "Process Id", example = "0", required = true)
          @RequestParam("processId") String processId,
          @RequestParam(name = "currentSplitFileName", required = false) String currentSplitFileName) throws SQLException, IOException {

    try {
      LOG.info("Method restoreSpecificSnapshotData starts for datasetId: {}, idSnapshot: {}, startingNumber: {}, endingNumber: {}, processId: {}",
              datasetId, idSnapshot, startingNumber, endingNumber, processId);

      recordStoreService.restoreSpecificFileSnapshot(datasetId, idSnapshot, startingNumber, endingNumber, processId, currentSplitFileName);

      LOG.info("Method restoreSpecificFileSnapshot ends");
    } catch (Exception e) {
      LOG.error("Error in method restoreSpecificSnapshotData for datasetId: {} with error {}", datasetId, e);
      throw e;
    }

  }

  /**
   * Check if data of file has been imported to dataset
   *
   * @param datasetId
   * @param firstFieldId
   * @param lastFieldId
   * @return
   */
  @HystrixCommand
  @GetMapping(value = "/recoverCheck")
  @ApiOperation(value = "Check if data of file has been imported to dataset", hidden = true)
  public boolean recoverCheck(
          @ApiParam(value = "Dataset Id", example = "0", required = true)
          @RequestParam("datasetId") Long datasetId,
          @ApiParam(value = "First FieldId", example = "0", required = true)
          @RequestParam("firstFieldId") String firstFieldId,
          @ApiParam(value = "Last FieldId", example = "0", required = true)
          @RequestParam("lastFieldId") String lastFieldId) {
    try {
      LOG.info("Method recoverCheck starts for datasetId: {}, firstFieldId: {}, lastFieldId: {}",
              datasetId, firstFieldId, lastFieldId);

      return recordStoreService.recoverCheckForStuckFile(datasetId, firstFieldId, lastFieldId);
    } catch (Exception e) {
      LOG.error("Error in method recoverCheck for datasetId: {} with error {}", datasetId, e);
      throw e;
    }
  }

  /**
   * Lists the tasks that are in progress for more than the specified period of time
   *
   * @param timeInMinutes
   * @return
   */
  @HystrixCommand
  @Override
  @GetMapping(value = "/findReleaseTasksInProgress/{timeInMinutes}")
  @ApiOperation(value = "Lists the tasks that are in progress for more than the specified period of time", hidden = true)
  public List<BigInteger> findReleaseTasksInProgress(@ApiParam(
          value = "Time limit in minutes that in progress release tasks exceed",
          example = "15") @PathVariable("timeInMinutes") long timeInMinutes) {
    LOG.info("Method findReleaseTasksInProgress finding in progress tasks that exceed {} minutes", timeInMinutes);
    try {
      return recordStoreService.getReleaseTasksInProgress(timeInMinutes);
    } catch (Exception e) {
      LOG.error("Error in method findReleaseTasksInProgress while finding in progress tasks that exceed {} minutes with error {}", timeInMinutes, e.getMessage());
      throw e;
    }
  }

  /**
   * Find the release task by task id
   *
   * @param taskId
   * @return
   */
  @HystrixCommand
  @GetMapping(value = "/findReleaseTaskByTaskId/{taskId}")
  @ApiOperation(value = "Find the release task by task id", hidden = true)
  public TaskVO findReleaseTaskByTaskId(
          @ApiParam(value = "Task Id") @PathVariable("taskId") long taskId) {
    LOG.info("Method findReleaseTaskByTaskId finding release task by task id {}", taskId);
    try {
      return recordStoreService.findReleaseTaskByTaskId(taskId);
    } catch (Exception e) {
      LOG.error("Error in method findReleaseTaskByTaskId while finding task with task id {} and error {}", taskId, e.getMessage());
      throw e;
    }
  }

  /**
   * Finds tasks by datasetId for in progress process
   * @param datasetId
   * @return
   */
  @HystrixCommand
  @GetMapping(value = "/private/releaseTasksByDatasetId/{datasetId}")
  @ApiOperation(value = "Find the release tasks for in progress process by datasetId", hidden = true)
  public List<ProcessTaskVO> findReleaseTasksForInProgressProcessByDatasetId(@ApiParam(value = "Dataset Id") @PathVariable("datasetId") Long datasetId) {
    List<String> processIds = processService.findProcessIdByDatasetAndStatusIn(datasetId, ProcessTypeEnum.RELEASE.toString(), Arrays.asList(ProcessStatusEnum.IN_PROGRESS.toString()));
    List<ProcessTaskVO> processTaskVOS = new ArrayList<>();
    processIds.forEach(processId -> {
      ProcessTaskVO processTaskVO = new ProcessTaskVO();
      processTaskVO.setProcessId(processId);
      List<TaskVO> taskVOS = taskService.findTaskByProcessId(processId);
      processTaskVO.setTasks(taskVOS);
      processTaskVOS.add(processTaskVO);
    });
    return processTaskVOS;
  }

  /**
   * Creates the update query view.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  @PutMapping("/createUpdateQueryView")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Creates or updates a View", hidden = true)
  public void createUpdateView(
          @ApiParam(value = "Dataset Id", example = "0") @RequestParam("datasetId") Long datasetId,
          @ApiParam(value = "Is the schema going to be materialized?",
                  example = "true") @RequestParam("isMaterialized") boolean isMaterialized) {
    try {
      LOG.info("Update query view for datasetId {}", datasetId);
      recordStoreService.createUpdateQueryView(datasetId, isMaterialized);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error creating update query view for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Finds tasks with type IMPORT_TASK and status IN_PROGRESS
   * @return the tasks
   */
  @HystrixCommand
  @GetMapping(value = "/private/findImportTasksInProgress")
  @ApiOperation(value = "Find the import tasks with status in progress", hidden = true)
  public List<TaskVO> findImportTasksInProgress() {
    return taskService.findImportTasksInProgress();
  }

  /**
   * saves task
   * @param task
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/private/task/save")
  public void saveTask(@RequestBody TaskVO task) {
    taskService.saveTask(task);
  }



  /**
   * Restore specific file snapshot data.
   *
   * @param datasetId
   * @param idSnapshot
   * @param startingNumber
   * @param endingNumber
   * @param forSplitting
   */
  @HystrixCommand
  @PostMapping(value = "/restoreSpecificFileSnapshotDataNoProcess")
  @ApiOperation(value = "Restore specific snapshot data", hidden = true)
  @ApiResponse(code = 500, message = "Could not restore the specific snapshot data")
  public void restoreSpecificFileSnapshotDataNoProcess(
          @ApiParam(value = "Dataset Id", example = "0", required = true)
          @RequestParam("datasetId") Long datasetId,
          @ApiParam(value = "Snapshot Id", example = "0", required = true)
          @RequestParam("idSnapshot") Long idSnapshot,
          @ApiParam(value = "Starting number", example = "0", required = true)
          @RequestParam("startingNumber") Long startingNumber,
          @ApiParam(value = "Ending number", example = "0", required = true)
          @RequestParam("endingNumber") Long endingNumber,
          @ApiParam(value = "TRUE or FALSE", example = "TRUE")
          @RequestParam("forSplitting") boolean forSplitting) {

    try {
      LOG.info("Method restoreSpecificSnapshotData starts for datasetId: {}, idSnapshot: {}, startingNumber: {}, endingNumber: {}, forSplitting: {}",
              datasetId, idSnapshot, startingNumber, endingNumber, forSplitting);

      recordStoreService.restoreSpecificFileSnapshot(datasetId, idSnapshot, startingNumber, endingNumber, forSplitting);

      LOG.info("Method restoreSpecificFileSnapshot ends");
    } catch (Exception e) {
      LOG.error("Error in method restoreSpecificSnapshotData for datasetId: {} with error {}", datasetId, e);
    }

  }

  /**
   * Get list of the latest release snapshot files
   * @param datasetId
   * @param dataflowId
   * @return
   */
  @HystrixCommand
  @GetMapping(value = "/getLatestReleaseSnapshots")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_STEWARD_SUPPORT') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD') OR hasAnyRole('ADMIN')")
  @Override
  public List<String> getLatestReleaseSnapshots(@RequestParam("datasetId") Long datasetId, @RequestParam("dataflowId") Long dataflowId) {
    List<String> snapshotFiles;
    try {
      snapshotFiles = recordStoreService.getLatestReleaseSnapshots(datasetId, dataflowId);
    } catch (Exception e) {
      LOG.error("Error retrieving snapshot files for datasetId " + datasetId);
      throw e;
    }
    return snapshotFiles;
  }

  @HystrixCommand
  @GetMapping(value = "/downloadSnapshot/{datasetId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_STEWARD_SUPPORT','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_STEWARD_SUPPORT','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_STEWARD_SUPPORT') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD') OR hasAnyRole('ADMIN')")
  @Override
  public void downloadSnapshotFile(@PathVariable("datasetId") Long datasetId, @RequestParam("dataflowId") Long dataflowId,
                                   @RequestParam("fileName") String fileName, HttpServletResponse response) throws EEAException, IOException {

    String user = SecurityContextHolder.getContext().getAuthentication().getName();
    File file = new File(new File(pathSnapshot), FilenameUtils.getName(fileName));
    if (!file.exists()) {
      LOG.error("Error downloading file {}, file doesn't exist", fileName);
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    }
    kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_FILE_START_EVENT, null,
            NotificationVO.builder().datasetId(datasetId).user(user).fileName(fileName).build());
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    try (OutputStream out = response.getOutputStream();
         FileInputStream in = new FileInputStream(file)) {
      IOUtils.copyLarge(in, out);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error downloading file {}. Message: {}", fileName, e.getMessage());
      throw e;
    }
  }
}



