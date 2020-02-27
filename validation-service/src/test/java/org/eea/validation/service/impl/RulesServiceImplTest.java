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
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
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

  /**
   * Delete rule by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleById() throws EEAException {
    rulesServiceImpl.deleteRuleById("ObjectId", "ObjectId");
    Mockito.verify(rulesRepository, times(1)).deleteRuleById(Mockito.any(), Mockito.any());

  }

  /**
   * Delete rule by reference id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByReferenceId() throws EEAException {
    rulesServiceImpl.deleteRuleByReferenceId("ObjectId", "ObjectId");
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
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.any())).thenReturn(null);
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
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.any()))
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
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.any())).thenReturn(null);
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
    when(rulesRepository.getRulesWithActiveCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
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
    Mockito.when(rulesRepository.getRulesWithTypeRuleCriteria(Mockito.any(), Mockito.any()))
        .thenReturn(ruleSchema);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.TEXT, EntityTypeEnum.FIELD, Boolean.FALSE);

  }

  /**
   * Creates the empty rules schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createEmptyRulesSchemaTest() throws EEAException {
    rulesServiceImpl.createEmptyRulesSchema(new ObjectId("5e44110d6a9e3a270ce13fac"),
        new ObjectId("5e44110d6a9e3a270ce13fac"));
    Mockito.verify(rulesRepository, times(1)).save(Mockito.any());
  }

  /**
   * Delete empty rules scehma test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteEmptyRulesScehmaTest() throws EEAException {
    when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(new RulesSchema());
    rulesServiceImpl.deleteEmptyRulesScehma(new ObjectId("5e44110d6a9e3a270ce13fac"));
    Mockito.verify(rulesRepository, times(1)).deleteByIdDatasetSchema(Mockito.any());
  }

  /**
   * Delete empty rules scehma no schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteEmptyRulesScehmaNoSchemaTest() throws EEAException {
    when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(null);
    rulesServiceImpl.deleteEmptyRulesScehma(new ObjectId("5e44110d6a9e3a270ce13fac"));
  }

  /**
   * Creates the new rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createNewRuleTest() throws EEAException {
    Rule rule = new Rule();
    rulesServiceImpl.createNewRule("5e44110d6a9e3a270ce13fac", rule);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Delete rule required test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleRequiredTest() throws EEAException {
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
    when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);
    rulesServiceImpl.updateRule("5e44110d6a9e3a270ce13fac", rule);
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update rule no id schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRuleNoIdSchemaTest() throws EEAException {
    Rule rule = new Rule();
    rulesServiceImpl.updateRule(null, rule);
  }

  /**
   * Update rule no rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRuleNoRuleTest() throws EEAException {
    rulesServiceImpl.updateRule("5e44110d6a9e3a270ce13fac", null);
  }

  /**
   * Insert rule in position test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRuleInPositionTest() throws EEAException {
    Rule rule = new Rule();
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    when(rulesRepository.deleteRule(Mockito.any(), Mockito.any())).thenReturn(true);
    when(rulesRepository.insertRuleInPosition("5e44110d6a9e3a270ce13fac", rule, 0))
        .thenReturn(true);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0);
    Mockito.verify(rulesRepository, times(1)).insertRuleInPosition("5e44110d6a9e3a270ce13fac", rule,
        0);
  }

  /**
   * Insert rule in position no rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRuleInPositionNoRuleTest() throws EEAException {
    Rule rule = null;
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", null, 0);
  }

  /**
   * Insert rule in position no delete test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRuleInPositionNoDeleteTest() throws EEAException {
    Rule rule = new Rule();
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    when(rulesRepository.deleteRule(Mockito.any(), Mockito.any())).thenReturn(false);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0);
  }



  /**
   * Insert rule in position no insert test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void insertRuleInPositionNoInsertTest() throws EEAException {
    Rule rule = new Rule();
    when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    when(rulesRepository.deleteRule(Mockito.any(), Mockito.any())).thenReturn(true);
    when(rulesRepository.insertRuleInPosition("5e44110d6a9e3a270ce13fac", rule, 0))
        .thenReturn(false);
    rulesServiceImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0);
  }
}
