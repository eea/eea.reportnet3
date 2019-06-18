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


  private static String GENERAL_WARNING = "WARNING!";
  private static String VALIDATION_WARNING = "WARNING!,PROBABLY THIS IS NOT CORRECT";
  private static String GENERAL_ERROR = "ERROR!";
  private static String STRING_ERROR = "ERROR!, THIS IS NOT TEXT";
  private static String INTEGER_ERROR = "ERROR!, THIS IS NOT A NUMBER";
  private static String BOOLEAN_ERROR = "ERROR!, THIS IS NOT A TRUE/FALSE VALUE";
  private static String COORDINATE_ERROR = "ERROR!, THIS IS NOT A COORDINATE";

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
    listaStrinsDataset.add(GENERAL_ERROR);
    listaStrinsDataset.add(GENERAL_WARNING);
    ruleDataset.setThenCondition(listaStrinsDataset);

    ruleDataset.setRuleId(new ObjectId());
    ruleDataset.setDataFlowId(1L);
    ruleDataset.setIdDataSetSchema(idDataSetSchema);
    ruleDataset.setScope(TypeEntityEnum.DATASET);
    ruleDataset.setWhenCondition("id == null");
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
      ruleTable.setDataFlowId(1L);
      ruleTable.setIdTableSchema(idTableSchema);
      ruleTable.setWhenCondition("id == null");
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

      RuleRecord ruleRecord = new RuleRecord();
      List<String> listaStrinsRuleRecord = new ArrayList<String>();
      ruleRecord.setRuleId(new ObjectId());
      ruleRecord.setDataFlowId(1L);
      ruleRecord.setScope(TypeEntityEnum.RECORD);
      ruleRecord.setIdRecordSchema(idRecordSchema);
      ruleRecord.setWhenCondition("id == null");
      ruleRecord.setRuleName("RecordRule_" + i);
      listaStrinsRuleRecord.add(VALIDATION_WARNING);
      listaStrinsRuleRecord.add(GENERAL_WARNING);
      ruleRecord.setThenCondition(listaStrinsRuleRecord);
      ruleRecordList.add(ruleRecord);

      // Create fields in the Schema
      List<FieldSchema> fieldSchemas = new ArrayList<>();
      int headersSize = table.getTableHeadersCollections().size();
      for (int j = 1; j <= headersSize; j++) {
        ObjectId idFieldSchema = new ObjectId();
        TableHeadersCollection header = table.getTableHeadersCollections().get(j - 1);

        List<RuleField> ruleField = new ArrayList<RuleField>();
        RuleField rule = new RuleField();
        rule.setRuleId(new ObjectId());
        rule.setDataFlowId(1L);
        rule.setIdFieldSchema(idFieldSchema);
        rule.setWhenCondition("id == null");
        rule.setRuleName("FieldRule_" + i + "." + j);
        List<String> listaMsgValidation = new ArrayList<String>();
        listaMsgValidation.add(VALIDATION_WARNING);
        listaMsgValidation.add(GENERAL_ERROR);
        rule.setThenCondition(listaMsgValidation);
        ruleField.add(rule);
        rule.setScope(TypeEntityEnum.FIELD);

        RuleField rule2 = new RuleField();
        List<String> listaMsgTypeValidation = new ArrayList<String>();
        switch (header.getHeaderType().toLowerCase().trim()) {
          case "string":
            rule2.setRuleId(new ObjectId());
            rule2.setDataFlowId(1L);
            rule2.setIdFieldSchema(idFieldSchema);
            rule2.setWhenCondition("type == null");
            rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
            listaMsgTypeValidation.add(STRING_ERROR);
            listaMsgTypeValidation.add(GENERAL_ERROR);
            rule2.setThenCondition(listaMsgTypeValidation);
            ruleField.add(rule2);
            rule2.setScope(TypeEntityEnum.FIELD);
            break;
          case "integer":
            rule2.setRuleId(new ObjectId());
            rule2.setDataFlowId(1L);
            rule2.setIdFieldSchema(idFieldSchema);
            rule2.setWhenCondition("isValid(value)");
            rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
            listaMsgTypeValidation.add(INTEGER_ERROR);
            listaMsgTypeValidation.add(GENERAL_ERROR);
            rule2.setThenCondition(listaMsgTypeValidation);
            ruleField.add(rule2);
            rule2.setScope(TypeEntityEnum.FIELD);
            break;
          case "boolean":
            rule2.setRuleId(new ObjectId());
            rule2.setDataFlowId(1L);
            rule2.setIdFieldSchema(idFieldSchema);
            rule2.setWhenCondition("value==true || value==false");
            rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
            listaMsgTypeValidation.add(BOOLEAN_ERROR);
            listaMsgTypeValidation.add(GENERAL_ERROR);
            rule2.setThenCondition(listaMsgTypeValidation);
            ruleField.add(rule2);
            rule2.setScope(TypeEntityEnum.FIELD);
            break;
          case "coordinateLat":
            rule2.setRuleId(new ObjectId());
            rule2.setDataFlowId(1L);
            rule2.setIdFieldSchema(idFieldSchema);
            rule2.setWhenCondition("isValid(value) && (value >= -90 && value <= 90)");
            rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
            listaMsgTypeValidation.add(COORDINATE_ERROR);
            listaMsgTypeValidation.add(GENERAL_ERROR);
            rule2.setThenCondition(listaMsgTypeValidation);
            ruleField.add(rule2);
            rule2.setScope(TypeEntityEnum.FIELD);
            break;
          case "coordinateLong":
            rule2.setRuleId(new ObjectId());
            rule2.setDataFlowId(1L);
            rule2.setIdFieldSchema(idFieldSchema);
            rule2.setWhenCondition("isValid(value) && (value >= -180 && value <= 180)");
            rule2.setRuleName("FieldRule_" + i + "." + j + "." + 1);
            listaMsgTypeValidation.add(COORDINATE_ERROR);
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
