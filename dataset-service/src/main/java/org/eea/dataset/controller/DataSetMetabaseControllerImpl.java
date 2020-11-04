/*
 *
 */
package org.eea.dataset.controller;

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetStatusMessageVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/** The Class DataSetMetabaseControllerImpl. */
@RestController
@RequestMapping("/datasetmetabase")
public class DataSetMetabaseControllerImpl implements DatasetMetabaseController {

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The reporting dataset service. */
  @Autowired
  private ReportingDatasetService reportingDatasetService;

  /** The design dataset service. */
  @Autowired
  private DesignDatasetService designDatasetService;


  /** The Constant LOG_ERROR. */
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
   * @param schemaId the schema id
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
      @RequestParam(value = "datasetType", required = true) final DatasetTypeEnum datasetType,
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  public void updateDatasetName(@RequestParam(value = "datasetId", required = true) Long datasetId,
      @RequestParam(value = "datasetName", required = false) String datasetName) {
    if (!datasetMetabaseService.updateDatasetName(datasetId, datasetName)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }

  /**
   * Update dataset status and send Message
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   */
  @Override
  @PutMapping(value = "/updateDatasetStatus")
  @PreAuthorize("secondLevelAuthorize(#datasetStatusMessageVO.datasetId,'DATASET_CUSTODIAN')")
  public void updateDatasetStatus(@RequestBody DatasetStatusMessageVO datasetStatusMessageVO) {
    try {
      datasetMetabaseService.updateDatasetStatus(datasetStatusMessageVO);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')  OR secondLevelAuthorize(#idDataflow,'DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ')")
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

  /**
   * Find dataset schema id by id.
   *
   * @param datasetId the dataset id
   * @return the string
   */
  @Override
  @CheckForNull
  @GetMapping("/private/findDatasetSchemaIdById")
  public String findDatasetSchemaIdById(@RequestParam("datasetId") long datasetId) {
    return datasetMetabaseService.findDatasetSchemaIdById(datasetId);
  }


  /**
   * Gets the integrity dataset id.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetOriginSchemaId the dataset origin schema id
   * @param datasetReferencedSchemaId the dataset referenced schema id
   * @return the integrity dataset id
   */
  @Override
  @GetMapping("/private/getIntegrityDatasetId")
  public Long getIntegrityDatasetId(@RequestParam("id") Long datasetIdOrigin,
      @RequestParam(value = "datasetOriginSchemaId") String datasetOriginSchemaId,
      @RequestParam(value = "datasetReferencedSchemaId") String datasetReferencedSchemaId) {
    return datasetMetabaseService.getIntegrityDatasetId(datasetIdOrigin, datasetOriginSchemaId,
        datasetReferencedSchemaId);
  }

  /**
   * Creates the dataset foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @Override
  @PostMapping("/private/createForeignRelationship")
  public void createDatasetForeignRelationship(
      @RequestParam("datasetOriginId") final long datasetOriginId,
      @RequestParam("datasetReferencedId") final long datasetReferencedId,
      @RequestParam("originDatasetSchemaId") final String originDatasetSchemaId,
      @RequestParam("referencedDatasetSchemaId") final String referencedDatasetSchemaId) {
    datasetMetabaseService.createForeignRelationship(datasetOriginId, datasetReferencedId,
        originDatasetSchemaId, referencedDatasetSchemaId);

  }

  /**
   * Update dataset foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @Override
  @PutMapping("/private/updateForeignRelationship")
  public void updateDatasetForeignRelationship(
      @RequestParam("datasetOriginId") final long datasetOriginId,
      @RequestParam("datasetReferencedId") final long datasetReferencedId,
      @RequestParam("originDatasetSchemaId") final String originDatasetSchemaId,
      @RequestParam("referencedDatasetSchemaId") final String referencedDatasetSchemaId) {
    datasetMetabaseService.updateForeignRelationship(datasetOriginId, datasetReferencedId,
        originDatasetSchemaId, referencedDatasetSchemaId);
  }

  /**
   * Gets the dataset id by dataset schema id and data provider id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the dataset id by dataset schema id and data provider id
   */
  @Override
  @GetMapping("/private/getDatasetId/datasetSchema/{datasetSchemaId}")
  public Long getDesignDatasetIdByDatasetSchemaId(
      @PathVariable("datasetSchemaId") String datasetSchemaId) {
    return datasetMetabaseService.getDatasetIdByDatasetSchemaIdAndDataProviderId(datasetSchemaId,
        null);
  }


  /**
   * Delete foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @Override
  @DeleteMapping("/private/deleteForeignRelationship")
  public void deleteForeignRelationship(@RequestParam("datasetOriginId") Long datasetOriginId,
      @RequestParam(value = "datasetReferencedId", required = false) Long datasetReferencedId,
      @RequestParam("originDatasetSchemaId") String originDatasetSchemaId,
      @RequestParam("referencedDatasetSchemaId") String referencedDatasetSchemaId) {

    if (null == datasetReferencedId || datasetReferencedId.equals(datasetOriginId)) {
      datasetReferencedId =
          getIntegrityDatasetId(datasetOriginId, originDatasetSchemaId, referencedDatasetSchemaId);
    }
    datasetMetabaseService.deleteForeignRelation(datasetOriginId, datasetReferencedId,
        originDatasetSchemaId, referencedDatasetSchemaId);
  }

  /**
   * Gets the type.
   *
   * @param datasetId the dataset id
   * @return the type
   */
  @Override
  @GetMapping("/private/getType/{datasetId}")
  public DatasetTypeEnum getType(@PathVariable("datasetId") Long datasetId) {
    return datasetMetabaseService.getDatasetType(datasetId);
  }

  /**
   * Gets the dataset ids by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the dataset ids by dataflow id and data provider id
   */
  @Override
  @GetMapping("/private/getDatasetIdsByDataflowIdAndDataProviderId")
  public List<Long> getDatasetIdsByDataflowIdAndDataProviderId(
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam("dataProviderId") Long dataProviderId) {
    return datasetMetabaseService.getDatasetIdsByDataflowIdAndDataProviderId(dataflowId,
        dataProviderId);
  }

  /**
   * Gets the user provider ids by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the user provider ids by dataflow id
   */
  @Override
  @GetMapping("/private/getUserProviderIdsByDataflowId")
  public List<Long> getUserProviderIdsByDataflowId(@RequestParam("dataflowId") Long dataflowId) {
    return datasetMetabaseService.getUserProviderIdsByDataflowId(dataflowId);
  }

  @Override
  @GetMapping(value = "/private/lastDatasetValidationForReleasingById/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Long lastDatasetValidationForReleasingById(@PathVariable("id") Long datasetId) {
    return datasetMetabaseService.lastDatasetValidationForReleasingById(datasetId);
  }
}
