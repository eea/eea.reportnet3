package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;

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

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The data flow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The rules controller. */
  @Autowired
  private RulesController rulesController;

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


  /** The rules controller zuul. */
  @Autowired
  private RulesControllerZuul rulesControllerZuul;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;


  /** The validation commands. */
  @Autowired
  private List<ValidationSchemaCommand> validationCommands;

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;


  /** The pk catalogue repository. */
  @Autowired
  private PkCatalogueRepository pkCatalogueRepository;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataschemaServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The data set metabase repository.
   */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

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

    // create the schema of its rules
    rulesControllerZuul.createEmptyRulesSchema(idDataSetSchema.toString(),
        new ObjectId().toString());

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
   * Gets the data schema by id.
   *
   * @param dataschemaId the dataschema id
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
   * @param schemaId the schema id
   */
  @Override
  @Transactional
  public void deleteDatasetSchema(String schemaId) {
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
    recordStoreControllerZull.restoreSnapshotData(idDataset, idSnapshot, 0L, DatasetTypeEnum.DESIGN,
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
    // when we delete a table we need to delete all rules of this table, we mean, rules of the
    // records fields, etc
    Document recordSchemadocument =
        schemasRepository.findRecordSchema(datasetSchemaId, idTableSchema);
    // if the table havent got any record he hasnt any document too
    if (null != recordSchemadocument) {
      List<Document> fieldSchemasList = (List<Document>) recordSchemadocument.get("fieldSchemas");
      fieldSchemasList.stream().forEach(document -> {
        rulesController.deleteRuleByReferenceId(datasetSchemaId, document.get("_id").toString());
      });
      rulesController.deleteRuleByReferenceId(datasetSchemaId,
          recordSchemadocument.get("_id").toString());
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
   * Creates the field schema in mongo.
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

      if (fieldSchemaVO.getReferencedField() != null) {
        // We need to update the fieldSchema is referenced, the property isPKreferenced to true
        this.updateIsPkReferencedInFieldSchema(
            fieldSchemaVO.getReferencedField().getIdDatasetSchema(),
            fieldSchemaVO.getReferencedField().getIdPk(), true);
      }

      return schemasRepository
          .createFieldSchema(datasetSchemaId, fieldSchemaNoRulesMapper.classToEntity(fieldSchemaVO))
          .getModifiedCount() == 1 ? fieldSchemaVO.getId() : "";
    } catch (IllegalArgumentException e) {
      throw new EEAException(e.getMessage());
    }
  }

  /**
   * Update field schema in mongo and check if the field is a codelist or not.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return the type data
   * @throws EEAException the EEA exception
   */
  @Override
  public DataType updateFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {
    boolean typeModified = false;
    try {
      // Recuperar el FieldSchema de MongoDB
      Document fieldSchema =
          schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaVO.getId());

      if (fieldSchema != null) {
        // First of all, we update the previous data in the catalogue
        if (DataType.LINK.getValue().equals(fieldSchema.get("typeData"))) {
          // Proceed to the changes needed. Remove the previous reference
          String previousId = fieldSchema.get("_id").toString();
          Document previousReferenced = (Document) fieldSchema.get("referencedField");
          String previousIdPk = previousReferenced.get("idPk").toString();
          String previousIdDatasetReferenced = previousReferenced.get("idDatasetSchema").toString();
          PkCatalogueSchema catalogue =
              pkCatalogueRepository.findByIdPk(new ObjectId(previousIdPk));
          if (catalogue != null) {
            catalogue.getReferenced().remove(new ObjectId(previousId));
            pkCatalogueRepository.deleteByIdPk(catalogue.getIdPk());
            pkCatalogueRepository.save(catalogue);
            // We need to update the field isReferenced to false from the PK referenced if this was
            // the only field that was FK
            if (catalogue.getReferenced() != null && catalogue.getReferenced().isEmpty()) {
              this.updateIsPkReferencedInFieldSchema(previousIdDatasetReferenced, previousIdPk,
                  false);
            }

          }
        }


        // Modificarlo en función de lo que contiene el FieldSchemaVO recibido
        if (fieldSchemaVO.getType() != null
            && !fieldSchema.put("typeData", fieldSchemaVO.getType().getValue())
                .equals(fieldSchemaVO.getType().getValue())) {
          typeModified = true;
          if (!fieldSchemaVO.getType().getValue().equalsIgnoreCase("CODELIST")
              && fieldSchema.containsKey("codelistItems")) {
            fieldSchema.remove("codelistItems");
          }
        }
        if (fieldSchemaVO.getDescription() != null) {
          fieldSchema.put("description", fieldSchemaVO.getDescription());
        }
        if (fieldSchemaVO.getName() != null) {
          fieldSchema.put("headerName", fieldSchemaVO.getName());
        }
        if (fieldSchemaVO.getCodelistItems() != null && fieldSchemaVO.getCodelistItems().length != 0
            && fieldSchemaVO.getType().getValue().equalsIgnoreCase("CODELIST")) {
          fieldSchema.put("codelistItems", Arrays.asList(fieldSchemaVO.getCodelistItems()));
        }
        if (fieldSchemaVO.getRequired() != null) {
          fieldSchema.put("required", fieldSchemaVO.getRequired());
        }
        if (fieldSchemaVO.getPk() != null) {
          fieldSchema.put("pk", fieldSchemaVO.getPk());
        }
        if (fieldSchemaVO.getReferencedField() != null) {
          Document referenced = new Document();
          referenced.put("idDatasetSchema",
              new ObjectId(fieldSchemaVO.getReferencedField().getIdDatasetSchema()));
          referenced.put("idPk", new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));
          fieldSchema.put("referencedField", referenced);
          // We need to update the fieldSchema that is referenced, the property isPKreferenced to
          // true
          this.updateIsPkReferencedInFieldSchema(
              fieldSchemaVO.getReferencedField().getIdDatasetSchema(),
              fieldSchemaVO.getReferencedField().getIdPk(), true);
        }

        // Guardar el FieldSchema modificado en MongoDB
        UpdateResult updateResult =
            schemasRepository.updateFieldSchema(datasetSchemaId, fieldSchema);
        if (updateResult.getMatchedCount() == 1) {
          if (updateResult.getModifiedCount() == 1 && typeModified) {
            return fieldSchemaVO.getType();
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


  /**
   * Propagate rules after update schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @param type the type
   * @param datasetId the dataset id
   */
  @Override
  public void propagateRulesAfterUpdateSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO,
      DataType type, Long datasetId) {

    if (type != null) {
      // if we change the type we need to delete all rules
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId, fieldSchemaVO.getId());
      // Delete FK Rules
      rulesControllerZuul.deleteRuleByReferenceFieldSchemaPKId(datasetSchemaId,
          fieldSchemaVO.getId());

      if (Boolean.TRUE.equals(fieldSchemaVO.getRequired())) {
        rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(), type,
            EntityTypeEnum.FIELD, datasetId, Boolean.TRUE);
      }

      rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
          fieldSchemaVO.getType(), EntityTypeEnum.FIELD, datasetId, Boolean.FALSE);
      // update the dataset field value
      TenantResolver.setTenantName(String.format("dataset_%s", datasetId));
      datasetService.updateFieldValueType(datasetId, fieldSchemaVO.getId(), type);
    } else {
      if (Boolean.TRUE.equals(fieldSchemaVO.getRequired())) {
        if (!rulesControllerZuul.existsRuleRequired(datasetSchemaId, fieldSchemaVO.getId())) {
          rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
              fieldSchemaVO.getType(), EntityTypeEnum.FIELD, datasetId, Boolean.TRUE);
        }
      } else {
        rulesControllerZuul.deleteRuleRequired(datasetSchemaId, fieldSchemaVO.getId());
      }
    }


  }

  /**
   * Check pk allow update. Checks two things: no more than 1 PK in the same table, and in the case
   * we want to erase a PK, that PK is not being referenced
   * 
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return the boolean
   */
  @Override
  public Boolean checkPkAllowUpdate(String datasetSchemaId, FieldSchemaVO fieldSchemaVO) {

    Boolean allow = true;
    if (fieldSchemaVO.getPk() != null) {
      // Check existing PKs on the same table
      if (fieldSchemaVO.getPk()) {
        DataSetSchemaVO schema = this.getDataSchemaById(datasetSchemaId);
        TableSchemaVO table = null;
        for (TableSchemaVO tableVO : schema.getTableSchemas()) {
          if (tableVO.getRecordSchema() != null
              && tableVO.getRecordSchema().getFieldSchema() != null) {
            if (tableVO.getRecordSchema().getFieldSchema().stream()
                .anyMatch(field -> field.getId().equals(fieldSchemaVO.getId()))) {
              table = tableVO;
              break;
            }
          }
        }
        if (table != null) {
          for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
            if (field.getPk() != null && field.getPk()
                && !field.getId().equals(fieldSchemaVO.getId())) {
              allow = false;
              LOG_ERROR.error("There is actually an existing PK on the table. Update denied");
            }
          }
        }
      }
      // Check the PK is referenced or not in case we are trying to remove it
      if (!fieldSchemaVO.getPk()) {
        PkCatalogueSchema catalogue =
            pkCatalogueRepository.findByIdPk(new ObjectId(fieldSchemaVO.getId()));
        if (catalogue != null && catalogue.getReferenced() != null
            && !catalogue.getReferenced().isEmpty()) {
          allow = false;
          LOG_ERROR.error(
              "The PK the user is trying to delete is being referenced by a FK. Update denied");
        }
      }
    }
    return allow;

  }


  /**
   * Check existing pk referenced. Check against the PKCatalogue the PK is being referenced
   * 
   * @param fieldSchemaVO the field schema VO
   * @return the boolean
   */
  @Override
  public Boolean checkExistingPkReferenced(FieldSchemaVO fieldSchemaVO) {
    Boolean isReferenced = false;

    if (fieldSchemaVO.getPk() != null && fieldSchemaVO.getPk()) {
      PkCatalogueSchema catalogue =
          pkCatalogueRepository.findByIdPk(new ObjectId(fieldSchemaVO.getId()));
      if (catalogue != null && catalogue.getReferenced() != null
          && !catalogue.getReferenced().isEmpty()) {
        isReferenced = true;
      }

    }

    return isReferenced;
  }



  /**
   * Checks if is schema for deletion allowed.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the boolean
   */
  @Override
  public Boolean isSchemaForDeletionAllowed(String idDatasetSchema) {
    Boolean allow = true;
    DataSetSchemaVO schema = this.getDataSchemaById(idDatasetSchema);
    for (TableSchemaVO tableVO : schema.getTableSchemas()) {
      if (tableVO.getRecordSchema() != null && tableVO.getRecordSchema().getFieldSchema() != null) {
        for (FieldSchemaVO field : tableVO.getRecordSchema().getFieldSchema()) {
          if (field.getPk() != null && field.getPk() && field.getPkReferenced() != null
              && field.getPkReferenced()) {
            PkCatalogueSchema catalogue =
                pkCatalogueRepository.findByIdPk(new ObjectId(field.getId()));
            if (catalogue != null && catalogue.getReferenced() != null
                && !catalogue.getReferenced().isEmpty()) {
              for (ObjectId referenced : catalogue.getReferenced()) {
                Document fieldSchema =
                    schemasRepository.findFieldSchema(idDatasetSchema, referenced.toString());
                if (fieldSchema == null) {
                  allow = false;
                }
              }
            }
          }
        }
      }
    }
    return allow;
  }



  /**
   * Adds the to pk catalogue.
   *
   * @param fieldSchemaVO the field schema VO
   */
  @Override
  public void addToPkCatalogue(FieldSchemaVO fieldSchemaVO) {

    if (fieldSchemaVO.getReferencedField() != null) {
      PkCatalogueSchema catalogue = pkCatalogueRepository
          .findByIdPk(new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));

      if (catalogue != null && catalogue.getIdPk() != null) {
        catalogue.getReferenced().add(new ObjectId(fieldSchemaVO.getId()));
        pkCatalogueRepository
            .deleteByIdPk(new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));
      } else {
        catalogue = new PkCatalogueSchema();
        catalogue.setIdPk(new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));
        catalogue.setReferenced(new ArrayList<>());
        catalogue.getReferenced().add(new ObjectId(fieldSchemaVO.getId()));
      }
      pkCatalogueRepository.save(catalogue);
    }
  }

  /**
   * Delete from pk catalogue.
   *
   * @param fieldSchemaVO the field schema VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteFromPkCatalogue(FieldSchemaVO fieldSchemaVO) throws EEAException {
    // For fielSchemas that are PK
    if (fieldSchemaVO.getPk() != null && !fieldSchemaVO.getPk()) {
      PkCatalogueSchema catalogue =
          pkCatalogueRepository.findByIdPk(new ObjectId(fieldSchemaVO.getId()));
      if (catalogue != null) {
        pkCatalogueRepository.deleteByIdPk(catalogue.getIdPk());
      }
    }
    // For fieldSchemas that are FK
    if (DataType.LINK.equals(fieldSchemaVO.getType())) {
      PkCatalogueSchema catalogue = pkCatalogueRepository
          .findByIdPk(new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));
      if (catalogue != null) {
        catalogue.getReferenced().remove(new ObjectId(fieldSchemaVO.getId()));
        pkCatalogueRepository.deleteByIdPk(catalogue.getIdPk());
        pkCatalogueRepository.save(catalogue);
        // We need to update the field isReferenced from the PK referenced if this was the only
        // field that was FK
        if (catalogue.getReferenced() != null && catalogue.getReferenced().isEmpty()) {
          this.updateIsPkReferencedInFieldSchema(
              fieldSchemaVO.getReferencedField().getIdDatasetSchema(),
              fieldSchemaVO.getReferencedField().getIdPk(), false);
        }
      }
    }
  }

  /**
   * Adds the foreign relation into the metabase
   *
   * @param idDatasetOrigin the id dataset origin
   * @param fieldSchemaVO the field schema VO
   */
  @Override
  public void addForeignRelation(Long idDatasetOrigin, FieldSchemaVO fieldSchemaVO) {
    if (fieldSchemaVO.getReferencedField() != null) {
      datasetMetabaseService.addForeignRelation(idDatasetOrigin,
          this.getDesignDatasetIdDestinationFromFk(
              fieldSchemaVO.getReferencedField().getIdDatasetSchema()),
          fieldSchemaVO.getReferencedField().getIdPk(), fieldSchemaVO.getId());
    }
  }

  /**
   * Delete foreign relation from the metabase
   *
   * @param idDatasetOrigin the id dataset origin
   * @param fieldSchemaVO the field schema VO
   */
  @Override
  public void deleteForeignRelation(Long idDatasetOrigin, FieldSchemaVO fieldSchemaVO) {
    if (fieldSchemaVO.getReferencedField() != null) {
      datasetMetabaseService.deleteForeignRelation(idDatasetOrigin,
          this.getDesignDatasetIdDestinationFromFk(
              fieldSchemaVO.getReferencedField().getIdDatasetSchema()),
          fieldSchemaVO.getReferencedField().getIdPk(), fieldSchemaVO.getId());
    }
  }

  /**
   * Update foreign relation in the metabase
   *
   * @param idDatasetOrigin the id dataset origin
   * @param fieldSchemaVO the field schema VO
   * @param datasetSchemaId the dataset schema id
   */
  @Override
  public void updateForeignRelation(Long idDatasetOrigin, FieldSchemaVO fieldSchemaVO,
      String datasetSchemaId) {
    Document fieldSchema =
        schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaVO.getId());
    if (fieldSchema != null) {
      // First of all, we delete the previous relation on the Metabase, if applies
      if (DataType.LINK.getValue().equals(fieldSchema.get("typeData"))) {
        Document previousReferenced = (Document) fieldSchema.get("referencedField");
        String previousIdPk = previousReferenced.get("idPk").toString();
        String previousIdDatasetReferenced = previousReferenced.get("idDatasetSchema").toString();
        datasetMetabaseService.deleteForeignRelation(idDatasetOrigin,
            this.getDesignDatasetIdDestinationFromFk(previousIdDatasetReferenced), previousIdPk,
            fieldSchemaVO.getId());
      }

    }
    // If the type is Link, then we add the relation on the Metabase
    if (fieldSchemaVO.getType() != null
        && DataType.LINK.getValue().equals(fieldSchemaVO.getType().getValue())) {
      this.addForeignRelation(idDatasetOrigin, fieldSchemaVO);
    }
  }

  /**
   * Gets the field schema. Find the FieldSchema and converts into the VO
   *
   * @param datasetSchemaId the dataset schema id
   * @param idFieldSchema the id field schema
   * @return the field schema
   */
  @Override
  public FieldSchemaVO getFieldSchema(String datasetSchemaId, String idFieldSchema) {

    Document fieldSchemaDoc = schemasRepository.findFieldSchema(datasetSchemaId, idFieldSchema);
    FieldSchemaVO fieldVO = new FieldSchemaVO();
    if (fieldSchemaDoc != null) {

      JsonWriterSettings settings = JsonWriterSettings.builder()
          .objectIdConverter((value, writer) -> writer.writeString(value.toString())).build();

      String json = fieldSchemaDoc.toJson(settings);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      try {
        FieldSchema schema = objectMapper.readValue(json, FieldSchema.class);
        fieldVO = fieldSchemaNoRulesMapper.entityToClass(schema);
      } catch (JsonProcessingException e) {
        LOG_ERROR.error("Error getting the fieldSchemaVO {}", idFieldSchema);
      }
    }
    return fieldVO;
  }


  /**
   * Gets the design dataset id destination from fk.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the design dataset id destination from fk
   */
  private Long getDesignDatasetIdDestinationFromFk(String idDatasetSchema) {
    Long datasetIdDestination = null;

    Optional<DesignDataset> designDataset =
        designDatasetRepository.findFirstByDatasetSchema(idDatasetSchema);
    if (designDataset.isPresent()) {
      datasetIdDestination = designDataset.get().getId();
    }

    return datasetIdDestination;
  }

  /**
   * Update pk catalogue deleting schema. When deleting an schema, the PKCatalogue needs to be
   * updated. Search for all the FK references in the schema that is going to be deleted and then
   * update the catalogue one by one
   *
   * @param idDatasetSchema the id dataset schema
   * @throws EEAException the EEA exception
   */
  @Override
  public void updatePkCatalogueDeletingSchema(String idDatasetSchema) throws EEAException {

    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(idDatasetSchema));
    if (dataschema.isPresent()) {
      for (TableSchema table : dataschema.get().getTableSchemas()) {
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          if (field.getReferencedField() != null) {
            PkCatalogueSchema catalogue =
                pkCatalogueRepository.findByIdPk(field.getReferencedField().getIdPk());
            if (catalogue != null) {
              catalogue.getReferenced().remove(field.getIdFieldSchema());
              pkCatalogueRepository.deleteByIdPk(catalogue.getIdPk());
              pkCatalogueRepository.save(catalogue);
              // We need to update the field isReferenced from the PK referenced if this was the
              // only field that was FK
              if (catalogue.getReferenced() != null && catalogue.getReferenced().isEmpty()) {
                this.updateIsPkReferencedInFieldSchema(
                    field.getReferencedField().getIdDatasetSchema().toString(),
                    field.getReferencedField().getIdPk().toString(), false);
              }
            }
          }
        }
      }
    }
  }


  /**
   * Gets the referenced fields by schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the referenced fields by schema
   */
  @Override
  public List<ReferencedFieldSchema> getReferencedFieldsBySchema(String datasetSchemaId) {

    List<ReferencedFieldSchema> references = new ArrayList<>();
    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(datasetSchemaId));
    if (dataschema.isPresent()) {
      for (TableSchema table : dataschema.get().getTableSchemas()) {
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          if (field.getReferencedField() != null) {
            references.add(field.getReferencedField());
          }
        }
      }
    }
    return references;
  }


  /**
   * Delete from pk catalogue.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteFromPkCatalogue(String datasetSchemaId, String tableSchemaId)
      throws EEAException {

    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    TableSchema table = getTableSchema(tableSchemaId, datasetSchema);
    if (table != null && table.getRecordSchema() != null
        && table.getRecordSchema().getFieldSchema() != null) {
      table.getRecordSchema().getFieldSchema().forEach(field -> {
        try {
          deleteFromPkCatalogue(fieldSchemaNoRulesMapper.entityToClass(field));
        } catch (EEAException e) {
          LOG_ERROR.error("Error deleting the PK from the catalogue. Message: {}", e.getMessage(),
              e);
        }
      });
    }

  }


  /**
   * Update PK catalogue and foreigns after snapshot.
   *
   * @param idDatasetSchema the id dataset schema
   * @param idDataset the id dataset
   * @throws EEAException the EEA exception
   */
  @Override
  public void updatePKCatalogueAndForeignsAfterSnapshot(String idDatasetSchema, Long idDataset)
      throws EEAException {

    // After deleting the previous entries, we fill again the catalogue and references with the new
    // schema that has been restored
    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(idDatasetSchema));
    if (dataschema.isPresent()) {
      for (TableSchema table : dataschema.get().getTableSchemas()) {
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          if (field.getReferencedField() != null) {

            PkCatalogueSchema catalogue =
                pkCatalogueRepository.findByIdPk(field.getReferencedField().getIdPk());

            if (catalogue != null && catalogue.getIdPk() != null) {
              catalogue.getReferenced().add(field.getIdFieldSchema());
              pkCatalogueRepository.deleteByIdPk(field.getReferencedField().getIdPk());
            } else {
              catalogue = new PkCatalogueSchema();
              catalogue.setIdPk(field.getReferencedField().getIdPk());
              catalogue.setReferenced(new ArrayList<>());
              catalogue.getReferenced().add(field.getIdFieldSchema());
            }
            pkCatalogueRepository.save(catalogue);
            if (catalogue.getReferenced() != null && catalogue.getReferenced().isEmpty()) {
              this.updateIsPkReferencedInFieldSchema(
                  field.getReferencedField().getIdDatasetSchema().toString(),
                  field.getReferencedField().getIdPk().toString(), true);
            }
            // Add the relation into the metabase
            addForeignRelation(idDataset, fieldSchemaNoRulesMapper.entityToClass(field));
          }
        }
      }
    }

  }

  /**
   * Update the property isPKreferenced of the class FieldSchema
   * 
   * @param referencedIdDatasetSchema
   * @param referencedIdPk
   * @param referenced
   * @throws EEAException
   */
  private void updateIsPkReferencedInFieldSchema(String referencedIdDatasetSchema,
      String referencedIdPk, Boolean referenced) throws EEAException {

    Document fieldSchemaReferenced =
        schemasRepository.findFieldSchema(referencedIdDatasetSchema, referencedIdPk);
    fieldSchemaReferenced.put("pkReferenced", referenced);
    schemasRepository.updateFieldSchema(referencedIdDatasetSchema, fieldSchemaReferenced);
  }

}
