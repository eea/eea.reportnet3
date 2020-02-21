package org.eea.validation.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.RulesService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class RulesControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RulesControllerImplTest {

  /** The rules controller impl. */
  @InjectMocks
  private RulesControllerImpl rulesControllerImpl;

  /** The rules service. */
  @Mock
  private RulesService rulesService;

  @Mock
  private RuleMapper ruleMapper;

  /**
   * Delete rule by id throw id dataset schema.
   */
  @Test
  public void deleteRuleByIdThrowIdDatasetSchema() {
    try {
      rulesControllerImpl.deleteRuleById("", "ObjectId");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }

  }

  /**
   * Delete rule by id throw rule id.
   */
  @Test
  public void deleteRuleByIdThrowRuleId() {
    try {
      rulesControllerImpl.deleteRuleById("ObjectId", "");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.RULEID_INCORRECT, e.getReason());
    }
  }

  /**
   * Delete rule by id throw delete.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByIdThrowDelete() throws EEAException {
    doThrow(EEAException.class).when(rulesService).deleteRuleById(Mockito.any(), Mockito.any());
    try {
      rulesControllerImpl.deleteRuleById("ObjectId", "ObjectId");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_DELETING_RULE, e.getReason());
    }
  }

  /**
   * Delete rule by id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleById() throws EEAException {
    rulesControllerImpl.deleteRuleById("ObjectId", "ObjectId");
    Mockito.verify(rulesService, times(1)).deleteRuleById("ObjectId", "ObjectId");
  }

  /**
   * Delete rule by reference id throw id dataset schema.
   */
  @Test
  public void deleteRuleByReferenceIdThrowIdDatasetSchema() {
    try {
      rulesControllerImpl.deleteRuleByReferenceId("", "ObjectId");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Delete rule by reference id throw reference id.
   */
  @Test
  public void deleteRuleByReferenceIdThrowReferenceId() {
    try {
      rulesControllerImpl.deleteRuleByReferenceId("ObjectId", "");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.REFERENCEID_INCORRECT, e.getReason());
    }
  }

  /**
   * Delete rule by reference id throw delete.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByReferenceIdThrowDelete() throws EEAException {
    doThrow(EEAException.class).when(rulesService).deleteRuleByReferenceId(Mockito.any(),
        Mockito.any());
    try {
      rulesControllerImpl.deleteRuleByReferenceId("ObjectId", "ObjectId");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_DELETING_RULE, e.getReason());
    }
  }

  /**
   * Delete rule by reference id.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByReferenceId() throws EEAException {
    rulesControllerImpl.deleteRuleByReferenceId("ObjectId", "ObjectId");
    Mockito.verify(rulesService, times(1)).deleteRuleByReferenceId("ObjectId", "ObjectId");
  }

  /**
   * Find rule schema by dataset id blank test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRuleSchemaByDatasetIdBlankTest() throws EEAException {
    try {
      rulesControllerImpl.findRuleSchemaByDatasetId("");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Find rule schema by dataset id null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRuleSchemaByDatasetIdNullTest() throws EEAException {
    try {
      rulesControllerImpl.findRuleSchemaByDatasetId(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Find rule schema by dataset id success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findRuleSchemaByDatasetIdSuccessTest() throws EEAException {
    rulesControllerImpl.findRuleSchemaByDatasetId("5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).getRulesSchemaByDatasetId(Mockito.any());
  }

  /**
   * Find active rule schema by dataset id blank test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findActiveRuleSchemaByDatasetIdBlankTest() throws EEAException {
    try {
      rulesControllerImpl.findActiveRuleSchemaByDatasetId("");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Find active rule schema by dataset id null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findActiveRuleSchemaByDatasetIdNullTest() throws EEAException {
    try {
      rulesControllerImpl.findActiveRuleSchemaByDatasetId(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Find active rule schema by dataset id success test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void findActiveRuleSchemaByDatasetIdSuccessTest() throws EEAException {
    rulesControllerImpl.findActiveRuleSchemaByDatasetId("5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).getActiveRulesSchemaByDatasetId(Mockito.any());
  }


  /**
   * Creates the automatic rule test false.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRuleTestFalse() throws EEAException {
    rulesControllerImpl.createAutomaticRule("", "", TypeData.BOOLEAN, TypeEntityEnum.FIELD,
        Boolean.FALSE);
    Mockito.verify(rulesService, times(1)).createAutomaticRules("", "", TypeData.BOOLEAN,
        TypeEntityEnum.FIELD, Boolean.FALSE);
  }

  /**
   * Creates the automatic rule test false throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRuleTestFalseThrow() throws EEAException {

    doThrow(EEAException.class).when(rulesService).createAutomaticRules("", "", TypeData.BOOLEAN,
        TypeEntityEnum.FIELD, Boolean.FALSE);
    try {
      rulesControllerImpl.createAutomaticRule("", "", TypeData.BOOLEAN, TypeEntityEnum.FIELD,
          Boolean.FALSE);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE, e.getReason());
    }
  }

  /**
   * Creates the automatic rule test true.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRuleTestTrue() throws EEAException {
    rulesControllerImpl.createAutomaticRule("", "", TypeData.BOOLEAN, TypeEntityEnum.FIELD,
        Boolean.TRUE);
    Mockito.verify(rulesService, times(1)).createAutomaticRules("", "", null, TypeEntityEnum.FIELD,
        Boolean.TRUE);
  }

  /**
   * Creates the automatic rule test true throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRuleTestTrueThrow() throws EEAException {
    doThrow(EEAException.class).when(rulesService).createAutomaticRules("", "", null,
        TypeEntityEnum.FIELD, Boolean.TRUE);
    try {
      rulesControllerImpl.createAutomaticRule("", "", TypeData.BOOLEAN, TypeEntityEnum.FIELD,
          Boolean.TRUE);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE, e.getReason());
    }
  }

  @Test
  public void createEmptyRulesSchemaTest() throws EEAException {
    rulesControllerImpl.createEmptyRulesSchema("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).createEmptyRulesSchema(
        new ObjectId("5e44110d6a9e3a270ce13fac"), new ObjectId("5e44110d6a9e3a270ce13fac"));
  }

  @Test
  public void createEmptyRulesSchemaNoIdDataSchemaTest() throws EEAException {
    try {
      rulesControllerImpl.createEmptyRulesSchema(null, "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  @Test
  public void createEmptyRulesSchemaNoIdRulesTest() throws EEAException {
    try {
      rulesControllerImpl.createEmptyRulesSchema("5e44110d6a9e3a270ce13fac", null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.RULEID_INCORRECT, e.getReason());
    }

  }

  @Test
  public void deleteRulesSchemaTest() throws EEAException {
    rulesControllerImpl.deleteRulesSchema("5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1))
        .deleteEmptyRulesScehma(new ObjectId("5e44110d6a9e3a270ce13fac"));
  }

  @Test
  public void deleteRulesSchemaNoschemaIDTest() throws EEAException {
    try {
      rulesControllerImpl.deleteRulesSchema(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  @Test
  public void createNewRuleTest() throws EEAException {
    Rule rule = new Rule();
    RuleVO ruleVO = new RuleVO();
    when(ruleMapper.classToEntity(ruleVO)).thenReturn(rule);
    rulesControllerImpl.createNewRule("5e44110d6a9e3a270ce13fac", ruleVO);
    Mockito.verify(rulesService, times(1)).createNewRule("5e44110d6a9e3a270ce13fac", rule);
  }

  @Test
  public void createNewRuleNoDataSchemaIsTest() throws EEAException {
    try {
      rulesControllerImpl.createNewRule(null, null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  @Test
  public void createNewRuleNotWorkTest() throws EEAException {
    Rule rule = new Rule();
    RuleVO ruleVO = new RuleVO();
    doThrow(EEAException.class).when(rulesService).createNewRule("5e44110d6a9e3a270ce13fac", rule);
    when(ruleMapper.classToEntity(ruleVO)).thenReturn(rule);
    try {
      rulesControllerImpl.createNewRule("5e44110d6a9e3a270ce13fac", ruleVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_DELETING_RULE, e.getReason());
    }
  }

  @Test
  public void updateRuleTest() throws EEAException {
    Rule rule = new Rule();
    RuleVO ruleVO = new RuleVO();
    when(ruleMapper.classToEntity(ruleVO)).thenReturn(rule);
    when(rulesService.updateRule("5e44110d6a9e3a270ce13fac", rule)).thenReturn(true);
    rulesControllerImpl.updateRule("5e44110d6a9e3a270ce13fac", ruleVO);
    Mockito.verify(rulesService, times(1)).updateRule("5e44110d6a9e3a270ce13fac", rule);
  }

  @Test
  public void updateRuleNotWorkTest() throws EEAException {
    Rule rule = new Rule();
    RuleVO ruleVO = new RuleVO();
    when(ruleMapper.classToEntity(ruleVO)).thenReturn(rule);
    when(rulesService.updateRule("5e44110d6a9e3a270ce13fac", rule)).thenReturn(false);
    try {
      rulesControllerImpl.updateRule("5e44110d6a9e3a270ce13fac", ruleVO);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_UPDATING_RULE, e.getReason());
    }
  }


  @Test
  public void updateRuleNoDataSchemaIsTest() throws EEAException {
    try {
      rulesControllerImpl.updateRule(null, null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }


  @Test
  public void insertRuleInPositionTest() throws EEAException {
    when(rulesService.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0)).thenReturn(true);
    rulesControllerImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", 0,
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).insertRuleInPosition("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac", 0);
  }

  @Test
  public void insertRuleInPositionNoDataSchemaIdTest() throws EEAException {
    try {
      rulesControllerImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", 0, null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  @Test
  public void insertRuleInPositionNoIdRuleIsTest() throws EEAException {
    try {
      rulesControllerImpl.insertRuleInPosition(null, 0, "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.RULEID_INCORRECT, e.getReason());
    }
  }

  @Test
  public void insertRuleInPositionNotWorkTest() throws EEAException {
    when(rulesService.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0)).thenReturn(false);
    try {
      rulesControllerImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", 0,
          "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_ORDERING_RULE, e.getReason());
    }
  }

  @Test
  public void existsRuleRequiredTest() throws EEAException {
    rulesControllerImpl.existsRuleRequired("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).existsRuleRequired("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
  }

  @Test
  public void existsRuleRequiredNoIdReferenceIsTest() throws EEAException {
    try {

      rulesControllerImpl.existsRuleRequired("5e44110d6a9e3a270ce13fac", null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.REFERENCEID_INCORRECT, e.getReason());
    }
  }

  @Test
  public void existsRuleRequiredNoDataSchemaIdTest() throws EEAException {
    try {
      rulesControllerImpl.existsRuleRequired(null, "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  @Test
  public void deleteRuleRequireddNoIdReferenceIsTest() throws EEAException {
    try {

      rulesControllerImpl.deleteRuleRequired("5e44110d6a9e3a270ce13fac", null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.REFERENCEID_INCORRECT, e.getReason());
    }
  }

  @Test
  public void deleteRuleRequiredNoDataSchemaIdTest() throws EEAException {
    try {
      rulesControllerImpl.deleteRuleRequired(null, "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  @Test
  public void deleteRuleRequiredTest() throws EEAException {
    rulesControllerImpl.deleteRuleRequired("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).deleteRuleRequired("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
  }


}
