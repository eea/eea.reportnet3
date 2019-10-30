package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.NoRulesDataSchemaMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.domain.TableHeadersCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.rule.RuleDataSet;
import org.eea.dataset.persistence.schemas.domain.rule.RuleField;
import org.eea.dataset.persistence.schemas.domain.rule.RuleRecord;
import org.eea.dataset.persistence.schemas.domain.rule.RuleTable;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.interfaces.vo.ums.enums.ResourceTypeEnum;
import org.eea.interfaces.vo.ums.enums.SecurityRoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

/**
 * The type Dataschema service.
 */
@Service("datachemaService")
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
   * Creates the empty data set schema.
   *
   * @param dataflowId the dataflow id
   * @param datasetSchemaName the dataset schema name
   * @return the object id
   * @throws EEAException the EEA exception
   */
  @Override
  public ObjectId createEmptyDataSetSchema(Long dataflowId, String datasetSchemaName)
      throws EEAException {

    if (dataFlowControllerZuul.findById(dataflowId) == null) {
      throw new EEAException("DataFlow with id " + dataflowId + " not found");
    }

    DataSetSchema dataSetSchema = new DataSetSchema();
    ObjectId idDataSetSchema = new ObjectId();

    dataSetSchema.setNameDataSetSchema(datasetSchemaName);
    dataSetSchema.setIdDataFlow(dataflowId);
    dataSetSchema.setIdDataSetSchema(idDataSetSchema);
    dataSetSchema.setRuleDataSet(new ArrayList<RuleDataSet>());
    dataSetSchema.setTableSchemas(new ArrayList<TableSchema>());

    schemasRepository.save(dataSetSchema);

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
    userManagementControllerZull.addContributorToResource(datasetId,
        ResourceGroupEnum.DATASCHEMA_CUSTODIAN);
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
   * The table mapper.
   */
  @Autowired
  private TableSchemaMapper tableMapper;

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  @Override
  public void createDataSchema(Long datasetId, Long dataflowId) {

    DataSetSchema dataSetSchema = new DataSetSchema();
    Iterable<TableCollection> tables = dataSetMetabaseTableCollection.findAllByDataSetId(datasetId);
    ArrayList<TableCollection> values = Lists.newArrayList(tables);

    List<TableSchema> tableSchemas = new ArrayList<>();

    ObjectId idDataSetSchema = new ObjectId();
    dataSetSchema.setNameDataSetSchema("dataSet_" + datasetId);
    dataSetSchema.setIdDataFlow(dataflowId);
    dataSetSchema.setIdDataSetSchema(idDataSetSchema);
    List<RuleDataSet> ruleDataSetList = new ArrayList<>();
    RuleDataSet ruleDataset = new RuleDataSet();
    List<String> listaStrinsDataset = new ArrayList<>();
    listaStrinsDataset.add(GENERAL_ERROR);
    listaStrinsDataset.add(GENERAL_WARNING);
    ruleDataset.setThenCondition(listaStrinsDataset);

    ruleDataset.setRuleId(new ObjectId());
    ruleDataset.setDataFlowId(dataflowId);
    ruleDataset.setIdDataSetSchema(idDataSetSchema);
    ruleDataset.setScope(TypeEntityEnum.DATASET);
    ruleDataset.setWhenCondition(NULL);
    ruleDataset.setRuleName("dataset regla");
    ruleDataSetList.add(ruleDataset);
    dataSetSchema.setRuleDataSet(ruleDataSetList);

    for (int i = 1; i <= values.size(); i++) {
      ObjectId idTableSchema = new ObjectId();
      TableCollection table = values.get(i - 1);
      TableSchema tableSchema = new TableSchema();
      tableSchema.setIdTableSchema(idTableSchema);

      List<RuleTable> ruleTableList = new ArrayList<>();
      RuleTable ruleTable = new RuleTable();
      List<String> listaStrinsRuleTable = new ArrayList<>();
      listaStrinsRuleTable.add(VALIDATION_WARNING);
      listaStrinsRuleTable.add(GENERAL_ERROR);
      ruleTable.setThenCondition(listaStrinsRuleTable);

      ruleTable.setRuleId(new ObjectId());
      ruleTable.setDataFlowId(dataflowId);
      ruleTable.setIdTableSchema(idTableSchema);
      ruleTable.setWhenCondition(NULL);
      ruleTable.setRuleName("table regla" + i);
      ruleTable.setScope(TypeEntityEnum.TABLE);
      ruleTableList.add(ruleTable);

      tableSchema.setNameTableSchema(table.getTableName());
      ObjectId idRecordSchema = new ObjectId();
      RecordSchema recordSchema = new RecordSchema();
      recordSchema.setIdRecordSchema(idRecordSchema);
      recordSchema.setIdTableSchema(tableSchema.getIdTableSchema());

      // Create Records in the Schema
      List<RuleRecord> ruleRecordList = new ArrayList<>();

      // Create fields in the Schema
      List<FieldSchema> fieldSchemas = new ArrayList<>();
      int headersSize = table.getTableHeadersCollections().size();
      createRuleFields(i, table, recordSchema, fieldSchemas, headersSize, dataflowId);

      RuleRecord ruleRecord = new RuleRecord();
      List<String> listaStrinsRuleRecord = new ArrayList<>();
      ruleRecord.setRuleId(new ObjectId());
      ruleRecord.setDataFlowId(dataflowId);
      ruleRecord.setScope(TypeEntityEnum.RECORD);
      ruleRecord.setIdRecordSchema(idRecordSchema);
      ruleRecord.setWhenCondition("fields.size() != " + fieldSchemas.size());
      ruleRecord.setRuleName("RecordRule_" + i + "_");
      listaStrinsRuleRecord.add("ERROR IN RECORD LEVEL DIFFERENT DATA THAN SCHEMA");
      listaStrinsRuleRecord.add(GENERAL_ERROR);
      ruleRecord.setThenCondition(listaStrinsRuleRecord);
      ruleRecordList.add(ruleRecord);

      recordSchema.setRuleRecord(ruleRecordList);
      recordSchema.setFieldSchema(fieldSchemas);
      tableSchema.setRecordSchema(recordSchema);
      tableSchema.setRuleTable(ruleTableList);
      tableSchemas.add(tableSchema);
    }
    dataSetSchema.setTableSchemas(tableSchemas);
    schemasRepository.save(dataSetSchema);

  }


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
    for (int j = 1; j <= headersSize; j++) {
      ObjectId idFieldSchema = new ObjectId();
      TableHeadersCollection header = table.getTableHeadersCollections().get(j - 1);

      List<RuleField> ruleField = new ArrayList<>();
      RuleField rule = new RuleField();
      rule.setRuleId(new ObjectId());
      rule.setDataFlowId(dataflowId);
      rule.setIdFieldSchema(idFieldSchema);
      rule.setWhenCondition("!isBlank(value)");
      rule.setRuleName("FieldRule_" + i + "." + j);
      List<String> listaMsgValidation = new ArrayList<>();
      listaMsgValidation.add("that field must be filled");
      listaMsgValidation.add(GENERAL_WARNING);
      rule.setThenCondition(listaMsgValidation);
      ruleField.add(rule);
      rule.setScope(TypeEntityEnum.FIELD);

      RuleField rule2 = new RuleField();
      List<String> listaMsgTypeValidation = new ArrayList<>();
      switch (header.getHeaderType().toString().toLowerCase().trim()) {
        case "text":
          rule2.setRuleId(new ObjectId());
          rule2.setDataFlowId(dataflowId);
          rule2.setIdFieldSchema(idFieldSchema);
          rule2.setWhenCondition("isText(value)");
          rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
          listaMsgTypeValidation.add("that text have invalid caracteres");
          listaMsgTypeValidation.add("ERROR");
          rule2.setThenCondition(listaMsgTypeValidation);
          ruleField.add(rule2);
          rule2.setScope(TypeEntityEnum.FIELD);
          break;
        case "number":
          rule2.setRuleId(new ObjectId());
          rule2.setDataFlowId(dataflowId);
          rule2.setIdFieldSchema(idFieldSchema);
          rule2.setWhenCondition("!isValid(value,'') || value == null");
          rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
          listaMsgTypeValidation.add(INTEGER_ERROR);
          listaMsgTypeValidation.add(GENERAL_ERROR);
          rule2.setThenCondition(listaMsgTypeValidation);
          ruleField.add(rule2);
          rule2.setScope(TypeEntityEnum.FIELD);
          break;
        case "boolean":
          rule2.setRuleId(new ObjectId());
          rule2.setDataFlowId(dataflowId);
          rule2.setIdFieldSchema(idFieldSchema);
          rule2.setWhenCondition("value==true || value==false");
          rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
          listaMsgTypeValidation.add(BOOLEAN_ERROR);
          listaMsgTypeValidation.add(GENERAL_ERROR);
          rule2.setThenCondition(listaMsgTypeValidation);
          ruleField.add(rule2);
          rule2.setScope(TypeEntityEnum.FIELD);
          break;
        case "coordinate_lat":
          rule2.setRuleId(new ObjectId());
          rule2.setDataFlowId(dataflowId);
          rule2.setIdFieldSchema(idFieldSchema);
          rule2.setWhenCondition("!isCordenateLat(value)");
          rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
          listaMsgTypeValidation.add(COORDINATE_LAT_ERROR);
          listaMsgTypeValidation.add(GENERAL_ERROR);
          rule2.setThenCondition(listaMsgTypeValidation);
          ruleField.add(rule2);
          rule2.setScope(TypeEntityEnum.FIELD);
          break;
        case "coordinate_long":
          rule2.setRuleId(new ObjectId());
          rule2.setDataFlowId(dataflowId);
          rule2.setIdFieldSchema(idFieldSchema);
          rule2.setWhenCondition("!isCordenateLong(value)");
          rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
          listaMsgTypeValidation.add(COORDINATE_LONG_ERROR);
          listaMsgTypeValidation.add("WARNING");
          rule2.setThenCondition(listaMsgTypeValidation);
          ruleField.add(rule2);
          rule2.setScope(TypeEntityEnum.FIELD);
          break;
        case "date":
          rule2.setRuleId(new ObjectId());
          rule2.setDataFlowId(dataflowId);
          rule2.setIdFieldSchema(idFieldSchema);
          rule2.setWhenCondition("!isDateYYYYMMDD(value)");
          rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
          listaMsgTypeValidation.add(DATE_ERROR);
          listaMsgTypeValidation.add(GENERAL_ERROR);
          rule2.setThenCondition(listaMsgTypeValidation);
          ruleField.add(rule2);
          rule2.setScope(TypeEntityEnum.FIELD);
          break;
      }
      ruleField.add(rule2);
      FieldSchema fieldSchema = new FieldSchema();
      fieldSchema.setIdFieldSchema(idFieldSchema);
      fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
      fieldSchema.setHeaderName(header.getHeaderName());
      fieldSchema.setType(header.getHeaderType());
      fieldSchema.setRuleField(ruleField);

      fieldSchemas.add(fieldSchema);
    }
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
    }

    return dataSchemaVO;
  }

  /**
   * Find the dataschema per idDataFlow
   * 
   * @param idFlow the idDataFlow to look for
   * @throws EEAException
   */
  @Override
  public DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow, Boolean addRules, Long datasetId)
      throws EEAException {

    DataSetMetabase metabase = obtainDatasetMetabase(datasetId);
    DataSetSchema dataschema = schemasRepository.findByIdDataFlowAndIdDataSetSchema(idFlow,
        new ObjectId(metabase.getDatasetSchema()));
    LOG.info("Schema retrived by idFlow {}", idFlow);
    return Boolean.TRUE.equals(addRules) ? dataSchemaMapper.entityToClass(dataschema)
        : noRulesDataSchemaMapper.entityToClass(dataschema);

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
   * Delete table schema.
   *
   * @param idTableSchema the id table schema
   */
  @Override
  @Transactional
  public void deleteTableSchema(String idTableSchema) {
    schemasRepository.deleteTableSchemaById(idTableSchema);
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
   * Update table schema.
   *
   * @param id the id
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateTableSchema(String id, TableSchemaVO tableSchema) throws EEAException {
    DataSetSchema dataset = schemasRepository.findById(new ObjectId(id)).orElse(null);

    if (dataset != null) {
      TableSchema table = getTableSchema(tableSchema.getIdTableSchema(), dataset);
      if (table != null) {
        // set the attributtes of VO
        table.setNameTableSchema(tableSchema.getNameTableSchema());
        table.setIdDataSet(new ObjectId(id));
        table.setIdTableSchema(new ObjectId(tableSchema.getIdTableSchema()));

        schemasRepository.deleteTableSchemaById(tableSchema.getIdTableSchema());
        schemasRepository.insertTableSchema(table, id);
      } else {
        LOG.error(EEAErrorMessage.TABLE_NOT_FOUND);
        throw new EEAException(EEAErrorMessage.TABLE_NOT_FOUND);
      }
    } else {
      LOG.error(EEAErrorMessage.DATASET_NOTFOUND);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
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
   * Creates the table schema.
   *
   * @param id the id
   * @param tableSchema the table schema
   * @param datasetId the dataset id
   */
  @Override
  public void createTableSchema(String id, TableSchemaVO tableSchema, Long datasetId) {
    if (tableSchema.getIdTableSchema() == null) {
      tableSchema.setIdTableSchema(new ObjectId().toString());
    }
    TableSchema table = tableMapper.classToEntity(tableSchema);
    LOG.info("Creating table schema with id {}", tableSchema.getIdTableSchema());
    schemasRepository.insertTableSchema(table, id);
  }

  /**
   * Creates the field schema.
   *
   * @param idTableSchema the id table schema
   * @param fieldSchemaVO the field schema VO
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   */
  @Override
  public void createFieldSchema(String idTableSchema, FieldSchemaVO fieldSchemaVO, Long datasetId)
      throws EEAException {
    // insert a new id for the field
    fieldSchemaVO.setId(new ObjectId().toString());
    DataSetSchema dataset = schemasRepository.findByIdTableSchema(idTableSchema);
    if (dataset != null) {
      // search for the table to insert the field
      TableSchema table = getTableSchema(idTableSchema, dataset);
      if (table != null) {
        if (table.getRecordSchema() == null) {
          // initialize the record as it's the first record we have in the table
          RecordSchema recordSchema = new RecordSchema();
          recordSchema.setIdTableSchema(new ObjectId(idTableSchema));
          recordSchema.setIdRecordSchema(new ObjectId());
          List<FieldSchema> fieldSchemas = new ArrayList<>();
          fieldSchemaVO.setIdRecord(recordSchema.getIdRecordSchema().toString());
          fieldSchemas.add(fieldSchemaNoRulesMapper.classToEntity(fieldSchemaVO));
          recordSchema.setFieldSchema(fieldSchemas);
          table.setRecordSchema(recordSchema);
        } else {
          // insert the field in the record
          RecordSchema recordSchema = table.getRecordSchema();
          fieldSchemaVO.setIdRecord(recordSchema.getIdRecordSchema().toString());
          List<FieldSchema> fieldSchemas = table.getRecordSchema().getFieldSchema();
          fieldSchemas.add(fieldSchemaNoRulesMapper.classToEntity(fieldSchemaVO));
          table.getRecordSchema().setFieldSchema(fieldSchemas);
        }
        // delete old table document
        schemasRepository.deleteTableSchemaById(idTableSchema);
        // insert the new table document with the field
        schemasRepository.insertTableSchema(table, dataset.getIdDataSetSchema().toString());
      } else {
        LOG.error(EEAErrorMessage.TABLE_NOT_FOUND);
        throw new EEAException(EEAErrorMessage.TABLE_NOT_FOUND);
      }
    } else {
      LOG.error(EEAErrorMessage.DATASET_NOTFOUND);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
  }

  /**
   * Delete field schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @return true, if 1 and only 1 fieldSchema has been removed
   */
  @Override
  public boolean deleteFieldSchema(String datasetSchemaId, String fieldSchemaId) {
    return schemasRepository.deleteFieldSchema(datasetSchemaId, fieldSchemaId)
        .getModifiedCount() == 1;
  }
}
