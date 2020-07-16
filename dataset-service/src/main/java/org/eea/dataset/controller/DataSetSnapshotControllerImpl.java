package org.eea.dataset.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class DataSetSnapshotControllerImpl.
 */
@RestController
@RequestMapping("/snapshot")
public class DataSetSnapshotControllerImpl implements DatasetSnapshotController {

  /** The dataset metabase service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the by id.
   *
   * @param idSnapshot the id snapshot
   * @return the by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/{idSnapshot}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public SnapshotVO getById(@PathVariable("idSnapshot") Long idSnapshot) {
    SnapshotVO snapshot = null;
    try {
      snapshot = datasetSnapshotService.getById(idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the snapshot. ", e.getMessage(), e);
    }
    return snapshot;
  }

  /**
   * Gets the snapshots by id dataset.
   *
   * @param datasetId the dataset id
   *
   * @return the snapshots by id dataset
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataset/{idDataset}/listSnapshots",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ') OR (hasRole('DATA_CUSTODIAN'))")
  public List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("idDataset") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetSnapshotService.getSnapshotsByIdDataset(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the list of snapshots. ", e.getMessage(), e);
    }
    return snapshots;
  }

  /**
   * Creates the snapshot.
   *
   * @param datasetId the dataset id
   * @param createSnapshot the create snapshot
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @PostMapping(value = "/dataset/{idDataset}/create", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE')")
  public void createSnapshot(
      @LockCriteria(name = "datasetId") @PathVariable("idDataset") Long datasetId,
      @RequestBody CreateSnapshotVO createSnapshot) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    datasetSnapshotService.addSnapshot(datasetId, createSnapshot.getDescription(),
        createSnapshot.getReleased(), null);
  }

  /**
   * Delete snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{idSnapshot}/dataset/{idDataset}/delete")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATACOLLECTION_CUSTODIAN')")
  public void deleteSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.removeSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting a snapshot. ", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND, e);
    }
  }

  /**
   * Restore snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping(value = "/{idSnapshot}/dataset/{idDataset}/restore",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE') AND checkPermission('Dataset','MANAGE_DATA')")
  public void restoreSnapshot(
      @LockCriteria(name = "datasetId") @PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      // This method will release the lock
      datasetSnapshotService.restoreSnapshot(datasetId, idSnapshot, true);
    } catch (EEAException e) {
      LOG_ERROR.error("Error restoring a snapshot. ", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Release snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/{idSnapshot}/dataset/{idDataset}/release",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER')")
  @LockMethod(removeWhenFinish = false)
  public void releaseSnapshot(
      @LockCriteria(name = "datasetId") @PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    datasetSnapshotService.releaseSnapshot(datasetId, idSnapshot);
  }

  /**
   * Gets the schema snapshots by id dataset.
   *
   * @param datasetId the dataset id
   * @return the schema snapshots by id dataset
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataschema/{idDesignDataset}/listSnapshots",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE')")
  public List<SnapshotVO> getSchemaSnapshotsByIdDataset(
      @PathVariable("idDesignDataset") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetSnapshotService.getSchemaSnapshotsByIdDataset(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the list of schema snapshots. ", e.getMessage(), e);
    }
    return snapshots;
  }

  /**
   * Creates the schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   * @param description the description
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @PostMapping(value = "/dataschema/{idDatasetSchema}/dataset/{idDesignDataset}/create",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE')")
  public void createSchemaSnapshot(
      @LockCriteria(name = "datasetId") @PathVariable("idDesignDataset") Long datasetId,
      @PathVariable("idDatasetSchema") String idDatasetSchema,
      @RequestParam("description") String description) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    datasetSnapshotService.addSchemaSnapshot(datasetId, idDatasetSchema, description);
  }

  /**
   * Restore schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/restore",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE')")
  public void restoreSchemaSnapshot(
      @LockCriteria(name = "datasetId") @PathVariable("idDesignDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      // This method will release the lock
      datasetSnapshotService.restoreSchemaSnapshot(datasetId, idSnapshot);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error restoring a schema snapshot. ", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Delete schema snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @throws Exception the exception
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{idSnapshot}/dataschema/{idDesignDataset}/delete")
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE')")
  public void deleteSchemaSnapshot(@PathVariable("idDesignDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) throws Exception {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.removeSchemaSnapshot(datasetId, idSnapshot);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error deleting a schema snapshot", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND, e);
    }
  }

  /**
   * Creates the receipt PDF.
   *
   * @param response the response
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER')")
  @GetMapping(value = "/receiptPDF/dataflow/{dataflowId}/dataProvider/{dataProviderId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<StreamingResponseBody> createReceiptPDF(HttpServletResponse response,
      @PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId) {
    StreamingResponseBody stream =
        out -> datasetSnapshotService.createReceiptPDF(out, dataflowId, dataProviderId);

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment;filename=receipt.pdf");

    return new ResponseEntity<>(stream, HttpStatus.OK);
  }

}
