package org.eea.interfaces.controller.recordstore;

import java.util.List;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The interface Record store controller.
 */
public interface RecordStoreController {


  /**
   * The interface Record store controller zull.
   */
  @FeignClient(value = "recordstore", path = "/recordstore")
  interface RecordStoreControllerZull extends RecordStoreController {

  }

  /**
   * Reste data set data base. DO NOT USE IN PRODUCTION. TO BE REMOVED. ONLY FOR TEST PURPOSES
   *
   * @deprecated (reset db)
   */
  @Deprecated
  @RequestMapping(value = "/reset", method = RequestMethod.POST)
  void resteDataSetDataBase();


  /**
   * Creates the empty dataset.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   */
  @RequestMapping(value = "/dataset/create/{datasetName}", method = RequestMethod.POST)
  void createEmptyDataset(@PathVariable("datasetName") String datasetName,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema);

  /**
   * Gets connection to dataset.
   *
   * @param datasetName the dataset name
   *
   * @return connection to dataset
   */
  @RequestMapping(value = "/connections/{datasetName}", method = RequestMethod.GET)
  ConnectionDataVO getConnectionToDataset(@PathVariable("datasetName") String datasetName);

  /**
   * Gets connection to dataset.
   *
   * @return the connection to dataset
   */
  @RequestMapping(value = "/connections", method = RequestMethod.GET)
  List<ConnectionDataVO> getDataSetConnections();

  /**
   * Creates the snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param idPartitionDataset the id partition dataset
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/create")
  void createSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "idPartitionDataset", required = true) Long idPartitionDataset);



  /**
   * Restore snapshot data.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param datasetType the dataset type
   */
  @PostMapping(value = "/dataset/{datasetId}/snapshot/restore")
  void restoreSnapshotData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "idSnapshot", required = true) Long idSnapshot,
      @RequestParam(value = "typeDataset", required = true) TypeDatasetEnum datasetType);

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

}
