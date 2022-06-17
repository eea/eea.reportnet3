package org.eea.validation.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.CopySchemaVO;
import org.eea.interfaces.vo.dataset.schemas.audit.DatasetHistoricRuleVO;
import org.eea.interfaces.vo.dataset.schemas.audit.RuleHistoricInfoVO;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.enums.AutomaticRuleTypeEnum;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.mapper.DatasetHistoricRuleMapper;
import org.eea.validation.mapper.IntegrityMapper;
import org.eea.validation.mapper.RuleHistoricInfoMapper;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.repository.AuditRepository;
import org.eea.validation.persistence.repository.IntegritySchemaRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.RulesSequenceRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.repository.UniqueConstraintRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.UniqueConstraintSchema;
import org.eea.validation.persistence.schemas.audit.Audit;
import org.eea.validation.persistence.schemas.audit.RuleHistoricInfo;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RulesService;
import org.eea.validation.service.SqlRulesService;
import org.eea.validation.util.AutomaticRules;
import org.eea.validation.util.KieBaseManager;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

/**
 * The Class ValidationService.
 */
@Service("RulesService")
public class RulesServiceImpl implements RulesService {

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The data set metabase controller zuul. */
  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  /** The rules schema mapper. */
  @Autowired
  private RulesSchemaMapper rulesSchemaMapper;

  /** The rules sequence repository. */
  @Autowired
  private RulesSequenceRepository rulesSequenceRepository;

  /** The rule mapper. */
  @Autowired
  private RuleMapper ruleMapper;

  /** The integrity schema repository. */
  @Autowired
  private IntegritySchemaRepository integritySchemaRepository;

  /** The integrity mapper. */
  @Autowired
  private IntegrityMapper integrityMapper;

  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

  /** The sql rules service. */
  @Autowired
  private SqlRulesService sqlRulesService;

  /** The record store controller. */
  @Autowired
  private RecordStoreControllerZuul recordStoreController;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The unique repository. */
  @Autowired
  private UniqueConstraintRepository uniqueRepository;

  /** The dataset controller zuul. */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /** The audit repository. */
  @Autowired
  private AuditRepository auditRepository;

  /** The user management controller zuul. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZuul;

  /** The rule historic info mapper. */
  @Autowired
  private RuleHistoricInfoMapper ruleHistoricInfoMapper;

  /** The dataset historic rule mapper. */
  @Autowired
  private DatasetHistoricRuleMapper datasetHistoricRuleMapper;

  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;


  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RulesServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant FC_DESCRIPTION. */
  private static final String FC_DESCRIPTION = "Checks if the field is missing or empty";

  /** The Constant FT_DESCRIPTION. */
  private static final String FT_DESCRIPTION = "Checks if the field is a valid ";

  /** The Constant TB_DESCRIPTION. */
  private static final String TC_DESCRIPTION = "Checks if the record based on criteria is valid ";

  /** The Constant TO_DESCRIPTION: {@value}. */
  private static final String TO_DESCRIPTION =
      "Checks if contains all the records based on set criteria.";

  /** The Constant FIELD_TYPE. */
  private static final String FIELD_TYPE = "Field type ";

  /** The Constant DATASET: {@value}. */
  private static final String DATASET = "dataset_";

  /** The Constant TABLE: {@value}. */
  private static final String TABLE = "Table";

  /** The Constant FIELD: {@value}. */
  private static final String FIELD = "Field";

  /** The Constant CODE: {@value}. */
  private static final String CODE = "Code";

  /** The Constant QCNAME: {@value}. */
  private static final String QCNAME = "QC Name";

  /** The Constant QCDESC: {@value}. */
  private static final String QCDESC = "QC Description";

  /** The Constant MESSAGE: {@value}. */
  private static final String MESSAGE = "Message";

  /** The Constant EXPRESSION: {@value}. */
  private static final String EXPRESSION = "Expression";

  /** The Constant TYPE_OF_QC: {@value}. */
  private static final String TYPE_OF_QC = "Type of QC";

  /** The Constant LEVEL_ERROR: {@value}. */
  private static final String LEVEL_ERROR = "Level Error";

  /** The Constant CREATION_MODE: {@value}. */
  private static final String CREATION_MODE = "Creation Mode";

  /** The Constant STATUS: {@value}. */
  private static final String STATUS = "Status";

  /** The Constant VALID: {@value}. */
  private static final String VALID = "Valid";

  /** The path public file. */
  @Value("${validationExportPathFile}")
  private String pathPublicFile;

  /**
   * The delimiter.
   */
  @Value("${exportDataDelimiter}")
  private char delimiter;

  /** The Constant DOWNLOAD_QC_EXCEPTION: {@value}. */
  private static final String DOWNLOAD_QC_EXCEPTION =
      "Download exported QC's didn't found a file with the followings parameters:, datasetID: %s + filename: %s";


  /**
   * Gets the rules schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   *
   * @return the rules schema by dataset id
   */
  @Override
  public RulesSchemaVO getRulesSchemaByDatasetId(String datasetSchemaId) {
    RulesSchema rulesSchema =
        rulesRepository.getRulesWithActiveCriteria(new ObjectId(datasetSchemaId), false);
    RulesSchemaVO rulesVO = null;
    Long designDatasetRelated =
        dataSetMetabaseControllerZuul.getDesignDatasetIdByDatasetSchemaId(datasetSchemaId);
    if (null == rulesSchema) {
      rulesSchema = null;
    } else {
      List<Audit> audits = auditRepository.getAuditsByDatasetId(designDatasetRelated);
      for (Rule rule : rulesSchema.getRules()) {
        if (null != rule.getAutomaticType()
            && AutomaticRuleTypeEnum.FIELD_SQL_TYPE.equals(rule.getAutomaticType())) {
          rule.setSqlSentence(null);
        }

        Optional<Audit> aud = audits.stream()
            .filter(audit -> audit.getHistoric().get(0).getRuleId().equals(rule.getRuleId()))
            .findFirst();
        if (aud.isPresent()) {
          rule.setHasHistoric(true);
        }
      }
      rulesVO = rulesSchemaMapper.entityToClass(rulesSchema);
    }
    setIntegrityIntoVO(rulesSchema, rulesVO);
    return rulesVO;
  }

  /**
   * Gets the active rules schema by dataset id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the active rules schema by dataset id
   */
  @Override
  public RulesSchemaVO getActiveRulesSchemaByDatasetId(String datasetSchemaId) {
    RulesSchema rulesSchema =
        rulesRepository.getRulesWithActiveCriteria(new ObjectId(datasetSchemaId), true);

    RulesSchemaVO rulesVO =
        rulesSchema == null ? null : rulesSchemaMapper.entityToClass(rulesSchema);
    setIntegrityIntoVO(rulesSchema, rulesVO);
    return rulesVO;
  }

  /**
   * Creates the empty rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rulesSchemaId the rules schema id
   */
  @Override
  public void createEmptyRulesSchema(String datasetSchemaId, String rulesSchemaId) {
    RulesSchema rSchema = new RulesSchema();
    rSchema.setIdDatasetSchema(new ObjectId(datasetSchemaId));
    rSchema.setRulesSchemaId(new ObjectId(rulesSchemaId));
    rSchema.setRules(new ArrayList<Rule>());
    rulesRepository.save(rSchema);
  }

  /**
   * Delete empty rules schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @Override
  public void deleteEmptyRulesSchema(String datasetSchemaId, Long datasetId) {
    RulesSchema ruleSchema = rulesRepository.findByIdDatasetSchema(new ObjectId(datasetSchemaId));
    if (null != ruleSchema) {
      // Check first if the rule has an integrity type rule to delete the associated data
      deleteDatasetRuleAndIntegrityByDatasetSchemaId(datasetSchemaId, datasetId);
      rulesRepository.deleteByIdDatasetSchema(ruleSchema.getIdDatasetSchema());
      rulesSequenceRepository.deleteByDatasetSchemaId(ruleSchema.getIdDatasetSchema());
    }
  }

  /**
   * Delete rule by id.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteRuleById(long datasetId, String ruleId) throws EEAException {

    String datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    if (datasetSchemaId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    Rule rule = rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleId));

    if (null != rule && EntityTypeEnum.TABLE.equals(rule.getType())
        && rule.getIntegrityConstraintId() != null) {
      Optional<IntegritySchema> integritySchema =
          integritySchemaRepository.findById(rule.getIntegrityConstraintId());
      if (integritySchema.isPresent()) {
        dataSetMetabaseControllerZuul.deleteForeignRelationship(datasetId, null,
            integritySchema.get().getOriginDatasetSchemaId().toString(),
            integritySchema.get().getReferencedDatasetSchemaId().toString());
      }
      integritySchemaRepository.deleteById(rule.getIntegrityConstraintId());
    }
    rulesRepository.deleteRuleById(new ObjectId(datasetSchemaId), new ObjectId(ruleId));
  }

  /**
   * Delete rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  public void deleteRuleByReferenceId(String datasetSchemaId, String referenceId) {
    rulesRepository.deleteRuleByReferenceId(new ObjectId(datasetSchemaId),
        new ObjectId(referenceId));
  }


  /**
   * Delete automatic rule by reference id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  public void deleteAutomaticRuleByReferenceId(String datasetSchemaId, String referenceId) {
    rulesRepository.deleteAutomaticRuleByReferenceId(new ObjectId(datasetSchemaId),
        new ObjectId(referenceId));
  }

  /**
   * Delete rule by reference field schema PK id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceFieldSchemaPKId the reference field schema PK id
   */
  @Override
  public void deleteRuleByReferenceFieldSchemaPKId(String datasetSchemaId,
      String referenceFieldSchemaPKId) {
    rulesRepository.deleteRuleByReferenceFieldSchemaPKId(new ObjectId(datasetSchemaId),
        new ObjectId(referenceFieldSchemaPKId));
  }

