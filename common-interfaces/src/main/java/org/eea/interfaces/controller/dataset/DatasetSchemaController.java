package org.eea.interfaces.controller.dataset;

import java.util.List;
import org.eea.interfaces.vo.dataset.OrderVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
   * Find data schema by dataset id.
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
   * Find data schema with no rules by dataset id.
   *
   * @param datasetId the dataset id
   * @return the data set schema VO
   */
  @GetMapping(value = "{datasetId}/noRules", produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaWithNoRulesByDatasetId(@PathVariable("datasetId") Long datasetId);


  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   * @param forceDelete the force delete
   */
  @DeleteMapping(value = "/dataset/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "forceDelete", required = false) Boolean forceDelete);

  /**
   * Update dataset schema description.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaVO the dataset schema VO
   */
  @PutMapping("/{datasetId}/datasetSchema")
  void updateDatasetSchemaDescription(@PathVariable("datasetId") Long datasetId,
      @RequestBody(required = false) DataSetSchemaVO datasetSchemaVO);

  /**
   * Creates the table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   * @return the table VO
   */
  @PostMapping(value = "/{datasetId}/tableSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  TableSchemaVO createTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO);

  /**
   * Update table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   */
  @PutMapping("/{datasetId}/tableSchema")
  void updateTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO);

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @DeleteMapping("/{datasetId}/tableSchema/{tableSchemaId}")
  void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId);

  /**
   * Order table schema.
   *
   * @param datasetId the dataset id
   * @param orderVO the order VO
   */
  @PutMapping("/{datasetId}/tableSchema/order")
  void orderTableSchema(@PathVariable("datasetId") Long datasetId, @RequestBody OrderVO orderVO);

  /**
   * Creates the field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   * @return the string
   */
  @PostMapping("/{datasetId}/fieldSchema")
  String createFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody FieldSchemaVO fieldSchemaVO);

  /**
   * Update field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   */
  @PutMapping("/{datasetId}/fieldSchema")
  void updateFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody FieldSchemaVO fieldSchemaVO);

  /**
   * Delete field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  @DeleteMapping("/{datasetId}/fieldSchema/{fieldSchemaId}")
  void deleteFieldSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldSchemaId") String fieldSchemaId);

  /**
   * Order field schema.
   *
   * @param datasetId the dataset id
   * @param orderVO the order VO
   */
  @PutMapping("/{datasetId}/fieldSchema/order")
  void orderFieldSchema(@PathVariable("datasetId") Long datasetId, @RequestBody OrderVO orderVO);


  /**
   * Validate schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the boolean
   */
  @GetMapping(value = "{schemaId}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
  Boolean validateSchema(@PathVariable("schemaId") String datasetSchemaId);


  /**
   * Validate schemas.
   *
   * @param dataflowId the dataflow id
   * @return the boolean
   */
  @GetMapping(value = "/validate/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  Boolean validateSchemas(@PathVariable("dataflowId") Long dataflowId);


  /**
   * Find data schemas by id dataflow.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  @GetMapping(value = "/getSchemas/dataflow/{idDataflow}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataSetSchemaVO> findDataSchemasByIdDataflow(@PathVariable("idDataflow") Long idDataflow);


  /**
   * Gets the unique constraints.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the unique constraints
   */
  @GetMapping(value = "{schemaId}/getUniqueConstraints",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<UniqueConstraintVO> getUniqueConstraints(@PathVariable("schemaId") String datasetSchemaId);


  /**
   * Creates the unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  @PostMapping(value = "/createUniqueConstraint")
  void createUniqueConstraint(@RequestBody UniqueConstraintVO uniqueConstraint);

  /**
   * Delete unique constraint.
   *
   * @param uniqueConstraintId the unique constraint id
   */
  @DeleteMapping(value = "/deleteUniqueConstraint/{uniqueConstraintId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteUniqueConstraint(@PathVariable("uniqueConstraintId") String uniqueConstraintId);

  /**
   * Update unique constraint.
   *
   * @param uniqueConstraint the unique constraint
   */
  @PutMapping(value = "/updateUniqueConstraint")
  void updateUniqueConstraint(@RequestBody UniqueConstraintVO uniqueConstraint);

  /**
   * Gets the unique constraint.
   *
   * @param uniqueId the unique id
   * @return the unique constraint
   */
  @GetMapping(value = "/private/getUniqueConstraint/{uniqueId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  UniqueConstraintVO getUniqueConstraint(@PathVariable("uniqueId") String uniqueId);

}
