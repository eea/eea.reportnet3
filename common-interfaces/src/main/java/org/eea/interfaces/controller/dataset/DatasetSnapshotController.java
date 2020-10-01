package org.eea.interfaces.controller.dataset;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * The Interface DatasetSnapshotController.
 */
public interface DatasetSnapshotController {

  /**
   * The Interface DataSetSnapshotControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "snapshot", path = "/snapshot")
  interface DataSetSnapshotControllerZuul extends DatasetSnapshotController {

  }

  /**
   * Gets the by id.
   *
   * @param idSnapshot the id snapshot
   * @return the by id
   */
  @GetMapping(value = "/private/{idSnapshot}", produces = MediaType.APPLICATION_JSON_VALUE)
  SnapshotVO getById(@PathVariable("idSnapshot") Long idSnapshot);

  /**
   * Gets the schema by id.
   *
   * @param idSnapshot the id snapshot
   * @return the schema by id
   */
  @GetMapping(value = "/private/schemaSnapshot/{idSnapshot}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  SnapshotVO getSchemaById(@PathVariable("idSnapshot") Long idSnapshot);

  /**
   * Gets the snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the snapshots by id dataset
   */
  @GetMapping(value = "/dataset/{idDataset}/listSnapshots",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("idDataset") Long datasetId);



  /**
   * Creates the snapshot.
   *
   * @param datasetId the dataset id
   * @param createSnapshot the create snapshot
   */
  @PostMapping(value = "/dataset/{idDataset}/create", produces = MediaType.APPLICATION_JSON_VALUE)
  void createSnapshot(@PathVariable("idDataset") Long datasetId,
      @RequestBody CreateSnapshotVO createSnapshot);

  /**
   * Delete snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @DeleteMapping(value = "/{idSnapshot}/dataset/{idDataset}/delete")
  void deleteSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot);


  /**
   * Restore snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @PostMapping(value = "/{idSnapshot}/dataset/{idDataset}/restore",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void restoreSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot);


  /**
   * Release snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @PutMapping(value = "/{idSnapshot}/dataset/{idDataset}/release",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void releaseSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot);



  /**
   * Gets the schema snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the schema snapshots by id dataset
   */
  @GetMapping(value = "/dataschema/{idDesignDataset}/listSnapshots",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<SnapshotVO> getSchemaSnapshotsByIdDataset(@PathVariable("idDesignDataset") Long datasetId);


  /**
   * Creates the schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   * @param description the description
   */
  @PostMapping(value = "/dataschema/{idDatasetSchema}/dataset/{idDesignDataset}/create",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void createSchemaSnapshot(@PathVariable("idDesignDataset") Long datasetId,
      @PathVariable("idDatasetSchema") String idDatasetSchema,
      @RequestParam("description") String description);


  /**
   * Restore schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @PostMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/restore",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void restoreSchemaSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot);


  /**
   * Delete schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @throws Exception the exception
   */
  @DeleteMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/delete")
  void deleteSchemaSnapshot(@PathVariable("idDesignDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) throws Exception;

  /**
   * Creates the receipt PDF.
   *
   * @param response the response
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the response entity
   */
  @GetMapping(value = "/receiptPDF/dataflow/{dataflowId}/dataProvider/{dataProviderId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<StreamingResponseBody> createReceiptPDF(HttpServletResponse response,
      @PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId);


  /**
   * Historic releases.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @GetMapping(value = "/historicReleases", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReleaseVO> historicReleases(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId);

  /**
   * Historic releases by representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeId the representative id
   * @return the list
   */
  @GetMapping(value = "/historicReleasesRepresentative",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReleaseVO> historicReleasesByRepresentative(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("representativeId") Long representativeId);

  /**
   * Update snapshot EU release.
   *
   * @param datasetId the dataset id
   */
  @PutMapping("/private/eurelease/{idDataset}")
  void updateSnapshotEURelease(@PathVariable("idDataset") Long datasetId);
}
