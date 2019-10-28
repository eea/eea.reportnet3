package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DatasetSchemaController.
 */
public interface DatasetSchemaController {

  /**
   * The Interface DataSetSchemaControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "dataschema", path = "/dataschema")
  interface DataSetSchemaControllerZuul extends DatasetSchemaController {

  }

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  @PostMapping(value = "/createDataSchema/{id}")
  void createDataSchema(@PathVariable("id") final Long datasetId,
      @RequestParam("idDataflow") final Long dataflowId);

  /**
   * Creates the empty data schema.
   *
   * @param nameDataSetSchema the name data set schema
   * @param idDataFlow the id data flow
   */
  @PostMapping(value = "/createEmptyDatasetSchema")
  void createEmptyDatasetSchema(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("datasetSchemaName") final String datasetSchemaName);

  /**
   * Find data schema by id.
   *
   * @param id the id
   * @return the data set schema VO
   */
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id);

  /**
   * Find data schema by dataflow.
   *
   * @param idFlow the id flow
   * @return the data set schema VO
   */
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow);

  /**
   * Find data schema with no rules by dataflow.
   *
   * @param idFlow the id flow
   * @return the data set schema VO
   */
  @GetMapping(value = "/noRules/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaWithNoRulesByDataflow(@PathVariable("id") Long idFlow);

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  @RequestMapping(value = "/{datasetId}/tableschema/{tableSchemaId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String idTableSchema);


  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   * @param schemaId the schema id
   */
  @RequestMapping(value = "/dataset/{datasetId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId);



  /**
   * Update table schema.
   *
   * @param idSchema the id schema
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @RequestMapping(value = "/{idSchema}/updateTableSchema/{datasetId}", method = RequestMethod.PUT)
  void updateTableSchema(@PathVariable("idSchema") String idSchema,
      @PathVariable("datasetId") Long datasetId, @RequestBody TableSchemaVO tableSchema);

  /**
   * Creates the table schema.
   *
   * @param id the id
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @RequestMapping(value = "/{id}/createTableSchema/{datasetId}", method = RequestMethod.POST)
  void createTableSchema(@PathVariable("id") String id, @PathVariable("datasetId") Long datasetId,
      @RequestBody final TableSchemaVO tableSchema);



}