  /**
   * Validate rule.
   *
   * @param rule the rule
   * @throws EEAException the EEA exception
   */
  private void validateRule(Rule rule) throws EEAException {

    if (rule.getRuleId() == null) {
      throw new EEAException(EEAErrorMessage.RULE_ID_REQUIRED);
    }

    if (rule.getReferenceId() == null) {
      throw new EEAException(EEAErrorMessage.REFERENCE_ID_REQUIRED);
    }

    if (rule.getDescription() == null) {
      throw new EEAException(EEAErrorMessage.DESCRIPTION_REQUIRED);
    }

    if (rule.getRuleName() == null) {
      throw new EEAException(EEAErrorMessage.RULE_NAME_REQUIRED);
    }

    if (rule.getWhenCondition() == null) {
      throw new EEAException(EEAErrorMessage.WHEN_CONDITION_REQUIRED);
    }

    if (rule.getThenCondition() == null || rule.getThenCondition().size() != 2) {
      throw new EEAException(EEAErrorMessage.THEN_CONDITION_REQUIRED);
    }

    if (rule.getShortCode() == null) {
      throw new EEAException(EEAErrorMessage.SHORT_CODE_REQUIRED);
    }

    if (rule.getType() == null) {
      throw new EEAException(EEAErrorMessage.ENTITY_TYPE_REQUIRED);
    }
  }

