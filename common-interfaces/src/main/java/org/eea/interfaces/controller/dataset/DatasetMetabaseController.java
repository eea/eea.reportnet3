package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
   * Find data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> findReportingDataSetIdByDataflowId(
      @PathVariable("id") final Long idDataflow);


  /**
   * Creates the empty data set.
   *
   * @param datasetType the dataset type
   * @param datasetName the dataset name
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   */
  @PostMapping(value = "/create")
  void createEmptyDataSet(
      @RequestParam(value = "datasetType", required = true) final DatasetTypeEnum datasetType,
      @RequestParam(value = "datasetName", required = true) String datasetName,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema,
      @RequestParam(value = "idDataflow", required = false) Long idDataflow);

  /**
   * Find dataset name.
   *
   * @param idDataset the id dataset
   * @return the string
   */
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetMetabaseVO findDatasetMetabaseById(@PathVariable("id") Long idDataset);

  /**
   * Find design data set id by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/design/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DesignDatasetVO> findDesignDataSetIdByDataflowId(@PathVariable("id") final Long idDataflow);


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
   * Gets the statistics by id.
   *
   * @param datasetId the dataset id
   * @return the statistics by id
   */

  @GetMapping(value = "/{id}/loadStatistics", produces = MediaType.APPLICATION_JSON_VALUE)
  StatisticsVO getStatisticsById(@PathVariable("id") Long datasetId);



  /**
   * Gets the global statistics by dataschema id.
   *
   * @param dataschemaId the dataschema id
   * @return the global statistics by dataschema id
   */
  @GetMapping(value = "/globalStatistics/{dataschemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<StatisticsVO> getGlobalStatisticsByDataschemaId(
      @PathVariable("dataschemaId") String dataschemaId);


  /**
   * Gets the reportings id by schema id.
   *
   * @param schemaId the schema id
   * @return the reportings id by schema id
   */
  @GetMapping(value = "/findReportings/{schemaId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReportingDatasetVO> getReportingsIdBySchemaId(@PathVariable("schemaId") String schemaId);


}
