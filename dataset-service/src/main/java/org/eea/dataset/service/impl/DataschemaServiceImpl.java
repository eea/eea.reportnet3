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

  /** The Constant VALIDATE_ERROR. */
  private static final String VALIDATE_ERROR = "Validation Error";

  /** The Constant ERROR. */
  private static final String ERROR = "ERROR";

  /** The Constant VALIDATE_WARNING. */
  private static final String VALIDATE_WARNING = "Validation Warning";

  private static final String WARNING = "WARNING";

  /** The Constant NULL. */
  private static final String NULL = "id == null";

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void createDataSchema(Long datasetId) {

    DataSetSchema dataSetSchema = new DataSetSchema();
    Iterable<TableCollection> tables = dataSetMetabaseTableCollection.findAllByDataSetId(datasetId);
    ArrayList<TableCollection> values = Lists.newArrayList(tables);

    List<TableSchema> tableSchemas = new ArrayList<>();

    ObjectId idDataSetSchema = new ObjectId();
    dataSetSchema.setNameDataSetSchema("dataSet_" + datasetId);
    dataSetSchema.setIdDataFlow(1L);
    dataSetSchema.setIdDataSetSchema(idDataSetSchema);
    List<RuleDataSet> ruleDataSetList = new ArrayList<RuleDataSet>();
    RuleDataSet ruleDataset = new RuleDataSet();
    List<String> listaStrinsDataset = new ArrayList<String>();
    listaStrinsDataset.add(VALIDATE_ERROR);
    listaStrinsDataset.add(ERROR);
    ruleDataset.setThenCondition(listaStrinsDataset);

    ruleDataset.setRuleId(new ObjectId());
    ruleDataset.setDataFlowId(1L);
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
      if (i % 2 == 0) {
        listaStrinsRuleTable.add(VALIDATE_ERROR);
        listaStrinsRuleTable.add(ERROR);
      } else {
        listaStrinsRuleTable.add(VALIDATE_WARNING);
        listaStrinsRuleTable.add(WARNING);
      }
      ruleTable.setThenCondition(listaStrinsRuleTable);

      ruleTable.setRuleId(new ObjectId());
      ruleTable.setDataFlowId(1L);
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

      List<RuleRecord> ruleRecordList = new ArrayList<RuleRecord>();
      RuleRecord ruleRecord = new RuleRecord();
      List<String> listaStrinsRuleRecord = new ArrayList<String>();
      if (i % 2 == 0) {
        listaStrinsRuleRecord.add(VALIDATE_ERROR);
        listaStrinsRuleRecord.add(ERROR);
      } else {
        listaStrinsRuleRecord.add(VALIDATE_WARNING);
        listaStrinsRuleRecord.add(WARNING);
      }
      ruleRecord.setThenCondition(listaStrinsRuleRecord);

      ruleRecord.setRuleId(new ObjectId());
      ruleRecord.setDataFlowId(1L);
      ruleRecord.setScope(TypeEntityEnum.RECORD);
      ruleRecord.setIdRecordSchema(idRecordSchema);
      ruleRecord.setWhenCondition(NULL);
      ruleRecord.setRuleName("record regla" + i);
      ruleRecordList.add(ruleRecord);


      List<FieldSchema> fieldSchemas = new ArrayList<>();

      int headersSize = table.getTableHeadersCollections().size();
      createRuleFields(i, table, recordSchema, fieldSchemas, headersSize);

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
      List<FieldSchema> fieldSchemas, int headersSize) {
    for (int j = 1; j <= headersSize; j++) {
      ObjectId idFieldSchema = new ObjectId();
      TableHeadersCollection header = table.getTableHeadersCollections().get(j - 1);

      List<RuleField> ruleField = new ArrayList<RuleField>();
      RuleField rule = new RuleField();
      rule.setRuleId(new ObjectId());
      rule.setDataFlowId(1L);
      rule.setIdFieldSchema(idFieldSchema);
      rule.setWhenCondition(NULL);
      rule.setRuleName("field regla" + i + " y " + j);
      List<String> listaStrins = new ArrayList<String>();
      if (j % 2 == 0) {
        listaStrins.add(VALIDATE_ERROR);
        listaStrins.add(ERROR);
      } else {
        listaStrins.add(VALIDATE_WARNING);
        listaStrins.add(WARNING);
      }
      rule.setThenCondition(listaStrins);
      ruleField.add(rule);
      rule.setScope(TypeEntityEnum.FIELD);

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
