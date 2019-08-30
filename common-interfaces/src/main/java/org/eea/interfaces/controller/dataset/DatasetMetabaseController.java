package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DatasetMetabaseController.
 */
public interface DatasetMetabaseController {

  /**
   * The Interface DataSetMetabaseControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "datasetmetabase", path = "/datasetmetabase")
  interface DataSetMetabaseControllerZuul extends DatasetMetabaseController {

  }


  /**
   * Find data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> findDataSetIdByDataflowId(@PathVariable("id") final Long idDataflow);


  /**
   * Gets the snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots by id dataset
   */
  @GetMapping(value = "/{id}/listSnapshots", produces = MediaType.APPLICATION_JSON_VALUE)
  List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("id") Long datasetId);


  /**
   * Creates the snapshot.
   *
   * @param datasetId the dataset id
   * @param description the description
   */
  @PostMapping(value = "/{id}/snapshot/create", produces = MediaType.APPLICATION_JSON_VALUE)
  void createSnapshot(@PathVariable("id") Long datasetId,
      @RequestParam("description") String description);

  /**
   * Delete snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @DeleteMapping(value = "/{id}/snapshot/delete/{idSnapshot}")
  void deleteSnapshot(@PathVariable("id") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot);


  @PostMapping(value = "/{id}/snapshot/restore", produces = MediaType.APPLICATION_JSON_VALUE)
  void restoreSnapshot(@PathVariable("id") Long datasetId,
      @RequestParam("idSnapshot") Long idSnapshot);


  @PutMapping(value = "/{id}/snapshot/release", produces = MediaType.APPLICATION_JSON_VALUE)
  void releaseSnapshot(@PathVariable("id") Long datasetId,
      @RequestParam("idSnapshot") Long idSnapshot);

}
