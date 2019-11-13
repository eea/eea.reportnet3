package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
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
   * Find data schema by datasetId.
   *
   * @param datasetId the dataset id
   * @return the data set schema VO
   */
  @RequestMapping(value = "/datasetId/{datasetId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaByDatasetId(@PathVariable("datasetId") Long datasetId);


  /**
   * Gets the dataset schema id.
   *
   * @param datasetId the dataset id
   * @return the dataset schema id
   */
  @RequestMapping(value = "/getDataSchema/{datasetId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  String getDatasetSchemaId(@PathVariable("datasetId") Long datasetId);

  /**
   * Find data schema with no rules by dataflow.
   *
   * @param idFlow the id flow
   * @return the data set schema VO
   */
  @GetMapping(value = "{datasetId}/noRules", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaWithNoRulesByDatasetId(@PathVariable("datasetId") Long datasetId);

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  @DeleteMapping(value = "/{datasetId}/tableschema/{tableSchemaId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String idTableSchema);

  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   */
  @DeleteMapping(value = "/dataset/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId);

  /**
   * Update table schema.
   *
   * @param idSchema the id schema
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @PutMapping("/{idSchema}/updateTableSchema/{datasetId}")
  void updateTableSchema(@PathVariable("idSchema") String idSchema,
      @PathVariable("datasetId") Long datasetId, @RequestBody TableSchemaVO tableSchema);

  /**
   * Creates the table schema.
   *
   * @param id the id
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @PostMapping("/{id}/createTableSchema/{datasetId}")
  void createTableSchema(@PathVariable("id") String id, @PathVariable("datasetId") Long datasetId,
      @RequestBody final TableSchemaVO tableSchema);

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   * @param fieldSchemaVO the field schema VO
   */
  @PostMapping("/{datasetSchemaId}/createFieldSchema/{datasetId}/{tableSchemaId}")
  void createFieldSchema(@PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaVO,
      @RequestBody final FieldSchemaVO fieldSchemaVO);

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  @DeleteMapping("/{datasetSchemaId}/deleteFieldSchema/{datasetId}/{fieldSchemaId}")
  void deleteFieldSchema(@PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldSchemaId") String fieldSchemaId);

  /**
   * Update field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   */
  @PutMapping("/{datasetSchemaId}/updateFieldSchema/{datasetId}")
  void updateFieldSchema(@PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("datasetId") Long datasetId, @RequestBody FieldSchemaVO fieldSchemaVO);

  /**
   * Order table schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   * @param newPosition the new position
   * @param tableSchema the table schema
   */
  @PutMapping("/{idDatasetSchema}/orderTable/{position}/{datasetId}")
  void orderTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("idDatasetSchema") String idDatasetSchema,
      @PathVariable("position") int newPosition, @RequestBody TableSchemaVO tableSchema);

  /**
   * Order field schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   * @param newPosition the new position
   * @param fieldSchema the field schema
   */
  @PutMapping("/{idDatasetSchema}/orderTable/{position}/{datasetId}")
  void orderFieldSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("idDatasetSchema") String idDatasetSchema,
      @PathVariable("position") int newPosition, @RequestBody FieldSchemaVO fieldSchema);
}
