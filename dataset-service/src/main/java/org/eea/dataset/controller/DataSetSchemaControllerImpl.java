package org.eea.dataset.controller;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DatasetSnapshotService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.OrderVO;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
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
import io.netty.util.internal.StringUtil;


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
   * The dataset snapshot service.
   */
  @Autowired
  private DatasetSnapshotService datasetSnapshotService;

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
   * The dataflow controller zuul.
   */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /**
   * Creates the empty dataset schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyDatasetSchema")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN') AND hasRole('DATA_CUSTODIAN')")
  public void createEmptyDatasetSchema(@RequestParam("dataflowId") final Long dataflowId,
      @RequestParam("datasetSchemaName") final String datasetSchemaName) {

    try {
      Future<Long> datasetId = datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.DESIGN,
          datasetSchemaName, dataschemaService.createEmptyDataSetSchema(dataflowId).toString(),
          dataflowId, null, null, 0);
      datasetId.get();
    } catch (InterruptedException | ExecutionException | EEAException e) {
      LOG.error("Aborted DataSetSchema creation: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Error creating design dataset");
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
   * Find data schema by datasetId.
   *
   * @param datasetId the id dataset
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/datasetId/{datasetId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_PROVIDER', 'DATACOLLECTION_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaByDatasetId(@PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(true, datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the dataset schema id.
   *
   * @param datasetId the dataset id
   *
   * @return the dataset schema id
   */
  @Override
  @RequestMapping(value = "/getDataSchema/{datasetId}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String getDatasetSchemaId(@PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDatasetSchemaId(datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Find data schema with no rules by dataset id.
   *
   * @param datasetId the dataset id
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "{datasetId}/noRules", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN','DATASCHEMA_PROVIDER')")
  public DataSetSchemaVO findDataSchemaWithNoRulesByDatasetId(
      @PathVariable("datasetId") Long datasetId) {
    try {
      return dataschemaService.getDataSchemaByDatasetId(false, datasetId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  @DeleteMapping(value = "/dataset/{datasetId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  public void deleteDatasetSchema(@PathVariable("datasetId") Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    String schemaId = getDatasetSchemaId(datasetId);
    if (schemaId.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND);
    }

    // Check if the dataflow its on the correct state to allow delete design datasets
    try {
      Long dataflowId = datasetService.getDataFlowIdById(datasetId);
      DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataflowId);

      if (TypeStatusEnum.DESIGN == dataflow.getStatus()) {
        // delete the schema snapshots too
        datasetSnapshotService.deleteAllSchemaSnapshots(datasetId);

        // delete the schema in Mongo
        dataschemaService.deleteDatasetSchema(schemaId);

        // delete the schema to dataset
        rulesControllerZuul.deleteRulesSchema(schemaId);
        // delete the metabase
        datasetMetabaseService.deleteDesignDataset(datasetId);
        // delete the schema in BBDD
        recordStoreControllerZull.deleteDataset("dataset_" + datasetId);

        // delete the group in keycloak
        dataschemaService.deleteGroup(datasetId, ResourceGroupEnum.DATASCHEMA_CUSTODIAN,
            ResourceGroupEnum.DATASCHEMA_PROVIDER);

      } else {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
            EEAErrorMessage.NOT_ENOUGH_PERMISSION);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR, e);
    }


  }

  /**
   * Creates the table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   *
   * @return the table VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PostMapping(value = "/{datasetId}/tableSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  public TableSchemaVO createTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO) {
    try {
      TableSchemaVO response = dataschemaService.createTableSchema(
          dataschemaService.getDatasetSchemaId(datasetId), tableSchemaVO, datasetId);
      datasetService.saveTablePropagation(datasetId, tableSchemaVO);
      return response;
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Update table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/tableSchema")
  public void updateTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody TableSchemaVO tableSchemaVO) {
    try {
      dataschemaService.updateTableSchema(dataschemaService.getDatasetSchemaId(datasetId),
          tableSchemaVO);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID, e);
    }
  }

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @DeleteMapping("/{datasetId}/tableSchema/{tableSchemaId}")
  public void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId) {
    try {
      final String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);
      dataschemaService.deleteTableSchema(datasetSchemaId, tableSchemaId);

      // we delete the rules associate to the table
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, tableSchemaId);

      datasetService.deleteTableValue(datasetId, tableSchemaId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXECUTION_ERROR, e);
    }
  }

  /**
   * Order table schema.
   *
   * @param datasetId the dataset id
   * @param orderVO the order VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/tableSchema/order")
  public void orderTableSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody OrderVO orderVO) {
    try {
      // Update the fieldSchema from the datasetSchema
      if (Boolean.FALSE.equals(
          dataschemaService.orderTableSchema(dataschemaService.getDatasetSchemaId(datasetId),
              orderVO.getId(), orderVO.getPosition()))) {
        throw new EEAException(EEAErrorMessage.EXECUTION_ERROR);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.SCHEMA_NOT_FOUND, e);
    }
  }

  /**
   * Creates the field schema.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   *
   * @return the string
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PostMapping(value = "/{datasetId}/fieldSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  public String createFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody final FieldSchemaVO fieldSchemaVO) {


    if (StringUtil.isNullOrEmpty(fieldSchemaVO.getName())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FIELD_NAME_NULL);
    }
    try {
      String response;
      String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);
      if (StringUtils.isBlank(
          response = dataschemaService.createFieldSchema(datasetSchemaId, fieldSchemaVO))) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID);
      }
      // propagate the new field to the existing records in the dataset value
      datasetService.prepareNewFieldPropagation(datasetId, fieldSchemaVO);
      // with that we create the rule automatic required
      if (Boolean.TRUE.equals(fieldSchemaVO.getRequired())) {

        rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
            fieldSchemaVO.getType(), TypeEntityEnum.FIELD, Boolean.TRUE);
      }
      // and with it we create the others automatic rules like number etc
      rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
          fieldSchemaVO.getType(), TypeEntityEnum.FIELD, Boolean.FALSE);
      return (response);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID,
          e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/fieldSchema")
  public void updateFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody FieldSchemaVO fieldSchemaVO) {
    try {
      final String datasetSchema = dataschemaService.getDatasetSchemaId(datasetId);
      // Update the fieldSchema from the datasetSchema
      TypeData type = dataschemaService.updateFieldSchema(datasetSchema, fieldSchemaVO);
      // If the update operation succeded, scale to the dataset
      if (type != null) {
        // if we changue the type we need to delete all rules
        rulesControllerZuul.deleteRuleByReferenceId(datasetSchema, fieldSchemaVO.getId());

        if (Boolean.TRUE.equals(fieldSchemaVO.getRequired())) {
          rulesControllerZuul.createAutomaticRule(datasetSchema, fieldSchemaVO.getId(), type,
              TypeEntityEnum.FIELD, Boolean.TRUE);
        }

        rulesControllerZuul.createAutomaticRule(datasetSchema, fieldSchemaVO.getId(),
            fieldSchemaVO.getType(), TypeEntityEnum.FIELD, Boolean.FALSE);
        // update metabase value
        datasetService.updateFieldValueType(datasetId, fieldSchemaVO.getId(), type);
      } else {
        if (Boolean.TRUE.equals(fieldSchemaVO.getRequired())) {
          if (!rulesControllerZuul.existsRuleRequired(datasetSchema, fieldSchemaVO.getId())) {
            rulesControllerZuul.createAutomaticRule(datasetSchema, fieldSchemaVO.getId(),
                fieldSchemaVO.getType(), TypeEntityEnum.FIELD, Boolean.TRUE);
          }
        } else {
          rulesControllerZuul.deleteRuleRequired(datasetSchema, fieldSchemaVO.getId());
        }
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND, e);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @DeleteMapping("/{datasetId}/fieldSchema/{fieldSchemaId}")
  public void deleteFieldSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldSchemaId") String fieldSchemaId) {
    try {

      String datasetSchemaId = dataschemaService.getDatasetSchemaId(datasetId);
      // Delete the fieldSchema from the datasetSchema
      if (!dataschemaService.deleteFieldSchema(datasetSchemaId, fieldSchemaId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID);
      }
      // Delete the rules from the fieldSchema
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, fieldSchemaId);
      // Delete the fieldSchema from the dataset
      datasetService.deleteFieldValues(datasetId, fieldSchemaId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.INVALID_OBJECTID,
          e);
    }
  }

  /**
   * Order field schema.
   *
   * @param datasetId the dataset id
   * @param orderVO the order VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/fieldSchema/order")
  public void orderFieldSchema(@PathVariable("datasetId") Long datasetId,
      @RequestBody OrderVO orderVO) {
    try {
      // Update the fieldSchema from the datasetSchema
      if (Boolean.FALSE.equals(
          dataschemaService.orderFieldSchema(dataschemaService.getDatasetSchemaId(datasetId),
              orderVO.getId(), orderVO.getPosition()))) {
        throw new EEAException(EEAErrorMessage.EXECUTION_ERROR);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND,
          e);
    }
  }

  /**
   * Update dataset schema description.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaVO the dataset schema VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_CUSTODIAN')")
  @PutMapping("/{datasetId}/datasetSchema")
  public void updateDatasetSchemaDescription(@PathVariable("datasetId") Long datasetId,
      @RequestBody(required = false) DataSetSchemaVO datasetSchemaVO) {
    try {
      if (!dataschemaService.updateDatasetSchemaDescription(
          dataschemaService.getDatasetSchemaId(datasetId), datasetSchemaVO.getDescription())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.EXECUTION_ERROR);
      }
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.SCHEMA_NOT_FOUND,
          e);
    }
  }


  /**
   * Validate schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the boolean
   */
  @Override
  @GetMapping(value = "{schemaId}/validate", produces = MediaType.APPLICATION_JSON_VALUE)
  public Boolean validateSchema(@PathVariable("schemaId") String datasetSchemaId) {
    return dataschemaService.validateSchema(datasetSchemaId);
  }


  /**
   * Validate schemas.
   *
   * @param dataflowId the dataflow id
   * @return the boolean
   */
  @Override
  @GetMapping(value = "/validate/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Boolean validateSchemas(@PathVariable("dataflowId") Long dataflowId) {
    // Recover the designs datasets of the dataflowId given. And then, for each design dataset
    // executes a validation.
    // At the first wrong design dataset, it stops and returns false. Otherwise it returns true
    DataFlowVO dataflow = dataflowControllerZuul.findById(dataflowId);
    Boolean isValid = false;
    if (dataflow.getDesignDatasets() != null && !dataflow.getDesignDatasets().isEmpty()) {
      isValid = dataflow.getDesignDatasets().parallelStream().noneMatch(
          ds -> Boolean.FALSE.equals(dataschemaService.validateSchema(ds.getDatasetSchema())));
    }
    return isValid;
  }

}
