package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
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

  /**
   * Delete rule by id.
   */
  @Test
  public void deleteRuleById() {
    rulesServiceImpl.deleteRuleById("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).deleteRuleById(Mockito.any(), Mockito.any());
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
        null, EntityTypeEnum.FIELD, Boolean.TRUE);
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
        DataType.BOOLEAN, EntityTypeEnum.FIELD, Boolean.FALSE);
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
    doc.put("idCodeList", 1L);
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.anyBoolean()))
        .thenReturn(ruleSchema);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.CODELIST, EntityTypeEnum.FIELD, Boolean.FALSE);
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
        DataType.COORDINATE_LAT, EntityTypeEnum.FIELD, Boolean.FALSE);
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
        DataType.COORDINATE_LONG, EntityTypeEnum.FIELD, Boolean.FALSE);
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
        DataType.DATE, EntityTypeEnum.FIELD, Boolean.FALSE);
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
        DataType.NUMBER, EntityTypeEnum.FIELD, Boolean.FALSE);
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
        DataType.TEXT, EntityTypeEnum.FIELD, Boolean.FALSE);

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
   */
  @Test
  public void createNewRuleTest() {
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(new Rule());
    rulesServiceImpl.createNewRule("5e44110d6a9e3a270ce13fac", new RuleVO());
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the new rule with empty id test.
   */
  @Test
  public void createNewRuleWithEmptyIdTest() {
    Rule rule = new Rule();
    rule.setRuleId(new ObjectId());
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    rulesServiceImpl.createNewRule("5e44110d6a9e3a270ce13fac", new RuleVO());
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
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
   */
  @Test
  public void updateRuleTest() {
    when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);
    rulesServiceImpl.updateRule("5e44110d6a9e3a270ce13fac", new RuleVO());
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update rule no rule test.
   */
  @Test
  public void updateRuleNoRuleTest() {
    rulesServiceImpl.updateRule("5e44110d6a9e3a270ce13fac", null);
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
