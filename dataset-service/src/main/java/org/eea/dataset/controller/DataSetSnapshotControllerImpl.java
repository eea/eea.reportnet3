package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSnapshotController;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RequestMapping("/snapshot")
public class DataSetSnapshotControllerImpl implements DatasetSnapshotController {


  /**
   * The dataset metabase service.
   */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;



  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
  public List<SnapshotVO> getSnapshotsByIdDataset(@PathVariable("idDataset") Long datasetId) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    List<SnapshotVO> snapshots = null;
    try {
      snapshots = datasetSnapshotService.getSnapshotsByIdDataset(datasetId);
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
  @PostMapping(value = "/dataset/{idDataset}/snapshot/create",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void createSnapshot(@PathVariable("idDataset") Long datasetId,
      @RequestParam("description") String description) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.addSnapshot(datasetId, description);
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
  @DeleteMapping(value = "/{idSnapshot}/dataset/{idDataset}/delete")
  public void deleteSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.removeSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }


  @Override
  @PostMapping(value = "/{idSnapshot}/dataset/{idDataset}/restore",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void restoreSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.restoreSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }


  @Override
  @PutMapping(value = "/{idSnapshot}/dataset/{idDataset}/release",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void releaseSnapshot(@PathVariable("idDataset") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetSnapshotService.releaseSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }



}
