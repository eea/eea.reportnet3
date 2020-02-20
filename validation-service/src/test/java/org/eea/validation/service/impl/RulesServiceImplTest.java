package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.bson.Document;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
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
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        null, TypeEntityEnum.FIELD, Boolean.TRUE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules boolean test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesBooleanTest() throws EEAException {
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.BOOLEAN, TypeEntityEnum.FIELD, Boolean.FALSE);
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
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.CODELIST, TypeEntityEnum.FIELD, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules long test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesLongTest() throws EEAException {
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.COORDINATE_LAT, TypeEntityEnum.FIELD, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules lat test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesLatTest() throws EEAException {
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.COORDINATE_LONG, TypeEntityEnum.FIELD, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules date test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesDateTest() throws EEAException {
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.DATE, TypeEntityEnum.FIELD, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules number test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesNumberTest() throws EEAException {
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.NUMBER, TypeEntityEnum.FIELD, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

  }

  /**
   * Creates the automatic rules text test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRulesTextTest() throws EEAException {
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        TypeData.TEXT, TypeEntityEnum.FIELD, Boolean.FALSE);

  }
}
