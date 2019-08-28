package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSchemaMapper;
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
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
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

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The data set metabase table collection. */
  @Autowired
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  /** The dataschema mapper. */
  @Autowired
  private DataSchemaMapper dataSchemaMapper;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataschemaServiceImpl.class);


  private static final String GENERAL_WARNING = "WARNING";
  private static final String VALIDATION_WARNING = "WARNING!,PROBABLY THIS IS NOT CORRECT";
  private static final String GENERAL_ERROR = "ERROR";
  private static final String STRING_WARNING =
      "WARNING!, THIS TEXT IS LONGER THAN 30 CHARACTERES SHOULD BE MORE SHORT";
  private static final String INTEGER_ERROR = "ERROR!, THIS IS NOT A NUMBER";
  private static final String BOOLEAN_ERROR = "ERROR!, THIS IS NOT A TRUE/FALSE VALUE";
  private static final String COORDINATE_LAT_ERROR = "ERROR!, THIS IS NOT A COORDINATE LAT";
  private static final String COORDINATE_LONG_ERROR = "ERROR!, THIS IS NOT A COORDINATE LONG";
  private static final String DATE_ERROR = "ERROR!, THIS IS NOT A DATE";

  private static final String WARNING = "WARNING";

  /** The Constant NULL. */
  private static final String NULL = "id == null";

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
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
    List<RuleDataSet> ruleDataSetList = new ArrayList<RuleDataSet>();
    RuleDataSet ruleDataset = new RuleDataSet();
    List<String> listaStrinsDataset = new ArrayList<String>();
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


      List<RuleTable> ruleTableList = new ArrayList<RuleTable>();
      RuleTable ruleTable = new RuleTable();
      List<String> listaStrinsRuleTable = new ArrayList<String>();
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
      List<RuleRecord> ruleRecordList = new ArrayList<RuleRecord>();



      // }
      // Create fields in the Schema
      List<FieldSchema> fieldSchemas = new ArrayList<>();
      int headersSize = table.getTableHeadersCollections().size();
      createRuleFields(i, table, recordSchema, fieldSchemas, headersSize, dataflowId);


      RuleRecord ruleRecord = new RuleRecord();
      List<String> listaStrinsRuleRecord = new ArrayList<String>();
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
   */
  private void createRuleFields(int i, TableCollection table, RecordSchema recordSchema,
      List<FieldSchema> fieldSchemas, int headersSize, Long dataflowId) {
    for (int j = 1; j <= headersSize; j++) {
      ObjectId idFieldSchema = new ObjectId();
      TableHeadersCollection header = table.getTableHeadersCollections().get(j - 1);

      List<RuleField> ruleField = new ArrayList<RuleField>();
      RuleField rule = new RuleField();
      rule.setRuleId(new ObjectId());
      rule.setDataFlowId(dataflowId);
      rule.setIdFieldSchema(idFieldSchema);
      rule.setWhenCondition("!isBlank(value)");
      rule.setRuleName("FieldRule_" + i + "." + j);
      List<String> listaMsgValidation = new ArrayList<String>();
      listaMsgValidation.add("that field must be filled");
      listaMsgValidation.add(GENERAL_WARNING);
      rule.setThenCondition(listaMsgValidation);
      ruleField.add(rule);
      rule.setScope(TypeEntityEnum.FIELD);

      RuleField rule2 = new RuleField();
      List<String> listaMsgTypeValidation = new ArrayList<String>();
      switch (header.getHeaderType().toString().toLowerCase().trim()) {
        case "string":
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
          rule2.setWhenCondition("!isValid(value) || value == null");
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
   */
  @Override
  public DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow) {

    DataSetSchema dataschema = schemasRepository.findSchemaByIdFlow(idFlow);
    LOG.info("Schema retrived by idFlow {}", idFlow);
    return dataSchemaMapper.entityToClass(dataschema);

  }

}