  /**
   * Creates the new rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void createNewRule(long datasetId, RuleVO ruleVO) throws EEAException {

    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    if (dataset == null || dataset.getDatasetSchema() == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    String datasetSchemaId = dataset.getDatasetSchema();
    if (EntityTypeEnum.TABLE.equals(ruleVO.getType()) && ruleVO.getIntegrityVO() == null
        && StringUtils.isBlank(ruleVO.getSqlSentence()) && !ruleVO.isAutomatic()) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE_TABLE);
    }
    if ((EntityTypeEnum.RECORD.equals(ruleVO.getType())
        || EntityTypeEnum.FIELD.equals(ruleVO.getType()))
        && StringUtils.isBlank(ruleVO.getSqlSentence()) && null == ruleVO.getWhenCondition()) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE_FIELD_RECORD);
    }

    Rule rule = ruleMapper.classToEntity(ruleVO);
    rule.setRuleId(new ObjectId());
    rule.setAutomatic(false);
    rule.setActivationGroup(null);
    rule.setVerified(null);

    if (rule.getSqlSentence() != null) {
      try {
        rule.setSqlCost(sqlRulesService.evaluateSqlRule(datasetId, rule.getSqlSentence()));
      } catch (ParseException | EEAException e) {
        rule.setSqlCost(null);
      }
    }

    if (null == ruleVO.getWhenCondition()) {
      rulesWhenConditionNull(datasetId, ruleVO, datasetSchemaId, rule);
    } else {
      createRule(datasetSchemaId, rule);
      kieBaseManager.validateRule(datasetSchemaId, rule);
    }
    DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataset.getDataflowId());
    if (TypeStatusEnum.DRAFT.equals(dataflow.getStatus())) {
      addHistoricRuleInfo(rule, null, datasetId, null);
    }
  }

  /**
   * Rules when condition null.That kind of rules is diferent from manual rules, this method is for
   * any of manual rules about record or field
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @throws EEAException the EEA exception
   */
  private void rulesWhenConditionNull(long datasetId, RuleVO ruleVO, String datasetSchemaId,
      Rule rule) throws EEAException {
    // we create the whencondition Integrity for the rule
    if (EntityTypeEnum.TABLE.equals(ruleVO.getType()) && ruleVO.getIntegrityVO() != null) {
      ObjectId integrityConstraintId = new ObjectId();
      IntegrityVO integrityVO = ruleVO.getIntegrityVO();
      integrityVO.setId(integrityConstraintId.toString());
      IntegritySchema integritySchema = integrityMapper.classToEntity(integrityVO);
      integritySchema.setRuleId(rule.getRuleId());
      integritySchemaRepository.save(integritySchema);
      rule.setVerified(true);
      rule.setEnabled(ruleVO.isEnabled());
      rule.setIntegrityConstraintId(integrityConstraintId);
      rule.setWhenCondition("checkIntegrityConstraint(this.datasetId,'"
          + integrityConstraintId.toString() + "','" + rule.getRuleId().toString() + "')");
      Long datasetReferencedId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrityVO.getReferencedDatasetSchemaId());
      dataSetMetabaseControllerZuul.createDatasetForeignRelationship(datasetId, datasetReferencedId,
          integrityVO.getOriginDatasetSchemaId(), integrityVO.getReferencedDatasetSchemaId());
      // send notification
      NotificationVO notificationVO = NotificationVO.builder()
          .user(SecurityContextHolder.getContext().getAuthentication().getName())
          .datasetSchemaId(datasetSchemaId).shortCode(rule.getShortCode()).build();
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATED_QC_RULE_EVENT, null,
          notificationVO);

    } else if (EntityTypeEnum.TABLE.equals(ruleVO.getType()) && ruleVO.isAutomatic()
        && (StringUtils.isBlank(ruleVO.getSqlSentence()))) {
      rule.setAutomatic(true);
      rule.setVerified(true);
      rule.setEnabled(true);
      rule.setWhenCondition("isTableEmpty(this)");
    } else if (null != ruleVO.getSqlSentence() && !ruleVO.getSqlSentence().isEmpty()) {
      if (rule.getSqlSentence().contains("!=")) {
        rule.setSqlSentence(rule.getSqlSentence().replace("!=", "<>"));
      }
      rule.setWhenCondition(new StringBuilder().append("isSQLSentenceWithCode(this.datasetId.id, '")
          .append(rule.getRuleId().toString())
          .append(
              "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX'")
          .append(")").toString());
      recordStoreController.createUpdateQueryView(datasetId, false);
      sqlRulesService.validateSQLRule(datasetId, datasetSchemaId, rule);
    }
    createRule(datasetSchemaId, rule);
  }

  /**
   * Creates the rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @throws EEAException the EEA exception
   */
  private void createRule(String datasetSchemaId, Rule rule) throws EEAException {
    validateRule(rule);
    if (!rulesRepository.createNewRule(new ObjectId(datasetSchemaId), rule)) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE);
    }
  }

  /**
   * Creates the automatic rules. That method create all automatic rules, and this check if that
   * rules are about diferent types
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param datasetId the dataset id
   * @param required the required
   * @throws EEAException the EEA exception
   */
  @Override
  public void createAutomaticRules(String datasetSchemaId, String referenceId, DataType typeData,
      EntityTypeEnum typeEntityEnum, Long datasetId, boolean required) throws EEAException {
    Document document;
    List<Rule> ruleList = new ArrayList<>();
    // we use that if to sort between a rule required and rule for any other type(Boolean,
    // number etc)
    Long shortcode = rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));

    if (required && typeData.equals(DataType.POINT)) {
      ruleList.add(
          AutomaticRules.createRequiredRulePoint(referenceId, typeEntityEnum, "Field cardinality",
              "FC" + shortcode, AutomaticRuleTypeEnum.FIELD_CARDINALITY, FC_DESCRIPTION));
    } else if (required) {
      ruleList
          .add(AutomaticRules.createRequiredRule(referenceId, typeEntityEnum, "Field cardinality",
              "FC" + shortcode, AutomaticRuleTypeEnum.FIELD_CARDINALITY, FC_DESCRIPTION));
    } else {
      switch (typeData) {
        case NUMBER_INTEGER:
          ruleList.add(AutomaticRules.createNumberIntegerAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + "NUMBER - INTEGER", "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + "NUMBER - INTEGER"));
          break;
        case NUMBER_DECIMAL:
          ruleList.add(AutomaticRules.createNumberDecimalAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + "NUMBER - DECIMAL", "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + "NUMBER - DECIMAL"));
          break;
        case DATE:
          ruleList.add(AutomaticRules.createDateAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + typeData));
          break;
        case DATETIME:
          ruleList.add(AutomaticRules.createDateTimeAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + typeData));
          break;
        case BOOLEAN:
          ruleList.add(AutomaticRules.createBooleanAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + typeData));
          break;
        case EXTERNAL_LINK:
        case LINK:
          // we call this method to find the tableschemaid because we want to create that validation
          // at TABLE level
          // that is for avoid do many calls to database and collapse it
          DataSetSchema datasetSchema =
              schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));
          String tableSchemaId = getTableSchemaIdFromIdFieldSchema(datasetSchema, referenceId);
          FieldSchema fieldSchemaPK = getPKFieldSchemaFromSchema(datasetSchema, referenceId);

          ruleList.add(AutomaticRules.createFKAutomaticRule(referenceId, EntityTypeEnum.TABLE,
              FIELD_TYPE + typeData, "TC" + shortcode, AutomaticRuleTypeEnum.FIELD_LINK,
              TC_DESCRIPTION + typeData, tableSchemaId, false));

          if (null != fieldSchemaPK && Boolean.TRUE.equals(fieldSchemaPK.getPkMustBeUsed())) {

            Long shortcodeAux =
                rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));

            ruleList.add(AutomaticRules.createFKAutomaticRule(referenceId, EntityTypeEnum.TABLE,
                "Table Completeness", "TO" + shortcodeAux, AutomaticRuleTypeEnum.TABLE_COMPLETNESS,
                TO_DESCRIPTION, tableSchemaId, true));
          }

          break;
        case CODELIST:
          // we find values available to create this validation for a codelist, same value with
          // capital letter and without capital letters
          document = schemasRepository.findFieldSchema(datasetSchemaId, referenceId);
          List<String> singleCodeListItems = (ArrayList) document.get("codelistItems");
          ruleList.addAll(AutomaticRules.createCodelistAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, singleCodeListItems, "FT" + shortcode,
              AutomaticRuleTypeEnum.FIELD_TYPE, FT_DESCRIPTION + "SINGLESELECT_CODELIST"));
          break;
        case MULTISELECT_CODELIST:
          // we find values available to create this validation for a codelist, same value with
          // capital letter and without capital letters
          document = schemasRepository.findFieldSchema(datasetSchemaId, referenceId);
          List<String> codeListItems = (ArrayList) document.get("codelistItems");
          ruleList.addAll(AutomaticRules.createMultiSelectCodelistAutomaticRule(referenceId,
              typeEntityEnum, FIELD_TYPE + typeData, codeListItems, "FT" + shortcode,
              AutomaticRuleTypeEnum.FIELD_TYPE, FT_DESCRIPTION + typeData));
          break;
        case URL:
          ruleList.add(AutomaticRules.createUrlAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + typeData));
          break;
        case EMAIL:
          ruleList.add(AutomaticRules.createEmailAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + typeData));
          break;
        case PHONE:
          ruleList.add(AutomaticRules.createPhoneAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_TYPE,
              FT_DESCRIPTION + typeData));
          break;
        case MULTIPOLYGON:
        case POINT:
        case MULTIPOINT:
        case LINESTRING:
        case MULTILINESTRING:
        case POLYGON:
        case GEOMETRYCOLLECTION:
          ruleList.add(AutomaticRules.createGeometryAutomaticRule(typeData, referenceId,
              typeEntityEnum, FIELD_TYPE + typeData, "FT" + shortcode,
              AutomaticRuleTypeEnum.FIELD_TYPE, FT_DESCRIPTION + typeData));
          // add additional rule for the EPSG SRID check
          shortcode = rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));
          ruleList.add(AutomaticRules.createGeometryAutomaticRuleCheckEPSGSRID(typeData,
              referenceId, typeEntityEnum, FIELD_TYPE + typeData, "FT" + shortcode,
              AutomaticRuleTypeEnum.FIELD_TYPE, FT_DESCRIPTION + typeData));
          // add SQL check for Geometries.
          if (rulesRepository.findGeometrySQLRulesByreferenceId(new ObjectId(datasetSchemaId),
              new ObjectId(referenceId)) == null) {
            shortcode = rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));
            document = schemasRepository.findFieldSchema(datasetSchemaId, referenceId);
            // Validate Geometry
            ruleList.add(AutomaticRules.createGeometryAutomaticRuleCheckGeometries(datasetId,
                document, typeData, referenceId, typeEntityEnum, FIELD_TYPE + typeData,
                "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_SQL_TYPE, FT_DESCRIPTION + typeData));
            // ST_Transform
            shortcode = rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));
            ruleList.add(AutomaticRules.createGeometryAutomaticRuleCheckSTtransform(datasetId,
                document, typeData, referenceId, typeEntityEnum, FIELD_TYPE + typeData,
                "FT" + shortcode, AutomaticRuleTypeEnum.FIELD_SQL_TYPE, FT_DESCRIPTION + typeData));
          }
          break;
        default:
          LOG.info("This Data Type has not automatic rule {}", typeData.getValue());
          break;
      }
    }
    if (!ruleList.isEmpty()) {
      ruleList.stream()
          .forEach(rule -> rulesRepository.createNewRule(new ObjectId(datasetSchemaId), rule));
    }
  }


  /**
   * Gets the PK field schema from schema.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the PK field schema from schema
   */
  private FieldSchema getPKFieldSchemaFromSchema(DataSetSchema schema, String idFieldSchema) {

    FieldSchema field = null;
    boolean locatedPK = false;

    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema fieldAux : table.getRecordSchema().getFieldSchema()) {
        if (fieldAux.getIdFieldSchema().toString().equals(idFieldSchema)) {
          field = fieldAux;
          locatedPK = true;
          break;
        }
      }
      if (locatedPK) {
        break;
      }
    }
    return field;
  }


  /**
   * Gets the table schema id from id field schema. We look for a
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   * @return the table schema id from id field schema
   */
  private String getTableSchemaIdFromIdFieldSchema(DataSetSchema schema, String idFieldSchema) {
    String tableSchemaId = "";
    Boolean locatedTable = false;
    for (TableSchema table : schema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().toString().equals(idFieldSchema)) {
          tableSchemaId = table.getIdTableSchema().toString();
          locatedTable = Boolean.TRUE;
          break;
        }
      }
      if (locatedTable.equals(Boolean.TRUE)) {
        break;
      }
    }
    return tableSchemaId;
  }

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @param typeData the type data
   */
  @Override
  public void deleteRuleRequired(String datasetSchemaId, String referenceId, DataType typeData) {
    if (typeData.equals(DataType.POINT)) {
      rulesRepository.deleteRulePointRequired(new ObjectId(datasetSchemaId),
          new ObjectId(referenceId));
    } else {
      rulesRepository.deleteRuleRequired(new ObjectId(datasetSchemaId), new ObjectId(referenceId));
    }
  }

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  @Override
  public boolean existsRuleRequired(String datasetSchemaId, String referenceId) {
    return rulesRepository.existsRuleRequired(new ObjectId(datasetSchemaId),
        new ObjectId(referenceId));
  }

  /**
   * Update rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateRule(long datasetId, RuleVO ruleVO) throws EEAException {
    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    if (dataset == null || dataset.getDatasetSchema() == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    String datasetSchemaId = dataset.getDatasetSchema();
    if (EntityTypeEnum.TABLE.equals(ruleVO.getType()) && ruleVO.getIntegrityVO() == null
        && StringUtils.isBlank(ruleVO.getSqlSentence())) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE_TABLE);
    }
    if ((EntityTypeEnum.RECORD.equals(ruleVO.getType())
        || EntityTypeEnum.FIELD.equals(ruleVO.getType()))
        && StringUtils.isBlank(ruleVO.getSqlSentence()) && null == ruleVO.getWhenCondition()) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE_FIELD_RECORD);
    }
    var ruleOriginal =
        rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleVO.getRuleId()));
    if (null == ruleOriginal) {
      throw new EEAException(EEAErrorMessage.RULE_NOT_FOUND);
    }
    var originalIntegrityVO = new IntegrityVO();
    if (ruleOriginal.getIntegrityConstraintId() != null) {
      originalIntegrityVO =
          getIntegrityConstraint(ruleOriginal.getIntegrityConstraintId().toString());
    }
    Rule rule = ruleMapper.classToEntity(ruleVO);
    rule.setAutomatic(false);
    rule.setActivationGroup(null);
    rule.setVerified(null);
    if (rule.getSqlSentence() != null) {
      try {
        rule.setSqlCost(sqlRulesService.evaluateSqlRule(datasetId, rule.getSqlSentence()));
      } catch (ParseException | EEAException e) {
        rule.setSqlCost(null);
      }
    }
    DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataset.getDataflowId());
    TypeStatusEnum dataflowStatus = dataflow.getStatus();
    if (null == ruleVO.getWhenCondition()) {
      ruleWhenCondtionUpdateNull(datasetId, ruleVO, datasetSchemaId, rule, dataflowStatus);
    } else {
      validateRule(rule);
      if (!rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule)) {
        throw new EEAException(EEAErrorMessage.ERROR_UPDATING_RULE);
      }
      kieBaseManager.validateRule(datasetSchemaId, rule);
    }

    if (TypeStatusEnum.DRAFT.equals(dataflowStatus)) {
      addHistoricRuleInfo(rule, ruleOriginal, datasetId, originalIntegrityVO);
    }
  }

  /**
   * Rules when condition null.That kind of rules is diferent from manual rules, this method is for
   * any of manual rules about record or field
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @param dataflowStatus the dataflow status
   * @throws EEAException the EEA exception
   */
  private void ruleWhenCondtionUpdateNull(long datasetId, RuleVO ruleVO, String datasetSchemaId,
      Rule rule, TypeStatusEnum dataflowStatus) throws EEAException {
    if (EntityTypeEnum.TABLE.equals(ruleVO.getType()) && ruleVO.getIntegrityVO() != null) {

      IntegritySchema integritySchema = integrityMapper.classToEntity(ruleVO.getIntegrityVO());
      integritySchemaRepository.deleteById(new ObjectId(ruleVO.getIntegrityVO().getId()));
      integritySchema.setRuleId(new ObjectId(ruleVO.getRuleId()));
      integritySchemaRepository.save(integritySchema);

      rule.setVerified(true);
      rule.setEnabled(ruleVO.isEnabled());
      rule.setWhenCondition("checkIntegrityConstraint(this.datasetId,'"
          + integritySchema.getId().toString() + "','" + rule.getRuleId().toString() + "')");
      dataSetMetabaseControllerZuul.updateDatasetForeignRelationship(datasetId, datasetId,
          integritySchema.getOriginDatasetSchemaId().toString(),
          integritySchema.getReferencedDatasetSchemaId().toString());
      rule.setIntegrityConstraintId(integritySchema.getId());
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATED_QC_RULE_EVENT, null,
          NotificationVO.builder()
              .user(SecurityContextHolder.getContext().getAuthentication().getName())
              .datasetSchemaId(datasetSchemaId).shortCode(rule.getShortCode()).build());
    } else if (null != ruleVO.getSqlSentence() && !ruleVO.getSqlSentence().isEmpty()) {
      if (rule.getSqlSentence().contains("!=")) {
        rule.setSqlSentence(rule.getSqlSentence().replaceAll("!=", "<>"));
      }
      rule.setWhenCondition(new StringBuilder().append("isSQLSentenceWithCode(this.datasetId.id,'")
          .append(rule.getRuleId().toString())
          .append(
              "', this.records.size > 0 && this.records.get(0) != null && this.records.get(0).dataProviderCode != null ? this.records.get(0).dataProviderCode : 'XX'")
          .append(")").toString());
      if (!TypeStatusEnum.DRAFT.equals(dataflowStatus)) {
        recordStoreController.createUpdateQueryView(datasetId, false);
      }
      sqlRulesService.validateSQLRule(datasetId, datasetSchemaId, rule);
    }
    validateRule(rule);
    if (!rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule)) {
      throw new EEAException(EEAErrorMessage.ERROR_UPDATING_RULE);
    }
  }

  /**
   * Update automatic rule.
   *
   * @param datasetId the dataset id
   * @param ruleVO the rule VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void updateAutomaticRule(long datasetId, RuleVO ruleVO) throws EEAException {

    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    String ruleId = ruleVO.getRuleId();

    if (null != dataset && null != dataset.getDatasetSchema()) {
      String datasetSchemaId = dataset.getDatasetSchema();
      if (null != ruleId && ObjectId.isValid(ruleId)) {

        // Find the actual rule
        Rule rule = rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleId));
        if (null != rule) {
          Rule originalRule =
              rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleId));
          if (null == originalRule) {
            LOG_ERROR.error("RuleId not valid: {}", ruleId);
            throw new EEAException(EEAErrorMessage.RULEID_INCORRECT);
          }
          // Update only allowed properties
          updateAllowedRuleProperties(ruleVO, rule);
          // Save the modified rule
          rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
          DataFlowVO dataflow = dataflowControllerZuul.getMetabaseById(dataset.getDataflowId());
          if (TypeStatusEnum.DRAFT.equals(dataflow.getStatus())) {
            addHistoricRuleInfo(rule, originalRule, datasetId, null);
          }
        } else {
          LOG_ERROR.error("Rule not found for datasetSchemaId {} and ruleId {}", datasetSchemaId,
              ruleId);
          throw new EEAException(
              String.format(EEAErrorMessage.RULE_NOT_FOUND, datasetSchemaId, ruleId));
        }
      } else {
        LOG_ERROR.error("RuleId not valid: {}", ruleId);
        throw new EEAException(EEAErrorMessage.RULEID_INCORRECT);
      }
    } else {
      LOG_ERROR.error("DatasetSchemaId not found for datasetId {}", datasetId);
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
  }


  /**
   * Update allowed rule properties.
   *
   * @param ruleVO the rule VO
   * @param rule the rule
   */
  private void updateAllowedRuleProperties(RuleVO ruleVO, Rule rule) {

    rule.setEnabled(ruleVO.isEnabled());

    if (null != ruleVO.getRuleName()) {
      rule.setRuleName(ruleVO.getRuleName());
    }

    if (null != ruleVO.getDescription()) {
      rule.setDescription(ruleVO.getDescription());
    }

    if (null != ruleVO.getShortCode()) {
      rule.setShortCode(ruleVO.getShortCode());
    }

    if (null != ruleVO.getThenCondition() && ruleVO.getThenCondition().size() == 2) {
      rule.setThenCondition(ruleVO.getThenCondition());
    }
  }

  /**
   * Creates the unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param tableSchemaId the table schema id
   * @param uniqueId the unique id
   */
  @Override
  public void createUniqueConstraint(String datasetSchemaId, String tableSchemaId,
      String uniqueId) {
    Long shortcode = rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));
    StringBuilder description = new StringBuilder();
    StringBuilder message = new StringBuilder();
    Optional<UniqueConstraintSchema> uniqueResult =
        uniqueRepository.findById(new ObjectId(uniqueId));
    if (uniqueResult.isPresent()) {
      int i = 0;
      StringBuilder fieldNames = new StringBuilder();
      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));
      Map<ObjectId, String> fieldSchemaIdName = new HashMap<>();
      for (TableSchema table : schema.getTableSchemas()) {
        for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
          fieldSchemaIdName.put(field.getIdFieldSchema(), field.getHeaderName());
        }
      }

      for (ObjectId fieldSchemaId : uniqueResult.get().getFieldSchemaIds()) {
        if (uniqueResult.get().getFieldSchemaIds().size() - 1 == i
            && uniqueResult.get().getFieldSchemaIds().size() > 1) {
          fieldNames.append(" and ");
        } else if (i < uniqueResult.get().getFieldSchemaIds().size() - 1
            && uniqueResult.get().getFieldSchemaIds().size() > 1 && i != 0) {
          fieldNames.append(", ");
        }
        fieldNames.append(fieldSchemaIdName.get(fieldSchemaId));
        i++;
      }
      if (uniqueResult.get().getFieldSchemaIds().size() > 1) {
        description.append("Checks if ").append(fieldNames).append(" are uniques within table");
        message.append("The fields ").append(fieldNames).append(" are uniques within table");
      } else {
        description.append("Checks if ").append(fieldNames).append(" is unique within table");
        message.append("The field ").append(fieldNames).append(" is unique within table");
      }
    }
    Rule rule =
        AutomaticRules.createUniqueConstraintAutomaticRule(tableSchemaId, EntityTypeEnum.TABLE,
            "Table type uniqueConstraint", "TU" + shortcode, AutomaticRuleTypeEnum.TABLE_UNIQUENESS,
            description.toString(), message.toString(), uniqueId);
    rulesRepository.createNewRule(new ObjectId(datasetSchemaId), rule);
  }

  /**
   * Delete unique constraint.
   *
   * @param datasetSchemaId the dataset schema id
   * @param uniqueId the unique id
   */
  @Override
  public void deleteUniqueConstraint(String datasetSchemaId, String uniqueId) {
    rulesRepository.deleteByUniqueConstraintId(new ObjectId(datasetSchemaId),
        new ObjectId(uniqueId));
  }

  /**
   * Delete rule high level like. That service delete the rules with high level
   * (record,table,dataset) for a deleted fieldSchemaId
   *
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   */
  @Override
  public void deleteRuleHighLevelLike(String datasetSchemaId, String fieldSchemaId) {
    boolean deleted =
        rulesRepository.deleteRuleHighLevelLike(new ObjectId(datasetSchemaId), fieldSchemaId);

    if (deleted) {
      LOG.info(
          "Rules associated with fieldSchemaId {} in datasetSchemaId {} , were deleted in high level(record,table,dataset)",
          fieldSchemaId, datasetSchemaId);
    } else {
      LOG.info(
          "No rules associated with fieldSchemaId {} in datasetSchemaId {} in high level(record,table,dataset)",
          fieldSchemaId, datasetSchemaId);
    }
  }

  /**
   * Gets the integrity constraint.
   *
   * @param integrityId the integrity id
   * @return the integrity constraint
   */
  @Override
  public IntegrityVO getIntegrityConstraint(String integrityId) {
    IntegritySchema integritySchema =
        integritySchemaRepository.findById(new ObjectId(integrityId)).orElse(null);
    return integritySchema == null ? null : integrityMapper.entityToClass(integritySchema);
  }

  /**
   * Sets the integrity into VO.
   *
   * @param ruleSchema the rule schema
   * @param ruleSchemaVO the rule schema VO
   */
  private void setIntegrityIntoVO(RulesSchema ruleSchema, RulesSchemaVO ruleSchemaVO) {
    if (null != ruleSchema) {
      Map<String, IntegrityVO> integrityMap = new HashMap<>();
      if (ruleSchema.getRules() != null) {
        for (Rule rule : ruleSchema.getRules().stream()
            .filter(rule -> rule.getIntegrityConstraintId() != null).collect(Collectors.toList())) {
          IntegritySchema integrityschema = integritySchemaRepository
              .findById(rule.getIntegrityConstraintId()).orElse(new IntegritySchema());
          integrityMap.put(rule.getRuleId().toString(),
              integrityMapper.entityToClass(integrityschema));
        }
      }
      // Set integrity into VO
      if (!integrityMap.isEmpty() && ruleSchemaVO.getRules() != null) {
        ruleSchemaVO.getRules().stream()
            .forEach(rule -> rule.setIntegrityVO(integrityMap.get(rule.getRuleId())));
      }
    }
  }

  /**
   * Delete dataset rule and integrity by id field schema. We use that method to delete the
   * dependences of integrity in others dataset or the same dataset
   *
   * @param fieldSchemaId the field schema id
   * @param datasetId the dataset id
   */
  @Override
  @Async
  public void deleteDatasetRuleAndIntegrityByFieldSchemaId(String fieldSchemaId, Long datasetId) {
    // we find the values salved in database by origin or referenced in integritySchema
    List<IntegritySchema> integritySchema =
        integritySchemaRepository.findByOriginOrReferenceFields(new ObjectId(fieldSchemaId));
    // we delete the integrity object and delete the rules associated to the originDataset
    if (null != integritySchema && !integritySchema.isEmpty()) {
      integritySchema.stream().forEach(integritySchemaData -> {
        // we delete the rule associate with that field
        rulesRepository.deleteRuleById(integritySchemaData.getOriginDatasetSchemaId(),
            integritySchemaData.getRuleId());

        // we delete the integrity rule in mongodb in integrity collection
        integritySchemaRepository.deleteById(integritySchemaData.getId());
        LOG.info(
            "Rule integrity associated to the fieldschemaId {} and the integrity data with id {} , in the datasetOrigin id {} was deleted!",
            fieldSchemaId, integritySchemaData.getId(),
            integritySchemaData.getOriginDatasetSchemaId());

        // we delete the pk relation in the database
        dataSetMetabaseControllerZuul.deleteForeignRelationship(datasetId, null,
            integritySchemaData.getOriginDatasetSchemaId().toString(),
            integritySchemaData.getReferencedDatasetSchemaId().toString());
      });

    }
  }

  /**
   * Delete dataset rule and integrity by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @param datasetId the dataset id
   */
  @Override
  @Async
  public void deleteDatasetRuleAndIntegrityByDatasetSchemaId(String datasetSchemaId,
      Long datasetId) {
    // we find the values salved in database by origin or referenced in integritySchema
    List<IntegritySchema> integritySchema = integritySchemaRepository
        .findByOriginOrReferenceDatasetSchemaId(new ObjectId(datasetSchemaId));

    if (null != integritySchema && !integritySchema.isEmpty()) {
      integritySchema.stream().forEach(integritySchemaData -> {
        // we delete the rule associate with that field
        rulesRepository.deleteRuleById(integritySchemaData.getOriginDatasetSchemaId(),
            integritySchemaData.getRuleId());
        // we delete the integrity rule in mongodb in integrity collection
        integritySchemaRepository.deleteById(integritySchemaData.getId());
        LOG.info(
            "Rule integrity associated to the datasetId {} and the integrity data with id {} , in the datasetOrigin id {} was deleted!",
            datasetSchemaId, integritySchemaData.getId(),
            integritySchemaData.getOriginDatasetSchemaId());

        // we delete the pk relation in the database
        dataSetMetabaseControllerZuul.deleteForeignRelationship(datasetId, null,
            integritySchemaData.getOriginDatasetSchemaId().toString(),
            integritySchemaData.getReferencedDatasetSchemaId().toString());
      });

    }
  }

  /**
   * Copy rules schema.
   *
   * @param rules the rules
   * @return the map
   * @throws EEAException the EEA exception
   */
  @Override
  public Map<String, String> copyRulesSchema(CopySchemaVO rules) throws EEAException {

    // We've got the dictionaries and the list of the origin dataset schemas involved to get the
    // rules of them, and with the help of the dictionary,
    // replace the objectIds from the origin to the new ones of the target schemas, to finally save
    // them as new rules. The data needed is inside the auxiliary CopySchemaVO
    List<String> listDatasetSchemaIdToCopy = rules.getOriginDatasetSchemaIds();
    Map<String, String> dictionaryOriginTargetObjectId = rules.getDictionaryOriginTargetObjectId();
    Map<Long, Long> dictionaryOriginTargetDatasetsId = rules.getDictionaryOriginTargetDatasetsId();
    for (String originDatasetSchemaId : listDatasetSchemaIdToCopy) {
      String newDatasetSchemaId = dictionaryOriginTargetObjectId.get(originDatasetSchemaId);
      RulesSchema originRules =
          rulesRepository.getRulesWithActiveCriteria(new ObjectId(originDatasetSchemaId), false);

      // Delete the the rules created in the steps before on the new schema, we are going to copy
      // them directly
      // from the original schema with properties like 'enabled'
      rulesRepository.emptyRulesOfSchemaByDatasetSchemaId(new ObjectId(newDatasetSchemaId));
      rulesSequenceRepository.deleteByDatasetSchemaId(new ObjectId(newDatasetSchemaId));

      for (Rule rule : originRules.getRules()) {
        // We copy only the rules that are not of type Link, because these one are created
        // automatically in the process when we update the fieldSchema in previous calls of the copy
        // process
        copyData(dictionaryOriginTargetObjectId, originDatasetSchemaId,
            dictionaryOriginTargetDatasetsId, newDatasetSchemaId, rule);
      }
    }
    return dictionaryOriginTargetObjectId;
  }

  /**
   * Delete not empty rule.
   *
   * @param tableSchemaId the table schema id
   * @param datasetId the dataset id
   */
  @Override
  public void deleteNotEmptyRule(String tableSchemaId, Long datasetId) {
    String datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    rulesRepository.deleteNotEmptyRule(new ObjectId(tableSchemaId), new ObjectId(datasetSchemaId));
  }


  /**
   * Copy data.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param originDatasetSchemaId the origin dataset schema id
   * @param dictionaryOriginTargetDatasetsId the dictionary origin target datasets id
   * @param newDatasetSchemaId the new dataset schema id
   * @param rule the rule
   * @return the map
   * @throws EEAException the EEA exception
   */
  private Map<String, String> copyData(Map<String, String> dictionaryOriginTargetObjectId,
      String originDatasetSchemaId, Map<Long, Long> dictionaryOriginTargetDatasetsId,
      String newDatasetSchemaId, Rule rule) throws EEAException {

    // Here we change the fields of the rule involved with the help of the dictionary
    fillRuleCopied(rule, dictionaryOriginTargetObjectId, dictionaryOriginTargetDatasetsId);

    // If the rule is a Dataset type, we need to do the same process with the
    // IntegritySchema
    if (EntityTypeEnum.TABLE.equals(rule.getType()) && null != rule.getIntegrityConstraintId()) {
      copyIntegrity(originDatasetSchemaId, dictionaryOriginTargetObjectId, rule);
    }

    LOG.info(
        "A new rule is going to be created in the copy schema process {}, with this Reference id {}",
        rule.getRuleName(), rule.getReferenceId());
    if (!rulesRepository.createNewRule(new ObjectId(newDatasetSchemaId), rule)) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE);
    } else {
      // add the rules sequence
      rulesSequenceRepository.updateSequence(new ObjectId(newDatasetSchemaId));
    }

    return dictionaryOriginTargetObjectId;
  }


  /**
   * Fill rule copied.
   *
   * @param rule the rule
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param dictionaryOriginTargetDatasetsId the dictionary origin target datasets id
   * @return the map
   */
  private Map<String, String> fillRuleCopied(Rule rule,
      Map<String, String> dictionaryOriginTargetObjectId,
      Map<Long, Long> dictionaryOriginTargetDatasetsId) {

    String newRuleId = new ObjectId().toString();
    dictionaryOriginTargetObjectId.put(rule.getRuleId().toString(), newRuleId);
    rule.setRuleId(new ObjectId(newRuleId));
    if (dictionaryOriginTargetObjectId.containsKey(rule.getReferenceId().toString())) {
      rule.setReferenceId(
          new ObjectId(dictionaryOriginTargetObjectId.get(rule.getReferenceId().toString())));
    }
    if (rule.getReferenceFieldSchemaPKId() != null && dictionaryOriginTargetObjectId
        .containsKey(rule.getReferenceFieldSchemaPKId().toString())) {
      rule.setReferenceFieldSchemaPKId(new ObjectId(
          dictionaryOriginTargetObjectId.get(rule.getReferenceFieldSchemaPKId().toString())));
    }
    if (rule.getUniqueConstraintId() != null) {
      String newUniqueConstraintId = new ObjectId().toString();
      dictionaryOriginTargetObjectId.put(rule.getUniqueConstraintId().toString(),
          newUniqueConstraintId);
      rule.setUniqueConstraintId(new ObjectId(
          dictionaryOriginTargetObjectId.get(rule.getUniqueConstraintId().toString())));
    }

    if (rule.getIntegrityConstraintId() != null) {
      String newIntegrityConstraintId = new ObjectId().toString();
      dictionaryOriginTargetObjectId.put(rule.getIntegrityConstraintId().toString(),
          newIntegrityConstraintId);
      rule.setIntegrityConstraintId(new ObjectId(
          dictionaryOriginTargetObjectId.get(rule.getIntegrityConstraintId().toString())));

    }

    // modify the when condition
    if (StringUtils.isNotBlank(rule.getWhenCondition())) {
      dictionaryOriginTargetObjectId.forEach((String oldObjectId, String newObjectId) -> {
        String newWhenCondition = rule.getWhenCondition();
        newWhenCondition = newWhenCondition.replace(oldObjectId, newObjectId);
        rule.setWhenCondition(newWhenCondition);
      });

      // Special case for SQL Sentences
      if (rule.getWhenCondition().contains("isSQLSentence")) {
        dictionaryOriginTargetDatasetsId.forEach((Long oldDatasetId, Long newDatasetId) -> {

          // Change the datasetId in "isSQLSentence(xxx,...."
          String newWhenCondition = rule.getWhenCondition();
          newWhenCondition = newWhenCondition.replace("(" + oldDatasetId.toString(),
              "(" + newDatasetId.toString());

          // Change the dataset_X in the sentence itself if necessary, like
          // select * from table_one t1 inner join dataset_256.table_two....
          newWhenCondition = newWhenCondition.replace(DATASET + oldDatasetId.toString(),
              DATASET + newDatasetId.toString());
          rule.setWhenCondition(newWhenCondition);

          // Do the same in the property SqlSentence
          if (StringUtils.isNotBlank(rule.getSqlSentence())) {
            String newSqlSentence = rule.getSqlSentence();
            newSqlSentence = newSqlSentence.replace(DATASET + oldDatasetId.toString(),
                DATASET + newDatasetId.toString());
            rule.setSqlSentence(newSqlSentence);
          }

        });
      }
    }
    return dictionaryOriginTargetObjectId;
  }

  /**
   * Copy integrity.
   *
   * @param originDatasetSchemaId the origin dataset schema id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param rule the rule
   */
  private void copyIntegrity(String originDatasetSchemaId,
      Map<String, String> dictionaryOriginTargetObjectId, Rule rule) {

    List<IntegritySchema> integritySchemas = integritySchemaRepository
        .findByOriginOrReferenceDatasetSchemaId(new ObjectId(originDatasetSchemaId));
    for (IntegritySchema integrity : integritySchemas) {
      integrity.setId(rule.getIntegrityConstraintId());
      integrity.setOriginDatasetSchemaId(new ObjectId(
          dictionaryOriginTargetObjectId.get(integrity.getOriginDatasetSchemaId().toString())));
      integrity.setReferencedDatasetSchemaId(new ObjectId(
          dictionaryOriginTargetObjectId.get(integrity.getReferencedDatasetSchemaId().toString())));
      integrity.setRuleId(rule.getRuleId());
      for (int i = 0; i < integrity.getOriginFields().size(); i++) {
        integrity.getOriginFields().set(i, new ObjectId(
            dictionaryOriginTargetObjectId.get(integrity.getOriginFields().get(i).toString())));
      }
      for (int i = 0; i < integrity.getReferencedFields().size(); i++) {
        integrity.getReferencedFields().set(i, new ObjectId(
            dictionaryOriginTargetObjectId.get(integrity.getReferencedFields().get(i).toString())));
      }

      integritySchemaRepository.save(integrity);

      Long datasetReferencedId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrity.getReferencedDatasetSchemaId().toString());
      Long datasetOriginId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrity.getOriginDatasetSchemaId().toString());
      dataSetMetabaseControllerZuul.createDatasetForeignRelationship(datasetOriginId,
          datasetReferencedId, integrity.getOriginDatasetSchemaId().toString(),
          integrity.getReferencedDatasetSchemaId().toString());
    }
  }

  /**
   * Return update sequence.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the long
   */
  @Override
  public Long updateSequence(String datasetSchemaId) {
    return rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));
  }

  /**
   * Find sql sentences by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  @Override
  public List<RuleVO> findSqlSentencesByDatasetSchemaId(String datasetSchemaId) {
    List<Rule> rules = rulesRepository.findSqlRules(new ObjectId(datasetSchemaId));
    return ruleMapper.entityListToClass(rules);

  }

  /**
   * Gets the all disabled rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all disabled rules
   */
  @Override
  public Integer getAllDisabledRules(Long dataflowId, List<DesignDatasetVO> designs) {
    int disabledRules = 0;

    for (DesignDatasetVO schema : designs) {
      RulesSchema scheamaAux =
          rulesRepository.getAllDisabledRules(new ObjectId(schema.getDatasetSchema()));
      if (null != scheamaAux.getRules()) {
        disabledRules += scheamaAux.getRules().size();
      }
    }
    return disabledRules;
  }

  /**
   * Gets the all unchecked rules.
   *
   * @param dataflowId the dataflow id
   * @param designs the designs
   * @return the all unchecked rules
   */
  @Override
  public Integer getAllUncheckedRules(Long dataflowId, List<DesignDatasetVO> designs) {
    int uncheckedRules = 0;

    for (DesignDatasetVO schema : designs) {
      RulesSchema scheamaAux =
          rulesRepository.getAllUncheckedRules(new ObjectId(schema.getDatasetSchema()));
      if (null != scheamaAux.getRules()) {
        uncheckedRules += scheamaAux.getRules().size();
      }
    }

    return uncheckedRules;
  }


  /**
   * Gets the integrity schemas.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the integrity schemas
   */
  @Override
  public List<IntegrityVO> getIntegritySchemas(String datasetSchemaId) {
    List<IntegritySchema> integrities =
        integritySchemaRepository.findByOriginDatasetSchemaId(new ObjectId(datasetSchemaId));
    return integrityMapper.entityListToClass(integrities);
  }


  /**
   * Insert integrity schemas.
   *
   * @param integritiesVO the integrities VO
   */
  @Override
  public void insertIntegritySchemas(List<IntegrityVO> integritiesVO) {

    List<IntegritySchema> integrities = integrityMapper.classListToEntity(integritiesVO);
    for (IntegritySchema integrity : integrities) {
      integritySchemaRepository.save(integrity);

      Long datasetReferencedId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrity.getReferencedDatasetSchemaId().toString());
      Long datasetOriginId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrity.getOriginDatasetSchemaId().toString());
      dataSetMetabaseControllerZuul.createDatasetForeignRelationship(datasetOriginId,
          datasetReferencedId, integrity.getOriginDatasetSchemaId().toString(),
          integrity.getReferencedDatasetSchemaId().toString());
    }
  }


  /**
   * Import rules schema.
   *
   * @param qcRulesBytes the qc rules bytes
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param integritiesVo the integrities vo
   * @return the map
   * @throws EEAException the EEA exception
   */
  @Override
  public Map<String, String> importRulesSchema(List<byte[]> qcRulesBytes,
      Map<String, String> dictionaryOriginTargetObjectId, List<IntegrityVO> integritiesVo)
      throws EEAException {

    List<RulesSchema> schemaRules = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    for (byte[] content : qcRulesBytes) {
      if (content != null && content.length > 0) {
        try {
          schemaRules.add(objectMapper.readValue(content, RulesSchema.class));
        } catch (IOException e) {
          LOG_ERROR.error("Error converting from bytes[] to RulesSchema class. Message {}",
              e.getMessage(), e);
        }
      }
    }

    // We've got the dictionaries and the list of the origin dataset schemas involved to get the
    // rules of them, and with the help of the dictionary,
    // replace the objectIds from the origin to the new ones of the target schemas, to finally save
    // them as new rules.
    for (RulesSchema ruleSchema : schemaRules) {
      String newDatasetSchemaId =
          dictionaryOriginTargetObjectId.get(ruleSchema.getIdDatasetSchema().toString());

      // Delete the the rules created in the steps before on the new schema, we are going to copy
      // them directly
      // from the original schema with properties like 'enabled'
      rulesRepository.emptyRulesOfSchemaByDatasetSchemaId(new ObjectId(newDatasetSchemaId));
      rulesSequenceRepository.deleteByDatasetSchemaId(new ObjectId(newDatasetSchemaId));

      for (Rule rule : ruleSchema.getRules()) {
        List<IntegritySchema> integrities = integrityMapper.classListToEntity(integritiesVo);
        importData(dictionaryOriginTargetObjectId, newDatasetSchemaId, rule, integrities);
      }
    }
    return dictionaryOriginTargetObjectId;
  }

  /**
   * Export data validation CSV file.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Async
  @Override
  public void exportQCCSV(Long datasetId) throws EEAException, IOException {
    DatasetTypeEnum datasetType = dataSetControllerZuul.getDatasetType(datasetId);

    // Sets the validation file name and it's root directory
    String folderName = "dataset-" + datasetId + "-QCS";
    String composedFileName = folderName + "-"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss"));
    String fileNameWithExtension = composedFileName + "." + FileTypeEnum.CSV.getValue();
    File fileFolder = new File(pathPublicFile, folderName);

    String creatingFileError =
        String.format("Failed generating CSV file with name %s using datasetID %s",
            fileNameWithExtension, datasetId);

    fileFolder.mkdirs();

    // Creates notification VO and passes the datasetID, the filename and the datasetType
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .fileName(fileNameWithExtension).datasetType(datasetType).error(creatingFileError).build();

    // We create the CSV
    StringWriter stringWriter = new StringWriter();

    try (CSVWriter csvWriter =
        new CSVWriter(stringWriter, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

      // Creates an array list containing all the column names from the CSV defined as constants
      List<String> headers = new ArrayList<>(Arrays.asList(TABLE, FIELD, CODE, QCNAME, QCDESC,
          MESSAGE, EXPRESSION, TYPE_OF_QC, LEVEL_ERROR, CREATION_MODE, STATUS, VALID));

      // Writes the column names into the CSV Writer and sets the array String to headers size so it
      // only writes at most the number of columns as variables per row
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 12;

      fillQCExportData(csvWriter, datasetId, nHeaders);

    }

    catch (IOException e) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_QC_FAILED_EVENT, null,
          notificationVO);
      LOG_ERROR
          .error(String.format(EEAErrorMessage.FILE_NOT_FOUND + ". DatasetId: %s, with error: %s",
              datasetId, e.getMessage(), e));
      return;
    }

    // Convert the writer data to a bytes array to write it into a file
    String csv = stringWriter.getBuffer().toString();
    byte[] file = csv.getBytes();

    File fileWrite = new File(new File(pathPublicFile, folderName), fileNameWithExtension);

    // Tries to write the data obtained into the file, if it's successful, throws a notification
    // event completed
    try (OutputStream out = new FileOutputStream(fileWrite.toString())) {
      out.write(file);
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_QC_COMPLETED_EVENT, null,
          notificationVO);
    } catch (FileNotFoundException e) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.EXPORT_QC_FAILED_EVENT, null,
          notificationVO);
      LOG_ERROR
          .error(String.format(EEAErrorMessage.FILE_NOT_FOUND + ". DatasetId: %s, with error: %s",
              datasetId, e.getMessage(), e));
    }
  }


  /**
   * Download QCCSV.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ResponseStatusException the response status exception
   */
  @Override
  public File downloadQCCSV(Long datasetId, String fileName)
      throws IOException, ResponseStatusException {

    String folderName = "dataset-" + datasetId + "-QCS";
    // we compound the route and create the file
    File file = new File(new File(pathPublicFile, folderName), fileName);
    if (!file.exists()) {

      LOG_ERROR.error(String.format(DOWNLOAD_QC_EXCEPTION, datasetId, fileName));
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format(DOWNLOAD_QC_EXCEPTION, datasetId, fileName));
    }

    return file;
  }

  /**
   * Gets the rule historic info.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   * @return the rule historic info
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public List<RuleHistoricInfoVO> getRuleHistoricInfo(Long datasetId, String ruleId)
      throws EEAException {
    List<RuleHistoricInfoVO> historic = new ArrayList<>();
    String datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    if (datasetSchemaId == null) {
      LOG.error("Datasetschema id not found on dataset {}", datasetId);
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    var rule = rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleId));
    if (null == rule) {
      LOG.error("Rule with id {} not found", ruleId);
      throw new EEAException(EEAErrorMessage.RULE_NOT_FOUND);
    }
    var audit = auditRepository.getAuditByRuleId(rule.getRuleId());
    if (null != audit) {
      historic = ruleHistoricInfoMapper.entityListToClass(audit.getHistoric());

    }
    return historic;
  }

  /**
   * Gets the rule historic info by dataset id.
   *
   * @param datasetId the dataset id
   * @return the rule historic info by dataset id
   */
  @Override
  @Transactional
  public List<DatasetHistoricRuleVO> getRuleHistoricInfoByDatasetId(Long datasetId) {
    List<Audit> audits = auditRepository.getAuditsByDatasetId(datasetId);
    List<DatasetHistoricRuleVO> historic = new ArrayList<>();
    for (Audit audit : audits) {
      for (RuleHistoricInfo historicRuleInfo : audit.getHistoric()) {
        historic.add(datasetHistoricRuleMapper.entityToClass(historicRuleInfo));
      }
    }
    return historic;
  }


  /**
   * Import data.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param newDatasetSchemaId the new dataset schema id
   * @param rule the rule
   * @param integrities the integrities
   * @return the map
   * @throws EEAException the EEA exception
   */
  private Map<String, String> importData(Map<String, String> dictionaryOriginTargetObjectId,
      String newDatasetSchemaId, Rule rule, List<IntegritySchema> integrities) throws EEAException {

    // Here we change the fields of the rule involved with the help of the dictionary
    fillRuleImport(rule, dictionaryOriginTargetObjectId);

    // If the rule is a Dataset type, we need to do the same process with the
    // IntegritySchema
    if (EntityTypeEnum.TABLE.equals(rule.getType()) && null != rule.getIntegrityConstraintId()) {
      importIntegrity(integrities, dictionaryOriginTargetObjectId, rule);
    }
    LOG.info(
        "A new rule is going to be created in the import schema process {}, with this Reference id {}",
        rule.getRuleName(), rule.getReferenceId());
    if (!rulesRepository.createNewRule(new ObjectId(newDatasetSchemaId), rule)) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE);
    } else {
      // add the rules sequence
      rulesSequenceRepository.updateSequence(new ObjectId(newDatasetSchemaId));
    }

    return dictionaryOriginTargetObjectId;
  }

  /**
   * Fill rule import.
   *
   * @param rule the rule
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @return the map
   */
  private Map<String, String> fillRuleImport(Rule rule,
      Map<String, String> dictionaryOriginTargetObjectId) {

    String newRuleId = new ObjectId().toString();
    dictionaryOriginTargetObjectId.put(rule.getRuleId().toString(), newRuleId);
    rule.setRuleId(new ObjectId(newRuleId));
    if (dictionaryOriginTargetObjectId.containsKey(rule.getReferenceId().toString())) {
      rule.setReferenceId(
          new ObjectId(dictionaryOriginTargetObjectId.get(rule.getReferenceId().toString())));
    }
    if (rule.getReferenceFieldSchemaPKId() != null && dictionaryOriginTargetObjectId
        .containsKey(rule.getReferenceFieldSchemaPKId().toString())) {
      rule.setReferenceFieldSchemaPKId(new ObjectId(
          dictionaryOriginTargetObjectId.get(rule.getReferenceFieldSchemaPKId().toString())));
    }
    if (rule.getUniqueConstraintId() != null) {
      rule.setUniqueConstraintId(new ObjectId(
          dictionaryOriginTargetObjectId.get(rule.getUniqueConstraintId().toString())));
    }

    if (rule.getIntegrityConstraintId() != null) {
      String newIntegrityConstraintId = new ObjectId().toString();
      dictionaryOriginTargetObjectId.put(rule.getIntegrityConstraintId().toString(),
          newIntegrityConstraintId);
      rule.setIntegrityConstraintId(new ObjectId(
          dictionaryOriginTargetObjectId.get(rule.getIntegrityConstraintId().toString())));

    }

    // modify the when condition
    if (StringUtils.isNotBlank(rule.getWhenCondition())) {
      dictionaryOriginTargetObjectId.forEach((String oldObjectId, String newObjectId) -> {
        String newWhenCondition = rule.getWhenCondition();
        newWhenCondition = newWhenCondition.replace(oldObjectId, newObjectId);
        rule.setWhenCondition(newWhenCondition);
      });
    }

    // Special case for SQL Sentences
    if (StringUtils.isNotBlank(rule.getWhenCondition())
        && rule.getWhenCondition().contains("isSQLSentence")
        && StringUtils.isNotBlank(rule.getSqlSentence())) {
      dictionaryOriginTargetObjectId.forEach((String oldObjectId, String newObjectId) -> {
        String newSqlSentence = rule.getSqlSentence();
        newSqlSentence = newSqlSentence.replace(oldObjectId, newObjectId);
        rule.setSqlSentence(newSqlSentence);
      });
    }

    return dictionaryOriginTargetObjectId;
  }


  /**
   * Import integrity.
   *
   * @param integritySchemas the integrity schemas
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param rule the rule
   */
  private void importIntegrity(List<IntegritySchema> integritySchemas,
      Map<String, String> dictionaryOriginTargetObjectId, Rule rule) {

    for (IntegritySchema integrity : integritySchemas) {
      integrity.setId(rule.getIntegrityConstraintId());
      integrity.setOriginDatasetSchemaId(new ObjectId(
          dictionaryOriginTargetObjectId.get(integrity.getOriginDatasetSchemaId().toString())));
      integrity.setReferencedDatasetSchemaId(new ObjectId(
          dictionaryOriginTargetObjectId.get(integrity.getReferencedDatasetSchemaId().toString())));
      integrity.setRuleId(rule.getRuleId());
      for (int i = 0; i < integrity.getOriginFields().size(); i++) {
        integrity.getOriginFields().set(i, new ObjectId(
            dictionaryOriginTargetObjectId.get(integrity.getOriginFields().get(i).toString())));
      }
      for (int i = 0; i < integrity.getReferencedFields().size(); i++) {
        integrity.getReferencedFields().set(i, new ObjectId(
            dictionaryOriginTargetObjectId.get(integrity.getReferencedFields().get(i).toString())));
      }

      integritySchemaRepository.save(integrity);

      Long datasetReferencedId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrity.getReferencedDatasetSchemaId().toString());
      Long datasetOriginId = dataSetMetabaseControllerZuul
          .getDesignDatasetIdByDatasetSchemaId(integrity.getOriginDatasetSchemaId().toString());
      dataSetMetabaseControllerZuul.createDatasetForeignRelationship(datasetOriginId,
          datasetReferencedId, integrity.getOriginDatasetSchemaId().toString(),
          integrity.getReferencedDatasetSchemaId().toString());
    }
  }

  /**
   * Fill QC export data.
   *
   * @param csvWriter the csv writer
   * @param datasetId the dataset id
   * @param nHeaders the n headers
   */
  private void fillQCExportData(CSVWriter csvWriter, Long datasetId, int nHeaders) {

    String dataSetSchema = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);

    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataSetSchema));

    List<TableSchema> tables = schema.getTableSchemas();
    Map<String, String> tableNames = new HashMap<>();
    Map<String, String> fieldNames = new HashMap<>();

    retrieveTableAndFieldNames(tables, tableNames, fieldNames);

    RulesSchemaVO dataSetRules = getRulesSchemaByDatasetId(dataSetSchema);

    String[] fieldsToWrite;

    for (RuleVO rule : dataSetRules.getRules()) {
      fieldsToWrite = new String[nHeaders];

      if (rule.getType() == EntityTypeEnum.TABLE || rule.getType() == EntityTypeEnum.RECORD) {
        fieldsToWrite[0] =
            tableNames.containsKey(rule.getReferenceId()) ? tableNames.get(rule.getReferenceId())
                : "Table not found"; // Table Name
      } else {
        fieldsToWrite[0] =
            tableNames.containsKey(rule.getReferenceId()) ? tableNames.get(rule.getReferenceId())
                : ""; // Table Name if it has a Field reference ID
      }

      fieldsToWrite[1] =
          fieldNames.containsKey(rule.getReferenceId()) ? fieldNames.get(rule.getReferenceId())
              : ""; // Field Name
      fieldsToWrite[2] =
          rule.getShortCode().startsWith("=") ? " " + rule.getShortCode() : rule.getShortCode();
      fieldsToWrite[3] =
          rule.getRuleName().startsWith("=") ? " " + rule.getRuleName() : rule.getRuleName();
      fieldsToWrite[4] = rule.getDescription().startsWith("=") ? " " + rule.getDescription()
          : rule.getDescription();
      fieldsToWrite[5] =
          rule.getThenCondition().get(0).startsWith("=") ? " " + rule.getThenCondition().get(0)
              : rule.getThenCondition().get(0); // Message
      if (rule.getSqlSentence() != null) {
        fieldsToWrite[6] = rule.getSqlSentence().startsWith("=") ? " " + rule.getSqlSentence()
            : rule.getSqlSentence();
      } else if (rule.getExpressionText() != null) {
        fieldsToWrite[6] = rule.getExpressionText().startsWith("=") ? " " + rule.getExpressionText()
            : rule.getExpressionText();
      }
      fieldsToWrite[7] = rule.getType().toString(); // Type of QC
      fieldsToWrite[8] = rule.getThenCondition().get(1); // Level Error
      fieldsToWrite[9] = Boolean.toString(rule.isAutomatic()); // Creation Mode
      fieldsToWrite[10] = Boolean.toString(rule.isEnabled()); // Status
      fieldsToWrite[11] = rule.getVerified().toString(); // Valid

      csvWriter.writeNext(fieldsToWrite);
    }
  }


  /**
   * Adds the historic rule info.
   *
   * @param rule the rule
   * @param ruleOriginal the rule original
   * @param datasetId the dataset id
   * @param originalIntegrityVO the original integrity VO
   * @throws EEAException the EEA exception
   */
  private void addHistoricRuleInfo(Rule rule, Rule ruleOriginal, Long datasetId,
      IntegrityVO originalIntegrityVO) throws EEAException {
    UserRepresentationVO user = userManagementControllerZuul.getUserByUserId();
    Audit audit = auditRepository.getAuditByRuleId(rule.getRuleId());
    boolean metadata = false;
    boolean status = false;
    boolean expression = false;
    if (null != ruleOriginal) {
      metadata = checkMetadataHasChange(rule, ruleOriginal);
      status = checkStatusHasChange(rule, ruleOriginal);
      expression = checkExpressionHasChange(rule, ruleOriginal, originalIntegrityVO);
    }
    try {
      if (null == audit) {
        LOG.info("Creating a new historic for the rule {}", rule.getRuleId());
        auditRepository.createAudit(rule, user, datasetId, status, expression, metadata);
      } else {
        LOG.info("Adding new information in the historic of the rule {}", rule.getRuleId());
        auditRepository.updateAudit(audit, user, rule, status, expression, metadata);
      }
    } catch (JsonProcessingException e) {
      LOG.error("Error updating historic information for rule {}", rule.getRuleId());
      throw new EEAException(EEAErrorMessage.HISTORIC_QC_UPDATE_ERROR);
    }
  }


  /**
   * Check expression has change.
   *
   * @param ruleActual the rule actual
   * @param ruleOriginal the rule original
   * @param originalIntegrityVO the original integrity VO
   * @return true, if successful
   */
  private boolean checkExpressionHasChange(Rule ruleActual, Rule ruleOriginal,
      IntegrityVO originalIntegrityVO) {
    return checkSQLSentenceHasChange(ruleActual, ruleOriginal)
        || checkExpressionTextHasChange(ruleActual, ruleOriginal)
        || checkDatasetComparisonHasChange(ruleActual, originalIntegrityVO);
  }

  /**
   * Check dataset comparison has change.
   *
   * @param ruleActual the rule actual
   * @param originalIntegrityVO the original integrity VO
   * @return true, if successful
   */
  private boolean checkDatasetComparisonHasChange(Rule ruleActual,
      IntegrityVO originalIntegrityVO) {
    IntegrityVO actualIntegrityVO = null;
    if (ruleActual.getIntegrityConstraintId() != null) {
      actualIntegrityVO = getIntegrityConstraint(ruleActual.getIntegrityConstraintId().toString());
    }
    return (actualIntegrityVO != null && originalIntegrityVO != null
        && ruleActual.getSqlSentence() == null && ruleActual.getExpressionText().equals("")
        && (!(actualIntegrityVO.getOriginFields().equals(originalIntegrityVO.getOriginFields()))
            || !(actualIntegrityVO.getReferencedFields()
                .equals(originalIntegrityVO.getReferencedFields()))
            || (actualIntegrityVO.getOriginFields().size() > originalIntegrityVO.getOriginFields()
                .size()
                || actualIntegrityVO.getReferencedFields().size() > originalIntegrityVO
                    .getReferencedFields().size())));
  }

  /**
   * Check SQL sentence has change.
   *
   * @param ruleActual the rule actual
   * @param ruleOriginal the rule original
   * @return true, if successful
   */
  private boolean checkSQLSentenceHasChange(Rule ruleActual, Rule ruleOriginal) {
    boolean change = false;
    if (ruleActual.getSqlSentence() == null && ruleOriginal.getSqlSentence() == null) {
      change = false;
    } else {
      if ((ruleActual.getSqlSentence() != null && ruleOriginal.getSqlSentence() == null)
          || !(ruleActual.getSqlSentence().equals(ruleOriginal.getSqlSentence()))) {
        change = true;
      }
    }
    return change;
  }

  /**
   * Check expression text has change.
   *
   * @param ruleActual the rule actual
   * @param ruleOriginal the rule original
   * @return true, if successful
   */
  private boolean checkExpressionTextHasChange(Rule ruleActual, Rule ruleOriginal) {
    boolean change = false;
    if (ruleActual.getExpressionText() == null && ruleOriginal.getExpressionText() == null) {
      change = false;
    } else {
      if ((ruleActual.getExpressionText() != null && ruleOriginal.getExpressionText() == null)
          || !(ruleActual.getExpressionText().equals(ruleOriginal.getExpressionText()))) {
        change = true;
      }
    }
    return change;
  }

  /**
   * Check metadata has change.
   *
   * @param ruleActual the rule actual
   * @param ruleOriginal the rule original
   * @return true, if successful
   */
  private boolean checkMetadataHasChange(Rule ruleActual, Rule ruleOriginal) {
    return !(ruleActual.getRuleName().equals(ruleOriginal.getRuleName()))
        || !(ruleActual.getDescription().equals(ruleOriginal.getDescription()))
        || !(ruleActual.getShortCode().equals(ruleOriginal.getShortCode()))
        || !(ruleActual.getThenCondition().get(0).equals(ruleOriginal.getThenCondition().get(0)))
        || !(ruleActual.getThenCondition().get(1).equals(ruleOriginal.getThenCondition().get(1)));
  }

  /**
   * Check status has change.
   *
   * @param ruleActual the rule actual
   * @param ruleOriginal the rule original
   * @return true, if successful
   */
  private boolean checkStatusHasChange(Rule ruleActual, Rule ruleOriginal) {
    return ruleActual.isEnabled() != ruleOriginal.isEnabled();
  }


  /**
   * Retrieve table and field names.
   *
   * @param tables the tables
   * @param tableNames the table names
   * @param fieldNames the field names
   */
  private void retrieveTableAndFieldNames(List<TableSchema> tables, Map<String, String> tableNames,
      Map<String, String> fieldNames) {
    for (TableSchema table : tables) {
      tableNames.put(table.getIdTableSchema().toString(), table.getNameTableSchema());

      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        fieldNames.put(field.getIdFieldSchema().toString(), field.getHeaderName());
        tableNames.put(field.getIdFieldSchema().toString(), table.getNameTableSchema());
        tableNames.put(field.getIdRecord().toString(), table.getNameTableSchema());

      }
    }
  }



}
