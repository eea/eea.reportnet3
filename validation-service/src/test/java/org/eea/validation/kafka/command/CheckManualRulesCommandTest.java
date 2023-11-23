package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.notification.factory.NotificableEventFactory;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RuleExpressionService;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.impl.RulesServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class CheckManualRulesCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckManualRulesCommandTest {
  /**
   * The Check manual rules command.
   */
  @InjectMocks
  private CheckManualRulesCommand CheckManualRulesCommand;

  @Mock
  private RulesServiceImpl rulesService;
  /**
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The dataset metabase controller.
   */
  @Mock
  private DatasetMetabaseController datasetMetabaseController;

  /**
   * The rules repository.
   */
  @Mock
  private RulesRepository rulesRepository;

  /**
   * The schemas repository.
   */
  @Mock
  private SchemasRepository schemasRepository;

  /**
   * The rule expression service.
   */
  @Mock
  private RuleExpressionService ruleExpressionService;

  /**
   * The dataset schema controller.
   */
  @Mock
  private DatasetSchemaController datasetSchemaController;

  /**
   * The dataset repository.
   */
  @Mock
  private DatasetRepository datasetRepository;

  /**
   * The security context.
   */
  private SecurityContext securityContext;

  /**
   * The authentication.
   */
  private Authentication authentication;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    MockitoAnnotations.openMocks(this);
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", Boolean.TRUE);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence(null);
    rule1.setType(EntityTypeEnum.FIELD);
    rule1.setReferenceId(new ObjectId());
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document fieldSchema = new Document();
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);

    List<Rule> rulesSQL = new ArrayList<>();

    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute record test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeRecordTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", Boolean.TRUE);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence(null);
    rule1.setType(EntityTypeEnum.RECORD);
    rule1.setReferenceId(new ObjectId());
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rule1.setWhenCondition("when");
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document recordSchema = new Document();
    Document fieldSchema = new Document();
    fieldSchema.put("_id", "5cf0e9b3b793310e9ceca190");
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);
    List<Document> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema);
    recordSchema.put("fieldSchemas", fieldSchemas);

    List<Rule> rulesSQL = new ArrayList<>();

    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);


    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute default dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeDefaultDatasetTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", Boolean.TRUE);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence(null);
    rule1.setType(EntityTypeEnum.DATASET);
    rule1.setReferenceId(new ObjectId());
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rule1.setWhenCondition("when");
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document recordSchema = new Document();
    Document fieldSchema = new Document();
    fieldSchema.put("_id", "5cf0e9b3b793310e9ceca190");
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);
    List<Document> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema);
    recordSchema.put("fieldSchemas", fieldSchemas);

    List<Rule> rulesSQL = new ArrayList<>();

    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute table test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeTableTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", Boolean.TRUE);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence(null);
    rule1.setType(EntityTypeEnum.TABLE);
    rule1.setReferenceId(new ObjectId());
    rule1.setVerified(Boolean.TRUE);
    rule1.setEnabled(Boolean.FALSE);
    rule1.setWhenCondition("when");
    rule1.setThenCondition(Arrays.asList("when", "woah"));
    rule1.setRuleId(new ObjectId());
    rules.add(rule1);
    ruleSchema.setRules(rules);

    Document recordSchema = new Document();
    Document fieldSchema = new Document();
    fieldSchema.put("_id", "5cf0e9b3b793310e9ceca190");
    fieldSchema.put("typeData", DataType.NUMBER_INTEGER);
    List<Document> fieldSchemas = new ArrayList<>();
    fieldSchemas.add(fieldSchema);
    recordSchema.put("fieldSchemas", fieldSchemas);

    List<Rule> rulesSQL = new ArrayList<>();

    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute SQL test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeSQLTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", false);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

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

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute SQL field test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeSQLFieldTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", false);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence("sentence");
    rule1.setType(EntityTypeEnum.FIELD);
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

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute SQL record test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeSQLRecordTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", false);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence("sentence");
    rule1.setType(EntityTypeEnum.RECORD);
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

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Execute SQL query error test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void allQcRuleValidation_executeSQLQueryErrorTest() throws EEAException {

    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", false);

    String schema = new ObjectId().toString();
    DataSetMetabaseVO dsMetabaseVO = new DataSetMetabaseVO();
    dsMetabaseVO.setDataflowId(1L);

    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule1 = new Rule();
    rule1.setAutomatic(Boolean.FALSE);
    rule1.setSqlSentence("delete");
    rule1.setType(EntityTypeEnum.RECORD);
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

    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.VALIDATE_MANUAL_QC_COMMAND, CheckManualRulesCommand.getEventType());
  }

}
