package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DataCollectionController.DataCollectionControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.dataset.EUDatasetController.EUDatasetControllerZuul;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController.ReferenceDatasetControllerZuul;
import org.eea.interfaces.vo.dataset.DataCollectionVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.EUDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.exception.EEAForbiddenSQLCommandException;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class SqlRulesServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class SqlRulesServiceImplTest {

  /** The dataset repository. */
  @Mock
  private DatasetRepository datasetRepository;

  /** The dataset schema controller. */
  @Mock
  private DatasetSchemaControllerZuul datasetSchemaController;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The rule mapper. */
  @Mock
  private RuleMapper ruleMapper;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /** The dataset metabase controller. */
  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  /** The data collection controller. */
  @Mock
  private DataCollectionControllerZuul dataCollectionController;

  /** The representative controller. */
  @Mock
  private RepresentativeControllerZuul representativeController;

  /** The eu dataset controller. */
  @Mock
  private EUDatasetControllerZuul euDatasetController;


  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private ReferenceDatasetControllerZuul referenceDatasetController;

  /** The dataset schema controller zuul. */
  @Mock
  private DatasetSchemaControllerZuul datasetSchemaControllerZuul;



  /** The sql rules service impl. */
  @InjectMocks
  private SqlRulesServiceImpl sqlRulesServiceImpl;

  /** The dataset id. */
  private Long datasetId;

  /** The dataset schema id. */
  private String datasetSchemaId;

  /** The rule. */
  private Rule rule;

  /** The rule VO. */
  private RuleVO ruleVO;

  /** The schema. */
  private DataSetSchemaVO schema;

  /** The json parser. */
  private JSONParser jsonParser;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;

  /**
   * Inits the mocks.
   */
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


    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  /**
   * Test validate SQL rule field.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateSQLRuleField() throws Exception {
    rule.setSqlSentence("SELECT * from dataset_1.table_value");
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    String auxId = new ObjectId().toString();
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(auxId);

    List<EUDatasetVO> euDatasetList = new ArrayList<>();
    EUDatasetVO euDataset = new EUDatasetVO();
    euDataset.setId(1L);
    euDataset.setDatasetSchema(auxId);
    euDatasetList.add(euDataset);

    Mockito.when(euDatasetController.findEUDatasetByDataflowId(Mockito.anyLong()))
        .thenReturn(euDatasetList);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  /**
   * Test validate SQL rule record.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateSQLRuleRecord() throws Exception {
    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.RECORD);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }

  /**
   * Test validate SQL rule table.
   *
   * @throws Exception the exception
   */
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
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REPORTING);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }


  /**
   * Test validate SQL rule bad sintaxt.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateSQLRuleBadSintaxt() throws Exception {
    rule.setSqlSentence("INSERT * from dataset_1.table_value");
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());

  }


  /**
   * Test validate SQL rule not passed.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateSQLRuleNotPassed() throws Exception {
    rule = new Rule();
    rule.setSqlSentence("SELECT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());

    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.DESIGN);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    sqlRulesServiceImpl.validateSQLRule(datasetId, datasetSchemaId, rule);

    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }


  /**
   * Test validate SQL rule from datacollection OK.
   *
   * @throws Exception the exception
   */
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
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.COLLECTION);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(dataCollectionController.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(auxId);

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

  /**
   * Test validate SQL rule from datacollection KO.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateSQLRuleFromDatacollectionKO() throws Exception {
    rule.setSqlSentence("INSERT * from dataset_1.table_value;");
    ruleVO.setSqlSentence("INSERT * from dataset_1.table_value;");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());
    String auxId = new ObjectId().toString();
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.COLLECTION);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(dataCollectionController.findDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(auxId);
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

  /**
   * Test get rule.
   *
   * @throws Exception the exception
   */
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

  /**
   * Test retrieve table data.
   *
   * @throws Exception the exception
   */
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
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDatasetSchema("5ce524fad31fc52540abae73");
    when(datasetRepository.getTableId(Mockito.any(), Mockito.any())).thenReturn(1L);

    when(datasetRepository.queryRSExecution(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(tableValue);
    DataSetSchema datasetSchema = new DataSetSchema();
    datasetSchema.setTableSchemas(new ArrayList<>());
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    sqlRulesServiceImpl.retrieveTableData("", dataset, rule, Boolean.FALSE);

    Mockito.verify(datasetRepository, times(1)).queryRSExecution(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());

  }

  /**
   * Test retrieve table dataset data.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRetrieveTableDatasetData() throws Exception {

    Validation validation = new Validation();
    validation.setLevelError(ErrorTypeEnum.ERROR);

    List<FieldValidation> fieldsValidations = new ArrayList<>();
    FieldValidation fieldV = new FieldValidation();
    fieldV.setValidation(validation);
    FieldValue fieldValue = new FieldValue();
    fieldValue.setId("test");
    fieldV.setFieldValue(fieldValue);
    fieldsValidations.add(fieldV);

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
    rule.setType(EntityTypeEnum.DATASET);
    rule.setReferenceId(new ObjectId());

    when(datasetRepository.queryRSExecution(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(tableValue);

    when(datasetRepository.queryFieldValidationExecution(Mockito.anyString()))
        .thenReturn(fieldsValidations);
    when(datasetRepository.queryRecordValidationExecution(Mockito.anyString()))
        .thenReturn(recordsValidation);
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(1L);
    dataset.setDatasetSchema("5ce524fad31fc52540abae73");
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(new DataSetSchema());
    sqlRulesServiceImpl.retrieveTableData("", dataset, rule, Boolean.FALSE);

    Mockito.verify(datasetRepository, times(1)).queryRSExecution(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any());

  }

  /**
   * Validate SQL rules test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void validateSQLRulesTest() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", false);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);
    dsMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.COLLECTION);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence("sentence");
    rule1.setType(EntityTypeEnum.TABLE);
    rule1.setReferenceId(new ObjectId("5ce524fad31fc52540abae73"));
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rule1.setWhenCondition("when");
    rule1.setThenCondition(Arrays.asList("when", "woah"));
    rule1.setRuleId(new ObjectId());
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document recordSchema = new Document();
    Document fieldSchema = new Document();
    fieldSchema.put("_id", "5ce524fad31fc52540abae73");
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);
    List<Document> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema);
    recordSchema.put("fieldSchemas", fieldSchemas);

    List<Rule> rulesSQL = new ArrayList<>();
    rulesSQL.add(rule1);
    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);

    DataSetSchemaVO schemaVO = new DataSetSchemaVO();
    schemaVO.setIdDataSetSchema("5ce524fad31fc52540abae73");
    schemaVO.setNameDatasetSchema("test");
    TableSchemaVO tableVO = new TableSchemaVO();
    RecordSchemaVO recordVO = new RecordSchemaVO();
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setPkReferenced(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    recordVO.setIdRecordSchema("5ce524fad31fc52540abae73");
    tableVO.setRecordSchema(recordVO);
    tableVO.setIdTableSchema("5ce524fad31fc52540abae73");
    tableVO.setNameTableSchema("");
    schemaVO.setTableSchemas(Arrays.asList(tableVO));

    Mockito.when(ruleMapper.entityListToClass(Mockito.any())).thenReturn(Arrays.asList(ruleVO));
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule1);
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(schema);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dsMetabaseVO);

    Mockito.when(rulesRepository.findSqlRules(Mockito.any())).thenReturn(rulesSQL);

    Mockito.when(rulesRepository.getAllDisabledRules(Mockito.any())).thenReturn(ruleSchema);
    Mockito.when(rulesRepository.getAllUncheckedRules(Mockito.any())).thenReturn(ruleSchema);

    sqlRulesServiceImpl.validateSQLRules(1L, "5ce524fad31fc52540abae73", true);
    Mockito.verify(rulesRepository, Mockito.times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void validateSQLRulesCompletedEventTest() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", false);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.COLLECTION);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence("sentence");
    rule1.setType(EntityTypeEnum.TABLE);
    rule1.setReferenceId(new ObjectId("5ce524fad31fc52540abae73"));
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rule1.setWhenCondition("when");
    rule1.setThenCondition(Arrays.asList("when", "woah"));
    rule1.setRuleId(new ObjectId());
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document recordSchema = new Document();
    Document fieldSchema = new Document();
    fieldSchema.put("_id", "5ce524fad31fc52540abae73");
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);
    List<Document> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema);
    recordSchema.put("fieldSchemas", fieldSchemas);

    List<Rule> rulesSQL = new ArrayList<>();
    rulesSQL.add(rule1);
    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);

    DataSetSchemaVO schemaVO = new DataSetSchemaVO();
    schemaVO.setIdDataSetSchema("5ce524fad31fc52540abae73");
    schemaVO.setNameDatasetSchema("test");
    TableSchemaVO tableVO = new TableSchemaVO();
    RecordSchemaVO recordVO = new RecordSchemaVO();
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setPkReferenced(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    recordVO.setIdRecordSchema("5ce524fad31fc52540abae73");
    tableVO.setRecordSchema(recordVO);
    tableVO.setIdTableSchema("5ce524fad31fc52540abae73");
    tableVO.setNameTableSchema("");
    schemaVO.setTableSchemas(Arrays.asList(tableVO));

    Mockito.when(ruleMapper.entityListToClass(Mockito.any())).thenReturn(Arrays.asList(ruleVO));
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule1);
    Mockito.when(datasetMetabaseController.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(schema);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(rulesRepository.findSqlRules(Mockito.any())).thenReturn(rulesSQL);

    ruleSchema.setRules(new ArrayList<>());
    Mockito.when(rulesRepository.getAllDisabledRules(Mockito.any())).thenReturn(ruleSchema);
    Mockito.when(rulesRepository.getAllUncheckedRules(Mockito.any())).thenReturn(ruleSchema);

    sqlRulesServiceImpl.validateSQLRules(1L, "5ce524fad31fc52540abae73", true);
    Mockito.verify(rulesRepository, Mockito.times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void runSQLRuleTest() throws EEAException {

    String sqlRule = "SELECT * from dataset_1.table_value";
    List<String> datasetIds = new ArrayList<>();
    List<String> fields = new ArrayList<>();


    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    DataSetSchemaVO datasetSchemaVO = new DataSetSchemaVO();
    datasetSchemaVO.setIdDataSetSchema("dsId");
    datasetSchemaVO.setTableSchemas(new ArrayList<>());


    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(datasetSchemaControllerZuul.findDataSchemaByDatasetId(1L))
        .thenReturn(datasetSchemaVO);

    sqlRulesServiceImpl.runSqlRule(1L, sqlRule, false);

    Mockito.verify(datasetRepository, Mockito.times(1)).runSqlRule(1L,
        "WITH  SELECT * FROM (SELECT * from table_value) as userSelect OFFSET 0 LIMIT 10");

    sqlRulesServiceImpl.runSqlRule(1L, sqlRule, true);

    Mockito.verify(datasetRepository, Mockito.times(1)).runSqlRule(1L,
        "WITH  SELECT * FROM (SELECT * from table_value) as userSelect OFFSET 0 LIMIT 10");
  }

  @Test(expected = EEAInvalidSQLException.class)
  public void runSQLRuleInvalidSQLExceptionTest() throws EEAException {

    String sqlRule = "WRONG SQL RULE";
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);
    Mockito.when(sqlRulesServiceImpl.runSqlRule(datasetId, sqlRule, false))
        .thenThrow(new EEAInvalidSQLException());

    try {
      sqlRulesServiceImpl.runSqlRule(1L, sqlRule, false);
    } catch (EEAInvalidSQLException e) {
      assertEquals("Couldn't execute the SQL Rule: " + sqlRule, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAForbiddenSQLCommandException.class)
  public void runSQLRuleForbiddenSQLCommandExceptionTest() throws EEAException {

    String sqlRule = "DELETE * from dataset_111.table_value WHERE VALUE > 5";
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    try {
      sqlRulesServiceImpl.runSqlRule(1L, sqlRule, false);
    } catch (EEAForbiddenSQLCommandException e) {
      assertEquals("SQL Command not allowed in SQL Rule: " + sqlRule, e.getMessage());
      throw e;
    }
  }

  @Test
  public void evaluateSQLRuleTest() throws EEAException, ParseException {

    String sqlRule = "SELECT * FROM dataset_1.table_value";
    JSONArray jsonArray = new JSONArray();
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);

    datasetRepository.evaluateSqlRule(1L, "EXPLAIN (FORMAT JSON) " + sqlRule);

    Mockito.verify(datasetRepository, Mockito.times(1)).evaluateSqlRule(1L,
        "EXPLAIN (FORMAT JSON) SELECT * FROM dataset_1.table_value");
  }

  @Test(expected = EEAForbiddenSQLCommandException.class)
  public void evaluateSQLEEAForbiddenSQLCommandExceptionTest() throws EEAException, ParseException {

    String sqlRule = "DELETE * from dataset_1";
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(datasetMetabaseVO);

    try {
      sqlRulesServiceImpl.evaluateSqlRule(1L, sqlRule);
    } catch (EEAForbiddenSQLCommandException e) {
      assertEquals("SQL Command not allowed in SQL Rule: " + sqlRule, e.getMessage());
      throw e;
    }
  }

  @Test(expected = EEAInvalidSQLException.class)
  public void evaluateSQLEEAInvalidSQLExceptionTest() throws EEAException, ParseException {

    String sqlRule = "SELECT ME AS";
    DataSetMetabaseVO datasetMetabaseVO = new DataSetMetabaseVO();
    datasetMetabaseVO.setDataflowId(1L);
    datasetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.EUDATASET);
    Mockito.when(datasetRepository.evaluateSqlRule(1L, sqlRule))
        .thenThrow(new EEAInvalidSQLException());

    try {
      datasetRepository.evaluateSqlRule(1L, sqlRule);
    } catch (EEAInvalidSQLException e) {
      throw e;
    }
  }
}
