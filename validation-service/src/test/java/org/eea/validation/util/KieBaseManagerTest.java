package org.eea.validation.util;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.ReferencedFieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RuleExpressionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class KieBaseManagerTest {

  @InjectMocks
  private KieBaseManager kieBaseManager;

  @Mock
  private RulesRepository rulesRepository;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private DatasetMetabaseController datasetMetabaseController;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private RuleExpressionService ruleExpressionService;

  private DataSetSchema datasetSchema;
  private ObjectId id;
  private FieldSchema fieldSchema;
  private SecurityContext securityContext;

  private Authentication authentication;

  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
    id = new ObjectId();
    datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemas = new ArrayList<>();
    List<FieldSchema> fieldSchemas = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    RecordSchema recordSchema = new RecordSchema();
    fieldSchema = new FieldSchema();
    ReferencedFieldSchema referencedFieldSchema = new ReferencedFieldSchema();
    referencedFieldSchema.setIdPk(id);
    fieldSchema.setIdFieldSchema(id);
    fieldSchema.setReferencedField(referencedFieldSchema);
    fieldSchema.setPkMustBeUsed(Boolean.TRUE);
    fieldSchemas.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemas);
    recordSchema.setIdRecordSchema(id);
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setIdTableSchema(id);
    tableSchemas.add(tableSchema);
    datasetSchema.setTableSchemas(tableSchemas);
  }

  @Test
  public void testReloadRulesDataset() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.DATASET);
    rule.setReferenceId(new ObjectId());
    rule.setRuleId(new ObjectId());
    rule.setThenCondition(thenCondition);
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    rules.add(rule);
    rule.setShortCode("123");
    schemaRules.setRules(rules);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(new DataSetMetabaseVO());
    assertNotNull(kieBaseManager.reloadRules(1L, new ObjectId().toString(), rule));
  }

  @Test
  public void testReloadRulesTable() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setThenCondition(thenCondition);
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    rules.add(rule);
    rule.setShortCode("123");
    schemaRules.setRules(rules);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    assertNotNull(kieBaseManager.reloadRules(1L, id.toString(), rule));
  }

  @Test
  public void testReloadRulesRecord() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.RECORD);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setThenCondition(thenCondition);
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    rules.add(rule);
    rule.setShortCode("123");
    schemaRules.setRules(rules);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    assertNotNull(kieBaseManager.reloadRules(1L, id.toString(), rule));
  }

  @Test
  public void testReloadRulesFieldInteger() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setThenCondition(thenCondition);
    rules.add(rule);
    rule.setShortCode("123");
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    schemaRules.setRules(rules);
    fieldSchema.setType(DataType.NUMBER_INTEGER);
    Document fieldDocument = new Document();
    fieldDocument.put("typeData", DataType.NUMBER_INTEGER);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldDocument);
    assertNotNull(kieBaseManager.reloadRules(1L, id.toString(), rule));
  }

  @Test
  public void testReloadRulesFieldDecimal() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setThenCondition(thenCondition);
    rules.add(rule);
    rule.setShortCode("123");
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    schemaRules.setRules(rules);
    fieldSchema.setType(DataType.NUMBER_DECIMAL);
    Document fieldDocument = new Document();
    fieldDocument.put("typeData", DataType.NUMBER_DECIMAL);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldDocument);
    assertNotNull(kieBaseManager.reloadRules(1L, id.toString(), rule));
  }

  @Test
  public void testReloadRulesFieldDate() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setShortCode("123");
    rule.setThenCondition(thenCondition);
    rules.add(rule);
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    schemaRules.setRules(rules);
    fieldSchema.setType(DataType.DATE);
    Document fieldDocument = new Document();
    fieldDocument.put("typeData", DataType.DATE);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldDocument);
    assertNotNull(kieBaseManager.reloadRules(1L, id.toString(), rule));
  }

  @Test
  public void testReloadRulesFieldBoolean() throws FileNotFoundException {
    RulesSchema schemaRules = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setThenCondition(thenCondition);
    rules.add(rule);
    rule.setShortCode("123");
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    schemaRules.setRules(rules);
    fieldSchema.setType(DataType.BOOLEAN);
    Document fieldDocument = new Document();
    fieldDocument.put("typeData", DataType.BOOLEAN);
    Mockito.when(rulesRepository.getActiveAndVerifiedRules(Mockito.any())).thenReturn(schemaRules);
    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(fieldDocument);
    assertNotNull(kieBaseManager.reloadRules(1L, id.toString(), rule));
  }

  @Test
  public void testTextRuleCorrectTable() throws EEAException {
    Rule rule = new Rule();
    List<String> thenCondition = new ArrayList<>();
    thenCondition.add("");
    thenCondition.add("");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setShortCode("123");
    rule.setThenCondition(thenCondition);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    kieBaseManager.validateRule(id.toString(), rule);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void testTextRuleCorrectRecord() throws EEAException {
    Rule rule = new Rule();
    List<String> thenCondition = new ArrayList<>();
    List<Document> fields = new ArrayList<>();
    Document document = new Document();
    Document field = new Document();
    field.put("_id", new ObjectId());
    field.put("typeData", DataType.NUMBER_INTEGER);
    fields.add(field);
    document.put("fieldSchemas", fields);
    thenCondition.add("");
    thenCondition.add("");
    rule.setType(EntityTypeEnum.RECORD);
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setShortCode("123");
    rule.setThenCondition(thenCondition);
    Mockito.when(schemasRepository.findRecordSchema(Mockito.any(), Mockito.any()))
        .thenReturn(document);
    Mockito.when(ruleExpressionService.isDataTypeCompatible(Mockito.anyString(), Mockito.any(),
        Mockito.any())).thenReturn(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    kieBaseManager.validateRule(id.toString(), rule);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void testTextRuleCorrectField() throws EEAException {
    Rule rule = new Rule();
    List<String> thenCondition = new ArrayList<>();
    Document document = new Document();
    document.put("typeData", DataType.NUMBER_DECIMAL);
    thenCondition.add("");
    thenCondition.add("");
    rule.setType(EntityTypeEnum.FIELD);
    rule.setReferenceId(id);
    rule.setRuleId(id);
    rule.setShortCode("123");
    rule.setWhenCondition("RuleOperators.fieldNumberEquals(\"5\", 5)");
    rule.setThenCondition(thenCondition);
    Mockito.when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any()))
        .thenReturn(document);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    kieBaseManager.validateRule(id.toString(), rule);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}
