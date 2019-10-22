package org.eea.dataset.controller;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

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
   * Find dataset name.
   *
   * @param idDataSet the id data set
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetMetabaseVO findDatasetMetabaseById(@PathVariable("id") Long idDataset) {
    return datasetMetabaseService.findDatasetMetabase(idDataset);
  }

  /**
   * Creates the empty data set.
   *
   * @param datasetname the datasetname
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/create")
  public void createEmptyDataSet(
      @RequestParam(value = "datasetName", required = true) final String datasetname,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema,
      @RequestParam(value = "idDataflow", required = false) Long idDataflow) {
    if (StringUtils.isBlank(datasetname)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetMetabaseService.createEmptyDataset(datasetname, idDatasetSchema, idDataflow);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

  }

  @Override
  @PutMapping(value = "/updateDatasetName")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN') AND hasRole('DATA_CUSTODIAN')")
  public void updateDatasetName(@RequestParam(value = "datasetId", required = true) Long datasetId,
      @RequestParam(value = "datasetName", required = true) String datasetName) {
    if (!datasetMetabaseService.updateDatasetName(datasetId, datasetName)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }
}
