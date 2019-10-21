package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DatasetSchemaController.
 */
public interface DatasetSchemaController {


  /**
   * The interface Data set controller zuul.
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
  @RequestMapping(value = "/createDataSchema/{id}", method = RequestMethod.POST)
  void createDataSchema(@PathVariable("id") final Long datasetId,
      @RequestParam("idDataflow") final Long dataflowId);

  /**
   * Find data schema by id.
   *
   * @param id the dataschema id
   *
   * @return the data set schema VO
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id);

  /**
   * Find data schema by dataflow.
   *
   * @param idFlow the id flow
   *
   * @return the data set schema VO
   */
  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow);

  /**
   * Find data schema with no rules by dataflow.
   *
   * @param idFlow the id flow
   *
   * @return the data set schema vo
   */
  @RequestMapping(value = "/noRules/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
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
  @RequestMapping(value = "/{datasetId}/datasetschema/{schemaId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("schemaId") String schemaId);
}
