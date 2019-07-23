package org.eea.dataset.controller;

import java.util.List;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
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


  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Find data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DataSetMetabaseVO> findDataSetIdByDataflowId(Long idDataflow) {

    return datasetMetabaseService.getDataSetIdByDataflowId(idDataflow);

  }


  @Override
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


  @Override
  @PostMapping(value = "/{id}/snapshot/create", produces = MediaType.APPLICATION_JSON_VALUE)
  public void createSnapshot(@PathVariable("id") Long datasetId,
      @RequestParam("description") String description) {

    try {
      datasetMetabaseService.addSnapshot(datasetId, description);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }


  @Override
  @DeleteMapping(value = "/{id}/snapshot/delete/{idSnapshot}")
  public void deleteSnapshot(@PathVariable("id") Long datasetId,
      @PathVariable("idSnapshot") Long idSnapshot) {

    try {
      datasetMetabaseService.removeSnapshot(datasetId, idSnapshot);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.USER_REQUEST_NOTFOUND);
    }
  }

}
