package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.util.List;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.metabase.SnapshotVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DataSetMetabaseControllerImpl.
 */
@RestController
@RequestMapping("/datasetmetabase")
public class DataSetMetabaseControllerImpl implements DatasetMetabaseController {


  /**
   * The dataset metabase service.
   */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /**
   * The reporting dataset service.
   */
  @Autowired
  private ReportingDatasetService reportingDatasetService;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ReportingDatasetVO> findDataSetIdByDataflowId(Long idDataflow) {

    return reportingDatasetService.getDataSetIdByDataflowId(idDataflow);

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
  @GetMapping(value = "/{id}/listSnapshots", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("id") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetMetabaseService.getSnapshotsByIdDataset(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return snapshots;

  }


  /**
   * Creates the snapshot.
   *
   * @param datasetId the dataset id
   * @param description the description
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/{id}/snapshot/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createSnapshot(@PathVariable("id") Long datasetId,
      @RequestParam("description") String description) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetMetabaseService.addSnapshot(datasetId, description);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }


  /**
   * Delete snapshot.
   *
   * @param datasetId the dataset id
   * @param idSnapshot the id snapshot
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{id}/snapshot/delete/{idSnapshot}")
  public void deleteSnapshot(@PathVariable("id") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetMetabaseService.removeSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }


  @Override
  @PostMapping(value = "/{id}/snapshot/restore", produces = MediaType.APPLICATION_JSON_VALUE)
  public void restoreSnapshot(@PathVariable("id") Long datasetId,
      @RequestParam("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetMetabaseService.restoreSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }

}
