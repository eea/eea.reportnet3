package org.eea.recordstore.controller;

import java.util.List;
import org.eea.interfaces.controller.recordstore.RecordStoreController;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.recordstore.exception.RecordStoreAccessException;
import org.eea.recordstore.service.RecordStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class RecordStoreControllerImpl.
 */
@RestController
@RequestMapping("/recordstore")
public class RecordStoreControllerImpl implements RecordStoreController {

  /**
   * The record store service.
   */
  @Autowired
  private RecordStoreService recordStoreService;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RecordStoreControllerImpl.class);

  /**
   * Reste data set data base.
   */
  @Override
  @RequestMapping(value = "/reset", method = RequestMethod.POST)
  public void resteDataSetDataBase() {
    try {
      recordStoreService.resetDatasetDatabase();
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
    }
  }


  /**
   * Creates the empty dataset.
   *
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  @RequestMapping(value = "/dataset/create/{datasetName}", method = RequestMethod.POST)
  public void createEmptyDataset(@PathVariable("datasetName") final String datasetName,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema) {
    // TODO neeed to create standar
    try {
      recordStoreService.createEmptyDataSet(datasetName, idDatasetSchema);
      LOG.info("Dataset with name {} created", datasetName);
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
      // TODO Error control
    }
  }

  /**
   * Gets the connection to dataset.
   *
   * @param datasetName the dataset name
   *
   * @return the connection to dataset
   */
  @Override
  @RequestMapping(value = "/connection/{datasetName}", method = RequestMethod.GET)
  public ConnectionDataVO getConnectionToDataset(
      @PathVariable("datasetName") final String datasetName) {
    ConnectionDataVO vo = null;
    try {
      vo = recordStoreService.getConnectionDataForDataset(datasetName);
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
    }
    return vo;
  }

  /**
   * Gets the connection to dataset.
   *
   * @return the connection to dataset
   */
  @Override
  @RequestMapping(value = "/connections", method = RequestMethod.GET)
  public List<ConnectionDataVO> getDataSetConnections() {
    List<ConnectionDataVO> vo = null;
    try {
      vo = recordStoreService.getConnectionDataForDataset();
    } catch (final RecordStoreAccessException e) {
      LOG_ERROR.error(e.getMessage(), e);
    }
    return vo;
  }
}
