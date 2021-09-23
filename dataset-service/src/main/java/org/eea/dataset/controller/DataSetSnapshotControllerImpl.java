package org.eea.dataset.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataset.CreateSnapshotVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.metabase.ReleaseVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
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

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataSetSnapshotControllerImpl.class);

  /** The dataset metabase service. */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The dataflow controller zull. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZull;

  /** The lock service. */
  @Autowired
  private LockService lockService;

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
      LOG_ERROR.error("Error getting the snapshot. Error message: {}", e.getMessage(), e);
    }
    return snapshot;
  }


  @Override
  @HystrixCommand
  @GetMapping(value = "/private/schemaSnapshot/{idSnapshot}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public SnapshotVO getSchemaById(@PathVariable("idSnapshot") Long idSnapshot) {
    SnapshotVO snapshot = null;
    try {
      snapshot = datasetSnapshotService.getSchemaById(idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the snapshot schema. ", e.getMessage(), e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  public List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("idDataset") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetSnapshotService.getSnapshotsByIdDataset(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error getting the list of snapshots. Error Message: {}", e.getMessage(), e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  public void createSnapshot(
      @LockCriteria(name = "datasetId") @PathVariable("idDataset") Long datasetId,
      @LockCriteria(name = "released",
          path = "released") @RequestBody CreateSnapshotVO createSnapshot) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // This method will release the lock
    datasetSnapshotService.addSnapshot(datasetId, createSnapshot, null, null);
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
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_CUSTODIAN','DATASET_REPORTER_WRITE','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  public void deleteSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.removeSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting a snapshot. Error Message: {}", e.getMessage(), e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
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
      // Check if the snapshot creation process is running and locked
      Map<String, Object> createSchemaSnapshot = new HashMap<>();
      createSchemaSnapshot.put(LiteralConstants.SIGNATURE,
          LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
      createSchemaSnapshot.put(LiteralConstants.DATASETID, datasetId);
      LockVO importLockVO = lockService.findByCriteria(createSchemaSnapshot);
      if (importLockVO != null) {
        throw new ResponseStatusException(HttpStatus.LOCKED,
            "Snapshot restoration is locked because creation is in progress.");
      } else {
        // This method will release the lock
        datasetSnapshotService.restoreSnapshot(datasetId, idSnapshot, true);
      }
    } catch (EEAException e) {
      LOG_ERROR.error("Error restoring a snapshot. Error Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Release snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   * @param dateRelease the date release
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/private/{idSnapshot}/dataset/{idDataset}/release",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER')")
  public void releaseSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot,
      @RequestParam("dateRelease") String dateRelease) {

    LOG.info("The user invoking DataSetSnaphotControllerImpl.releaseSnapshot is {}",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.releaseSnapshot(datasetId, idSnapshot, dateRelease);
    } catch (EEAException e) {
      LOG_ERROR.error("Error releasing a snapshot. Error Message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR, e);
    }
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
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
      LOG_ERROR.error("Error getting the list of schema snapshots. Error message: {}",
          e.getMessage(), e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
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
      LOG_ERROR.error("Error restoring a schema snapshot. Error Message {}", e.getMessage(), e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD')")
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
      Map<String, Object> createSchemaSnapshot = new HashMap<>();
      createSchemaSnapshot.put(LiteralConstants.SIGNATURE,
          LockSignature.CREATE_SCHEMA_SNAPSHOT.getValue());
      createSchemaSnapshot.put(LiteralConstants.DATASETID, datasetId);
      LockVO importLockVO = lockService.findByCriteria(createSchemaSnapshot);
      if (importLockVO != null) {
        throw new ResponseStatusException(HttpStatus.LOCKED,
            "Snapshot restoration is locked because creation is in progress.");
      } else {
        // This method will release the lock
        datasetSnapshotService.removeSchemaSnapshot(datasetId, idSnapshot);
      }
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error deleting a schema snapshot. Error message: {}", e.getMessage(), e);
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
      produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<StreamingResponseBody> createReceiptPDF(HttpServletResponse response,
      @PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId) {
    StreamingResponseBody stream =
        out -> datasetSnapshotService.createReceiptPDF(out, dataflowId, dataProviderId);

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment;filename=receipt.pdf");

    return new ResponseEntity<>(stream, HttpStatus.OK);
  }


  /**
   * Obtain the dataset historic releases.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/historicReleases", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_NATIONAL_COORDINATOR','DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER') OR checkApiKey(#dataflowId,null,#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD')")

  public List<ReleaseVO> historicReleases(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId) {
    List<ReleaseVO> releases;
    // get dataset type
    try {
      releases = datasetSnapshotService.getReleases(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error retreiving releases. Error message: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATASET_NOTFOUND,
          e);
    }

    return releases;
  }

  /**
   * Datasets historic releases by representative for all datasets involved.
   *
   * @param dataflowId the dataflow id
   * @param representativeId the representative id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/historicReleasesRepresentative",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_READ','DATAFLOW_REPORTER_WRITE','DATAFLOW_NATIONAL_COORDINATOR','DATAFLOW_OBSERVER')")
  public List<ReleaseVO> historicReleasesByRepresentative(
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam("representativeId") Long representativeId) {
    List<ReleaseVO> releases = new ArrayList<>();

    List<ReportingDataset> datasets = reportingDatasetRepository.findByDataflowId(dataflowId);
    List<Long> datasetIds =
        datasets.stream().filter(dataset -> dataset.getDataProviderId().equals(representativeId))
            .map(ReportingDataset::getId).collect(Collectors.toList());
    for (Long id : datasetIds) {
      releases.addAll(datasetSnapshotService.getSnapshotsReleasedByIdDataset(id));
    }
    return releases;
  }

  /**
   * Update snapshot EU release.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/eurelease/{idDataset}")
  public void updateSnapshotEURelease(@PathVariable("idDataset") Long datasetId) {
    datasetSnapshotService.updateSnapshotEURelease(datasetId);
  }


  /**
   * Creates the release snapshots.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @PostMapping(value = "/dataflow/{dataflowId}/dataProvider/{dataProviderId}/release",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER')")
  public void createReleaseSnapshots(
      @LockCriteria(name = "dataflowId") @PathVariable(value = "dataflowId",
          required = true) Long dataflowId,
      @LockCriteria(name = "dataProviderId") @PathVariable(value = "dataProviderId",
          required = true) Long dataProviderId,
      @RequestParam(name = "restrictFromPublic", required = true,
          defaultValue = "false") boolean restrictFromPublic) {

    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    LOG.info("The user invoking DataSetSnaphotControllerImpl.createReleaseSnapshots is {}",
        SecurityContextHolder.getContext().getAuthentication().getName());

    DataFlowVO dataflow = dataflowControllerZull.getMetabaseById(dataflowId);
    if (null != dataflow && dataflow.isReleasable()) {
      try {
        if (TypeDataflowEnum.BUSINESS.equals(dataflow.getType())) {
          restrictFromPublic = true;
        }
        datasetSnapshotService.createReleaseSnapshots(dataflowId, dataProviderId,
            restrictFromPublic);
      } catch (EEAException e) {
        LOG_ERROR.error("Error releasing a snapshot. Error Message: {}", e.getMessage(), e);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR,
            e);
      }
    } else {
      throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
          String.format(EEAErrorMessage.DATAFLOW_NOT_RELEASABLE, dataflowId));
    }
  }


  /**
   * Release locks from release datasets.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   */
  @Override
  @PutMapping("/private/releaseLocksRelatedToReleaseDataset/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
  public void releaseLocksFromReleaseDatasets(@PathVariable("dataflowId") Long dataflowId,
      @PathVariable("dataProviderId") Long dataProviderId) {
    try {
      datasetSnapshotService.releaseLocksRelatedToRelease(dataflowId, dataProviderId);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error releasing the locks in the operation release datasets. Error Message: {}",
          e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXECUTION_ERROR, e);
    }
  }

}
