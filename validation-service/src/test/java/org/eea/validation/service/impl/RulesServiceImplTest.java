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
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class RulesServiceImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RulesServiceImplTest {

  /** The rules service impl. */
  @InjectMocks
  private RulesServiceImpl rulesServiceImpl;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

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
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(new RulesSchema());
    when(rulesSchemaMapper.entityToClass(Mockito.any())).thenReturn(new RulesSchemaVO());
    assertEquals(new RulesSchemaVO(),
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
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
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
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
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.CODELIST, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.DATE, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules number test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesNumberTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.NUMBER, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.TEXT, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);

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
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));

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
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
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
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));

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
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));

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
}
