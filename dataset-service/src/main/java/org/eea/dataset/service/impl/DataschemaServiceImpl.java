package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.mapper.UniqueConstraintMapper;
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
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class DataschemaServiceImpl.
 */
@Service("dataschemaService")
public class DataschemaServiceImpl implements DatasetSchemaService {

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The resource management controller zull. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementControllerZull;

  /** The data flow controller zuul. */
  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  /** The data schema mapper. */
  @Autowired
  private DataSchemaMapper dataSchemaMapper;

  /** The no rules data schema mapper. */
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

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataschemaServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The unique constraint repository. */
  @Autowired
  private UniqueConstraintRepository uniqueConstraintRepository;

  /** The unique constraint mapper. */
  @Autowired
  private UniqueConstraintMapper uniqueConstraintMapper;

  /**
   * Creates the empty data set schema.
   *
   * @param dataflowId the dataflow id
   *
   * @return the object id
   *
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
    dataSetSchema.setTableSchemas(new ArrayList<>());
    schemasRepository.save(dataSetSchema);

    // create the rules schema
    rulesControllerZuul.createEmptyRulesSchema(idDataSetSchema.toString(),
        new ObjectId().toString());

    return idDataSetSchema;
  }

  /**
   * Delete group and remove user.
   *
   * @param datasetId the dataset id
   * @param resourceTypeEnum the resource type enum
   */
  @Override
  public void deleteGroup(Long datasetId, ResourceTypeEnum resourceTypeEnum) {
    // We find all types of data of this schema and delete it
    List<ResourceInfoVO> resourceCustodian = resourceManagementControllerZull
        .getGroupsByIdResourceType(datasetId, ResourceTypeEnum.DATA_SCHEMA);
    resourceManagementControllerZull.deleteResource(resourceCustodian);
  }

