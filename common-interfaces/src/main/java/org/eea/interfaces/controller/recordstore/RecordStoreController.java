package org.eea.interfaces.controller.recordstore;

import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.interfaces.vo.validation.ProcessTaskVO;
import org.eea.interfaces.vo.validation.TaskVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Interface RecordStoreController.
 */
public interface RecordStoreController {

  /**
   * The Interface RecordStoreControllerZuul.
   */
  @FeignClient(value = "recordstore", path = "/recordstore")
  interface RecordStoreControllerZuul extends RecordStoreController {

  }


  /**
   * Creates the empty dataset.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   */
  @PostMapping(value = "/private/dataset/create/{datasetName}")
  void createEmptyDataset(@PathVariable("datasetName") String datasetName,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema);

  /**
   * Gets connection to dataset.
   *
   * @param datasetName the dataset name
   *
   * @return connection to dataset
   */
  @GetMapping("/private/connection")
  ConnectionDataVO getConnectionToDataset(@RequestParam String datasetName);

  /**
   * Gets connection to dataset.
   *
   * @return the connection to dataset
   */
  @GetMapping(value = "/private/connections")
  List<ConnectionDataVO> getDataSetConnections();

  /**
   * Creates the snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartitionDataset the id partition dataset
   * @param dateRelease the date release
   * @param prefillingReference the prefilling reference
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/create")
  void createSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "idPartitionDataset", required = true) Long idPartitionDataset,
      @RequestParam(value = "dateRelease", required = false) String dateRelease,
      @RequestParam(value = "prefillingReference", required = false,
          defaultValue = "false") Boolean prefillingReference,
      @RequestParam(value = "processId", required = false) String processId);



  /**
   * Restore snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   * @param prefillingReference the prefilling reference
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/restore")
  void restoreSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "partitionId", required = true) Long partitionId,
      @RequestParam(value = "typeDataset", required = true) DatasetTypeEnum datasetType,
      @RequestParam(value = "isSchemaSnapshot", required = true) Boolean isSchemaSnapshot,
      @RequestParam(value = "deleteData", defaultValue = "false") Boolean deleteData,
      @RequestParam(value = "prefillingReference", required = false,
          defaultValue = "false") Boolean prefillingReference,
      @RequestParam(value = "processId", required = false) String processId);

  /**
   * Delete snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/delete")
  void deleteSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot);

  /**
   * Delete dataset.
   *
   * @param datasetSchemaName the dataset schema name
   */
  @DeleteMapping(value = "/dataset/{datasetSchemaName}")
  void deleteDataset(@PathVariable("datasetSchemaName") String datasetSchemaName);

  /**
   * Creates a schema for each entry in the list by executing the queries contained in an external
   * file. Also releases events to feed the new schemas. Uses the dataflow to release the lock and
   * send the finish notification.
   * <p>
   * <b>Note:</b> {@literal @}<i>Async</i> annotated method.
   * </p>
   *
   * @param datasetIdsAndSchemaIds Map matching datasetIds with datasetSchemaIds.
   * @param dataflowId The DataCollection's dataflow.
   * @param isCreation the is creation
   * @param isMaterialized the is materialized
   */
  @PutMapping("/private/dataset/create/dataCollection/{dataflowId}")
  void createSchemas(@RequestBody Map<Long, String> datasetIdsAndSchemaIds,
      @PathVariable("dataflowId") Long dataflowId, @RequestParam("isCreation") boolean isCreation,
      @RequestParam("isMaterialized") boolean isMaterialized);



  /**
   * Creates the update query view.
   *
   * @param datasetId the dataset id
   * @param isMaterialized the is materialized
   */
  @PutMapping("/private/createUpdateQueryView")
  void createUpdateQueryView(@RequestParam("datasetId") Long datasetId,
      @RequestParam("isMaterialized") boolean isMaterialized);


  /**
   * Refresh materialized view.
   *
   * @param datasetId the dataset id
   * @param processId the process id
   */
  @PutMapping("/private/refreshMaterializedView")
  void refreshMaterializedView(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "processId", required = false) String processId);


  /**
   * Clone data.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param originDataset the origin dataset
   * @param targetDataset the target dataset
   * @param partitionDatasetTarget the partition dataset target
   * @param tableSchemasIdPrefill the table schemas id prefill
   */
  @PutMapping("/private/cloneData/origin/{originDataset}/target/{targetDataset}")
  void cloneData(@RequestBody Map<String, String> dictionaryOriginTargetObjectId,
      @PathVariable("originDataset") Long originDataset,
      @PathVariable("targetDataset") Long targetDataset,
      @RequestParam("partitionDatasetTarget") Long partitionDatasetTarget,
      @RequestParam("tableSchemasId") List<String> tableSchemasIdPrefill);

  /**
   * Update snapshot disabled.
   *
   * @param datasetId the dataset id
   */
  @PutMapping("/private/updateSnapshotDisabled/{datasetId}")
  void updateSnapshotDisabled(@PathVariable("datasetId") Long datasetId);

  /**
   * Distribute tables.
   *
   * @param datasetId the dataset id
   */
  @PutMapping("/private/dataset/create/dataCollection/finish/{datasetId}")
  void distributeTables(@PathVariable("datasetId") Long datasetId);

  /**
   * Lists the release task ids that are in progress for more than the specified period of time
   *
   * @param timeInMinutes
   * @return
   */
  @GetMapping(value = "/findReleaseTasksInProgress/{timeInMinutes}")
  List<BigInteger> findReleaseTasksInProgress(@PathVariable("timeInMinutes") long timeInMinutes);


  /**
   * Gets release task by task id
   *
   * @param taskId
   * @return
   */
  @GetMapping(value = "/findReleaseTaskByTaskId/{taskId}")
  TaskVO findReleaseTaskByTaskId(@PathVariable("taskId") long taskId);

  /**
   * Restore release process
   *
   * @param
   * @return
   */
  @PostMapping(value = "/restoreSpecificFileSnapshotData")
  void restoreSpecificFileSnapshotData(
      @RequestParam("datasetId") Long datasetId,
      @RequestParam("idSnapshot") Long idSnapshot,
      @RequestParam("startingNumber") int startingNumber,
      @RequestParam("endingNumber") int endingNumber,
      @RequestParam("processId") String processId,
      @RequestParam(name = "currentSplitFileName", required = false) String currentSplitFileName) throws SQLException, IOException;

  /**
   * Check if data of file has been imported to dataset
   *
   * @param datasetId
   * @param firstFieldId
   * @param lastFieldId
   * @return
   */
  @GetMapping(value = "/recoverCheck")
  boolean recoverCheck(
      @RequestParam("datasetId") Long datasetId,
      @RequestParam("firstFieldId") String firstFieldId,
      @RequestParam("lastFieldId") String lastFieldId);

  /**
   * Finds tasks by datasetId for in progress process
   * @param datasetId
   * @return
   */
  @GetMapping(value = "/private/releaseTasksByDatasetId/{datasetId}")
  List<ProcessTaskVO> findReleaseTasksForInProgressProcessByDatasetId(@PathVariable("datasetId") Long datasetId);
}
