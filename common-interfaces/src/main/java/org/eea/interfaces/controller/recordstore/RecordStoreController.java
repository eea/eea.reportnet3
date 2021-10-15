package org.eea.interfaces.controller.recordstore;

import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/create")
  void createSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "idPartitionDataset", required = true) Long idPartitionDataset,
      @RequestParam(value = "dateRelease", required = false) String dateRelease);



  /**
   * Restore snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param partitionId the partition id
   * @param datasetType the dataset type
   * @param isSchemaSnapshot the is schema snapshot
   * @param deleteData the delete data
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/restore")
  void restoreSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "partitionId", required = true) Long partitionId,
      @RequestParam(value = "typeDataset", required = true) DatasetTypeEnum datasetType,
      @RequestParam(value = "isSchemaSnapshot", required = true) Boolean isSchemaSnapshot,
      @RequestParam(value = "deleteData", defaultValue = "false") Boolean deleteData);

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
}
