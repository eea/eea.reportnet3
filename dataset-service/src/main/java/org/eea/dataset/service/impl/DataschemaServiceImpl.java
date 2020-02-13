package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Dataschema service.
 */
@Service("dataschemaService")
public class DataschemaServiceImpl implements DatasetSchemaService {

  /**
   * The schemas repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;
  /**
   * The data set metabase table collection.
   */
  @Autowired
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The data flow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /**
   * The dataschema mapper.
   */
  @Autowired
  private DataSchemaMapper dataSchemaMapper;

  /** Mapper to map dataset schemas with no rules. */
  @Autowired
  private NoRulesDataSchemaMapper noRulesDataSchemaMapper;

  /** The field schema no rules mapper. */
  @Autowired
  private FieldSchemaNoRulesMapper fieldSchemaNoRulesMapper;

  /** The table schema mapper. */
  @Autowired
  private TableSchemaMapper tableSchemaMapper;

  /** The record store controller zull. */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;


  /** The validation commands. */
  @Autowired
  private List<ValidationSchemaCommand> validationCommands;


  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataschemaServiceImpl.class);


  /**
   * The Constant GENERAL_WARNING.
   */
  private static final String GENERAL_WARNING = "WARNING";

  /**
   * The Constant VALIDATION_WARNING.
   */
  private static final String VALIDATION_WARNING = "WARNING!,PROBABLY THIS IS NOT CORRECT";

  /**
   * The Constant GENERAL_ERROR.
   */
  private static final String GENERAL_ERROR = "ERROR";

  /**
   * The Constant INTEGER_ERROR.
   */
  private static final String INTEGER_ERROR = "ERROR!, THIS IS NOT A NUMBER";

  /**
   * The Constant BOOLEAN_ERROR.
   */
  private static final String BOOLEAN_ERROR = "ERROR!, THIS IS NOT A TRUE/FALSE VALUE";

  /**
   * The Constant COORDINATE_LAT_ERROR.
   */
  private static final String COORDINATE_LAT_ERROR = "ERROR!, THIS IS NOT A COORDINATE LAT";

  /**
   * The Constant COORDINATE_LONG_ERROR.
   */
  private static final String COORDINATE_LONG_ERROR = "ERROR!, THIS IS NOT A COORDINATE LONG";

  /**
   * The Constant DATE_ERROR.
   */
  private static final String DATE_ERROR = "ERROR!, THIS IS NOT A DATE";

  /**
   * The Constant NULL.
   */
  private static final String NULL = "id == null";

  /**
   * The data set metabase repository.
   */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /**
   * Creates the empty data set schema.
   *
   * @param dataflowId the dataflow id
   * @return the object id
   * @throws EEAException the EEA exception
   */
  @Override
  public ObjectId createEmptyDataSetSchema(Long dataflowId) throws EEAException {

    if (dataFlowControllerZuul.findById(dataflowId) == null) {
      throw new EEAException("DataFlow with id " + dataflowId + " not found");
    }

    DataSetSchema dataSetSchema = new DataSetSchema();
    ObjectId idDataSetSchema = new ObjectId();


    dataSetSchema.setIdDataFlow(dataflowId);
    dataSetSchema.setIdDataSetSchema(idDataSetSchema);
    dataSetSchema.setTableSchemas(new ArrayList<TableSchema>());

    schemasRepository.save(dataSetSchema);

    RulesSchema rulesSchema = new RulesSchema();

    rulesSchema.setRulesSchemaId(idDataSetSchema);
    rulesSchema.setRulesSchemaId(new ObjectId());

    return idDataSetSchema;
  }

  /**
   * Creates the group and add user.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void createGroupAndAddUser(Long datasetId) {

    // Create group Dataschema-X-DATA_CUSTODIAN
    resourceManagementControllerZull.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.DATA_CUSTODIAN));

    // Create group Dataschema-X-DATA_PROVIDER
    resourceManagementControllerZull.createResource(
        createGroup(datasetId, ResourceTypeEnum.DATA_SCHEMA, SecurityRoleEnum.DATA_PROVIDER));

    // Add user to new group Dataschema-X-DATA_CUSTODIAN
    userManagementControllerZull.addUserToResource(datasetId,
        ResourceGroupEnum.DATASCHEMA_CUSTODIAN);
  }

  /**
   * Delete group and remove user.
   *
   * @param datasetId the dataset id
   * @param roles the roles
   */
  @Override
  public void deleteGroup(Long datasetId, ResourceGroupEnum... roles) {
    List<String> resources = new ArrayList<>();
    // Remove groups from list
    Arrays.asList(roles).stream().forEach(role -> resources.add(role.getGroupName(datasetId)));
    resourceManagementControllerZull.deleteResourceByName(resources);
  }


  /**
   * Creates the group.
   *
   * @param datasetId the dataset id
   * @param type the type
   * @param role the role
   * @return the resource info VO
   */
  private ResourceInfoVO createGroup(Long datasetId, ResourceTypeEnum type, SecurityRoleEnum role) {

    ResourceInfoVO resourceInfoVO = new ResourceInfoVO();
    resourceInfoVO.setResourceId(datasetId);
    resourceInfoVO.setResourceTypeEnum(type);
    resourceInfoVO.setSecurityRoleEnum(role);

    return resourceInfoVO;
  }

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  @Override
  public void createDataSchema(Long datasetId, Long dataflowId) {}


  /**
   * Creates the rule fields.
   *
   * @param i the i
   * @param table the table
   * @param recordSchema the record schema
   * @param fieldSchemas the field schemas
   * @param headersSize the headers size
   * @param dataflowId the dataflow id
   */
  private void createRuleFields(int i, TableCollection table, RecordSchema recordSchema,
      List<FieldSchema> fieldSchemas, int headersSize, Long dataflowId) {


  }

  /**
   * Find the dataschema per id.
   *
   * @param dataschemaId the idDataschema
   *
   * @return the data schema by id
   */
  @Override
  public DataSetSchemaVO getDataSchemaById(String dataschemaId) {

    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(dataschemaId));

    DataSetSchemaVO dataSchemaVO = new DataSetSchemaVO();
    if (dataschema.isPresent()) {
      DataSetSchema datasetSchema = dataschema.get();
      dataSchemaVO = dataSchemaMapper.entityToClass(datasetSchema);
      setNameSchema(dataschemaId, dataSchemaVO);
    }

    return dataSchemaVO;
  }

  /**
   * Find the dataschema per idDataFlow.
   *
   * @param addRules the add rules
   * @param datasetId the dataset id
   * @return the data schema by dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public DataSetSchemaVO getDataSchemaByDatasetId(Boolean addRules, Long datasetId)
      throws EEAException {

    DataSetMetabase metabase = obtainDatasetMetabase(datasetId);
    DataSetSchema dataschema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(metabase.getDatasetSchema()));
    LOG.info("Schema retrived by datasetId {}", datasetId);
    DataSetSchemaVO dataschemaVO =
        Boolean.TRUE.equals(addRules) ? dataSchemaMapper.entityToClass(dataschema)
            : noRulesDataSchemaMapper.entityToClass(dataschema);
    setNameSchema(metabase.getDatasetSchema(), dataschemaVO);
    return dataschemaVO;

  }

  /**
   * Sets the name schema.
   *
   * @param schemaId the schema id
   * @param dataschemaVO the dataschema VO
   */
  private void setNameSchema(String schemaId, DataSetSchemaVO dataschemaVO) {
    Optional<DesignDataset> designDataset =
        designDatasetRepository.findFirstByDatasetSchema(schemaId);
    if (designDataset.isPresent()) {
      dataschemaVO.setNameDatasetSchema(designDataset.get().getDataSetName());
    }
  }


  /**
   * Gets the dataset schema id.
   *
   * @param datasetId the dataset id
   * @return the dataset schema id
   * @throws EEAException the EEA exception
   */
  @Override
  public String getDatasetSchemaId(Long datasetId) throws EEAException {
    return obtainDatasetMetabase(datasetId).getDatasetSchema();
  }

  /**
   * Obtain dataset metabase.
   *
   * @param datasetId the dataset id
   *
   * @return the data set metabase
   *
   * @throws EEAException the EEA exception
   */
  private DataSetMetabase obtainDatasetMetabase(final Long datasetId) throws EEAException {
    final DataSetMetabase datasetMetabase =
        dataSetMetabaseRepository.findById(datasetId).orElse(null);
    if (datasetMetabase == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return datasetMetabase;
  }

  /**
   * Delete dataset schema.
   *
   * @param datasetId the dataset id
   * @param schemaId the schema id
   */
  @Override
  @Transactional
  public void deleteDatasetSchema(Long datasetId, String schemaId) {
    schemasRepository.deleteDatasetSchemaById(schemaId);
  }

  /**
   * Gets the table schema.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   * @return the table schema
   */
  private TableSchema getTableSchema(String idTableSchema, DataSetSchema dataSetSchema) {
    // Find the Id of tableSchema in MongoDB
    return dataSetSchema.getTableSchemas() == null ? null
        : dataSetSchema.getTableSchemas().stream()
            .filter(
                tableSchema -> tableSchema.getIdTableSchema().equals(new ObjectId(idTableSchema)))
            .findAny().orElse(null);
  }

  /**
   * Replace schema.
   *
   * @param idSchema the id schema
   * @param schema the schema
   * @param idDataset the id dataset
   * @param idSnapshot the id snapshot
   */
  @Override
  @Transactional
  public void replaceSchema(String idSchema, DataSetSchema schema, Long idDataset,
      Long idSnapshot) {
    schemasRepository.deleteDatasetSchemaById(idSchema);
    schemasRepository.save(schema);
    // Call to recordstores to make the restoring of the dataset data (table, records and fields
    // values)
    recordStoreControllerZull.restoreSnapshotData(idDataset, idSnapshot, 0L, TypeDatasetEnum.DESIGN,
        (String) ThreadPropertiesManager.getVariable("user"), true, true);
  }

  /**
   * Creates the table schema.
   *
   * @param id the id
   * @param tableSchemaVO the table schema VO
   * @param datasetId the dataset id
   * @return the table schema VO
   */
  @Override
  public TableSchemaVO createTableSchema(String id, TableSchemaVO tableSchemaVO, Long datasetId) {
    ObjectId tableSchemaId = new ObjectId();
    tableSchemaVO.setIdTableSchema(tableSchemaId.toString());
    RecordSchema recordSchema = new RecordSchema();
    ObjectId recordSchemaId = new ObjectId();
    recordSchema.setIdRecordSchema(recordSchemaId);
    recordSchema.setIdTableSchema(tableSchemaId);
    recordSchema.setFieldSchema(new ArrayList<>());
    TableSchema table = tableSchemaMapper.classToEntity(tableSchemaVO);
    table.setRecordSchema(recordSchema);
    LOG.info("Creating table schema with id {}", tableSchemaId);
    schemasRepository.insertTableSchema(table, id);
    // prepare ids to return to the frontend
    RecordSchemaVO recordSchemaVO = new RecordSchemaVO();
    recordSchemaVO.setIdRecordSchema(recordSchemaId.toString());
    tableSchemaVO.setRecordSchema(recordSchemaVO);
    return (tableSchemaVO);
  }

  /**
   * Update table schema.
   *
   * @param datasetSchemaId the dataset schemaid
   * @param tableSchemaVO the table schema VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateTableSchema(String datasetSchemaId, TableSchemaVO tableSchemaVO)
      throws EEAException {
    try {
      // Recuperar el TableSchema de MongoDB
      Document tableSchema =
          schemasRepository.findTableSchema(datasetSchemaId, tableSchemaVO.getIdTableSchema());

      if (tableSchema != null) {
        // Modificarlo en función de lo que contiene el TableSchemaVO recibido
        if (tableSchemaVO.getDescription() != null) {
          tableSchema.put("description", tableSchemaVO.getDescription());
        }
        if (tableSchemaVO.getNameTableSchema() != null) {
          tableSchema.put("nameTableSchema", tableSchemaVO.getNameTableSchema());
        }

        // Guardar el TableSchema modificado en MongoDB
        if (schemasRepository.updateTableSchema(datasetSchemaId, tableSchema)
            .getModifiedCount() == 1) {
          return;
        }
      }
      LOG.error(EEAErrorMessage.TABLE_NOT_FOUND);
      throw new EEAException(EEAErrorMessage.TABLE_NOT_FOUND);
    } catch (IllegalArgumentException e) {
      throw new EEAException(e);
    }
  }

  /**
   * Delete table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param idTableSchema the id table schema
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteTableSchema(String datasetSchemaId, String idTableSchema) throws EEAException {
    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    TableSchema table = getTableSchema(idTableSchema, datasetSchema);
    if (table == null) {
      LOG.error(EEAErrorMessage.TABLE_NOT_FOUND);
      throw new EEAException(EEAErrorMessage.TABLE_NOT_FOUND);
    }
    schemasRepository.deleteTableSchemaById(idTableSchema);
  }

  /**
   * Order table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param position the position
   * @return the boolean
   * @throws EEAException the EEA exception
   */
  @Override
  public Boolean orderTableSchema(String datasetSchemaId, String tableSchemaId, Integer position)
      throws EEAException {
    Document tableSchema = schemasRepository.findTableSchema(datasetSchemaId, tableSchemaId);
    if (tableSchema != null) {
      schemasRepository.deleteTableSchemaById(tableSchemaId);
      return schemasRepository.insertTableInPosition(datasetSchemaId, tableSchema, position)
          .getModifiedCount() == 1;
    }
    return false;
  }

  /**
   * Creates the field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  @Override
  public String createFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {
    try {
      fieldSchemaVO.setId(new ObjectId().toString());
      return schemasRepository
          .createFieldSchema(datasetSchemaId, fieldSchemaNoRulesMapper.classToEntity(fieldSchemaVO))
          .getModifiedCount() == 1 ? fieldSchemaVO.getId() : "";
    } catch (IllegalArgumentException e) {
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Update field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return the string
   * @throws EEAException the EEA exception
   */
  @Override
  public String updateFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {
    boolean typeModified = false;
    try {
      // Recuperar el FieldSchema de MongoDB
      Document fieldSchema =
          schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaVO.getId());

      if (fieldSchema != null) {
        // Modificarlo en función de lo que contiene el FieldSchemaVO recibido
        if (fieldSchemaVO.getType() != null
            && !fieldSchema.put("typeData", fieldSchemaVO.getType().getValue())
                .equals(fieldSchemaVO.getType().getValue())) {
          typeModified = true;
          if (!fieldSchemaVO.getType().getValue().equalsIgnoreCase("CODELIST")
              && fieldSchema.containsKey("idCodeList")) {
            fieldSchema.remove("idCodeList");
          }
        }
        if (fieldSchemaVO.getDescription() != null) {
          fieldSchema.put("description", fieldSchemaVO.getDescription());
        }
        if (fieldSchemaVO.getName() != null) {
          fieldSchema.put("headerName", fieldSchemaVO.getName());
        }
        if (fieldSchemaVO.getIdCodeList() != null) {
          fieldSchema.put("idCodeList", fieldSchemaVO.getIdCodeList());
        }
        // Guardar el FieldSchema modificado en MongoDB
        if (schemasRepository.updateFieldSchema(datasetSchemaId, fieldSchema)
            .getModifiedCount() == 1) {
          if (typeModified) {
            return fieldSchemaVO.getType().getValue();
          }
          return null;
        }
      }
      LOG.error(EEAErrorMessage.FIELD_NOT_FOUND);
      throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);
    } catch (IllegalArgumentException e) {
      throw new EEAException(e);
    }
  }

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return true, if 1 and only 1 fieldSchema has been removed
   * @throws EEAException the EEA exception
   */
  @Override
  public boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId)
      throws EEAException {
    return schemasRepository.deleteFieldSchema(datasetSchemaId, fieldSchemaId)
        .getModifiedCount() == 1;
  }

  /**
   * Order field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param position the position
   * @return the boolean
   * @throws EEAException the EEA exception
   */
  @Override
  public Boolean orderFieldSchema(String datasetSchemaId, String fieldSchemaId, Integer position)
      throws EEAException {
    Document fieldSchema = schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaId);
    if (fieldSchema != null) {
      schemasRepository.deleteFieldSchema(datasetSchemaId, fieldSchemaId);
      return schemasRepository.insertFieldInPosition(datasetSchemaId, fieldSchema, position)
          .getModifiedCount() == 1;
    }
    return false;
  }

  /**
   * Update dataset schema description.
   *
   * @param datasetSchemaId the dataset schema id
   * @param description the description
   * @return the boolean
   */
  @Override
  public Boolean updateDatasetSchemaDescription(String datasetSchemaId, String description) {
    return schemasRepository.updateDatasetSchemaDescription(datasetSchemaId, description)
        .getModifiedCount() == 1;
  }

  /**
   * Gets the table schema name.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @return the table schema name
   */
  @Override
  public String getTableSchemaName(String datasetSchemaId, String tableSchemaId) {
    Document tableSchema = schemasRepository.findTableSchema(datasetSchemaId, tableSchemaId);
    if (tableSchema != null) {
      return (String) tableSchema.get("nameTableSchema");
    }
    return null;
  }


  /**
   * Validate schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the boolean
   */
  @Override
  public Boolean validateSchema(String datasetSchemaId) {

    Boolean isValid = true;
    DataSetSchemaVO schema = this.getDataSchemaById(datasetSchemaId);
    for (ValidationSchemaCommand command : validationCommands) {
      if (!command.execute(schema)) {
        isValid = false;
      }
    }

    return isValid;
  }
}
