/*
 *
 */
package org.eea.dataset.controller;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.ReportingDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetStatusMessageVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetPublicVO;
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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/** The Class DataSetMetabaseControllerImpl. */
@RestController
@RequestMapping("/datasetmetabase")
public class DataSetMetabaseControllerImpl implements DatasetMetabaseController {


  /** The Constant REGEX_NAME_SCHEMA: {@value}. */
  private static final String REGEX_NAME_SCHEMA = "[a-zA-Z0-9\\s\\(\\)_-]+";

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
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER')")
  @ApiOperation(value = "Find reporting dataset id by dataflow id", hidden = true)
  public List<ReportingDatasetVO> findReportingDataSetIdByDataflowId(@ApiParam(type = "Long",
      value = "dataflow Id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    return reportingDatasetService.getDataSetIdByDataflowId(dataflowId);
  }


  /**
   * Find data set id by dataflow id.
   *
   * @param schemaId the schema id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/findReportings/{schemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "get reporting list id", hidden = true)
  public List<ReportingDatasetVO> getReportingsIdBySchemaId(
      @ApiParam(type = "String", value = "Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("schemaId") String schemaId) {
    return reportingDatasetService.getDataSetIdBySchemaId(schemaId);
  }

  /**
   * Find dataset name.
   *
   * @param datasetId the id dataset
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  // @PreAuthorize("secondLevelAuthorize(#datasetId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_OBSERVER','DATAFLOW_LEAD_REPORTER')")
  @ApiOperation(value = "find dataset metabase", hidden = true)
  public DataSetMetabaseVO findDatasetMetabaseById(@ApiParam(type = "Long", value = "dataset Id",
      example = "0") @PathVariable("datasetId") Long datasetId) {
    return datasetMetabaseService.findDatasetMetabase(datasetId);
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
  @PostMapping(value = "/private/create")
  @ApiOperation(value = "create empty dataset", hidden = true)
  public void createEmptyDataSet(
      @ApiParam(value = "dataset type", example = "REPORTING") @RequestParam(value = "datasetType",
          required = true) final DatasetTypeEnum datasetType,
      @RequestParam(value = "datasetName", required = true) final String datasetname,
      @ApiParam(type = "String", value = "dataset schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "idDatasetSchema",
              required = false) String idDatasetSchema,
      @ApiParam(type = "Long", value = "dataflow Id",
          example = "0") @RequestParam(value = "idDataflow", required = false) Long idDataflow) {
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
  @ApiOperation(value = "Update dataset name", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated dataset name"),
      @ApiResponse(code = 400, message = "Dataset id is incorrect or not informed")})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE')")
  public void updateDatasetName(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @RequestParam(value = "datasetId", required = true) Long datasetId,
      @ApiParam(type = "String", value = "Dataset Name", example = "dataset1") @RequestParam(
          value = "datasetName", required = false) String datasetName) {

    String nameTrimmed = datasetName.trim();
    filterName(nameTrimmed);
    datasetName = nameTrimmed;

    if (!datasetMetabaseService.updateDatasetName(datasetId, datasetName)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }

  /**
   * Update dataset status and send Message.
   *
   * @param datasetStatusMessageVO the dataset status message VO
   */
  @Override
  @PutMapping(value = "/updateDatasetStatus")
  @ApiOperation(value = "Update dataset Status", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated status"),
      @ApiResponse(code = 500, message = "Error updating status")})
  @PreAuthorize("secondLevelAuthorize(#datasetStatusMessageVO.datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD')")
  public void updateDatasetStatus(@ApiParam(
      value = "dataset Status message object") @RequestBody DatasetStatusMessageVO datasetStatusMessageVO) {
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
  @ApiOperation(value = "Get design dataset id list", hidden = true)
  @GetMapping(value = "/private/design/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DesignDatasetVO> findDesignDataSetIdByDataflowId(
      @ApiParam(type = "Long", value = "dataflow Id", example = "0") Long idDataflow) {

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
  @GetMapping(value = "/{datasetId}/loadStatistics", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "get statistics by dataset", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_CUSTODIAN','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_READ','DATASET_REPORTER_WRITE','DATASET_OBSERVER','DATASET_NATIONAL_COORDINATOR')")
  public StatisticsVO getStatisticsById(@ApiParam(type = "Long", value = "dataset Id",
      example = "0") @PathVariable("datasetId") Long datasetId) {

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
  @GetMapping(value = "/globalStatistics/dataflow/{dataflowId}/dataSchema/{dataschemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "get global statistics", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  public List<StatisticsVO> getGlobalStatisticsByDataschemaId(
      @ApiParam(type = "String", value = "Dataset schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("dataschemaId") String dataschemaId,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("dataflowId") Long dataflowId) {

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
  @ApiOperation(value = "find dataset schema by dataset Id", hidden = true)
  @GetMapping("/private/findDatasetSchemaIdById")
  public String findDatasetSchemaIdById(@ApiParam(type = "Long", value = "Dataset Id",
      example = "0") @RequestParam("datasetId") long datasetId) {
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
  @ApiOperation(value = "Get integrity dataset Id", hidden = true)
  public Long getIntegrityDatasetId(
      @ApiParam(type = "Long", value = "Dataset Id Origin",
          example = "0") @RequestParam("id") Long datasetIdOrigin,
      @ApiParam(type = "String", value = "Dataset origin schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(
              value = "datasetOriginSchemaId") String datasetOriginSchemaId,
      @ApiParam(type = "String", value = "Dataset reference schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(
              value = "datasetReferencedSchemaId") String datasetReferencedSchemaId) {
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
  @ApiOperation(value = "create foreign relation", hidden = true)
  public void createDatasetForeignRelationship(
      @ApiParam(type = "Long", value = "Dataset Id origin",
          example = "0") @RequestParam("datasetOriginId") final long datasetOriginId,
      @ApiParam(type = "Long", value = "Dataset Id referenced",
          example = "0") @RequestParam("datasetReferencedId") final long datasetReferencedId,
      @ApiParam(type = "String", value = "Dataset Origin schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("originDatasetSchemaId") final String originDatasetSchemaId,
      @ApiParam(type = "String", value = "Dataset reference schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referencedDatasetSchemaId") final String referencedDatasetSchemaId) {
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
  @ApiOperation(value = "Update foreign relation", hidden = true)
  public void updateDatasetForeignRelationship(
      @ApiParam(type = "Long", value = "Dataset Id origin",
          example = "0") @RequestParam("datasetOriginId") final long datasetOriginId,
      @ApiParam(type = "Long", value = "Dataset Id reference",
          example = "0") @RequestParam("datasetReferencedId") final long datasetReferencedId,
      @ApiParam(type = "String", value = "Datasaet Origin schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("originDatasetSchemaId") final String originDatasetSchemaId,
      @ApiParam(type = "String", value = "Dataset reference schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referencedDatasetSchemaId") final String referencedDatasetSchemaId) {
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
  @ApiOperation(value = "Get design dataset id by dataset schema Id", hidden = true)
  public Long getDesignDatasetIdByDatasetSchemaId(@ApiParam(type = "String",
      value = "Dataset schema Id",
      example = "5cf0e9b3b793310e9ceca190") @PathVariable("datasetSchemaId") String datasetSchemaId) {
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
  @ApiOperation(value = "Delete foreign relations", hidden = true)
  public void deleteForeignRelationship(
      @ApiParam(type = "Long", value = "Dataset origin Id",
          example = "0") @RequestParam("datasetOriginId") Long datasetOriginId,
      @ApiParam(type = "Long", value = "Dataset referenced Id", example = "0") @RequestParam(
          value = "datasetReferencedId", required = false) Long datasetReferencedId,
      @ApiParam(type = "String", value = "origin dataset schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("originDatasetSchemaId") String originDatasetSchemaId,
      @ApiParam(type = "String", value = "referenced Dataset schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("referencedDatasetSchemaId") String referencedDatasetSchemaId) {

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
  @ApiOperation(value = "get dataset type", hidden = true)
  public DatasetTypeEnum getType(@ApiParam(type = "Long", value = "Dataset Id",
      example = "0") @PathVariable("datasetId") Long datasetId) {
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
  @ApiOperation(value = "get lists of dataset ids by dataflow id and provider Id", hidden = true)
  @GetMapping("/private/getDatasetIdsByDataflowIdAndDataProviderId")
  public List<Long> getDatasetIdsByDataflowIdAndDataProviderId(
      @ApiParam(type = "Long", value = "dataflow Id",
          example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @RequestParam("dataProviderId") Long dataProviderId) {
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
  @ApiOperation(value = "get list of user provider ids by dataflow Id ", hidden = true)
  public List<Long> getUserProviderIdsByDataflowId(@ApiParam(type = "Long", value = "Dataflow Id",
      example = "0") @RequestParam("dataflowId") Long dataflowId) {
    return datasetMetabaseService.getUserProviderIdsByDataflowId(dataflowId);
  }

  /**
   * Gets the last dataset validation for release.
   *
   * @param datasetId the dataset id
   * @return the last dataset validation for release
   */
  @Override
  @GetMapping(value = "/private/getLastDatasetValidationForRelease/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "get last dataset validation for release", hidden = true)
  public Long getLastDatasetValidationForRelease(@ApiParam(type = "Long", value = "Dataset Id",
      example = "0") @PathVariable("id") Long datasetId) {
    return datasetMetabaseService.getLastDatasetValidationForRelease(datasetId);
  }

  /**
   * Find reporting data set public by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/reportingPublic/dataflow/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find reporting dataset public by dataflow Id", hidden = true)
  public List<ReportingDatasetPublicVO> findReportingDataSetPublicByDataflowId(@ApiParam(
      type = "Long", value = "Dataflow Id", example = "0") @PathVariable("id") Long dataflowId) {
    return reportingDatasetService.getDataSetPublicByDataflow(dataflowId);
  }

  /**
   * Find reporting data set public by dataflow id and provider id.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/reportingPublic/dataflow/{id}/provider/{providerId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find reporting dataset public by dataflow Id and provider id",
      hidden = true)
  public List<ReportingDatasetPublicVO> findReportingDataSetPublicByDataflowIdAndProviderId(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("id") Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @PathVariable("providerId") Long providerId) {
    return reportingDatasetService.getDataSetPublicByDataflowAndProviderId(dataflowId, providerId);
  }


  /**
   * Find reporting data set id by dataflow id and provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/dataflow/{id}/dataProvider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find reporting datasets by dataflow Id and provider id", hidden = true)
  public List<ReportingDatasetVO> findReportingDataSetIdByDataflowIdAndProviderId(
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @PathVariable("id") Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @PathVariable("dataProviderId") Long dataProviderId) {
    return reportingDatasetService.getDataSetIdByDataflowIdAndDataProviderId(dataflowId,
        dataProviderId);
  }


  /**
   * Find reporting data set by dataflow ids.
   *
   * @param dataflowIds the dataflow ids
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/reportings/dataflowIds",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find reporting datasets by dataflow Ids list", hidden = true)
  public List<ReportingDatasetVO> findReportingDataSetByDataflowIds(
      @ApiParam(value = "Dataflow Ids list") @RequestParam("dataflowIds") List<Long> dataflowIds) {
    return reportingDatasetService.getReportingsByDataflowIds(dataflowIds);
  }


  /**
   * Filter name.
   *
   * @param nameTrimmed the name trimmed
   */
  private void filterName(String nameTrimmed) {
    if (!Pattern.matches(REGEX_NAME_SCHEMA, nameTrimmed)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_SCHEMA_INVALID_NAME_ERROR);
    }
  }

  /**
   * Gets the datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the datasets summary list
   */
  @GetMapping(value = "/private/datasetsSummary/dataflow/{id}")
  @ApiOperation(value = "Get a summary of the information of all the dataset types of a dataflow",
      hidden = true)
  public List<DatasetsSummaryVO> getDatasetsSummaryList(
      @ApiParam(value = "Dataflow Id", example = "0") @PathVariable("id") Long dataflowId) {
    return datasetMetabaseService.getDatasetsSummaryList(dataflowId);
  }



}
