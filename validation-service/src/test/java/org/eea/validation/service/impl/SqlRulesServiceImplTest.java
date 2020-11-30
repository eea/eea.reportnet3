package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.controller.dataset.DataCollectionController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.dataset.EUDatasetController;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SqlRulesServiceImplTest {

  @Mock
  private DatasetRepository datasetRepository;

  @Mock
  private DatasetSchemaController datasetSchemaController;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private RuleMapper ruleMapper;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private DatasetMetabaseController datasetMetabaseController;

  @Mock
  private DataCollectionController dataCollectionController;

  @Mock
  private RepresentativeController representativeController;

  @Mock
  private EUDatasetController euDatasetController;

  @InjectMocks
  private SqlRulesServiceImpl sqlRulesServiceImpl;

  private Long datasetId;

  private String datasetSchemaId;

  private Rule rule;

  private RuleVO ruleVO;

  private DataSetSchemaVO schema;

  @Before
  public void initMocks() {
    datasetId = 1L;
    datasetSchemaId = new ObjectId().toString();

    rule = new Rule();

    schema = new DataSetSchemaVO();
    schema.setIdDataSetSchema("test");
    List<TableSchemaVO> tables = new ArrayList<>();
    TableSchemaVO table = new TableSchemaVO();
    table.setIdTableSchema("test");
    RecordSchemaVO record = new RecordSchemaVO();
    record.setIdRecordSchema("test");
    List<FieldSchemaVO> fields = new ArrayList<>();
    FieldSchemaVO field = new FieldSchemaVO();
    field.setId("test");
    field.setName("test");
    fields.add(field);
    record.setFieldSchema(fields);
    table.setRecordSchema(record);
    tables.add(table);
    schema.setTableSchemas(tables);

    ruleVO = new RuleVO();
    ruleVO.setSqlSentence("SELECT * from dataset_1.table_value;");

  }

  @Test
  public void testValidateSQLRuleField() throws Exception {
    rule.setSqlSentence("SELECT * from dataset_1.table_value");
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    String auxId = new ObjectId().toString();
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.EUDATASET);
    Mockito.when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(schema);
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(auxId);
    Mockito.when(datasetRepository.queryRSExecution(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyLong(), Mockito.anyLong())).thenReturn(null);
    Mockito.when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(schema);
    Mockito.when(datasetRepository.getTableId(Mockito.any(), Mockito.anyLong())).thenReturn(1L);

    List<EUDatasetVO> euDatasetList = new ArrayList<>();
    EUDatasetVO euDataset = new EUDatasetVO();
    euDataset.setId(1L);
    euDataset.setDatasetSchema(auxId);
    euDatasetList.add(euDataset);

    Mockito.when(euDatasetController.findEUDatasetByDataflowId(Mockito.anyLong()))
        .thenReturn(euDatasetList);

    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  @Test
  public void testValidateSQLRuleRecord() throws Exception {
    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.RECORD);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong())).thenReturn(schema);
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  @Test
  public void testValidateSQLRuleTable() throws Exception {
    TableValue tableValue = new TableValue();
    List<RecordValue> recordsValue = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    recordValue.setId("test");
    recordsValue.add(recordValue);
    tableValue.setRecords(recordsValue);

    List<FieldValidation> fieldsValidation = new ArrayList<>();
    FieldValidation fieldV = new FieldValidation();
    FieldValue fieldValue = new FieldValue();
    fieldValue.setId("test");
    fieldV.setFieldValue(fieldValue);
    fieldsValidation.add(fieldV);

    List<RecordValidation> recordsValidation = new ArrayList<>();
    RecordValidation recordV = new RecordValidation();
    recordV.setRecordValue(recordValue);
    recordsValidation.add(recordV);

    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(schema);

    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }


  @Test
  public void testValidateSQLRuleBadSintaxt() throws Exception {
    rule.setSqlSentence("INSERT * from dataset_1.table_value");
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.EUDATASET);
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }


  @Test
  public void testValidateSQLRuleNotPassed() throws Exception {
    rule = new Rule();
    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.DESIGN);
    Mockito.when(datasetRepository.queryRSExecution(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.anyLong(), Mockito.anyLong())).thenReturn(null);
    Mockito.when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(schema);
    Mockito.when(datasetRepository.getTableId(Mockito.any(), Mockito.anyLong())).thenReturn(1L);

    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  @Test
  public void testValidateSQLRuleFromDatacollectionOK() throws Exception {

    Validation validation = new Validation();
    validation.setLevelError(ErrorTypeEnum.ERROR);

    List<FieldValidation> fieldsValidation = new ArrayList<>();
    FieldValidation fieldV = new FieldValidation();
    fieldV.setValidation(validation);
    FieldValue fieldValue = new FieldValue();
    fieldValue.setId("test");
    fieldV.setFieldValue(fieldValue);
    fieldsValidation.add(fieldV);

    TableValue tableValue = new TableValue();
    List<RecordValue> recordsValue = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    List<FieldValue> fieldsValue = new ArrayList<>();
    fieldsValue.add(fieldValue);
    recordValue.setFields(fieldsValue);
    recordValue.setId("test");
    recordsValue.add(recordValue);

    tableValue.setRecords(recordsValue);

    String auxId = new ObjectId().toString();
    schema.setIdDataSetSchema(auxId);

    List<RecordValidation> recordsValidation = new ArrayList<>();

    RecordValidation recordV = new RecordValidation();
    recordV.setValidation(validation);
    recordV.setRecordValue(recordValue);
    recordsValidation.add(recordV);

    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setRuleId(new ObjectId());
    rule.setReferenceId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    Mockito.when(dataCollectionController.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList());
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(auxId);
    Mockito.when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(schema);

    Mockito.when(ruleMapper.classToEntity(ruleVO)).thenReturn(rule);

    List<DataCollectionVO> reportingDatasetList = new ArrayList<>();
    DataCollectionVO reportingDataset = new DataCollectionVO();
    reportingDataset.setId(1L);
    reportingDataset.setDatasetSchema(auxId);
    reportingDatasetList.add(reportingDataset);

    Mockito.when(dataCollectionController.findDataCollectionIdByDataflowId(Mockito.anyLong()))
        .thenReturn(reportingDatasetList);


    sqlRulesServiceImpl.validateSQLRuleFromDatacollection(datasetId, datasetSchemaId, ruleVO);

    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void testValidateSQLRuleFromDatacollectionKO() throws Exception {
    rule.setSqlSentence("INSERT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());
    String auxId = new ObjectId().toString();
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.getType(Mockito.anyLong()))
        .thenReturn(DatasetTypeEnum.COLLECTION);
    Mockito.when(dataCollectionController.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList());
    Mockito.when(ruleMapper.classToEntity(ruleVO)).thenReturn(rule);
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(auxId);
    Mockito.when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong()))
        .thenReturn(schema);
    List<DataCollectionVO> reportingDatasetList = new ArrayList<>();
    DataCollectionVO reportingDataset = new DataCollectionVO();
    reportingDataset.setId(1L);
    reportingDataset.setDatasetSchema(auxId);
    reportingDatasetList.add(reportingDataset);

    Mockito.when(dataCollectionController.findDataCollectionIdByDataflowId(Mockito.anyLong()))
        .thenReturn(reportingDatasetList);

    sqlRulesServiceImpl.validateSQLRuleFromDatacollection(datasetId, datasetSchemaId, ruleVO);

    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }


  @Test
  public void testGetRule() throws Exception {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    ObjectId idRule = new ObjectId();
    rule.setRuleId(idRule);
    rules.add(rule);
    ruleSchema.setRules(rules);

    when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(new ObjectId().toString());
    when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(ruleSchema);

    assertEquals(rule, (sqlRulesServiceImpl.getRule(1L, idRule.toString())));

  }

  @Test
  public void testRetrieveTableData() throws Exception {

    Validation validation = new Validation();
    validation.setLevelError(ErrorTypeEnum.ERROR);

    List<FieldValidation> fieldsValidation = new ArrayList<>();
    FieldValidation fieldV = new FieldValidation();
    fieldV.setValidation(validation);
    FieldValue fieldValue = new FieldValue();
    fieldValue.setId("test");
    fieldV.setFieldValue(fieldValue);
    fieldsValidation.add(fieldV);

    TableValue tableValue = new TableValue();
    List<RecordValue> recordsValue = new ArrayList<>();
    RecordValue recordValue = new RecordValue();
    List<FieldValue> fieldsValue = new ArrayList<>();
    fieldsValue.add(fieldValue);
    recordValue.setFields(fieldsValue);
    recordValue.setId("test");
    recordsValue.add(recordValue);

    tableValue.setRecords(recordsValue);


    List<RecordValidation> recordsValidation = new ArrayList<>();

    RecordValidation recordV = new RecordValidation();
    recordV.setValidation(validation);
    recordV.setRecordValue(recordValue);
    recordsValidation.add(recordV);

    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(new ObjectId());


    when(datasetSchemaController.findDataSchemaByDatasetId(Mockito.anyLong())).thenReturn(schema);
    when(datasetRepository.getTableId(Mockito.any(), Mockito.any())).thenReturn(1L);

    when(datasetRepository.queryRSExecution(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(tableValue);

    sqlRulesServiceImpl.retrieveTableData("", 1L, rule, Boolean.FALSE);

    Mockito.verify(datasetRepository, times(1)).queryRSExecution(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());

  }

}
