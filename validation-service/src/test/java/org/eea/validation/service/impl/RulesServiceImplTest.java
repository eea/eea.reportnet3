package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.mapper.IntegrityMapper;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.IntegritySchemaRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.RulesSequenceRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.util.KieBaseManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** The Class RulesServiceImplTest. */
@RunWith(MockitoJUnitRunner.class)
public class RulesServiceImplTest {

  /** The rules service impl. */
  @InjectMocks
  private RulesServiceImpl rulesServiceImpl;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /** The table repository. */
  @Mock
  private TableRepository tableRepository;

  /** The rules schema mapper. */
  @Mock
  private RulesSchemaMapper rulesSchemaMapper;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The rule mapper. */
  @Mock
  private RuleMapper ruleMapper;

  /** The data set metabase controller zuul. */
  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The rules sequence repository. */
  @Mock
  private RulesSequenceRepository rulesSequenceRepository;


  /** The kie base manager. */
  @Mock
  private KieBaseManager kieBaseManager;

  /** The integrity schema repository. */
  @Mock
  private IntegritySchemaRepository integritySchemaRepository;

  /** The integrity mapper. */
  @Mock
  private IntegrityMapper integrityMapper;

  /**
   * Delete rule by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByIdTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.deleteRuleById(Mockito.any(), Mockito.any())).thenReturn(true);
    rulesServiceImpl.deleteRuleById(1L, "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleById(Mockito.any(), Mockito.any());
  }

  /**
   * Delete rule by id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void deleteRuleByIdExceptionTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(null);
    try {
      rulesServiceImpl.deleteRuleById(1L, "5e44110d6a9e3a270ce13fac");
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete rule by reference id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByReferenceId() throws EEAException {
    rulesServiceImpl.deleteRuleByReferenceId("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleByReferenceId(Mockito.any(), Mockito.any());
  }


  /**
   * Delete rule by reference field schema PK id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByReferenceFieldSchemaPKId() throws EEAException {
    rulesServiceImpl.deleteRuleByReferenceFieldSchemaPKId("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleByReferenceFieldSchemaPKId(Mockito.any(),
        Mockito.any());
  }

  /**
   * Gets the rules schema by dataset id not found test.
   *
   * @return the rules schema by dataset id not found test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getRulesSchemaByDatasetIdNotFoundTest() throws EEAException {
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(null);
    assertNull(rulesServiceImpl.getRulesSchemaByDatasetId("5e44110d6a9e3a270ce13fac"));
  }

  /**
   * Gets the rules schema by dataset id success test.
   *
   * @return the rules schema by dataset id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getRulesSchemaByDatasetIdSuccessTest() throws EEAException {
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(new RulesSchema());
    when(rulesSchemaMapper.entityToClass(Mockito.any())).thenReturn(new RulesSchemaVO());
    assertEquals(new RulesSchemaVO(),
        rulesServiceImpl.getRulesSchemaByDatasetId("5e44110d6a9e3a270ce13fac"));
  }

  /**
   * Gets the active rules schema by dataset id not found test.
   *
   * @return the active rules schema by dataset id not found test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getActiveRulesSchemaByDatasetIdNotFoundTest() throws EEAException {
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(null);
    assertNull(rulesServiceImpl.getActiveRulesSchemaByDatasetId("5e44110d6a9e3a270ce13fac"));
  }

  /**
   * Gets the active rules schema by dataset id success test.
   *
   * @return the active rules schema by dataset id success test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getActiveRulesSchemaByDatasetIdSuccessTest() throws EEAException {
    List<IntegritySchema> integrities = new ArrayList<>();
    List<Rule> rules = new ArrayList<>();
    List<RuleVO> rulesVO = new ArrayList<>();
    List<IntegrityVO> listIntegrityVO = new ArrayList<>();
    ObjectId id = new ObjectId();
    new RulesSchemaVO();
    Rule rule = new Rule();
    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId(id.toString());
    rulesVO.add(ruleVO);
    rule.setRuleId(id);
    rule.setIntegrityConstraintId(id);
    rules.add(rule);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(rules);
    IntegrityVO integrityVO = new IntegrityVO();
    integrityVO.setId(id.toString());
    integrities.add(new IntegritySchema());
    listIntegrityVO.add(integrityVO);
    RulesSchemaVO ruleSchemaVO = new RulesSchemaVO();
    ruleSchemaVO.setRules(rulesVO);
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
    when(rulesSchemaMapper.entityToClass(Mockito.any())).thenReturn(ruleSchemaVO);
    when(integrityMapper.entityToClass(Mockito.any())).thenReturn(integrityVO);
    assertEquals(ruleSchemaVO,
        rulesServiceImpl.getActiveRulesSchemaByDatasetId("5e44110d6a9e3a270ce13fac"));
  }


  /**
   * Creates the automatic rules required test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesRequiredTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        null, EntityTypeEnum.FIELD, 1L, Boolean.TRUE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }


  /**
   * Creates the automatic rules boolean test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesBooleanTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.BOOLEAN, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }


  /**
   * Creates the automatic rules PK test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesPKTest() throws EEAException {
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemaList = new ArrayList();
    TableSchema tableSchema = new TableSchema();
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema = new FieldSchema();
    List<FieldSchema> fieldSchemaList = new ArrayList();
    fieldSchema.setIdFieldSchema(new ObjectId("5e44110d6a9e3a270ce13fac"));
    fieldSchemaList.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemaList);
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchemaList.add(tableSchema);
    datasetSchema.setTableSchemas(tableSchemaList);
    datasetSchema.setIdDataSetSchema(new ObjectId());



    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());


    Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.LINK, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }


  /**
   * Creates the automatic rules codelist test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesCodelistTest() throws EEAException {
    Document doc = new Document();
    doc.put("codelistItems", "[2, 2]");
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.CODELIST, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules codelist test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesMultiCodelistTest() throws EEAException {
    Document doc = new Document();
    doc.put("codelistItems", "[2, 2]");
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.MULTISELECT_CODELIST, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules long test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesLongTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.COORDINATE_LAT, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }


  /**
   * Creates the automatic rules lat test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesLatTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.COORDINATE_LONG, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules date test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesDateTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.DATE, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }


  /**
   * Creates the automatic rules number decimal test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesNumberDecimalTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.NUMBER_DECIMAL, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }


  /**
   * Creates the automatic rules URL test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesURLTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.URL, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }


  /**
   * Creates the automatic rules email test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesEmailTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.EMAIL, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the automatic rules phone test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesPhoneTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.PHONE, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the automatic rules number integer test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesNumberIntegerTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.NUMBER_INTEGER, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the automatic rules text test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesTextTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.TEXT, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesSequenceRepository, times(1)).updateSequence(Mockito.any());

  }

  /**
   * Creates the empty rules schema test.
   */
  @Test
  public void createEmptyRulesSchemaTest() {
    rulesServiceImpl.createEmptyRulesSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).save(Mockito.any());
  }

  /**
   * Delete empty rules scehma test.
   */
  @Test
  public void deleteEmptyRulesSchemaTest() {
    when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(new RulesSchema());
    rulesServiceImpl.deleteEmptyRulesSchema("5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteByIdDatasetSchema(Mockito.any());
  }

  /**
   * Delete empty rules scehma no schema test.
   */
  @Test
  public void deleteEmptyRulesScehmaNoSchemaTest() {
    when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(null);
    rulesServiceImpl.deleteEmptyRulesSchema("5e44110d6a9e3a270ce13fac");

    Mockito.verify(rulesRepository, times(1)).findByIdDatasetSchema(Mockito.any());
  }

  /**
   * Creates the new rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createNewRuleTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setType(EntityTypeEnum.FIELD);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(true);
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);

    rulesServiceImpl.createNewRule(1L, new RuleVO());
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the new rule dataset id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleDatasetIdExceptionTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(null);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule repository exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleRepositoryExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setType(EntityTypeEnum.FIELD);
    rule.setThenCondition(Arrays.asList("success", "error"));
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(false);
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule short code null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleShortCodeNullExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.SHORT_CODE_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule entity type null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleEntityTypeNullExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ENTITY_TYPE_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule then condition size exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleThenConditionSizeExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success"));

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.THEN_CONDITION_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule then condition null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleThenConditionNullExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.THEN_CONDITION_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule when condition null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleWhenConditionNullExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setDescription("description");
    rule.setRuleName("ruleName");

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.WHEN_CONDITION_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule rule name null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleRuleNameNullExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());
    rule.setDescription("description");

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.RULE_NAME_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule description null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleDescriptionNullExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setReferenceId(new ObjectId());

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.DESCRIPTION_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the new rule reference id null exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createNewRuleReferenceIdNullExceptionTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(new Rule());
    try {
      rulesServiceImpl.createNewRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.REFERENCE_ID_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete rule required test.
   */
  @Test
  public void deleteRuleRequiredTest() {
    rulesServiceImpl.deleteRuleRequired("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleRequired(Mockito.any(), Mockito.any());
  }

  /**
   * Exists rule required test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void existsRuleRequiredTest() throws EEAException {
    rulesServiceImpl.existsRuleRequired("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).existsRuleRequired(Mockito.any(), Mockito.any());
  }

  /**
   * Update rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRuleTest() throws EEAException {

    Rule rule = new Rule();
    rule.setRuleId(new ObjectId());
    rule.setReferenceId(new ObjectId());
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setType(EntityTypeEnum.FIELD);

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);
    rulesServiceImpl.updateRule(1L, new RuleVO());
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update rule exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateRuleRuleIdExceptionTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(new Rule());
    try {
      rulesServiceImpl.updateRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.RULE_ID_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Update rule exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateRuleExceptionTest() throws EEAException {

    Rule rule = new Rule();
    rule.setRuleId(new ObjectId());
    rule.setReferenceId(new ObjectId());
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setType(EntityTypeEnum.FIELD);

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(false);
    try {
      rulesServiceImpl.updateRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_UPDATING_RULE, e.getMessage());
      throw e;
    }
  }

  /**
   * Update rule dataset id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateRuleDatasetIdExceptionTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(null);
    try {
      rulesServiceImpl.updateRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getMessage());
      throw e;
    }
  }

  /**
   * Insert rule in position test.
   */
  @Test
  public void insertRuleInPositionTest() {
    Rule rule = new Rule();
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    when(rulesRepository.deleteRuleById(Mockito.any(), Mockito.any())).thenReturn(true);
    when(rulesRepository.insertRuleInPosition(Mockito.any(), Mockito.any(), Mockito.anyInt()))
        .thenReturn(true);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0);
    Mockito.verify(rulesRepository, times(1)).insertRuleInPosition(Mockito.any(), Mockito.any(),
        Mockito.anyInt());
  }

  /**
   * Insert rule in position no delete test.
   */
  @Test
  public void insertRuleInPositionNoDeleteTest() {
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    when(rulesRepository.deleteRuleById(Mockito.any(), Mockito.any())).thenReturn(false);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0);
    Mockito.verify(rulesRepository, times(1)).deleteRuleById(Mockito.any(), Mockito.any());
  }

  /**
   * Insert rule in position no insert test.
   */
  @Test
  public void insertRuleInPositionNoInsertTest() {
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    when(rulesRepository.deleteRuleById(Mockito.any(), Mockito.any())).thenReturn(true);
    when(rulesRepository.insertRuleInPosition(Mockito.any(), Mockito.any(), Mockito.anyInt()))
        .thenReturn(false);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0);
    Mockito.verify(rulesRepository, times(1)).insertRuleInPosition(Mockito.any(), Mockito.any(),
        Mockito.anyInt());
  }

  /**
   * Insert rule in position not found test.
   */
  @Test
  public void insertRuleInPositionNotFoundTest() {
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(null);
    Assert.assertEquals(false, rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac", 0));
  }

  /**
   * Udate automatic rule all properties updated test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void udateAutomaticRuleAllPropertiesUpdatedTest() throws EEAException {

    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);
    ruleVO.setRuleName("ruleName");
    ruleVO.setDescription("description");
    ruleVO.setShortCode("shortCode");
    ruleVO.setThenCondition(Arrays.asList("ERROR", "error message"));

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);

    rulesServiceImpl.updateAutomaticRule(1L, ruleVO);
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Udate automatic rule only enabled property updated test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void udateAutomaticRuleOnlyEnabledPropertyUpdatedTest() throws EEAException {

    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);

    rulesServiceImpl.updateAutomaticRule(1L, ruleVO);
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Udate automatic rule empty then condition array test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void udateAutomaticRuleEmptyThenConditionArrayTest() throws EEAException {

    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);
    ruleVO.setThenCondition(new ArrayList<String>());

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);

    rulesServiceImpl.updateAutomaticRule(1L, ruleVO);
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update automatic rule invalid dataset id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateAutomaticRuleInvalidDatasetIdExceptionTest() throws EEAException {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn(null);
    try {
      rulesServiceImpl.updateAutomaticRule(1L, new RuleVO());
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.DATASET_INCORRECT_ID, e.getMessage());
      throw e;
    }
  }

  /**
   * Update automatic rule invalid rule id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateAutomaticRuleInvalidRuleIdExceptionTest() throws EEAException {

    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId("notObjectIdComplaining");

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    try {
      rulesServiceImpl.updateAutomaticRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.RULEID_INCORRECT, e.getMessage());
      throw e;
    }
  }

  /**
   * Udate automatic rule rule not found exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void udateAutomaticRuleRuleNotFoundExceptionTest() throws EEAException {

    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(null);

    try {
      rulesServiceImpl.updateAutomaticRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(String.format(EEAErrorMessage.RULE_NOT_FOUND, "5e44110d6a9e3a270ce13fac",
          "5e44110d6a9e3a270ce13fac"), e.getMessage());
      throw e;
    }
  }

  /**
   * Delete unique constraint test.
   */
  @Test
  public void deleteUniqueConstraintTest() {
    rulesServiceImpl.deleteUniqueConstraint("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteByUniqueConstraintId(Mockito.any(),
        Mockito.any());
  }

  /**
   * Creates the unique constraint test.
   */
  @Test
  public void createUniqueConstraintTest() {
    when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createUniqueConstraint("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }


  /**
   * Delete rule high level like like test.
   */
  @Test
  public void deleteRuleHighLevelLikeLikeTest() {
    when(rulesRepository.deleteRuleHighLevelLike(new ObjectId("5e44110d6a9e3a270ce13fac"),
        "5e44110d6a9e3a270ce13fac")).thenReturn(true);
    rulesServiceImpl.deleteRuleHighLevelLike("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleHighLevelLike(
        new ObjectId("5e44110d6a9e3a270ce13fac"), "5e44110d6a9e3a270ce13fac");
  }

  /**
   * Delete rule high level like non delete test.
   */
  @Test
  public void deleteRuleHighLevelLikeNonDeleteTest() {
    when(rulesRepository.deleteRuleHighLevelLike(new ObjectId("5e44110d6a9e3a270ce13fac"),
        "5e44110d6a9e3a270ce13fac")).thenReturn(false);
    rulesServiceImpl.deleteRuleHighLevelLike("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleHighLevelLike(
        new ObjectId("5e44110d6a9e3a270ce13fac"), "5e44110d6a9e3a270ce13fac");
  }

  /**
   * Delete dataset rule and integrity by field schema id test.
   */
  @Test
  public void deleteDatasetRuleAndIntegrityByFieldSchemaIdTest() {
    List<IntegritySchema> integritySchemaList = new ArrayList<>();
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId());
    integritySchema.setReferencedDatasetSchemaId(new ObjectId());
    integritySchemaList.add(integritySchema);
    when(integritySchemaRepository.findByOriginOrReferenceFields(Mockito.any()))
        .thenReturn(integritySchemaList);
    rulesServiceImpl.deleteDatasetRuleAndIntegrityByFieldSchemaId("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(integritySchemaRepository, times(1))
        .findByOriginOrReferenceFields(Mockito.any());
  }

  /**
   * Delete dataset rule and integrity by dataset schema id.
   */
  @Test
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaIdEmptyTest() {
    when(integritySchemaRepository.findByOriginOrReferenceFields(Mockito.any())).thenReturn(null);
    rulesServiceImpl.deleteDatasetRuleAndIntegrityByFieldSchemaId("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(integritySchemaRepository, times(1))
        .findByOriginOrReferenceFields(Mockito.any());
  }

  /**
   * Delete dataset rule and integrity by dataset schema id test.
   */
  @Test
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaIdTest() {
    List<IntegritySchema> integritySchemaList = new ArrayList<>();
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId());
    integritySchema.setReferencedDatasetSchemaId(new ObjectId());
    integritySchemaList.add(integritySchema);
    when(integritySchemaRepository.findByOriginOrReferenceDatasetSchemaId(Mockito.any()))
        .thenReturn(integritySchemaList);
    rulesServiceImpl.deleteDatasetRuleAndIntegrityByDatasetSchemaId("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(integritySchemaRepository, times(1))
        .findByOriginOrReferenceDatasetSchemaId(Mockito.any());
  }

  /**
   * Delete dataset rule and integrity by field schema id empty test.
   */
  @Test
  public void deleteDatasetRuleAndIntegrityByFieldSchemaIdEmptyTest() {
    when(integritySchemaRepository.findByOriginOrReferenceDatasetSchemaId(Mockito.any()))
        .thenReturn(null);
    rulesServiceImpl.deleteDatasetRuleAndIntegrityByDatasetSchemaId("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(integritySchemaRepository, times(1))
        .findByOriginOrReferenceDatasetSchemaId(Mockito.any());

  }
}
