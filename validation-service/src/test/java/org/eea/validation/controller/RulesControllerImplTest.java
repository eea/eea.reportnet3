package org.eea.validation.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.service.RulesService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

  /** The rule mapper. */
  @Mock
  private RuleMapper ruleMapper;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Delete rule by id test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByIdTest() throws EEAException {
    Mockito.doNothing().when(rulesService).deleteRuleById(Mockito.anyLong(), Mockito.any());
    rulesControllerImpl.deleteRuleById(1L, "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).deleteRuleById(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Delete rule by id exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void deleteRuleByIdExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(rulesService).deleteRuleById(Mockito.anyLong(),
        Mockito.any());
    try {
      rulesControllerImpl.deleteRuleById(1L, "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
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
    rulesControllerImpl.createAutomaticRule("", "", DataType.BOOLEAN, EntityTypeEnum.FIELD, 1L,
        Boolean.FALSE);
    Mockito.verify(rulesService, times(1)).createAutomaticRules("", "", DataType.BOOLEAN,
        EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
  }

  /**
   * Creates the automatic rule test false throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRuleTestFalseThrow() throws EEAException {

    doThrow(EEAException.class).when(rulesService).createAutomaticRules("", "", DataType.BOOLEAN,
        EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    try {
      rulesControllerImpl.createAutomaticRule("", "", DataType.BOOLEAN, EntityTypeEnum.FIELD, 1L,
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
    rulesControllerImpl.createAutomaticRule("", "", null, EntityTypeEnum.FIELD, 1L, Boolean.TRUE);
    Mockito.verify(rulesService, times(1)).createAutomaticRules("", "", null, EntityTypeEnum.FIELD,
        1L, Boolean.TRUE);
  }

  /**
   * Creates the automatic rule test true throw.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createAutomaticRuleTestTrueThrow() throws EEAException {
    doThrow(EEAException.class).when(rulesService).createAutomaticRules("", "", null,
        EntityTypeEnum.FIELD, 1L, Boolean.TRUE);
    try {
      rulesControllerImpl.createAutomaticRule("", "", null, EntityTypeEnum.FIELD, 1L, Boolean.TRUE);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE, e.getReason());
    }
  }

  /**
   * Creates the empty rules schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createEmptyRulesSchemaTest() throws EEAException {
    rulesControllerImpl.createEmptyRulesSchema("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).createEmptyRulesSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Delete rules schema test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRulesSchemaTest() throws EEAException {
    rulesControllerImpl.deleteRulesSchema("5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).deleteEmptyRulesSchema(Mockito.any());
  }

  /**
   * Delete rules schema noschema ID test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRulesSchemaNoschemaIDTest() throws EEAException {
    try {
      rulesControllerImpl.deleteRulesSchema(null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Creates the new rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createNewRuleTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doNothing().when(rulesService).createNewRule(Mockito.anyLong(), Mockito.any());
    rulesControllerImpl.createNewRule(1L, new RuleVO());
    Mockito.verify(rulesService, times(1)).createNewRule(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Creates the new rule exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void createNewRuleExceptionTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doThrow(EEAException.class).when(rulesService).createNewRule(Mockito.anyLong(),
        Mockito.any());

    try {
      rulesControllerImpl.createNewRule(1L, new RuleVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Update rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRuleTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doNothing().when(rulesService).updateRule(Mockito.anyLong(), Mockito.any());
    rulesControllerImpl.updateRule(1L, new RuleVO());
    Mockito.verify(rulesService, times(1)).updateRule(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Update rule not work test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateRuleExceptionTest() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    Mockito.doThrow(EEAException.class).when(rulesService).updateRule(Mockito.anyLong(),
        Mockito.any());
    try {
      rulesControllerImpl.updateRule(1L, new RuleVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Insert rule in position test.
   */
  @Test
  public void insertRuleInPositionTest() {
    when(rulesService.insertRuleInPosition("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        0)).thenReturn(true);
    rulesControllerImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", 0,
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).insertRuleInPosition("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac", 0);
  }

  /**
   * Insert rule in position not work test.
   */
  @Test
  public void insertRuleInPositionNotWorkTest() {
    when(rulesService.insertRuleInPosition(Mockito.any(), Mockito.any(), Mockito.anyInt()))
        .thenReturn(false);
    try {
      rulesControllerImpl.insertRuleInPosition("5e44110d6a9e3a270ce13fac", 0,
          "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.ERROR_ORDERING_RULE, e.getReason());
    }
  }

  /**
   * Exists rule required test.
   */
  @Test
  public void existsRuleRequiredTest() {
    rulesControllerImpl.existsRuleRequired("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).existsRuleRequired("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
  }

  /**
   * Exists rule required no id reference is test.
   */
  @Test
  public void existsRuleRequiredNoIdReferenceIsTest() {
    try {
      rulesControllerImpl.existsRuleRequired("5e44110d6a9e3a270ce13fac", null);
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.REFERENCEID_INCORRECT, e.getReason());
    }
  }

  /**
   * Exists rule required no data schema id test.
   */
  @Test
  public void existsRuleRequiredNoDataSchemaIdTest() {
    try {
      rulesControllerImpl.existsRuleRequired(null, "5e44110d6a9e3a270ce13fac");
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      Assert.assertEquals(EEAErrorMessage.IDDATASETSCHEMA_INCORRECT, e.getReason());
    }
  }

  /**
   * Delete rule required no id reference is test.
   */
  @Test
  public void deleteRuleRequiredTest() {
    rulesControllerImpl.deleteRuleRequired("5e44110d6a9e3a270ce13fac", null);
    Mockito.verify(rulesService, times(1)).deleteRuleRequired(Mockito.any(), Mockito.any());
  }

  /**
   * Delete rule by reference field schema PK id.
   */
  @Test
  public void deleteRuleByReferenceFieldSchemaPKId() {
    rulesControllerImpl.deleteRuleByReferenceFieldSchemaPKId("", "");
    Mockito.verify(rulesService, times(1)).deleteRuleByReferenceFieldSchemaPKId(Mockito.any(),
        Mockito.any());
  }

  /**
   * Update automatic rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateAutomaticRuleTest() throws EEAException {
    Mockito.doNothing().when(rulesService).updateAutomaticRule(Mockito.anyLong(), Mockito.any());
    rulesControllerImpl.updateAutomaticRule(1L, new RuleVO());
    Mockito.verify(rulesService, times(1)).updateAutomaticRule(Mockito.anyLong(), Mockito.any());
  }

  /**
   * Update automatic rule exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void updateAutomaticRuleExceptionTest() throws EEAException {
    try {
      Mockito.doThrow(EEAException.class).when(rulesService).updateAutomaticRule(Mockito.anyLong(),
          Mockito.any());
      rulesControllerImpl.updateAutomaticRule(1L, new RuleVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Creates the unique constraint test.
   */
  @Test
  public void createUniqueConstraintTest() {
    rulesControllerImpl.createUniqueConstraintRule("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).createUniqueConstraint(Mockito.any(), Mockito.any(),
        Mockito.any());
  }

  /**
   * Delete unique constraint test.
   */
  @Test
  public void deleteUniqueConstraintTest() {
    rulesControllerImpl.deleteUniqueConstraintRule("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).deleteUniqueConstraint(Mockito.any(), Mockito.any());
  }


  /**
   * Delete rule high level like.
   */
  @Test
  public void deleteRuleHighLevelLikeTest() {
    rulesControllerImpl.deleteRuleHighLevelLike("5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesService, times(1)).deleteRuleHighLevelLike(Mockito.any(), Mockito.any());
  }

  /**
   * Delete dataset rule and integrity by field schema id test.
   */
  @Test
  public void deleteDatasetRuleAndIntegrityByFieldSchemaIdTest() {
    rulesControllerImpl.deleteDatasetRuleAndIntegrityByFieldSchemaId(Mockito.any());
    Mockito.verify(rulesService, times(1))
        .deleteDatasetRuleAndIntegrityByFieldSchemaId(Mockito.any());
  }

  @Test
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaIdTest() {
    rulesControllerImpl.deleteDatasetRuleAndIntegrityByDatasetSchemaId(Mockito.any());
    Mockito.verify(rulesService, times(1))
        .deleteDatasetRuleAndIntegrityByFieldSchemaId(Mockito.any());
  }
}
