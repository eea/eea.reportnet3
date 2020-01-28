package org.eea.dataset.controller;


import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

  /** The design dataset service. */
  @Autowired
  private DesignDatasetService designDatasetService;

  /** The data collection service. */
  @Autowired
  private DataCollectionService dataCollectionService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The dataflow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataSetMetabaseControllerImpl.class);

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
  public List<ReportingDatasetVO> findReportingDataSetIdByDataflowId(Long idDataflow) {
    return reportingDatasetService.getDataSetIdByDataflowId(idDataflow);
  }

  /**
   * Find data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/findReportings/{schemaId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ReportingDatasetVO> getReportingsIdBySchemaId(
      @PathVariable("schemaId") String schemaId) {
    return reportingDatasetService.getDataSetIdBySchemaId(schemaId);
  }

  /**
   * Find dataset name.
   *
   * @param idDataset the id dataset
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
   * @param datasetType the dataset type
   * @param datasetname the datasetname
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/create")
  public void createEmptyDataSet(
      @RequestParam(value = "datasetType", required = true) final TypeDatasetEnum datasetType,
      @RequestParam(value = "datasetName", required = true) final String datasetname,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema,
      @RequestParam(value = "idDataflow", required = false) Long idDataflow) {
    if (StringUtils.isBlank(datasetname)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      RepresentativeVO representative = new RepresentativeVO();
      representative.setDataProviderId(0L);
      datasetMetabaseService.createEmptyDataset(datasetType, datasetname, idDatasetSchema,
          idDataflow, null, Arrays.asList(representative), 0);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_UNKNOW_TYPE);
    }

  }

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   */
  @Override
  @PutMapping(value = "/updateDatasetName")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void updateDatasetName(@RequestParam(value = "datasetId", required = true) Long datasetId,
      @RequestParam(value = "datasetName", required = false) String datasetName) {
    if (!datasetMetabaseService.updateDatasetName(datasetId, datasetName)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }


  /**
   * Find design data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/design/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DesignDatasetVO> findDesignDataSetIdByDataflowId(Long idDataflow) {

    return designDatasetService.getDesignDataSetIdByDataflowId(idDataflow);

  }


  /**
   * Gets the statistics by id.
   *
   * @param datasetId the dataset id
   *
   * @return the statistics by id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}/loadStatistics", produces = MediaType.APPLICATION_JSON_VALUE)
  @Cacheable(value = "cacheStatistics", key = "#datasetId")
  public StatisticsVO getStatisticsById(@PathVariable("id") Long datasetId) {

    StatisticsVO statistics = null;
    try {
      statistics = datasetMetabaseService.getStatistics(datasetId);
    } catch (EEAException | InstantiationException | IllegalAccessException e) {
      LOG_ERROR.error("Error getting statistics. Error message: {}", e.getMessage(), e);
    }

    return statistics;
  }



  /**
   * Gets the global statistics by dataschema id.
   *
   * @param dataschemaId the dataschema id
   * @return the global statistics by dataschema id
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/globalStatistics/{dataschemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public List<StatisticsVO> getGlobalStatisticsByDataschemaId(
      @PathVariable("dataschemaId") String dataschemaId) {

    List<StatisticsVO> statistics = null;

    try {
      statistics = datasetMetabaseService.getGlobalStatistics(dataschemaId);
    } catch (EEAException | InstantiationException | IllegalAccessException e) {
      LOG_ERROR.error("Error getting global statistics. Error message: {}", e.getMessage(), e);
    }

    return statistics;
  }



}
