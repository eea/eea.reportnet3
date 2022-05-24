package org.eea.validation.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.communication.NotificationController.NotificationControllerZuul;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ImportSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.audit.DatasetHistoricRuleVO;
import org.eea.interfaces.vo.dataset.schemas.audit.RuleHistoricInfoVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.SqlRuleVO;
import org.eea.validation.exception.EEAForbiddenSQLCommandException;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.SqlRulesService;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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

  @Mock
  private SqlRulesService sqlRulesService;

  @Mock
  private NotificationControllerZuul notificationControllerZuul;

  /** The http servlet response. */
  @Mock
  private HttpServletResponse httpServletResponse;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /** The folder. */
  @org.junit.Rule
  public TemporaryFolder folder = new TemporaryFolder();

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
      rulesControllerImpl.findRuleSchemaByDatasetId("", 0L);
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
      rulesControllerImpl.findRuleSchemaByDatasetId(null, 0L);
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
    rulesControllerImpl.findRuleSchemaByDatasetId("5e44110d6a9e3a270ce13fac", 0L);
    Mockito.verify(rulesService, times(1)).getRulesSchemaByDatasetId(Mockito.any());
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
    rulesControllerImpl.deleteRulesSchema("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(rulesService, times(1)).deleteEmptyRulesSchema(Mockito.any(), Mockito.any());
  }

  /**
   * Delete rules schema noschema ID test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRulesSchemaNoschemaIDTest() throws EEAException {
    try {
      rulesControllerImpl.deleteRulesSchema(null, 1L);
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
    rulesControllerImpl.deleteRuleRequired("5e44110d6a9e3a270ce13fac", null, null);
    Mockito.verify(rulesService, times(1)).deleteRuleRequired(Mockito.any(), Mockito.any(),
        Mockito.any());
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
    rulesControllerImpl.deleteDatasetRuleAndIntegrityByFieldSchemaId(Mockito.any(), Mockito.any());
    Mockito.verify(rulesService, times(1))
        .deleteDatasetRuleAndIntegrityByFieldSchemaId(Mockito.any(), Mockito.any());
  }

  @Test
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaIdTest() {
    rulesControllerImpl.deleteDatasetRuleAndIntegrityByDatasetSchemaId(Mockito.any(),
        Mockito.any());
    Mockito.verify(rulesService, times(1))
        .deleteDatasetRuleAndIntegrityByDatasetSchemaId(Mockito.any(), Mockito.any());
  }

  @Test
  public void testCopyRules() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    rulesControllerImpl.copyRulesSchema(new CopySchemaVO());
    Mockito.verify(rulesService, times(1)).copyRulesSchema(Mockito.any());
  }

  @Test(expected = ResponseStatusException.class)
  public void testCopyRulesException() throws EEAException {

    try {
      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");
      Mockito.doThrow(EEAException.class).when(rulesService).copyRulesSchema(Mockito.any());
      rulesControllerImpl.copyRulesSchema(new CopySchemaVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }

  }

  @Test
  public void deleteNotEmptyRule() throws EEAException {
    rulesControllerImpl.deleteNotEmptyRule(Mockito.anyString(), Mockito.any());
    Mockito.verify(rulesService, times(1)).deleteNotEmptyRule(Mockito.anyString(), Mockito.any());
  }

  @Test
  public void updateSequence() throws EEAException {
    rulesControllerImpl.updateSequence(Mockito.anyString());
    Mockito.verify(rulesService, times(1)).updateSequence(Mockito.any());
  }

  @Test
  public void findSqlSentencesByDatasetSchemaId() throws EEAException {

    rulesControllerImpl.findSqlSentencesByDatasetSchemaId(Mockito.anyString());
    Mockito.verify(rulesService, times(1)).findSqlSentencesByDatasetSchemaId(Mockito.any());
  }

  @Test
  public void validateSqlRuleDataCollection() throws EEAException {
    rulesControllerImpl.validateSqlRuleDataCollection(Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(sqlRulesService, times(1)).validateSQLRuleFromDatacollection(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void insertIntegrityTest() throws EEAException {
    rulesControllerImpl.insertIntegritySchema(Mockito.any());
    Mockito.verify(rulesService, times(1)).insertIntegritySchemas(Mockito.any());
  }

  @Test
  public void getIntegrityRulesByDatasetSchemaIdTest() throws EEAException {
    rulesControllerImpl.getIntegrityRulesByDatasetSchemaId(Mockito.any());
    Mockito.verify(rulesService, times(1)).getIntegritySchemas(Mockito.any());
  }


  @Test
  public void validateSqlRuleTest() throws EEAException {
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(new Rule());
    rulesControllerImpl.validateSqlRule(1L, "5e44110d6a9e3a270ce13fac", new RuleVO());
    Mockito.verify(sqlRulesService, times(1)).validateSQLRule(Mockito.anyLong(),
        Mockito.anyString(), Mockito.any());
  }


  @Test
  public void validateSqlRulesTest() throws EEAException {
    rulesControllerImpl.validateSqlRules(Mockito.anyLong(), Mockito.anyString(), Mockito.any());
    Mockito.verify(sqlRulesService, times(1)).validateSQLRules(Mockito.anyLong(),
        Mockito.anyString(), Mockito.any());
  }



  @Test
  public void getAllDisabledRulesTest() throws EEAException {
    rulesControllerImpl.getAllDisabledRules(Mockito.anyLong(), Mockito.any());
    Mockito.verify(rulesService, times(1)).getAllDisabledRules(Mockito.anyLong(), Mockito.any());
  }


  @Test
  public void getAllUncheckedRulesTest() throws EEAException {
    rulesControllerImpl.getAllUncheckedRules(Mockito.anyLong(), Mockito.any());
    Mockito.verify(rulesService, times(1)).getAllUncheckedRules(Mockito.anyLong(), Mockito.any());
  }

  @Test
  public void deleteAutomaticRuleByReferenceIdTest() {
    rulesControllerImpl.deleteAutomaticRuleByReferenceId(Mockito.any(), Mockito.any());
    Mockito.verify(rulesService, times(1)).deleteAutomaticRuleByReferenceId(Mockito.any(),
        Mockito.any());
  }

  @Test
  public void testImportRules() throws EEAException {

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    rulesControllerImpl.importRulesSchema(new ImportSchemaVO());
    Mockito.verify(rulesService, times(1)).importRulesSchema(Mockito.any(), Mockito.any(),
        Mockito.any());
  }


  @Test(expected = ResponseStatusException.class)
  public void testImportRulesException() throws EEAException {

    try {
      Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
      Mockito.when(authentication.getName()).thenReturn("user");
      Mockito.doThrow(EEAException.class).when(rulesService).importRulesSchema(Mockito.any(),
          Mockito.any(), Mockito.any());
      rulesControllerImpl.importRulesSchema(new ImportSchemaVO());
    } catch (ResponseStatusException e) {
      Assert.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }

  }

  @Test
  public void runSqlTest() throws EEAException {
    SqlRuleVO sqlRule = new SqlRuleVO();
    sqlRule.setSqlRule("SELECT * from dataset_1.table_value");
    rulesControllerImpl.runSqlRule(1L, sqlRule, true);
    Mockito.verify(sqlRulesService, times(1)).runSqlRule(1L, "SELECT * from dataset_1.table_value",
        true);
  }

  @Test(expected = ResponseStatusException.class)
  public void runSqlEEAForbiddenSQLCommandExceptionTest() throws EEAException {
    SqlRuleVO sqlRule = new SqlRuleVO();
    sqlRule.setSqlRule("DELETE * FROM DATASET_396.TABLE1");
    Mockito.when(sqlRulesService.runSqlRule(1L, sqlRule.getSqlRule(), true))
        .thenThrow(new EEAForbiddenSQLCommandException());
    try {
      rulesControllerImpl.runSqlRule(1L, sqlRule, true);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.SQL_COMMAND_NOT_ALLOWED, e.getReason());
      throw e;
    }

  }

  @Test(expected = ResponseStatusException.class)
  public void runSqlEEAExceptionTest() throws EEAException {
    SqlRuleVO sqlRule = new SqlRuleVO();
    sqlRule.setSqlRule("DELETE * FROM DATASET_396.TABLE1");
    Mockito.when(sqlRulesService.runSqlRule(1L, sqlRule.getSqlRule(), true))
        .thenThrow(new EEAException());
    try {
      rulesControllerImpl.runSqlRule(1L, sqlRule, true);
    } catch (ResponseStatusException e) {
      assertEquals(EEAErrorMessage.RUNNING_RULE, e.getReason());
      throw e;
    }

  }

  @Test(expected = ResponseStatusException.class)
  public void getRuleHistoricExceptionTest() throws EEAException {
    Mockito.doThrow(EEAException.class).when(rulesService).getRuleHistoricInfo(Mockito.anyLong(),
        Mockito.any());
    try {
      rulesControllerImpl.getRuleHistoric(1L, "RULE_ID");
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test
  public void getRuleHistoricTest() throws EEAException {
    List<RuleHistoricInfoVO> historicExpected = new ArrayList<>();
    RuleHistoricInfoVO ruleInfo = new RuleHistoricInfoVO();
    ruleInfo.setExpression(true);
    ruleInfo.setMetadata(false);
    ruleInfo.setRuleBefore("");
    ruleInfo.setRuleId("ruleid");
    ruleInfo.setRuleInfoId("ruleinfoid");
    ruleInfo.setStatus(false);
    ruleInfo.setTimestamp(new Date());
    ruleInfo.setUser("user");
    historicExpected.add(ruleInfo);

    Mockito.when(rulesService.getRuleHistoricInfo(Mockito.anyLong(), Mockito.any()))
        .thenReturn(historicExpected);

    assertEquals(rulesControllerImpl.getRuleHistoric(1L, "ruleId"), historicExpected);
  }


  @Test
  public void getRuleHistoricByDatasetIdTest() {
    DatasetHistoricRuleVO datasetHistoricVO = new DatasetHistoricRuleVO();
    datasetHistoricVO.setRuleId("ruleId");
    List<DatasetHistoricRuleVO> historic = new ArrayList<>();
    historic.add(datasetHistoricVO);
    Mockito.when(rulesService.getRuleHistoricInfoByDatasetId(Mockito.anyLong()))
        .thenReturn(historic);
    assertNotNull(rulesControllerImpl.getRuleHistoricByDatasetId(1L));
  }

  @Test
  public void exportQCCSVTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());
    rulesControllerImpl.exportQCCSV(1L);
    Mockito.verify(rulesService, times(1)).exportQCCSV(Mockito.anyLong());
  }

  @Test
  public void exportQCCSVExceptionTest() throws EEAException, IOException {
    Mockito.doNothing().when(notificationControllerZuul)
        .createUserNotificationPrivate(Mockito.anyString(), Mockito.any());
    Mockito.doThrow(EEAException.class).when(rulesService).exportQCCSV(Mockito.anyLong());
    rulesControllerImpl.exportQCCSV(1L);
  }

  @Test(expected = ResponseStatusException.class)
  public void evaluateSqlRuleParseExceptionTest() throws EEAException, ParseException {
    Mockito.doThrow(ParseException.class).when(sqlRulesService).evaluateSqlRule(Mockito.anyLong(),
        Mockito.any());
    try {
      rulesControllerImpl.evaluateSqlRule(1L, new SqlRuleVO());
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void evaluateSqlRuleEEAInvalidSQLExceptionTest() throws EEAException, ParseException {
    Mockito.doThrow(EEAInvalidSQLException.class).when(sqlRulesService)
        .evaluateSqlRule(Mockito.anyLong(), Mockito.any());
    try {
      rulesControllerImpl.evaluateSqlRule(1L, new SqlRuleVO());
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void evaluateSqlRuleEEAForbiddenSQLCommandExceptionTest()
      throws EEAException, ParseException {
    Mockito.doThrow(EEAForbiddenSQLCommandException.class).when(sqlRulesService)
        .evaluateSqlRule(Mockito.anyLong(), Mockito.any());
    try {
      rulesControllerImpl.evaluateSqlRule(1L, new SqlRuleVO());
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void evaluateSqlRuleEEAExceptionTest() throws EEAException, ParseException {
    Mockito.doThrow(EEAException.class).when(sqlRulesService).evaluateSqlRule(Mockito.anyLong(),
        Mockito.any());
    try {
      rulesControllerImpl.evaluateSqlRule(1L, new SqlRuleVO());
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void evaluateSqlRuleStringIndexOutOfBodundsExceptionTest()
      throws EEAException, ParseException {
    Mockito.doThrow(StringIndexOutOfBoundsException.class).when(sqlRulesService)
        .evaluateSqlRule(Mockito.anyLong(), Mockito.any());
    try {
      rulesControllerImpl.evaluateSqlRule(1L, new SqlRuleVO());
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      throw e;
    }
  }


  @Test
  public void evaluateSqlRuleTest() throws EEAException, ParseException {
    Mockito.when(sqlRulesService.evaluateSqlRule(Mockito.anyLong(), Mockito.any())).thenReturn(0.5);
    assertNotNull(rulesControllerImpl.evaluateSqlRule(1L, new SqlRuleVO()));
  }

  @Test
  public void findRuleSchemaByDatasetIdPrivateTest() {
    rulesControllerImpl.findRuleSchemaByDatasetIdPrivate("schema", 1L);
    Mockito.verify(rulesService, times(1)).getRulesSchemaByDatasetId(Mockito.anyString());
  }

  @Test(expected = ResponseStatusException.class)
  public void runSqlRuleNumberFormatExceptionTest() throws EEAException {
    Mockito.doThrow(NumberFormatException.class).when(sqlRulesService).runSqlRule(Mockito.anyLong(),
        Mockito.any(), Mockito.anyBoolean());
    try {
      SqlRuleVO sql = new SqlRuleVO();
      sql.setSqlRule("sql");
      rulesControllerImpl.runSqlRule(1L, sql, false);
    } catch (NumberFormatException e) {
      assertNotNull(e);
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void runSqlRuleStringIndexOutOfBoundsExceptionTest() throws EEAException {
    Mockito.doThrow(StringIndexOutOfBoundsException.class).when(sqlRulesService)
        .runSqlRule(Mockito.anyLong(), Mockito.any(), Mockito.anyBoolean());
    try {
      SqlRuleVO sql = new SqlRuleVO();
      sql.setSqlRule("sql");
      rulesControllerImpl.runSqlRule(1L, sql, false);
    } catch (StringIndexOutOfBoundsException e) {
      assertNotNull(e);
      throw e;
    }
  }

  /**
   * Run sql rule string EEA invalid SQL exception message test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void runSqlRuleStringEEAInvalidSQLExceptionMessageTest() throws EEAException {
    EEAInvalidSQLException exception = new EEAInvalidSQLException("",
        new EEAException("message", new EEAException("message", new EEAException("message"))));
    Mockito.doThrow(exception).when(sqlRulesService).runSqlRule(Mockito.anyLong(), Mockito.any(),
        Mockito.anyBoolean());
    try {
      SqlRuleVO sql = new SqlRuleVO();
      sql.setSqlRule("sql");
      rulesControllerImpl.runSqlRule(1L, sql, false);
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  /**
   * Run sql rule string EEA invalid SQL exception message null test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = ResponseStatusException.class)
  public void runSqlRuleStringEEAInvalidSQLExceptionMessageNullTest() throws EEAException {
    EEAInvalidSQLException exception = new EEAInvalidSQLException("",
        new EEAException("message", new EEAException("message", new EEAException())));
    Mockito.doThrow(exception).when(sqlRulesService).runSqlRule(Mockito.anyLong(), Mockito.any(),
        Mockito.anyBoolean());
    try {
      SqlRuleVO sql = new SqlRuleVO();
      sql.setSqlRule("sql");
      rulesControllerImpl.runSqlRule(1L, sql, false);
    } catch (ResponseStatusException e) {
      assertNotNull(e);
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test(expected = ResponseStatusException.class)
  public void downloadQCCSVExceptionTest() throws IOException {
    try {
      Mockito.doThrow(IOException.class).when(rulesService).downloadQCCSV(Mockito.anyLong(),
          Mockito.any());
      rulesControllerImpl.downloadQCCSV(1L, "FILENAME", null);
    } catch (IOException e) {
      assertNotNull(e);
      throw e;
    }
  }

  /**
   * Download QCCSV.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void downloadQCCSV() throws IOException {
    File file = folder.newFile("filename.txt");

    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);

    Mockito.when(rulesService.downloadQCCSV(Mockito.any(), Mockito.any())).thenReturn(file);
    Mockito.when(httpServletResponse.getOutputStream()).thenReturn(outputStream);
    Mockito.doNothing().when(outputStream).close();

    rulesControllerImpl.downloadQCCSV(1L, "FILENAME", httpServletResponse);
    Mockito.verify(outputStream, times(1)).close();
  }
}
