package org.eea.validation.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.dto.dataset.schemas.rule.RuleExpressionDTO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.mapper.IntegrityMapper;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.repository.AuditRepository;
import org.eea.validation.persistence.repository.IntegritySchemaRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.RulesSequenceRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.repository.UniqueConstraintRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.UniqueConstraintSchema;
import org.eea.validation.persistence.schemas.audit.Audit;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.util.GeometryValidationUtils;
import org.eea.validation.util.KieBaseManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;

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
   * The kafka sender utils.
   */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The Geometry validation utils. */
  @Mock
  private GeometryValidationUtils GeometryValidationUtils;

  /** The unique repository. */
  @Mock
  private UniqueConstraintRepository uniqueRepository;

  @Mock
  private UserManagementControllerZull userManagementControllerZuul;

  @Mock
  private AuditRepository auditRepository;

  private SecurityContext securityContext;

  private Authentication authentication;

  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

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
   * Delete rule by id dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void deleteRuleByIdDatasetTest() throws EEAException {
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId());
    integritySchema.setReferencedDatasetSchemaId(new ObjectId());
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setIntegrityConstraintId(new ObjectId());
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(integritySchemaRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(integritySchema));

    Mockito.when(rulesRepository.deleteRuleById(Mockito.any(), Mockito.any())).thenReturn(true);
    rulesServiceImpl.deleteRuleById(1L, "5e44110d6a9e3a270ce13fac");
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
    List<IntegritySchema> integrities = new ArrayList<>();
    List<Rule> rules = new ArrayList<>();
    List<RuleVO> rulesVO = new ArrayList<>();
    List<IntegrityVO> listIntegrityVO = new ArrayList<>();
    ObjectId id = new ObjectId();
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
    assertEquals(ruleSchemaVO,
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
  public void createAutomaticRulesPointRequiredTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(new ArrayList<Rule>());
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.POINT, EntityTypeEnum.FIELD, 1L, Boolean.TRUE);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());

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
        DataType.TEXT, EntityTypeEnum.FIELD, 1L, Boolean.TRUE);
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
    List<TableSchema> tableSchemaList = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema = new FieldSchema();
    List<FieldSchema> fieldSchemaList = new ArrayList<>();
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
    doc.put("codelistItems", new ArrayList());
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
    doc.put("codelistItems", new ArrayList());
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

  @Test
  public void createAutomaticRulesPositionTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.MULTIPOLYGON, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void createAutomaticRulesPointTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.POINT, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void createAutomaticRulesMultipointTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    List<Rule> rules = new ArrayList<>();
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);

    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.MULTIPOINT, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void createAutomaticRulesLinestringTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.LINESTRING, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void createAutomaticRulesMultilinestringTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.MULTILINESTRING, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void createAutomaticRulesPolygonTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.POLYGON, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
  }

  @Test
  public void createAutomaticRulesGeometrycollectionTest() throws EEAException {
    RulesSchema ruleSchema = new RulesSchema();
    List<Rule> rules = new ArrayList<>();
    Document doc = new Document();
    when(schemasRepository.findFieldSchema("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac"))
        .thenReturn(doc);
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rules.add(rule);
    ruleSchema.setRules(rules);
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    rulesServiceImpl.createAutomaticRules("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.GEOMETRYCOLLECTION, EntityTypeEnum.FIELD, 1L, Boolean.FALSE);
    Mockito.verify(rulesRepository, times(4)).createNewRule(Mockito.any(), Mockito.any());
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
    rulesServiceImpl.deleteEmptyRulesSchema("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(rulesRepository, times(1)).deleteByIdDatasetSchema(Mockito.any());
  }

  /**
   * Delete empty rules scehma no schema test.
   */
  @Test
  public void deleteEmptyRulesScehmaNoSchemaTest() {
    when(rulesRepository.findByIdDatasetSchema(Mockito.any())).thenReturn(null);
    rulesServiceImpl.deleteEmptyRulesSchema("5e44110d6a9e3a270ce13fac", 1L);
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
    UserRepresentationVO userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setUsername("userName");
    userRepresentationVO.setFirstName("First Name");
    userRepresentationVO.setLastName("Last Name");
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(true);
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(new HashMap<>());
    Mockito.doNothing().when(auditRepository).createAudit(Mockito.any(), Mockito.any());
    rulesServiceImpl.createNewRule(1L, new RuleVO());
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the new rule dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void createNewRuleDatasetTest() throws EEAException {

    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.TABLE);
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);
    IntegrityVO integrityVO = new IntegrityVO();
    integrityVO.setId("5e44110d6a9e3a270ce13fac");
    ruleVO.setIntegrityVO(integrityVO);
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId());
    integritySchema.setReferencedDatasetSchemaId(new ObjectId());
    Rule rule = new Rule();
    rule.setType(EntityTypeEnum.TABLE);
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setIntegrityConstraintId(new ObjectId());
    rule.setRuleId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setReferenceId(new ObjectId());
    rule.setDescription("");
    rule.setRuleName("");
    rule.setThenCondition(Arrays.asList("success", "error"));
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(integrityMapper.classToEntity(Mockito.any())).thenReturn(integritySchema);
    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    UserRepresentationVO userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setUsername("userName");
    userRepresentationVO.setFirstName("First Name");
    userRepresentationVO.setLastName("Last Name");

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(new HashMap<>());
    Mockito.doNothing().when(auditRepository).createAudit(Mockito.any(), Mockito.any());
    rulesServiceImpl.createNewRule(1L, ruleVO);
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
    RuleVO ruleVO = new RuleVO();
    ruleVO.setWhenCondition(new RuleExpressionDTO());
    try {
      rulesServiceImpl.createNewRule(1L, ruleVO);
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
    rulesServiceImpl.deleteRuleRequired("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        DataType.TEXT);
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
    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.DATASET);
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);
    ruleVO.setWhenCondition(null);
    ruleVO.setIntegrityVO(null);
    Rule rule = new Rule();
    rule.setRuleId(new ObjectId());
    rule.setReferenceId(new ObjectId());
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setType(EntityTypeEnum.FIELD);
    UserRepresentationVO userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setUsername("userName");
    userRepresentationVO.setFirstName("First Name");
    userRepresentationVO.setLastName("Last Name");
    Audit audit = new Audit();
    audit.setIdAudit(new ObjectId());
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(new HashMap<>());
    Mockito.when(auditRepository.getAuditByRuleId(Mockito.any())).thenReturn(audit);
    rulesServiceImpl.updateRule(1L, ruleVO);
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update rule dataset test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void updateRuleDatasetTest() throws EEAException {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.TABLE);
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);
    IntegrityVO integrityVO = new IntegrityVO();
    integrityVO.setId("5e44110d6a9e3a270ce13fac");
    ruleVO.setWhenCondition(null);
    ruleVO.setIntegrityVO(integrityVO);
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId());
    integritySchema.setReferencedDatasetSchemaId(new ObjectId());
    Rule rule = new Rule();
    rule.setShortCode("ft01");
    rule.setType(EntityTypeEnum.TABLE);
    rule.setIntegrityConstraintId(new ObjectId());
    rule.setRuleId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setReferenceId(new ObjectId());
    rule.setDescription("");
    rule.setRuleName("");
    rule.setThenCondition(Arrays.asList("success", "error"));
    UserRepresentationVO userRepresentationVO = new UserRepresentationVO();
    userRepresentationVO.setUsername("userName");
    userRepresentationVO.setFirstName("First Name");
    userRepresentationVO.setLastName("Last Name");
    Audit audit = new Audit();
    audit.setIdAudit(new ObjectId());
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(integrityMapper.classToEntity(Mockito.any())).thenReturn(integritySchema);
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(true);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getDetails()).thenReturn(new HashMap<>());
    Mockito.when(auditRepository.getAuditByRuleId(Mockito.any())).thenReturn(audit);
    rulesServiceImpl.updateRule(1L, ruleVO);
    Mockito.verify(rulesRepository, times(1)).updateRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update rule exception test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateRuleRuleIdExceptionTest() throws EEAException {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(new Rule());
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(new Rule());
    try {
      rulesServiceImpl.updateRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.RULE_ID_REQUIRED, e.getMessage());
      throw e;
    }
  }

  /**
   * Update rule table test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateRuleTableTest() throws EEAException {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.TABLE);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    try {
      rulesServiceImpl.updateRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE_TABLE, e.getMessage());
      throw e;
    }
  }

  /**
   * Update rule field record test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void updateRuleFieldRecordTest() throws EEAException {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.FIELD);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    try {
      rulesServiceImpl.updateRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE_FIELD_RECORD, e.getMessage());
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
    rule.setWhenCondition("testWhenCondition");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setType(EntityTypeEnum.FIELD);

    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.findRule(Mockito.any(), Mockito.any())).thenReturn(rule);
    Mockito.when(ruleMapper.classToEntity(Mockito.any())).thenReturn(rule);
    Mockito.when(rulesRepository.updateRule(Mockito.any(), Mockito.any())).thenReturn(false);
    RuleVO ruleVO = new RuleVO();
    ruleVO.setWhenCondition(new RuleExpressionDTO());
    ruleVO.setRuleId("5e44110d6a9e3a270ce13fac");
    ruleVO.setEnabled(true);
    ruleVO.setRuleName("ruleName");
    ruleVO.setDescription("description");
    ruleVO.setShortCode("shortCode");
    ruleVO.setThenCondition(Arrays.asList("ERROR", "error message"));
    try {
      rulesServiceImpl.updateRule(1L, ruleVO);
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
    UniqueConstraintSchema unique = new UniqueConstraintSchema();
    unique.setFieldSchemaIds(new ArrayList<>());
    when(uniqueRepository.findById(Mockito.any())).thenReturn(Optional.of(unique));
    DataSetSchema datasetSchema = new DataSetSchema();
    List<TableSchema> tableSchemaList = new ArrayList<>();
    TableSchema tableSchema = new TableSchema();
    RecordSchema recordSchema = new RecordSchema();
    FieldSchema fieldSchema = new FieldSchema();
    List<FieldSchema> fieldSchemaList = new ArrayList<>();
    fieldSchema.setIdFieldSchema(new ObjectId("5e44110d6a9e3a270ce13fac"));
    fieldSchemaList.add(fieldSchema);
    recordSchema.setFieldSchema(fieldSchemaList);
    tableSchema.setRecordSchema(recordSchema);
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchemaList.add(tableSchema);
    datasetSchema.setTableSchemas(tableSchemaList);
    datasetSchema.setIdDataSetSchema(new ObjectId());
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(datasetSchema);
    rulesServiceImpl.createUniqueConstraint("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac",
        "5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }

  /**
   * Creates the rule table test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createRuleTableTest() throws EEAException {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.TABLE);
    ruleVO.setRuleName("name");
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    try {
      rulesServiceImpl.createNewRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE_TABLE, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates the rule field record test.
   *
   * @throws EEAException the EEA exception
   */
  @Test(expected = EEAException.class)
  public void createRuleFieldRecordTest() throws EEAException {
    RuleVO ruleVO = new RuleVO();
    ruleVO.setType(EntityTypeEnum.FIELD);
    ruleVO.setRuleName("name");
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    try {
      rulesServiceImpl.createNewRule(1L, ruleVO);
    } catch (EEAException e) {
      Assert.assertEquals(EEAErrorMessage.ERROR_CREATING_RULE_FIELD_RECORD, e.getMessage());
      throw e;
    }
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

  /**
   * Gets the integrity constraint null test.
   *
   * @return the integrity constraint null test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getIntegrityConstraintNullTest() throws EEAException {
    when(integritySchemaRepository.findById(Mockito.any())).thenReturn(Optional.empty());
    assertNull(rulesServiceImpl.getIntegrityConstraint("5e44110d6a9e3a270ce13fac"));
  }

  /**
   * Gets the integrity constraint test.
   *
   * @return the integrity constraint test
   * @throws EEAException the EEA exception
   */
  @Test
  public void getIntegrityConstraintTest() throws EEAException {
    when(integritySchemaRepository.findById(Mockito.any()))
        .thenReturn(Optional.of(new IntegritySchema()));
    when(integrityMapper.entityToClass(Mockito.any())).thenReturn(new IntegrityVO());
    assertEquals(new IntegrityVO(),
        rulesServiceImpl.getIntegrityConstraint("5e44110d6a9e3a270ce13fac"));
  }

  /**
   * Copy rule test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void copyRuleTest() throws EEAException {
    CopySchemaVO copy = new CopySchemaVO();
    List<String> listDatasetSchemaIdToCopy = new ArrayList<>();
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    listDatasetSchemaIdToCopy.add("5e44110d6a9e3a270ce13fac");
    dictionaryOriginTargetObjectId.put("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");
    copy.setDictionaryOriginTargetObjectId(dictionaryOriginTargetObjectId);
    copy.setOriginDatasetSchemaIds(listDatasetSchemaIdToCopy);

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
    rule.setType(EntityTypeEnum.TABLE);
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("whenCondition 5e44110d6a9e3a270ce13fac");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setReferenceId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setReferenceFieldSchemaPKId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setUniqueConstraintId(new ObjectId("5e44110d6a9e3a270ce13fac"));
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
    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(true);

    List<IntegritySchema> integritySchemaList = new ArrayList<>();
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    integritySchema.setReferencedDatasetSchemaId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    integritySchema.setOriginFields(Arrays.asList(new ObjectId("5e44110d6a9e3a270ce13fac")));
    integritySchema.setReferencedFields(Arrays.asList(new ObjectId("5e44110d6a9e3a270ce13fac")));
    integritySchemaList.add(integritySchema);
    when(integritySchemaRepository.findByOriginOrReferenceDatasetSchemaId(Mockito.any()))
        .thenReturn(integritySchemaList);

    rulesServiceImpl.copyRulesSchema(copy);
    Mockito.verify(rulesRepository, times(1)).getRulesWithActiveCriteria(Mockito.any(),
        Mockito.anyBoolean());
  }

  /**
   * Delete not empty rule test.
   */
  @Test
  public void deleteNotEmptyRuleTest() {
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetSchemaIdById(Mockito.anyLong()))
        .thenReturn("5e44110d6a9e3a270ce13fac");
    Mockito.when(rulesRepository.deleteNotEmptyRule(Mockito.any(), Mockito.any())).thenReturn(true);
    rulesServiceImpl.deleteNotEmptyRule("5e44110d6a9e3a270ce13fac", 1L);
    Mockito.verify(rulesRepository, times(1)).deleteNotEmptyRule(Mockito.any(), Mockito.any());
  }

  /**
   * Update sequence test.
   */
  @Test
  public void updateSequenceTest() {
    Mockito.when(rulesSequenceRepository.updateSequence(Mockito.any())).thenReturn(1L);
    Assert.assertEquals(1L,
        rulesServiceImpl.updateSequence("5e44110d6a9e3a270ce13fac").longValue());
  }



  /**
   * Find sql sentences by dataset schema id.
   */
  @Test
  public void findSqlSentencesByDatasetSchemaIdTest() {
    Mockito.when(rulesRepository.findSqlRules(Mockito.any())).thenReturn(new ArrayList<>());
    rulesServiceImpl.findSqlSentencesByDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    Mockito.verify(rulesRepository, times(1))
        .findSqlRules(new ObjectId("5e44110d6a9e3a270ce13fac"));
  }

  @Test
  public void getIntegritySchemasTest() {
    Mockito.when(integritySchemaRepository.findByOriginDatasetSchemaId(Mockito.any()))
        .thenReturn(new ArrayList<>());
    rulesServiceImpl.getIntegritySchemas("5e44110d6a9e3a270ce13fac");
    Mockito.verify(integritySchemaRepository, times(1))
        .findByOriginDatasetSchemaId(new ObjectId("5e44110d6a9e3a270ce13fac"));
  }


  @Test
  public void insertIntegritySchemasTest() {
    IntegrityVO integrityVO = new IntegrityVO();
    integrityVO.setId("1");
    integrityVO.setRuleId("5e44110d6a9e3a270ce13fac");
    integrityVO.setOriginDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    integrityVO.setReferencedDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    integrityVO.setIsDoubleReferenced(false);
    integrityVO.setOriginFields(Arrays.asList("5e44110d6a9e3a270ce13fac"));
    integrityVO.setReferencedFields(Arrays.asList("5e44110d6a9e3a270ce13fac"));
    List<IntegrityVO> integritiesVO = new ArrayList<>();
    integritiesVO.add(integrityVO);
    List<IntegritySchema> integrities = new ArrayList<>();
    IntegritySchema integrity = new IntegritySchema();
    integrity.setId(new ObjectId());
    integrity.setOriginDatasetSchemaId(new ObjectId());
    integrity.setReferencedDatasetSchemaId(new ObjectId());
    integrities.add(integrity);
    when(integrityMapper.classListToEntity(Mockito.any())).thenReturn(integrities);

    rulesServiceImpl.insertIntegritySchemas(integritiesVO);
    Mockito.verify(integritySchemaRepository, times(1)).save(Mockito.any());
  }


  @Test
  public void getAllDisabledRulesTest() {
    DesignDatasetVO design = new DesignDatasetVO();
    design.setId(1L);
    design.setDatasetSchema("5e44110d6a9e3a270ce13fac");
    List<DesignDatasetVO> designs = new ArrayList<>();
    designs.add(design);
    Rule rule = new Rule();
    rule.setRuleId(new ObjectId());
    List<Rule> rules = new ArrayList<>();
    rules.add(rule);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(rules);
    Mockito.when(rulesRepository.getAllDisabledRules(Mockito.any())).thenReturn(ruleSchema);
    Assert.assertEquals(Integer.valueOf(1), rulesServiceImpl.getAllDisabledRules(1L, designs));
  }

  @Test
  public void getAllUncheckedRulesTest() {
    DesignDatasetVO design = new DesignDatasetVO();
    design.setId(1L);
    design.setDatasetSchema("5e44110d6a9e3a270ce13fac");
    List<DesignDatasetVO> designs = new ArrayList<>();
    designs.add(design);
    Rule rule = new Rule();
    rule.setRuleId(new ObjectId());
    List<Rule> rules = new ArrayList<>();
    rules.add(rule);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setRules(rules);
    Mockito.when(rulesRepository.getAllUncheckedRules(Mockito.any())).thenReturn(ruleSchema);
    Assert.assertEquals(Integer.valueOf(1), rulesServiceImpl.getAllUncheckedRules(1L, designs));
  }


  @Test
  public void importRuleTest() throws EEAException, IOException {

    List<String> listDatasetSchemaIdToCopy = new ArrayList<>();
    Map<String, String> dictionaryOriginTargetObjectId = new HashMap<>();
    listDatasetSchemaIdToCopy.add("5e44110d6a9e3a270ce13fac");
    dictionaryOriginTargetObjectId.put("5e44110d6a9e3a270ce13fac", "5e44110d6a9e3a270ce13fac");


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
    rule.setType(EntityTypeEnum.TABLE);
    rule.setShortCode("shortCode");
    rule.setDescription("description");
    rule.setRuleName("ruleName");
    rule.setWhenCondition("isSQLSentence 5e44110d6a9e3a270ce13fac");
    rule.setThenCondition(Arrays.asList("success", "error"));
    rule.setReferenceId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setReferenceFieldSchemaPKId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setUniqueConstraintId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    rule.setSqlSentence("sqlSentence");
    rules.add(rule);
    RulesSchema ruleSchema = new RulesSchema();
    ruleSchema.setIdDatasetSchema(new ObjectId("5e44110d6a9e3a270ce13fac"));
    ruleSchema.setRules(rules);
    IntegrityVO integrityVO = new IntegrityVO();
    integrityVO.setId(id.toString());
    integrityVO.setRuleId(new ObjectId().toString());
    integrityVO.setOriginDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    integrityVO.setReferencedDatasetSchemaId("5e44110d6a9e3a270ce13fac");
    integrityVO.setOriginFields(Arrays.asList("5e44110d6a9e3a270ce13fac"));
    integrityVO.setReferencedFields(Arrays.asList("5e44110d6a9e3a270ce13fac"));
    integrities.add(new IntegritySchema());
    listIntegrityVO.add(integrityVO);
    RulesSchemaVO ruleSchemaVO = new RulesSchemaVO();
    ruleSchemaVO.setRules(rulesVO);

    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(true);

    List<IntegritySchema> integritySchemaList = new ArrayList<>();
    IntegritySchema integritySchema = new IntegritySchema();
    integritySchema.setRuleId(new ObjectId());
    integritySchema.setOriginDatasetSchemaId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    integritySchema.setReferencedDatasetSchemaId(new ObjectId("5e44110d6a9e3a270ce13fac"));
    integritySchema.setOriginFields(Arrays.asList(new ObjectId("5e44110d6a9e3a270ce13fac")));
    integritySchema.setReferencedFields(Arrays.asList(new ObjectId("5e44110d6a9e3a270ce13fac")));
    integritySchemaList.add(integritySchema);

    Mockito.when(rulesRepository.createNewRule(Mockito.any(), Mockito.any())).thenReturn(true);
    Mockito.when(integrityMapper.classListToEntity(Mockito.any())).thenReturn(integritySchemaList);


    ObjectMapper objectMapperRules = new ObjectMapper();
    InputStream rulesStream =
        new ByteArrayInputStream(objectMapperRules.writeValueAsBytes(ruleSchema));
    List<byte[]> qcRulesList = new ArrayList<>();
    qcRulesList.add(IOUtils.toByteArray(rulesStream));

    rulesServiceImpl.importRulesSchema(qcRulesList, dictionaryOriginTargetObjectId,
        listIntegrityVO);
    Mockito.verify(rulesRepository, times(1)).createNewRule(Mockito.any(), Mockito.any());
  }


}
