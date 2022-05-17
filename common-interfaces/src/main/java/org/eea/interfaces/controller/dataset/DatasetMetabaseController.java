/*
 *
 */
package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DatasetStatusMessageVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.DatasetRunningStatusEnum;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DatasetMetabaseController.
 */
public interface DatasetMetabaseController {

  /**
   * The Interface DataSetMetabaseControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "datasetmetabase", path = "/datasetmetabase")
  interface DataSetMetabaseControllerZuul extends DatasetMetabaseController {

  }

  /**
   * Find reporting data set id by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> findReportingDataSetIdByDataflowId(
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Creates the empty data set.
   *
   * @param datasetType the dataset type
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   */
  @PostMapping(value = "/private/create")
  void createEmptyDataSet(
      @RequestParam(value = "datasetType", required = true) DatasetTypeEnum datasetType,
      @RequestParam(value = "datasetName", required = true) String datasetName,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema,
      @RequestParam(value = "idDataflow", required = false) Long idDataflow);

  /**
   * Find dataset name.
   *
   * @param datasetId the id dataset
   * @return the string
   */
  @GetMapping(value = "/private/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetMetabaseVO findDatasetMetabaseById(@PathVariable("datasetId") Long datasetId);


  /**
   * Find external dataset metabase by id.
   *
   * @param datasetId the dataset id
   * @return the data set metabase VO
   */
  @GetMapping(value = "/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetMetabaseVO findExternalDatasetMetabaseById(@PathVariable("datasetId") Long datasetId);

  /**
   * Find design data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/private/design/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DesignDatasetVO> findDesignDataSetIdByDataflowId(@PathVariable("id") Long idDataflow);

  /**
   * Update dataset name.
   *
   * @param datasetId the dataset id
   * @param datasetName the dataset name
   */
  @PutMapping(value = "/updateDatasetName")
  void updateDatasetName(@RequestParam(value = "datasetId", required = true) Long datasetId,
      @RequestParam(value = "datasetName", required = false) String datasetName);

  /**
   * Update dataset status and send message.
   *
   * @param statusMessageVO the status message VO
   */
  @PutMapping(value = "/updateDatasetStatus")
  void updateDatasetStatus(@RequestBody DatasetStatusMessageVO statusMessageVO);

  /**
   * Gets the statistics by id.
   *
   * @param datasetId the dataset id
   * @return the statistics by id
   */

  @GetMapping(value = "/{datasetId}/loadStatistics", produces = MediaType.APPLICATION_JSON_VALUE)
  StatisticsVO getStatisticsById(@PathVariable("datasetId") Long datasetId);

  /**
   * Gets the global statistics by dataschema id.
   *
   * @param dataschemaId the dataschema id
   * @param dataflowId the dataflow id
   * @return the global statistics by dataschema id
   */
  @GetMapping(value = "/globalStatistics/dataflow/{dataflowId}/dataSchema/{dataschemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<StatisticsVO> getGlobalStatisticsByDataschemaId(
      @PathVariable("dataschemaId") String dataschemaId,
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Gets the reportings id by schema id.
   *
   * @param schemaId the schema id
   * @return the reportings id by schema id
   */
  @GetMapping(value = "/private/findReportings/{schemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> getReportingsIdBySchemaId(@PathVariable("schemaId") String schemaId);

  /**
   * Find dataset schema id by id.
   *
   * @param datasetId the dataset id
   * @return the string
   */
  @GetMapping("/private/findDatasetSchemaIdById")
  String findDatasetSchemaIdById(@RequestParam("datasetId") long datasetId);

  /**
   * Gets the integrity dataset id.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetOriginSchemaId the dataset origin schema id
   * @param datasetReferencedSchemaId the dataset referenced schema id
   * @return the integrity dataset id
   */
  @GetMapping("/private/getIntegrityDatasetId")
  Long getIntegrityDatasetId(@RequestParam("id") Long datasetIdOrigin,
      @RequestParam(value = "datasetOriginSchemaId") String datasetOriginSchemaId,
      @RequestParam(value = "datasetReferencedSchemaId") String datasetReferencedSchemaId);

  /**
   * Creates the dataset foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @PostMapping("/private/createForeignRelationship")
  void createDatasetForeignRelationship(@RequestParam("datasetOriginId") long datasetOriginId,
      @RequestParam("datasetReferencedId") long datasetReferencedId,
      @RequestParam("originDatasetSchemaId") String originDatasetSchemaId,
      @RequestParam("referencedDatasetSchemaId") String referencedDatasetSchemaId);

  /**
   * Update dataset foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @PutMapping("/private/updateForeignRelationship")
  void updateDatasetForeignRelationship(@RequestParam("datasetOriginId") long datasetOriginId,
      @RequestParam("datasetReferencedId") long datasetReferencedId,
      @RequestParam("originDatasetSchemaId") String originDatasetSchemaId,
      @RequestParam("referencedDatasetSchemaId") String referencedDatasetSchemaId);

  /**
   * Gets the dataset id by dataset schema id and data provider id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the dataset id by dataset schema id and data provider id
   */
  @GetMapping("/private/getDatasetId/datasetSchema/{datasetSchemaId}")
  Long getDesignDatasetIdByDatasetSchemaId(@PathVariable("datasetSchemaId") String datasetSchemaId);

  /**
   * Delete foreign relationship.
   *
   * @param datasetOriginId the dataset origin id
   * @param datasetReferencedId the dataset referenced id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param referencedDatasetSchemaId the referenced dataset schema id
   */
  @DeleteMapping("/private/deleteForeignRelationship")
  void deleteForeignRelationship(@RequestParam("datasetOriginId") Long datasetOriginId,
      @RequestParam(value = "datasetReferencedId", required = false) Long datasetReferencedId,
      @RequestParam("originDatasetSchemaId") String originDatasetSchemaId,
      @RequestParam("referencedDatasetSchemaId") String referencedDatasetSchemaId);

  /**
   * Gets the type.
   *
   * @param datasetId the dataset id
   * @return the type
   */
  @GetMapping("/private/getType/{datasetId}")
  DatasetTypeEnum getType(@PathVariable("datasetId") Long datasetId);

  /**
   * Gets the dataset ids by dataflow id and data provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the dataset ids by dataflow id and data provider id
   */
  @GetMapping("/private/getDatasetIdsByDataflowIdAndDataProviderId")
  List<Long> getDatasetIdsByDataflowIdAndDataProviderId(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("dataProviderId") Long dataProviderId);

  /**
   * Gets the user provider ids by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the user provider ids by dataflow id
   */
  @GetMapping("/private/getUserProviderIdsByDataflowId")
  List<Long> getUserProviderIdsByDataflowId(@RequestParam("dataflowId") Long dataflowId);

  /**
   * Find reporting data set public by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/private/reportingPublic/dataflow/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetPublicVO> findReportingDataSetPublicByDataflowId(
      @PathVariable("id") Long idDataflow);

  /**
   * Find reporting data set public by dataflow id and provider id.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the list
   */
  @GetMapping(value = "/private/reportingPublic/dataflow/{id}/provider/{providerId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetPublicVO> findReportingDataSetPublicByDataflowIdAndProviderId(
      @PathVariable("id") Long dataflowId, @PathVariable("providerId") Long providerId);

  /**
   * Find reporting data set id by dataflow id and provider id.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @return the list
   */
  @GetMapping(value = "/private/dataflow/{id}/dataProvider/{dataProviderId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> findReportingDataSetIdByDataflowIdAndProviderId(
      @PathVariable("id") Long dataflowId, @PathVariable("dataProviderId") Long dataProviderId);

  /**
   * Find reporting data set by dataflow ids.
   *
   * @param dataflowIds the dataflow ids
   * @return the list
   */
  @GetMapping(value = "/private/reportings/dataflowIds",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> findReportingDataSetByDataflowIds(
      @RequestParam("dataflowIds") List<Long> dataflowIds);

  /**
   * Find data set by dataflow ids.
   *
   * @param dataflowIds the dataflow ids
   * @return the list
   */
  @GetMapping(value = "/private/datasets/dataflowIds", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataSetMetabaseVO> findDataSetByDataflowIds(
      @RequestParam("dataflowIds") List<Long> dataflowIds);

  /**
   * Gets the datasets summary list.
   *
   * @param dataflowId the dataflow id
   * @return the datasets summary list
   */
  @GetMapping(value = "/private/datasetsSummary/dataflow/{id}")
  List<DatasetsSummaryVO> getDatasetsSummaryList(@PathVariable("id") Long dataflowId);

  /**
   * Update dataset running status.
   *
   * @param datasetId the dataset id
   * @param datasetRunningStatus the dataset running status
   */
  @PutMapping(value = "/private/updateDatasetRunningStatus/{id}")
  void updateDatasetRunningStatus(@PathVariable("id") Long datasetId,
      @RequestParam("datasetRunningStatus") DatasetRunningStatusEnum datasetRunningStatus);

  /**
   * Find reporting data set by provider ids.
   *
   * @param providerIds the provider ids
   * @return the list
   */
  @GetMapping(value = "/private/providerIds", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataSetMetabaseVO> findReportingDataSetByProviderIds(
      @RequestParam("providerIds") List<Long> providerIds);


}