  /**
   * Gets the data schema by id.
   *
   * @param dataschemaId the dataschema id
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
   *
   * @return the data schema by dataset id
   *
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
   *
   * @return the dataset schema id
   *
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
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteDatasetSchema(String schemaId, Long datasetId) {
    // we delete the integrity rules associated with this dataset and delete the integrity in mongo
    rulesControllerZuul.deleteDatasetRuleAndIntegrityByDatasetSchemaId(schemaId, datasetId);
    schemasRepository.deleteDatasetSchemaById(schemaId);
  }

  /**
   * Gets the table schema.
   *
   * @param idTableSchema the id table schema
   * @param dataSetSchema the data set schema
   *
   * @return the table schema
   */
  private TableSchema getTableSchema(String idTableSchema, DataSetSchema dataSetSchema) {

    TableSchema tableSchema = null;

    if (null != dataSetSchema && null != dataSetSchema.getTableSchemas()
        && ObjectId.isValid(idTableSchema)) {
      ObjectId tableSchemaId = new ObjectId(idTableSchema);
      tableSchema = dataSetSchema.getTableSchemas().stream()
          .filter(ts -> tableSchemaId.equals(ts.getIdTableSchema())).findFirst().orElse(null);
    }

    return tableSchema;
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
   *
   * @return the table schema VO
   */
  @Override
  public TableSchemaVO createTableSchema(String id, TableSchemaVO tableSchemaVO, Long datasetId) {
    ObjectId tableSchemaId = new ObjectId();
    tableSchemaVO.setIdTableSchema(tableSchemaId.toString());
    tableSchemaVO.setToPrefill(false);
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
   *
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
        if (tableSchemaVO.getReadOnly() != null) {
          tableSchema.put("readOnly", tableSchemaVO.getReadOnly());
        }
        if (tableSchemaVO.getToPrefill() != null) {
          tableSchema.put("toPrefill", tableSchemaVO.getToPrefill());
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
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteTableSchema(String datasetSchemaId, String idTableSchema, Long datasetId)
      throws EEAException {
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
      List<?> fieldSchemasList = (ArrayList<?>) recordSchemadocument.get("fieldSchemas");
      fieldSchemasList.stream().forEach(document -> {
        rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId,
            ((Document) document).get("_id").toString());
        // we delete the ruleIntegrity for each fields that we have in this table
        // document.
        rulesControllerZuul.deleteDatasetRuleAndIntegrityByFieldSchemaId(
            ((Document) document).get("_id").toString(), datasetId);
      });
      rulesControllerZuul.deleteRuleByReferenceId(datasetSchemaId,
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
   *
   * @return the boolean
   *
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
   *
   * @return true, if successful
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public String createFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {
    try {
      fieldSchemaVO.setId(new ObjectId().toString());

      if (fieldSchemaVO.getReferencedField() != null) {
        // We need to update the fieldSchema is referenced, the property isPKreferenced to true
        updateIsPkReferencedInFieldSchema(fieldSchemaVO.getReferencedField().getIdDatasetSchema(),
            fieldSchemaVO.getReferencedField().getIdPk(), true);
      }
      // we create this if to clean blank space at begining and end of any codelistItem
      // n codelist and multiselect
      if (fieldSchemaVO.getCodelistItems() != null && fieldSchemaVO.getCodelistItems().length != 0
          && (DataType.MULTISELECT_CODELIST.equals(fieldSchemaVO.getType())
              || DataType.CODELIST.equals(fieldSchemaVO.getType()))) {
        String[] codelistItems = fieldSchemaVO.getCodelistItems();
        for (int i = 0; i < codelistItems.length; i++) {
          codelistItems[i] = codelistItems[i].trim();
        }
        fieldSchemaVO.setCodelistItems(codelistItems);
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
   *
   * @return the type data
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public DataType updateFieldSchema(String datasetSchemaId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {
    boolean typeModified = false;
    try {
      // Retrieve the FieldSchema from MongoDB
      if (fieldSchemaVO != null) {
        Document fieldSchema =
            schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaVO.getId());

        if (fieldSchema != null) {
          // First of all, we update the previous data in the catalog
          if (DataType.LINK.getValue().equals(fieldSchema.get(LiteralConstants.TYPE_DATA))) {
            // Proceed to the changes needed. Remove the previous reference
            Document previousReferenced =
                (Document) fieldSchema.get(LiteralConstants.REFERENCED_FIELD);
            if (previousReferenced != null && previousReferenced.get("idPk") != null) {
              String previousId = fieldSchema.get("_id").toString();
              String previousIdPk = previousReferenced.get("idPk").toString();
              String previousIdDatasetReferenced =
                  previousReferenced.get(LiteralConstants.ID_DATASET_SCHEMA).toString();
              PkCatalogueSchema catalogue =
                  pkCatalogueRepository.findByIdPk(new ObjectId(previousIdPk));
              if (catalogue != null) {
                catalogue.getReferenced().remove(new ObjectId(previousId));
                pkCatalogueRepository.deleteByIdPk(catalogue.getIdPk());
                pkCatalogueRepository.save(catalogue);
                // We need to update the field isReferenced to false from the PK referenced if this
                // was
                // the only field that was FK
                if (catalogue.getReferenced() != null && catalogue.getReferenced().isEmpty()) {
                  this.updateIsPkReferencedInFieldSchema(previousIdDatasetReferenced, previousIdPk,
                      false);
                }

              }
            }
          }

          // Update UniqueConstraints
          if (fieldSchemaVO.getPk() != fieldSchema.get(LiteralConstants.PK)) {
            if (fieldSchemaVO.getPk()) {
              if (null == fieldSchemaVO.getIdRecord()) {
                fieldSchemaVO.setIdRecord(fieldSchema.get("idRecord").toString());
              }
              createUniqueConstraintPK(datasetSchemaId, fieldSchemaVO);
            } else {
              deleteOnlyUniqueConstraintFromField(datasetSchemaId, fieldSchemaVO.getId());
            }
          }

          // Modify it based on FieldSchemaVO data received
          if (fieldSchemaVO.getType() != null
              && !fieldSchema.put(LiteralConstants.TYPE_DATA, fieldSchemaVO.getType().getValue())
                  .equals(fieldSchemaVO.getType().getValue())) {
            typeModified = true;
            if (!(DataType.MULTISELECT_CODELIST.equals(fieldSchemaVO.getType())
                || DataType.CODELIST.equals(fieldSchemaVO.getType()))
                && fieldSchema.containsKey(LiteralConstants.CODELIST_ITEMS)) {
              fieldSchema.remove(LiteralConstants.CODELIST_ITEMS);
            }
          }
          if (fieldSchemaVO.getDescription() != null) {
            fieldSchema.put("description", fieldSchemaVO.getDescription());
          }
          if (fieldSchemaVO.getName() != null) {
            fieldSchema.put("headerName", fieldSchemaVO.getName());
          }
          // that if control the codelist to add new items when codelist had already been created
          // this method work for codelist and multiselect_codedlist
          if (fieldSchemaVO.getCodelistItems() != null
              && fieldSchemaVO.getCodelistItems().length != 0
              && (DataType.MULTISELECT_CODELIST.equals(fieldSchemaVO.getType())
                  || DataType.CODELIST.equals(fieldSchemaVO.getType()))) {
            // we clean blank space in codelist and multiselect
            String[] codelistItems = fieldSchemaVO.getCodelistItems();
            for (int i = 0; i < codelistItems.length; i++) {
              codelistItems[i] = codelistItems[i].trim();
            }
            fieldSchema.put(LiteralConstants.CODELIST_ITEMS, Arrays.asList(codelistItems));
            typeModified = true;
          }
          if (fieldSchemaVO.getRequired() != null) {
            fieldSchema.put("required", fieldSchemaVO.getRequired());
          }
          if (fieldSchemaVO.getPk() != null) {
            fieldSchema.put("pk", fieldSchemaVO.getPk());
          }
          if (fieldSchemaVO.getPkMustBeUsed() != null) {
            fieldSchema.put("pkMustBeUsed", fieldSchemaVO.getPkMustBeUsed());
          }
          if (fieldSchemaVO.getPkHasMultipleValues() != null) {
            fieldSchema.put("pkHasMultipleValues", fieldSchemaVO.getPkHasMultipleValues());
          }
          if (fieldSchemaVO.getReferencedField() != null) {
            Document referenced = new Document();
            referenced.put(LiteralConstants.ID_DATASET_SCHEMA,
                new ObjectId(fieldSchemaVO.getReferencedField().getIdDatasetSchema()));
            referenced.put("idPk", new ObjectId(fieldSchemaVO.getReferencedField().getIdPk()));
            fieldSchema.put(LiteralConstants.REFERENCED_FIELD, referenced);
            // We need to update the fieldSchema that is referenced, the property isPKreferenced to
            // true
            this.updateIsPkReferencedInFieldSchema(
                fieldSchemaVO.getReferencedField().getIdDatasetSchema(),
                fieldSchemaVO.getReferencedField().getIdPk(), true);
          }

          // Save the modified FieldSchema in the MongoDB
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
      } else {
        LOG.error(EEAErrorMessage.FIELD_NOT_FOUND);
        throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);
      }
    } catch (IllegalArgumentException e) {
      throw new EEAException(e);
    }
  }

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   * @return true, if 1 and only 1 fieldSchema has been removed
   * @throws EEAException the EEA exception
   */
  @Override
  public boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId, Long datasetId)
      throws EEAException {

    // now we find if we have any record rule related with that fieldSchema to delete it
    rulesControllerZuul.deleteRuleHighLevelLike(datasetSchemaId, fieldSchemaId);

    // we call that method to find if this field have a integrity Rule, and if it has, delete the
    // integrity and the rule at datasetLevel
    rulesControllerZuul.deleteDatasetRuleAndIntegrityByFieldSchemaId(fieldSchemaId, datasetId);

    return schemasRepository.deleteFieldSchema(datasetSchemaId, fieldSchemaId)
        .getModifiedCount() == 1;
  }



  /**
   * Order field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param position the position
   *
   * @return the boolean
   *
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
   *
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
   *
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
   *
   * @return the boolean
   */
  @Override
  public Boolean validateSchema(String datasetSchemaId) {

    Boolean isValid = true;
    DataSetSchemaVO schema = getDataSchemaById(datasetSchemaId);
    for (ValidationSchemaCommand command : validationCommands) {
      if (Boolean.FALSE.equals(command.execute(schema))) {
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
      TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
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
      // If the type is Link, delete and create again the rule, the field pkMustBeUsed maybe has
      // changed
      if (DataType.LINK.equals(fieldSchemaVO.getType())) {
        // Delete previous fk rule and insert it again
        rulesControllerZuul.deleteRuleByReferenceFieldSchemaPKId(datasetSchemaId,
            fieldSchemaVO.getId());
        rulesControllerZuul.createAutomaticRule(datasetSchemaId, fieldSchemaVO.getId(),
            fieldSchemaVO.getType(), EntityTypeEnum.FIELD, datasetId, Boolean.FALSE);
      }
    }


  }

  /**
   * Check pk allow update. Checks two things: no more than 1 PK in the same table, and in the case
   * we want to erase a PK, that PK is not being referenced
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   *
   * @return the boolean
   */
  @Override
  public Boolean checkPkAllowUpdate(String datasetSchemaId, FieldSchemaVO fieldSchemaVO) {

    Boolean allow = true;
    if (fieldSchemaVO.getPk() != null) {
      // Check existing PKs on the same table
      if (Boolean.TRUE.equals(fieldSchemaVO.getPk())) {
        DataSetSchemaVO schema = this.getDataSchemaById(datasetSchemaId);
        TableSchemaVO table = null;
        for (TableSchemaVO tableVO : schema.getTableSchemas()) {
          if (tableVO.getRecordSchema() != null
              && tableVO.getRecordSchema().getFieldSchema() != null
              && tableVO.getRecordSchema().getFieldSchema().stream()
                  .anyMatch(field -> field.getId().equals(fieldSchemaVO.getId()))) {
            table = tableVO;
            break;
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
      if (Boolean.FALSE.equals(fieldSchemaVO.getPk())) {
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
   *
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
   * Checks if is schema allowed for deletion.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the boolean
   */
  @Override
  public Boolean isSchemaAllowedForDeletion(String idDatasetSchema) {
    Boolean allow = true;
    DataSetSchemaVO schema = this.getDataSchemaById(idDatasetSchema);
    if (null != schema && null != schema.getTableSchemas() && !schema.getTableSchemas().isEmpty()) {
      for (TableSchemaVO tableVO : schema.getTableSchemas()) {
        if (tableVO.getRecordSchema() != null
            && tableVO.getRecordSchema().getFieldSchema() != null) {
          for (FieldSchemaVO field : tableVO.getRecordSchema().getFieldSchema()) {
            if (field.getPk() != null && field.getPk() && field.getPkReferenced() != null
                && Boolean.TRUE.equals(field.getPkReferenced())) {
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
   *
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
    if (DataType.LINK.equals(fieldSchemaVO.getType())
        && fieldSchemaVO.getReferencedField() != null) {
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
   * Adds the foreign relation into the metabase.
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
   * Delete foreign relation from the metabase.
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
   * Update foreign relation in the metabase.
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
    if (fieldSchema != null
        && DataType.LINK.getValue().equals(fieldSchema.get(LiteralConstants.TYPE_DATA))) {
      // First of all, we delete the previous relation on the Metabase, if applies
      Document previousReferenced = (Document) fieldSchema.get(LiteralConstants.REFERENCED_FIELD);
      if (previousReferenced != null && previousReferenced.get("idPk") != null) {
        String previousIdPk = previousReferenced.get("idPk").toString();
        String previousIdDatasetReferenced =
            previousReferenced.get(LiteralConstants.ID_DATASET_SCHEMA).toString();
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
   *
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
   *
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
   *
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
                updateIsPkReferencedInFieldSchema(
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
   *
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
   *
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
   *
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
            // Update the PK referenced in field schema, to mark it as referenced=true
            updateIsPkReferencedInFieldSchema(
                field.getReferencedField().getIdDatasetSchema().toString(),
                field.getReferencedField().getIdPk().toString(), true);
            // Add the relation into the metabase
            addForeignRelation(idDataset, fieldSchemaNoRulesMapper.entityToClass(field));
          }
        }
      }
    }
  }

  /**
   * Update the property isPKreferenced of the class FieldSchema.
   *
   * @param referencedIdDatasetSchema the referenced id dataset schema
   * @param referencedIdPk the referenced id pk
   * @param referenced the referenced
   * @throws EEAException the EEA exception
   */
  private void updateIsPkReferencedInFieldSchema(String referencedIdDatasetSchema,
      String referencedIdPk, Boolean referenced) throws EEAException {

    Document fieldSchemaReferenced =
        schemasRepository.findFieldSchema(referencedIdDatasetSchema, referencedIdPk);
    if (fieldSchemaReferenced != null) {
      fieldSchemaReferenced.put("pkReferenced", referenced);
      schemasRepository.updateFieldSchema(referencedIdDatasetSchema, fieldSchemaReferenced);
    }
  }

  /**
   * Creates the unique constraint.
   *
   * @param uniqueConstraintVO the unique constraint VO
   */
  @Override
  public void createUniqueConstraint(UniqueConstraintVO uniqueConstraintVO) {
    LOG.info("Creating unique contraint");
    uniqueConstraintVO.setUniqueId(new ObjectId().toString());
    uniqueConstraintRepository.save(uniqueConstraintMapper.classToEntity(uniqueConstraintVO));
    rulesControllerZuul.createUniqueConstraintRule(uniqueConstraintVO.getDatasetSchemaId(),
        uniqueConstraintVO.getTableSchemaId(), uniqueConstraintVO.getUniqueId());
    LOG.info("unique constraint created with id {}", uniqueConstraintVO.getUniqueId());
  }

  /**
   * Delete unique constraint.
   *
   * @param uniqueId the unique id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteUniqueConstraint(String uniqueId) throws EEAException {
    LOG.info("deleting constraint {}", uniqueId);
    UniqueConstraintVO uniqueConstraint = getUniqueConstraint(uniqueId);
    uniqueConstraintRepository.deleteByUniqueId(new ObjectId(uniqueId));
    rulesControllerZuul.deleteUniqueConstraintRule(uniqueConstraint.getDatasetSchemaId(), uniqueId);
    LOG.info("unique constraint deleted with id {}", uniqueId);
  }

  /**
   * Delete uniques constraint from table.
   *
   * @param tableSchemaId the table schema id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteUniquesConstraintFromTable(String tableSchemaId) throws EEAException {
    List<UniqueConstraintSchema> constraints =
        uniqueConstraintRepository.findByTableSchemaId(new ObjectId(tableSchemaId));
    for (UniqueConstraintSchema uniqueConstraintSchema : constraints) {
      deleteUniqueConstraint(uniqueConstraintSchema.getUniqueId().toString());
    }
  }

  /**
   * Delete uniques constraint from field.
   *
   * @param schemaId the schema id
   * @param fieldSchemaId the field schema id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteUniquesConstraintFromField(String schemaId, String fieldSchemaId)
      throws EEAException {
    List<UniqueConstraintVO> constraints = getUniqueConstraints(schemaId);
    for (UniqueConstraintVO uniqueConstraintVO : constraints) {
      if (uniqueConstraintVO.getFieldSchemaIds().contains(fieldSchemaId)) {
        deleteUniqueConstraint(uniqueConstraintVO.getUniqueId());
      }
    }
  }

  /**
   * Delete only unique constraint from field.
   *
   * @param schemaId the schema id
   * @param fieldSchemaId the field schema id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteOnlyUniqueConstraintFromField(String schemaId, String fieldSchemaId)
      throws EEAException {
    List<UniqueConstraintVO> constraints = getUniqueConstraints(schemaId);
    for (UniqueConstraintVO uniqueConstraintVO : constraints) {
      if (uniqueConstraintVO.getFieldSchemaIds().size() == 1
          && uniqueConstraintVO.getFieldSchemaIds().contains(fieldSchemaId)) {
        deleteUniqueConstraint(uniqueConstraintVO.getUniqueId());
      }
    }
  }

  /**
   * Delete uniques constraint from dataset.
   *
   * @param datasetSchemaId the dataset schema id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteUniquesConstraintFromDataset(String datasetSchemaId) throws EEAException {
    List<UniqueConstraintVO> constraints = getUniqueConstraints(datasetSchemaId);
    for (UniqueConstraintVO uniqueConstraint : constraints) {
      deleteUniqueConstraint(uniqueConstraint.getUniqueId());
    }
  }

  /**
   * Update unique constraint.
   *
   * @param uniqueConstraintVO the unique constraint
   */
  @Override
  public void updateUniqueConstraint(UniqueConstraintVO uniqueConstraintVO) {
    LOG.info("updating constraint {}", uniqueConstraintVO.getUniqueId());
    uniqueConstraintRepository.deleteByUniqueId(new ObjectId(uniqueConstraintVO.getUniqueId()));
    rulesControllerZuul.deleteUniqueConstraintRule(uniqueConstraintVO.getDatasetSchemaId(),
        uniqueConstraintVO.getUniqueId());
    uniqueConstraintRepository.save(uniqueConstraintMapper.classToEntity(uniqueConstraintVO));
    rulesControllerZuul.createUniqueConstraintRule(uniqueConstraintVO.getDatasetSchemaId(),
        uniqueConstraintVO.getTableSchemaId(), uniqueConstraintVO.getUniqueId());
    LOG.info("unique constraint updated with id {}", uniqueConstraintVO.getUniqueId());
  }

  /**
   * Gets the unique constraints.
   *
   * @param schemaId the schema id
   * @return the unique constraints
   */
  @Override
  public List<UniqueConstraintVO> getUniqueConstraints(String schemaId) {
    LOG.info("get all unique Constraints of dataset {}", schemaId);
    return uniqueConstraintMapper.entityListToClass(
        uniqueConstraintRepository.findByDatasetSchemaId(new ObjectId(schemaId)));
  }


  /**
   * Gets the unique constraint.
   *
   * @param uniqueId the unique id
   * @return the unique constraint
   * @throws EEAException the EEA exception
   */
  @Override
  public UniqueConstraintVO getUniqueConstraint(String uniqueId) throws EEAException {
    LOG.info("get unique Constraints {}", uniqueId);
    Optional<UniqueConstraintSchema> uniqueResult =
        uniqueConstraintRepository.findById(new ObjectId(uniqueId));
    if (uniqueResult.isPresent()) {
      return uniqueConstraintMapper.entityToClass(uniqueResult.get());
    } else {
      LOG_ERROR.error(
          "Error finding the unique constraint from the catalogue. UniqueId: {} not found",
          uniqueId);
      throw new EEAException(String.format(EEAErrorMessage.UNIQUE_NOT_FOUND, uniqueId));
    }
  }

  /**
   * Creates the unique constraint PK.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   */
  @Override
  public void createUniqueConstraintPK(String datasetSchemaId, FieldSchemaVO fieldSchemaVO) {
    LOG.info("Creating Unique Constraint for field {}", fieldSchemaVO.getId());
    // if field is Pk we create a unique Constraint
    if (fieldSchemaVO.getPk() != null && fieldSchemaVO.getPk()) {
      // Get TableSchemaId
      DataSetSchema datasetSchema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));
      ObjectId idTableSchema = null;
      for (TableSchema table : datasetSchema.getTableSchemas()) {
        if (table.getRecordSchema().getIdRecordSchema().toString()
            .equals(fieldSchemaVO.getIdRecord())) {
          idTableSchema = table.getIdTableSchema();
        }
      }
      // Create Unique Constraint
      if (idTableSchema != null) {
        UniqueConstraintVO unique = new UniqueConstraintVO();
        ArrayList<String> fieldSchemaIds = new ArrayList<>();
        fieldSchemaIds.add(fieldSchemaVO.getId());
        unique.setDatasetSchemaId(datasetSchemaId);
        unique.setTableSchemaId(idTableSchema.toString());
        unique.setFieldSchemaIds(fieldSchemaIds);
        List<ObjectId> fields = new ArrayList<>();
        fields.add(new ObjectId(fieldSchemaVO.getId()));
        List<UniqueConstraintSchema> uniques =
            uniqueConstraintRepository.findByFieldSchemaIds(fields);
        if (uniques == null || uniques.isEmpty()) {
          createUniqueConstraint(unique);
        }
      }
    }
  }


  /**
   * Copy unique constraints catalogue.
   *
   * @param originDatasetSchemaIds the origin dataset schema ids
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  @Override
  public void copyUniqueConstraintsCatalogue(List<String> originDatasetSchemaIds,
      Map<String, String> dictionaryOriginTargetObjectId) {
    // We obtain the UniqueConstraints of the origin dataset schemas, and with the help of the
    // dictionary we replace the objectIds with the correct ones to finally save them. The result it
    // will be new constraints in the catalogue with correct data according to the new copied
    // schemas
    for (String datasetSchemaId : originDatasetSchemaIds) {
      List<UniqueConstraintVO> uniques = getUniqueConstraints(datasetSchemaId);
      for (UniqueConstraintVO uniqueConstraintVO : uniques) {
        uniqueConstraintVO
            .setUniqueId(dictionaryOriginTargetObjectId.get(uniqueConstraintVO.getUniqueId()));
        uniqueConstraintVO.setDatasetSchemaId(dictionaryOriginTargetObjectId.get(datasetSchemaId));
        uniqueConstraintVO.setTableSchemaId(
            dictionaryOriginTargetObjectId.get(uniqueConstraintVO.getTableSchemaId()));
        for (int i = 0; i < uniqueConstraintVO.getFieldSchemaIds().size(); i++) {
          uniqueConstraintVO.getFieldSchemaIds().set(i,
              dictionaryOriginTargetObjectId.get(uniqueConstraintVO.getFieldSchemaIds().get(i)));
        }
        LOG.info("A unique constraint is going to be created during the copy process");
        uniqueConstraintRepository.save(uniqueConstraintMapper.classToEntity(uniqueConstraintVO));
      }
    }

  }
}
