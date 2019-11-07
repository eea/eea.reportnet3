package org.eea.dataset.controller;

import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.security.authorization.ObjectAccessRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class DataSetSchemaControllerImpl.
 */
@RestController
@RequestMapping("/dataschema")
public class DataSetSchemaControllerImpl implements DatasetSchemaController {

  /**
   * The dataschema service.
   */
  @Autowired
  private DatasetSchemaService dataschemaService;

  /**
   * The dataset service.
   */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * The dataset metabase service.
   */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataSetSchemaControllerImpl.class);

  /**
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createDataSchema/{id}")
  public void createDataSchema(@PathVariable("id") final Long datasetId,
      @RequestParam("idDataflow") final Long dataflowId) {
    dataschemaService.createDataSchema(datasetId, dataflowId);
  }

  /**
   * Creates the data schema.
   *
   * @param idSchema the id schema
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/{idSchema}/updateTableSchema/{datasetId}", method = RequestMethod.PUT)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void updateTableSchema(@PathVariable("idSchema") String idSchema,
      @PathVariable("datasetId") Long datasetId, @RequestBody TableSchemaVO tableSchema) {
    try {
      dataschemaService.updateTableSchema(idSchema, tableSchema);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }

  /**
   * Creates the empty data set schema.
   *
   * @param datasetSchemaName the name data set schema
   * @param dataflowId the id data flow
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyDatasetSchema")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN') AND hasRole('DATA_CUSTODIAN')")
  public void createEmptyDatasetSchema(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("datasetSchemaName") final String datasetSchemaName) {

    try {
      dataschemaService.createGroupAndAddUser(
          datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.DESIGN, datasetSchemaName,
              dataschemaService.createEmptyDataSetSchema(dataflowId, datasetSchemaName).toString(),
              dataflowId));
    } catch (EEAException e) {
      LOG.error("Aborted DataSetSchema creation: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Error creating design dataset");
    }
  }

  /**
   * Creates the data schema.
   *
   * @param id the id
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/{id}/createTableSchema/{datasetId}", method = RequestMethod.POST)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void createTableSchema(@PathVariable("id") String id,
      @PathVariable("datasetId") Long datasetId, @RequestBody final TableSchemaVO tableSchema) {
    try {
      dataschemaService.createTableSchema(id, tableSchema, datasetId);
      datasetService.saveTablePropagation(datasetId, tableSchema);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }

  /**
   * Find data schema by id.
   *
   * @param id the id
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id) {
    return dataschemaService.getDataSchemaById(id);
  }

  /**
   * Find data schema by dataflow.
   *
   * @param idFlow the id flow
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand()
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idFlow,'DATAFLOW_PROVIDER') OR secondLevelAuthorize(#idFlow,'DATAFLOW_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow) {
    return dataschemaService.getDataSchemaByIdFlow(idFlow, true);
  }

  /**
   * Find data schema with no rules by dataflow.
   *
   * @param idFlow the id flow
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand()
  @GetMapping(value = "/noRules/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idFlow,'DATAFLOW_PROVIDER') OR secondLevelAuthorize(#idFlow,'DATAFLOW_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaWithNoRulesByDataflow(@PathVariable("id") Long idFlow) {
    return dataschemaService.getDataSchemaByIdFlow(idFlow, false);
  }

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  @Override
  @HystrixCommand()
  @RequestMapping(value = "/{datasetId}/tableschema/{tableSchemaId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String idTableSchema) {
    try {
      if (idTableSchema == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
      }
      if (datasetId == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.DATASET_INCORRECT_ID);
      }
      String schemaId = dataschemaService
          .getDataSchemaByIdFlow(datasetService.getDataFlowIdById(datasetId), false)
          .getIdDataSetSchema();
      if (StringUtils.isBlank(schemaId)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
      }
      dataschemaService.deleteTableSchema(schemaId, idTableSchema);
      datasetService.deleteTableValue(datasetId, idTableSchema);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXECUTION_ERROR, e);
    }
  }

  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  @RequestMapping(value = "/dataset/{datasetId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      String schemaId = dataschemaService
          .getDataSchemaByIdFlow(datasetService.getDataFlowIdById(datasetId), false)
          .getIdDataSetSchema();
      if (schemaId.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
      }
      dataschemaService.deleteDatasetSchema(datasetId, schemaId);
      datasetMetabaseService.deleteDesignDataset(datasetId);
      recordStoreControllerZull.deleteDataset("dataset_" + datasetId);
      dataschemaService.deleteGroup(datasetId, ObjectAccessRoleEnum.DATASCHEMA_CUSTODIAN,
          ObjectAccessRoleEnum.DATASCHEMA_PROVIDER);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXECUTION_ERROR, e);
    }
  }

  /**
   * Creates the data schema.
   *
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @param fieldSchema the field schema
   */
  @Override
  @HystrixCommand
  @PostMapping("/{idTableSchema}/createFieldSchema/{datasetId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void createFieldSchema(@PathVariable("idTableSchema") String idTableSchema,
      @PathVariable("datasetId") Long datasetId, @RequestBody final FieldSchemaVO fieldSchema) {
    try {
      dataschemaService.createFieldSchema(idTableSchema, fieldSchema);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }

  /**
   * Delete field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/{datasetId}/deleteFieldSchema/{fieldSchemaId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void deleteFieldSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldSchemaId") String fieldSchemaId) {

    // Delete the fieldSchema from the dataset
    String datasetSchemaId = datasetService.deleteFieldValues(datasetId, fieldSchemaId);

    try {
      // Delete the fieldSchema from the datasetSchema
      if (!dataschemaService.deleteFieldSchema(datasetSchemaId, fieldSchemaId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            EEAErrorMessage.WRONG_DATASET_SCHEMA);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    }
  }

  /**
   * Update field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   */
  @Override
  @HystrixCommand
  @PutMapping("/{datasetId}/updateFieldSchema")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void updateFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody FieldSchemaVO fieldSchemaVO) {

    try {
      // Update the fieldSchema from the datasetSchema
      String type = dataschemaService
          .updateFieldSchema(datasetService.findDatasetSchemaIdById(datasetId), fieldSchemaVO);

      // If the update operation succeded, scale to the dataset
      if (type != null) {
        datasetService.updateFieldValueType(datasetId, fieldSchemaVO.getId(), type);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    }
  }

}
