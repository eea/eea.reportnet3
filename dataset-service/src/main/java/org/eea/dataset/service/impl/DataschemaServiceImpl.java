package org.eea.dataset.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.SimpleDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.mapper.UniqueConstraintMapper;
import org.eea.dataset.mapper.WebFormMapper;
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
import org.eea.dataset.persistence.schemas.domain.webform.Webform;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.persistence.schemas.repository.UniqueConstraintRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.model.ImportSchemas;
import org.eea.dataset.validate.commands.ValidationSchemaCommand;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleDatasetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.SimpleTableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.WebformVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.uniqueContraintVO.UniqueConstraintVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class DataschemaServiceImpl.
 */
@Service("dataschemaService")
public class DataschemaServiceImpl implements DatasetSchemaService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataschemaServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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

  /** The record store controller zuul. */
  @Autowired
  private RecordStoreControllerZuul recordStoreControllerZuul;

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
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The pk catalogue repository. */
  @Autowired
  private PkCatalogueRepository pkCatalogueRepository;

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

  /** The simple data schema mapper. */
  @Autowired
  private SimpleDataSchemaMapper simpleDataSchemaMapper;

  /** The web form mapper. */
  @Autowired
  private WebFormMapper webFormMapper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  @Autowired
  private ContributorControllerZuul contributorControllerZuul;



  /** The Constant FIELDSCHEMAS: {@value}. */
  private static final String FIELDSCHEMAS = "fieldSchemas";

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
    schemasRepository.updateDatasetSchemaWebForm(idDataSetSchema.toString(), new Webform());
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
   * @param tableSchemaId the table schema id
   * @param datasetSchemaId the dataset schema id
   * @return the table schema
   */
  @Override
  public TableSchema getTableSchema(String tableSchemaId, String datasetSchemaId) {

    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    TableSchema tableSchema = null;

    if (null != datasetSchema && null != datasetSchema.getTableSchemas()
        && ObjectId.isValid(tableSchemaId)) {
      ObjectId oid = new ObjectId(tableSchemaId);
      tableSchema = datasetSchema.getTableSchemas().stream()
          .filter(ts -> oid.equals(ts.getIdTableSchema())).findFirst().orElse(null);
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
    recordStoreControllerZuul.restoreSnapshotData(idDataset, idSnapshot, 0L, DatasetTypeEnum.DESIGN,
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
    ObjectId recordSchemaId = new ObjectId();

    RecordSchemaVO recordSchemaVO = new RecordSchemaVO();
    recordSchemaVO.setIdRecordSchema(recordSchemaId.toString());

    tableSchemaVO.setRecordSchema(recordSchemaVO);
    tableSchemaVO.setIdTableSchema(tableSchemaId.toString());
    if (null == tableSchemaVO.getToPrefill()) {
      tableSchemaVO.setToPrefill(false);
    }
    if (null == tableSchemaVO.getNotEmpty()) {
      tableSchemaVO.setNotEmpty(false);
    }
    if (null == tableSchemaVO.getReadOnly()) {
      tableSchemaVO.setReadOnly(false);
    }
    if (null == tableSchemaVO.getFixedNumber()) {
      tableSchemaVO.setFixedNumber(false);
    }

    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setIdRecordSchema(recordSchemaId);
    recordSchema.setIdTableSchema(tableSchemaId);
    recordSchema.setFieldSchema(new ArrayList<>());

    TableSchema table = tableSchemaMapper.classToEntity(tableSchemaVO);
    table.setRecordSchema(recordSchema);

    schemasRepository.insertTableSchema(table, id);
    LOG.info("Created TableSchema {}: {}", tableSchemaId, table);

    if (Boolean.TRUE.equals(tableSchemaVO.getNotEmpty())) {
      createNotEmptyRule(tableSchemaId.toString(), datasetId);
    }

    return tableSchemaVO;
  }

  /**
   * Update table schema.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateTableSchema(Long datasetId, TableSchemaVO tableSchemaVO) throws EEAException {

    String datasetSchemaId = getDatasetSchemaId(datasetId);

    try {
      Document tableSchema =
          schemasRepository.findTableSchema(datasetSchemaId, tableSchemaVO.getIdTableSchema());

      if (tableSchema != null) {
        tableShemaAddAtributes(datasetId, tableSchemaVO, datasetSchemaId, tableSchema);
      } else {
        LOG.error("Table with schema {} from the datasetId {} not found",
            tableSchemaVO.getIdTableSchema(), datasetId);
        throw new EEAException(String.format(EEAErrorMessage.TABLE_NOT_FOUND,
            tableSchemaVO.getIdTableSchema(), datasetId));
      }
    } catch (IllegalArgumentException e) {
      throw new EEAException(e);
    }

    releaseCreateUpdateView(datasetId,
        SecurityContextHolder.getContext().getAuthentication().getName(), false);

  }

  /**
   * Table shema add atributes.
   *
   * @param datasetId the dataset id
   * @param tableSchemaVO the table schema VO
   * @param datasetSchemaId the dataset schema id
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  private void tableShemaAddAtributes(Long datasetId, TableSchemaVO tableSchemaVO,
      String datasetSchemaId, Document tableSchema) throws EEAException {
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
    if (tableSchemaVO.getFixedNumber() != null) {
      tableSchema.put("fixedNumber", tableSchemaVO.getFixedNumber());
    }
    if (tableSchemaVO.getNotEmpty() != null) {
      Boolean oldValue = tableSchema.getBoolean("notEmpty");
      Boolean newValue = tableSchemaVO.getNotEmpty();
      tableSchema.put("notEmpty", newValue);
      updateNotEmptyRule(oldValue, newValue, tableSchemaVO.getIdTableSchema(), datasetId);
    }

    if (schemasRepository.updateTableSchema(datasetSchemaId, tableSchema).getModifiedCount() != 1) {
      LOG.error("Error updating the table with id schema {} from the dataset id {}", tableSchema,
          datasetId);
      throw new EEAException(
          String.format(EEAErrorMessage.ERROR_UPDATING_TABLE_SCHEMA, tableSchema, datasetId));
    }
  }

  /**
   * Delete table schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the id table schema
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteTableSchema(String datasetSchemaId, String tableSchemaId, Long datasetId)
      throws EEAException {
    TableSchema tableSchema = getTableSchema(tableSchemaId, datasetSchemaId);
    if (tableSchema == null) {
      LOG.error("Table with schema {} from the datasetId {} not found", tableSchemaId, datasetId);
      throw new EEAException(
          String.format(EEAErrorMessage.TABLE_NOT_FOUND, tableSchemaId, datasetId));
    }
    // when we delete a table we need to delete all rules of this table, we mean, rules of the
    // records fields, etc
    Document recordSchemadocument =
        schemasRepository.findRecordSchema(datasetSchemaId, tableSchemaId);
    // if the table havent got any record he hasnt any document too
    if (null != recordSchemadocument) {
      List<?> fieldSchemasList = (ArrayList<?>) recordSchemadocument.get(FIELDSCHEMAS);
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

    schemasRepository.deleteTableSchemaById(tableSchemaId);

    // Delete the notEmpty rule if exists
    if (Boolean.TRUE.equals(tableSchema.getNotEmpty())) {
      deleteNotEmptyRule(tableSchemaId, datasetId);
    }
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

    // we check if the field name already exist in schema
    if (checkIfFieldNameAlreadyExist(datasetSchemaId, fieldSchemaVO)) {
      LOG.error(String.format(EEAErrorMessage.FIELD_NAME_DUPLICATED, fieldSchemaVO.getName(),
          fieldSchemaVO.getIdRecord(), datasetSchemaId));
      throw new EEAException(String.format(EEAErrorMessage.FIELD_NAME_DUPLICATED,
          fieldSchemaVO.getName(), fieldSchemaVO.getIdRecord(), datasetSchemaId));
    }

    try {
      fieldSchemaVO.setId(new ObjectId().toString());


      if (fieldSchemaVO.getReferencedField() != null) {
        // We need to update the fieldSchema is referenced, the property isPKreferenced to true
        updateIsPkReferencedInFieldSchema(fieldSchemaVO.getReferencedField().getIdDatasetSchema(),
            fieldSchemaVO.getReferencedField().getIdPk(), true);
      }
      if (null == fieldSchemaVO.getReadOnly()) {
        fieldSchemaVO.setReadOnly(false);
      }
      // we create this if to clean blank space at begining and end of any codelistItem
      // n codelist and multiselect
      if (fieldSchemaVO.getCodelistItems() != null
          && (DataType.MULTISELECT_CODELIST.equals(fieldSchemaVO.getType())
              || DataType.CODELIST.equals(fieldSchemaVO.getType()))) {
        String[] codelistItems = fieldSchemaVO.getCodelistItems();
        for (int i = 0; i < codelistItems.length; i++) {
          codelistItems[i] = codelistItems[i].trim();
        }
        fieldSchemaVO.setCodelistItems(codelistItems);
      }
      if (fieldSchemaVO.getValidExtensions() != null) {
        String[] validExtensions = fieldSchemaVO.getValidExtensions();
        for (int i = 0; i < validExtensions.length; i++) {
          validExtensions[i] = validExtensions[i].trim();
        }
        fieldSchemaVO.setValidExtensions(validExtensions);
      }
      if (fieldSchemaVO.getMaxSize() == null || fieldSchemaVO.getMaxSize() == 0
          || fieldSchemaVO.getMaxSize() > 20) {
        fieldSchemaVO.setMaxSize(20f);
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

    // we check if the field name already exist in schema
    if (null != fieldSchemaVO.getName()
        && checkIfFieldNameAlreadyExist(datasetSchemaId, fieldSchemaVO)) {
      LOG.error(String.format(EEAErrorMessage.FIELD_NAME_DUPLICATED, fieldSchemaVO.getName(),
          fieldSchemaVO.getIdRecord(), datasetSchemaId));
      throw new EEAException(String.format(EEAErrorMessage.FIELD_NAME_DUPLICATED,
          fieldSchemaVO.getName(), fieldSchemaVO.getIdRecord(), datasetSchemaId));
    }
    boolean typeModified = false;
    try {
      // Retrieve the FieldSchema from MongoDB
      Document fieldSchema =
          schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaVO.getId());
      if (fieldSchema != null) {
        // First of all, we update the previous data in the catalog
        updatePreviousDataInCatalog(fieldSchema);

        // Update UniqueConstraints
        updateUniqueConstraints(datasetSchemaId, fieldSchemaVO, fieldSchema);

        // we find if one data is modified
        typeModified = modifySchemaInUpdate(fieldSchemaVO, typeModified, fieldSchema);

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
    } catch (IllegalArgumentException e) {
      throw new EEAException(e);
    }
  }

  /**
   * Modify schema in update.
   *
   * @param fieldSchemaVO the field schema VO
   * @param typeModified the type modified
   * @param fieldSchema the field schema
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  private boolean modifySchemaInUpdate(FieldSchemaVO fieldSchemaVO, boolean typeModified,
      Document fieldSchema) throws EEAException {
    // Modify it based on FieldSchemaVO data received
    typeModified = modifyIsTrueAndCodeListConstants(fieldSchemaVO, typeModified, fieldSchema);
    if (fieldSchemaVO.getDescription() != null) {
      fieldSchema.put("description", fieldSchemaVO.getDescription());
    }
    if (fieldSchemaVO.getName() != null) {
      fieldSchema.put("headerName", fieldSchemaVO.getName());
    }
    // that if control the codelist to add new items when codelist had already been created
    // this method work for codelist and multiselect_codedlist
    typeModified = modifiedCodelist(fieldSchemaVO, typeModified, fieldSchema);
    if (fieldSchemaVO.getRequired() != null) {
      fieldSchema.put("required", fieldSchemaVO.getRequired());
    }
    pkFieldSchemaValues(fieldSchemaVO, fieldSchema);
    Float size = 20f;
    if (fieldSchemaVO.getMaxSize() != null && fieldSchemaVO.getMaxSize() != 0
        && fieldSchemaVO.getMaxSize() < 20) {
      size = fieldSchemaVO.getMaxSize();
    }
    fieldSchema.put("maxSize", size);
    if (fieldSchemaVO.getReadOnly() != null) {
      fieldSchema.put("readOnly", fieldSchemaVO.getReadOnly());
    }

    modifyValidExtensions(fieldSchemaVO, fieldSchema);
    if (fieldSchemaVO.getReferencedField() != null
        && DataType.LINK.equals(fieldSchemaVO.getType())) {
      ReferencedFieldSchemaVO referencedField = fieldSchemaVO.getReferencedField();
      Document referenced = fillReferencedDocument(referencedField);
      fieldSchema.put(LiteralConstants.REFERENCED_FIELD, referenced);
      // We need to update the fieldSchema that is referenced, the property isPKreferenced to
      // true
      updateIsPkReferencedInFieldSchema(referencedField.getIdDatasetSchema(),
          referencedField.getIdPk(), true);
    } else if (fieldSchema.get(LiteralConstants.REFERENCED_FIELD) != null
        && !DataType.LINK.equals(fieldSchemaVO.getType())) {
      // If the field is not a Link type, delete the referenced field to avoid problems
      fieldSchema.put(LiteralConstants.REFERENCED_FIELD, null);
    }
    return typeModified;
  }

  /**
   * Fill referenced document.
   *
   * @param referencedField the referenced field
   * @return the document
   */
  private Document fillReferencedDocument(ReferencedFieldSchemaVO referencedField) {
    Document referenced = new Document();
    referenced.put(LiteralConstants.ID_DATASET_SCHEMA,
        new ObjectId(referencedField.getIdDatasetSchema()));
    referenced.put("idPk", new ObjectId(referencedField.getIdPk()));
    if (StringUtils.isNotBlank(referencedField.getLabelId())) {
      referenced.put("labelId", new ObjectId(referencedField.getLabelId()));
    } else {
      referenced.put("labelId", null);
    }
    if (StringUtils.isNotBlank(referencedField.getLinkedConditionalFieldId())) {
      referenced.put("linkedConditionalFieldId",
          new ObjectId(referencedField.getLinkedConditionalFieldId()));
    } else {
      referenced.put("linkedConditionalFieldId", null);
    }
    if (StringUtils.isNotBlank(referencedField.getMasterConditionalFieldId())) {
      referenced.put("masterConditionalFieldId",
          new ObjectId(referencedField.getMasterConditionalFieldId()));
    } else {
      referenced.put("masterConditionalFieldId", null);
    }
    return referenced;
  }

  /**
   * Modify valid extensions.
   *
   * @param fieldSchemaVO the field schema VO
   * @param fieldSchema the field schema
   */
  private void modifyValidExtensions(FieldSchemaVO fieldSchemaVO, Document fieldSchema) {
    if (fieldSchemaVO.getValidExtensions() != null) {
      String[] validExtensions = fieldSchemaVO.getValidExtensions();
      for (int i = 0; i < validExtensions.length; i++) {
        validExtensions[i] = validExtensions[i].trim();
      }
      fieldSchema.put("validExtensions", Arrays.asList(validExtensions));
    }
  }

  /**
   * Modified codelist.
   *
   * @param fieldSchemaVO the field schema VO
   * @param typeModified the type modified
   * @param fieldSchema the field schema
   * @return true, if successful
   */
  private boolean modifiedCodelist(FieldSchemaVO fieldSchemaVO, boolean typeModified,
      Document fieldSchema) {
    if (fieldSchemaVO.getCodelistItems() != null && fieldSchemaVO.getCodelistItems().length != 0
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
    return typeModified;
  }

  /**
   * Pk field schema values.
   *
   * @param fieldSchemaVO the field schema VO
   * @param fieldSchema the field schema
   */
  private void pkFieldSchemaValues(FieldSchemaVO fieldSchemaVO, Document fieldSchema) {
    if (fieldSchemaVO.getPk() != null) {
      fieldSchema.put("pk", fieldSchemaVO.getPk());
    }
    if (fieldSchemaVO.getPkMustBeUsed() != null) {
      fieldSchema.put("pkMustBeUsed", fieldSchemaVO.getPkMustBeUsed());
    }
    if (fieldSchemaVO.getPkHasMultipleValues() != null) {
      fieldSchema.put("pkHasMultipleValues", fieldSchemaVO.getPkHasMultipleValues());
    }
  }

  /**
   * Modify is true and code list constants.
   *
   * @param fieldSchemaVO the field schema VO
   * @param typeModified the type modified
   * @param fieldSchema the field schema
   * @return true, if successful
   */
  private boolean modifyIsTrueAndCodeListConstants(FieldSchemaVO fieldSchemaVO,
      boolean typeModified, Document fieldSchema) {
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
    return typeModified;
  }

  /**
   * Update unique constraints.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @param fieldSchema the field schema
   * @throws EEAException the EEA exception
   */
  private void updateUniqueConstraints(String datasetSchemaId, FieldSchemaVO fieldSchemaVO,
      Document fieldSchema) throws EEAException {
    if (fieldSchemaVO.getPk() != fieldSchema.get(LiteralConstants.PK)) {
      if (Boolean.TRUE.equals(fieldSchemaVO.getPk())) {
        if (null == fieldSchemaVO.getIdRecord()) {
          fieldSchemaVO.setIdRecord(fieldSchema.get("idRecord").toString());
        }
        createUniqueConstraintPK(datasetSchemaId, fieldSchemaVO);
      } else {
        deleteOnlyUniqueConstraintFromField(datasetSchemaId, fieldSchemaVO.getId());
      }
    }
  }

  /**
   * Update previous data in catalog.
   *
   * @param fieldSchema the field schema
   * @throws EEAException the EEA exception
   */
  private void updatePreviousDataInCatalog(Document fieldSchema) throws EEAException {
    if (DataType.LINK.getValue().equals(fieldSchema.get(LiteralConstants.TYPE_DATA))) {
      // Proceed to the changes needed. Remove the previous reference
      Document previousReferenced = (Document) fieldSchema.get(LiteralConstants.REFERENCED_FIELD);
      if (previousReferenced != null && previousReferenced.get("idPk") != null) {
        String previousId = fieldSchema.get("_id").toString();
        String previousIdPk = previousReferenced.get("idPk").toString();
        String previousIdDatasetReferenced =
            previousReferenced.get(LiteralConstants.ID_DATASET_SCHEMA).toString();
        PkCatalogueSchema catalogue = pkCatalogueRepository.findByIdPk(new ObjectId(previousIdPk));
        if (catalogue != null) {
          catalogue.getReferenced().remove(new ObjectId(previousId));
          pkCatalogueRepository.deleteByIdPk(catalogue.getIdPk());
          pkCatalogueRepository.save(catalogue);
          // We need to update the field isReferenced to false from the PK referenced if this
          // was
          // the only field that was FK
          if (catalogue.getReferenced() != null && catalogue.getReferenced().isEmpty()) {
            updateIsPkReferencedInFieldSchema(previousIdDatasetReferenced, previousIdPk, false);
          }

        }
      }
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
  public void updateDatasetSchemaDescription(String datasetSchemaId, String description) {
    schemasRepository.updateDatasetSchemaDescription(datasetSchemaId, description);
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
      rulesControllerZuul.deleteAutomaticRuleByReferenceId(datasetSchemaId, fieldSchemaVO.getId());
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

      releaseCreateUpdateView(datasetId,
          SecurityContextHolder.getContext().getAuthentication().getName(), true);

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

      releaseCreateUpdateView(datasetId,
          SecurityContextHolder.getContext().getAuthentication().getName(), false);
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
          allow = fieldsPkFor(fieldSchemaVO, allow, table);
        }
      }
      // Check the PK is referenced or not in case we are trying to remove it
      allow = checkPkReferenced(fieldSchemaVO, allow);
    }
    return allow;

  }

  /**
   * Fields pk for.
   *
   * @param fieldSchemaVO the field schema VO
   * @param allow the allow
   * @param table the table
   * @return the boolean
   */
  private Boolean fieldsPkFor(FieldSchemaVO fieldSchemaVO, Boolean allow, TableSchemaVO table) {
    for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
      if (field.getPk() != null && field.getPk() && !field.getId().equals(fieldSchemaVO.getId())) {
        allow = false;
        LOG_ERROR.error("There is actually an existing PK on the table. Update denied");
      }
    }
    return allow;
  }

  /**
   * Check pk referenced.
   *
   * @param fieldSchemaVO the field schema VO
   * @param allow the allow
   * @return the boolean
   */
  private Boolean checkPkReferenced(FieldSchemaVO fieldSchemaVO, Boolean allow) {
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
            allow = fieldAllowedForDeletion(idDatasetSchema, allow, field);
          }
        }
      }
    }
    return allow;
  }

  /**
   * Field allowed for deletion.
   *
   * @param idDatasetSchema the id dataset schema
   * @param allow the allow
   * @param field the field
   * @return the boolean
   */
  private Boolean fieldAllowedForDeletion(String idDatasetSchema, Boolean allow,
      FieldSchemaVO field) {
    if (field.getPk() != null && field.getPk() && field.getPkReferenced() != null
        && Boolean.TRUE.equals(field.getPkReferenced())) {
      PkCatalogueSchema catalogue = pkCatalogueRepository.findByIdPk(new ObjectId(field.getId()));
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
            updateCatalogueDeleting(field);
          }
        }
      }
    }
  }

  /**
   * Update catalogue deleting.
   *
   * @param field the field
   * @throws EEAException the EEA exception
   */
  private void updateCatalogueDeleting(FieldSchema field) throws EEAException {
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
    TableSchema table = getTableSchema(tableSchemaId, datasetSchemaId);
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

            pkCatalogueMethod(idDataset, field);
          }
        }
      }
    }
  }

  /**
   * Pk catalogue method.
   *
   * @param idDataset the id dataset
   * @param field the field
   * @throws EEAException the EEA exception
   */
  private void pkCatalogueMethod(Long idDataset, FieldSchema field) throws EEAException {
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
    updateIsPkReferencedInFieldSchema(field.getReferencedField().getIdDatasetSchema().toString(),
        field.getReferencedField().getIdPk().toString(), true);
    // Add the relation into the metabase
    addForeignRelation(idDataset, fieldSchemaNoRulesMapper.entityToClass(field));
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

  /**
   * Gets the simple schema.
   *
   * @param datasetId the dataset id
   * @return the simple schema
   * @throws EEAException the EEA exception
   */
  @Override
  public SimpleDatasetSchemaVO getSimpleSchema(Long datasetId) throws EEAException {
    String schemaId = getDatasetSchemaId(datasetId);
    if (schemaId != null) {
      LOG.info("Getting schema from id {}", schemaId);
      Optional<DesignDataset> designDataset =
          designDatasetRepository.findFirstByDatasetSchema(schemaId);
      DataSetSchema datasetSchema = schemasRepository.findByIdDataSetSchema(new ObjectId(schemaId));
      if (datasetSchema != null) {
        SimpleDatasetSchemaVO simpleDatasetSchema =
            simpleDataSchemaMapper.entityToClass(datasetSchema);
        if (designDataset.isPresent()) {
          simpleDatasetSchema.setDatasetName(designDataset.get().getDataSetName());
        }
        setCountryCodeField(datasetId, simpleDatasetSchema);
        return simpleDatasetSchema;
      } else {
        throw new EEAException(String.format(EEAErrorMessage.DATASET_SCHEMA_NOT_FOUND, schemaId));
      }
    } else {
      throw new EEAException(String.format(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND, datasetId));
    }
  }

  /**
   * Sets the country code field if is a EUDatasset or DataCollection.
   *
   * @param datasetId the dataset id
   * @param simpleDatasetSchema the simple dataset schema
   */
  private void setCountryCodeField(Long datasetId, SimpleDatasetSchemaVO simpleDatasetSchema) {
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
    if (DatasetTypeEnum.EUDATASET.equals(datasetType)
        || DatasetTypeEnum.COLLECTION.equals(datasetType)) {
      SimpleFieldSchemaVO countryCode = new SimpleFieldSchemaVO();
      countryCode.setFieldName(LiteralConstants.COUNTRY_CODE);
      countryCode.setFieldType(DataType.TEXT);
      for (SimpleTableSchemaVO tables : simpleDatasetSchema.getTables()) {
        tables.getFields().add(0, countryCode);
      }
    }
  }


  /**
   * Check clear attachments.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return the boolean
   */
  @Override
  public Boolean checkClearAttachments(Long datasetId, String datasetSchemaId,
      FieldSchemaVO fieldSchemaVO) {
    Boolean hasToClean = false;
    Document fieldSchema =
        schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaVO.getId());
    if (fieldSchema != null) {
      Double previousMaxSize = (Double) fieldSchema.get("maxSize");
      List<String> previousExtensions = (List<String>) fieldSchema.get("validExtensions");
      List<String> differentExtensions = new ArrayList<>();
      if (previousExtensions != null) {
        List<String> similarExtensions = new ArrayList<>(previousExtensions);
        differentExtensions.addAll(similarExtensions);
        differentExtensions.addAll(Arrays.asList(fieldSchemaVO.getValidExtensions()));
        similarExtensions.retainAll(Arrays.asList(fieldSchemaVO.getValidExtensions()));
        differentExtensions.removeAll(similarExtensions);
      }
      // Clean if the data type was or is going to be an Attachment type
      if ((DataType.ATTACHMENT.getValue().equals(fieldSchema.get(LiteralConstants.TYPE_DATA))
          || DataType.ATTACHMENT.equals(fieldSchemaVO.getType()))
          && !fieldSchema.get(LiteralConstants.TYPE_DATA)
              .equals(fieldSchemaVO.getType().getValue())) {
        hasToClean = true;
      }
      // Clean if the type is still ATTACHMENT, but the maxSize or the list of file formats have
      // changed
      if (DataType.ATTACHMENT.equals(fieldSchemaVO.getType())
          && fieldSchemaVO.getType().getValue().equals(fieldSchema.get(LiteralConstants.TYPE_DATA))
          && previousMaxSize != null && previousExtensions != null
          && ((fieldSchemaVO.getMaxSize() != null
              && (previousMaxSize != fieldSchemaVO.getMaxSize().doubleValue()))
              || !differentExtensions.isEmpty())) {
        hasToClean = true;
      }
    }
    return hasToClean;
  }

  /**
   * Creates the not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  private void createNotEmptyRule(String tableSchemaId, Long datasetId) {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setReferenceId(tableSchemaId);
    ruleVO.setRuleName(LiteralConstants.RULE_TABLE_MANDATORY);
    ruleVO.setEnabled(true);
    ruleVO.setType(EntityTypeEnum.TABLE);
    ruleVO.setThenCondition(Arrays.asList("Mandatory table has no records", "ERROR"));
    ruleVO
        .setDescription("When a table is marked as mandatory, checks at least one record is added");

    Long shortcode = rulesControllerZuul
        .updateSequence(datasetMetabaseService.findDatasetSchemaIdById(datasetId));
    ruleVO.setShortCode("TB" + shortcode);

    rulesControllerZuul.createNewRule(datasetId, ruleVO);
    LOG.info("Created notEmpty rule for TableSchema {}", tableSchemaId);
  }

  /**
   * Delete not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  private void deleteNotEmptyRule(String tableSchemaId, Long datasetId) {
    rulesControllerZuul.deleteNotEmptyRule(tableSchemaId, datasetId);
    LOG.info("Deleted notEmpty rule for TableSchema {}", tableSchemaId);
  }

  /**
   * Update not empty rule.
   *
   * @param oldValue the old value
   * @param newValue the new value
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  private void updateNotEmptyRule(Boolean oldValue, Boolean newValue, String tableSchemaId,
      Long datasetId) {
    if (Boolean.TRUE.equals(oldValue)) {
      if (Boolean.FALSE.equals(newValue)) {
        deleteNotEmptyRule(tableSchemaId, datasetId);
      }
    } else {
      if (Boolean.TRUE.equals(newValue)) {
        createNotEmptyRule(tableSchemaId, datasetId);
      }
    }
  }


  /**
   * Update web form.
   *
   * @param datasetSchemaId the dataset schema id
   * @param webformVO the webform VO
   */
  @Override
  public void updateWebform(String datasetSchemaId, WebformVO webformVO) {
    schemasRepository.updateDatasetSchemaWebForm(datasetSchemaId,
        webFormMapper.classToEntity(webformVO));
  }

  /**
   * Check if field name already exist.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaVO the field schema VO
   * @return true, if successful
   */
  private boolean checkIfFieldNameAlreadyExist(String datasetSchemaId,
      FieldSchemaVO fieldSchemaVO) {
    boolean exist = false;
    Document document = schemasRepository.findRecordSchemaByRecordSchemaId(datasetSchemaId,
        fieldSchemaVO.getIdRecord());
    List<Document> documentListField = null != document && null != document.get(FIELDSCHEMAS)
        ? (List<Document>) document.get(FIELDSCHEMAS)
        : new ArrayList();

    // we found if we have the same name in the record , and check if the name that we found is
    // diferent form himself
    for (Document field : documentListField) {
      if (fieldSchemaVO.getName().equalsIgnoreCase(field.get("headerName").toString())
          && !field.get("_id").toString().equalsIgnoreCase(fieldSchemaVO.getId())) {
        exist = true;
        break;
      }
    }

    return exist;
  }

  /**
   * Release validate manual QC event.
   *
   * @param datasetId the dataset id
   * @param checkNoSQL the check no SQL
   */
  @Override
  public void releaseCreateUpdateView(Long datasetId, String user, boolean checkSQL) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put("isMaterialized", false);
    result.put("checkSQL", checkSQL);
    result.put(LiteralConstants.USER, user);
    kafkaSenderUtils.releaseKafkaEvent(EventType.CREATE_UPDATE_VIEW_EVENT, result);
  }


  @Override
  public byte[] exportSchemas(Long dataflowId) throws IOException {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Map<String, String> schemaNames = new HashMap<>();
    List<DesignDataset> designs = designDatasetRepository.findByDataflowId(dataflowId);
    try (ZipOutputStream zos = new ZipOutputStream(bos)) {
      for (DataSetSchema schema : schemasRepository.findByIdDataFlow(dataflowId)) {

        // Put the datasetSchemaId and the dataset name into a map to store later in the zip file
        DesignDataset design = designs.stream()
            .filter(d -> d.getDatasetSchema().equals(schema.getIdDataSetSchema().toString()))
            .findFirst().orElse(new DesignDataset());
        schemaNames.put(schema.getIdDataSetSchema().toString(), design.getDataSetName());

        ObjectMapper objectMapper = new ObjectMapper();
        String nameFile = "schema_" + schema.getIdDataSetSchema() + ".schema";
        InputStream schemaStream = new ByteArrayInputStream(objectMapper.writeValueAsBytes(schema));
        ZipEntry ze = new ZipEntry(nameFile);
        zos.putNextEntry(ze);
        byte[] bytes = new byte[1024];
        int count = schemaStream.read(bytes);
        while (count > -1) {
          zos.write(bytes, 0, count);
          count = schemaStream.read(bytes);
        }
        schemaStream.close();
      }
      // Store the dataset names
      ObjectMapper objectMapper = new ObjectMapper();
      InputStream schemaNamesStream =
          new ByteArrayInputStream(objectMapper.writeValueAsBytes(schemaNames));
      ZipEntry ze = new ZipEntry("datasetSchemaNames.names");
      zos.putNextEntry(ze);
      byte[] bytes = new byte[1024];
      int count = schemaNamesStream.read(bytes);
      while (count > -1) {
        zos.write(bytes, 0, count);
        count = schemaNamesStream.read(bytes);
      }
      schemaNamesStream.close();


    } catch (Exception e) {
      LOG.error("Error exporting schemas to a ZIP file {}", e.getMessage(), e);
    }
    return bos.toByteArray();
  }


  // @Async
  @Override
  public void importSchemas(Long dataflowId, MultipartFile multipartFile)
      throws IOException, EEAException {

    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    Map<Long, Long> dictionaryOriginTargetDatasetsId = new HashMap<>();
    Map<Long, DataSetSchema> mapDatasetsDestinyAndSchemasOrigin = new HashMap<>();

    try {
      try (InputStream input = multipartFile.getInputStream()) {
        String fileName = multipartFile.getOriginalFilename();
        String multipartFileMimeType = datasetService.getMimetype(fileName);

        if ("zip".equalsIgnoreCase(multipartFileMimeType)) {
          try (ZipInputStream zip = new ZipInputStream(input)) {

            ImportSchemas importClasses = unZip(zip);


            for (DataSetSchema schema : importClasses.getSchemas()) {
              LOG.info("El schema es: {}", schema);


              String newIdDatasetSchema = createEmptyDataSetSchema(dataflowId).toString();
              DataSetSchemaVO targetDatasetSchema = getDataSchemaById(newIdDatasetSchema);
              dictionaryOriginTargetObjectId.put(schema.getIdDataSetSchema().toString(),
                  newIdDatasetSchema);

              final Map<String, String> dictionaryOriginTargetTableObjectId = new HashMap<>();
              targetDatasetSchema.getTableSchemas().forEach(table -> {
                for (TableSchema tableSchema : schema.getTableSchemas()) {
                  if (table.getNameTableSchema().equals(tableSchema.getNameTableSchema())) {
                    dictionaryOriginTargetTableObjectId
                        .put(tableSchema.getIdTableSchema().toString(), table.getIdTableSchema());
                    dictionaryOriginTargetTableObjectId.put(
                        tableSchema.getRecordSchema().getIdRecordSchema().toString(),
                        table.getRecordSchema().getIdRecordSchema());
                  }
                }
              });
              dictionaryOriginTargetObjectId.putAll(dictionaryOriginTargetTableObjectId);

              Future<Long> datasetId =
                  datasetMetabaseService.createEmptyDataset(DatasetTypeEnum.DESIGN,
                      "IMPORTED_" + schemaName(schema.getIdDataSetSchema().toString(),
                          importClasses.getSchemaNames()),
                      newIdDatasetSchema, dataflowId, null, null, 0);



              LOG.info("New dataset created in the import process with id {}", datasetId.get());
              mapDatasetsDestinyAndSchemasOrigin.put(datasetId.get(), schema);
              Thread.sleep(4000);

              // dictionaryOriginTargetDatasetsId.put(design.getId(), datasetId.get());



              // After creating the datasets schemas on the DB, fill them and create the permissions
              for (Map.Entry<Long, DataSetSchema> itemNewDatasetAndSchema : mapDatasetsDestinyAndSchemasOrigin
                  .entrySet()) {
                contributorControllerZuul.createAssociatedPermissions(dataflowId,
                    itemNewDatasetAndSchema.getKey());

                fillAndUpdateDesignDatasetCopied(itemNewDatasetAndSchema.getValue(),
                    dictionaryOriginTargetObjectId
                        .get(itemNewDatasetAndSchema.getValue().getIdDataSetSchema().toString()),
                    dictionaryOriginTargetObjectId, itemNewDatasetAndSchema.getKey());

              }


            }
          }
        }
      }

      // kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_DATASET_SCHEMA_COMPLETED_EVENT,
      // null, NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
      // .dataflowId(dataflowId).build());

    } catch (Exception e) {
      LOG.error("An error in the import process happened. Message: {}", e.getMessage(), e);
    }
  }

  private ImportSchemas unZip(ZipInputStream zip) throws EEAException, IOException {

    List<DataSetSchema> schemas = new ArrayList<>();
    Map<String, String> schemaNames = new HashMap<>();
    for (ZipEntry entry; (entry = zip.getNextEntry()) != null;) {

      String entryName = entry.getName();
      String mimeType = datasetService.getMimetype(entryName);
      if ("schema".equalsIgnoreCase(mimeType)) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = zip.read(buf, 0, 1024)) != -1) {
          output.write(buf, 0, n);
        }
        byte[] content = output.toByteArray();
        DataSetSchema schema = objectMapper.readValue(content, DataSetSchema.class);
        LOG.info("Schema class recovered from zip file");
        schemas.add(schema);
      }
      if ("names".equalsIgnoreCase(mimeType)) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = zip.read(buf, 0, 1024)) != -1) {
          output.write(buf, 0, n);
        }
        byte[] content = output.toByteArray();
        schemaNames = objectMapper.readValue(content, Map.class);
        LOG.info("Schema names recovered from zip file");
      }
    }
    zip.closeEntry();
    zip.close();

    ImportSchemas fileUnziped = new ImportSchemas();
    fileUnziped.setSchemaNames(schemaNames);
    fileUnziped.setSchemas(schemas);
    return fileUnziped;
  }



  private Map<String, String> fillAndUpdateDesignDatasetCopied(DataSetSchema schemaOrigin,
      String newIdDatasetSchema, Map<String, String> dictionaryOriginTargetObjectId, Long datasetId)
      throws EEAException {

    // We've got the new schema created during the copy process. Now using the dictionary we'll
    // replace the objectIds of the schema, because at this moment the new schema has the origin
    // values, so we'll change it to new ObjectIds and finally we'll update it
    DataSetSchema schema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(newIdDatasetSchema));
    schema.setDescription(schemaOrigin.getDescription());
    schema.setWebform(schemaOrigin.getWebform());
    // table level
    for (TableSchema table : schemaOrigin.getTableSchemas()) {
      ObjectId newTableId = new ObjectId();
      dictionaryOriginTargetObjectId.put(table.getIdTableSchema().toString(),
          newTableId.toString());

      table.setIdTableSchema(newTableId);
      // record level
      RecordSchema record = new RecordSchema();
      ObjectId newRecordId = new ObjectId();
      record.setIdRecordSchema(newRecordId);
      if (table.getRecordSchema() != null && table.getRecordSchema().getFieldSchema() != null) {
        dictionaryOriginTargetObjectId.put(table.getRecordSchema().getIdRecordSchema().toString(),
            newRecordId.toString());
        record.setFieldSchema(new ArrayList<>());
        record.setIdTableSchema(newTableId);
        // field level
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          ObjectId newFieldId = new ObjectId();
          dictionaryOriginTargetObjectId.put(field.getIdFieldSchema().toString(),
              newFieldId.toString());
          // FieldSchema field = fieldSchemaNoRulesMapper.classToEntity(fieldVO);
          field.setIdFieldSchema(newFieldId);
          field.setIdRecord(newRecordId);
          // check if the field has referencedField, but the type is no LINK, set the referenced
          // part as null
          if (!DataType.LINK.equals(field.getType()) && null != field.getReferencedField()) {
            field.setReferencedField(null);
          }
          record.getFieldSchema().add(field);

          // if the type is Link we store to later modify the schema id's with the proper fk
          // relations after all the process it's done
          // mapLinkResult(datasetId, mapDatasetIdFKRelations, fieldVO, field);
        }
        table.setRecordSchema(record);
      }
      schema.getTableSchemas().add(table);

      // propagate new table into the datasets schema
      datasetService.saveTablePropagation(datasetId, tableSchemaMapper.entityToClass(table));
    }
    // save the schema with the new values
    schemasRepository.updateSchemaDocument(schema);

    return dictionaryOriginTargetObjectId;
  }

  private String schemaName(String datasetSchemaId, Map<String, String> schemaNames) {
    String name = schemaNames.get(datasetSchemaId);
    return name;
  }

}
