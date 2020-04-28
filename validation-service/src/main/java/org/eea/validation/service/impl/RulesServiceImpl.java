package org.eea.validation.service.impl;

import java.util.ArrayList;
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
import org.eea.validation.persistence.repository.RulesSequenceRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RulesService;
import org.eea.validation.util.AutomaticRules;
import org.eea.validation.util.KieBaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

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


  /** The Constant FIELD_TYPE. */
  private static final String FIELD_TYPE = "Field type ";

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
    return rulesSchema == null ? null : rulesSchemaMapper.entityToClass(rulesSchema);
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
    return rulesSchema == null ? null : rulesSchemaMapper.entityToClass(rulesSchema);
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
   */
  @Override
  public void deleteEmptyRulesSchema(String datasetSchemaId) {
    RulesSchema ruleSchema = rulesRepository.findByIdDatasetSchema(new ObjectId(datasetSchemaId));
    if (null != ruleSchema) {
      rulesRepository.deleteByIdDatasetSchema(ruleSchema.getRulesSchemaId());
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

    String datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    if (datasetSchemaId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    Rule rule = ruleMapper.classToEntity(ruleVO);
    rule.setRuleId(new ObjectId());
    rule.setType(EntityTypeEnum.FIELD);
    rule.setAutomatic(false);
    rule.setActivationGroup(null);
    rule.setVerified(null);

    validateRule(rule);

    if (!rulesRepository.createNewRule(new ObjectId(datasetSchemaId), rule)) {
      throw new EEAException(EEAErrorMessage.ERROR_CREATING_RULE);
    }

    // Check if rule is valid
    kieBaseManager.textRuleCorrect(datasetSchemaId, rule);
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

    List<Rule> ruleList = new ArrayList<>();
    // we use that if to sort between a rule required and rule for any other type(Boolean,
    // number etc)
    Long shortcode = rulesSequenceRepository.updateSequence(new ObjectId(datasetSchemaId));
    if (required) {
      ruleList.add(AutomaticRules.createRequiredRule(referenceId, typeEntityEnum,
          "Field cardinality", "FC" + shortcode, FC_DESCRIPTION));
    } else {
      switch (typeData) {
        case NUMBER_INTEGER:
          ruleList.add(AutomaticRules.createNumberIntegerAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case NUMBER_DECIMAL:
          ruleList.add(AutomaticRules.createNumberDecimalAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case DATE:
          ruleList.add(AutomaticRules.createDateAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case BOOLEAN:
          ruleList.add(AutomaticRules.createBooleanAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case COORDINATE_LAT:
          ruleList.add(AutomaticRules.createLatAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case COORDINATE_LONG:
          ruleList.add(AutomaticRules.createLongAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case LINK:
          // we call this method to find the tableschemaid because we want to create that validation
          // at TABLE level
          // that is for avoid do many calls to database and collapse it
          DataSetSchema datasetSchema =
              schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));
          String tableSchemaId = getTableSchemaIdFromIdFieldSchema(datasetSchema, referenceId);

          ruleList.add(AutomaticRules.createPKAutomaticRule(referenceId, EntityTypeEnum.TABLE,
              FIELD_TYPE + typeData, "TC" + shortcode, TC_DESCRIPTION + typeData, tableSchemaId,
              datasetId));
          break;
        case CODELIST:
          // we find values available to create this validation for a codelist, same value with
          // capital letter and without capital letters
          Document document = schemasRepository.findFieldSchema(datasetSchemaId, referenceId);
          ruleList.addAll(AutomaticRules.createCodelistAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, document.get("codelistItems").toString(), "FT" + shortcode,
              FT_DESCRIPTION + typeData));
          break;
        case URL:
          ruleList.add(AutomaticRules.createUrlAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case EMAIL:
          ruleList.add(AutomaticRules.createEmailAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
          break;
        case PHONE:
          ruleList.add(AutomaticRules.createPhoneAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData));
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
   */
  @Override
  public void deleteRuleRequired(String datasetSchemaId, String referenceId) {
    rulesRepository.deleteRuleRequired(new ObjectId(datasetSchemaId), new ObjectId(referenceId));
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

    String datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    if (datasetSchemaId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    Rule rule = ruleMapper.classToEntity(ruleVO);
    rule.setType(EntityTypeEnum.FIELD);
    rule.setAutomatic(false);
    rule.setActivationGroup(null);
    rule.setVerified(null);

    validateRule(rule);

    if (!rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule)) {
      throw new EEAException(EEAErrorMessage.ERROR_UPDATING_RULE);
    }

    // Check if rule is valid
    kieBaseManager.textRuleCorrect(datasetSchemaId, rule);
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

    String datasetSchemaId = dataSetMetabaseControllerZuul.findDatasetSchemaIdById(datasetId);
    String ruleId = ruleVO.getRuleId();

    if (null != datasetSchemaId) {
      if (null != ruleId && ObjectId.isValid(ruleId)) {

        // Find the actual rule
        Rule rule = rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleId));

        if (null != rule) {

          // Update only allowed properties
          updateAllowedRuleProperties(ruleVO, rule);

          // Save the modified rule
          rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
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
   * Insert rule in position.
   *
   * @param datasetSchemaId the dataset schema id
   * @param ruleId the rule id
   * @param position the position
   * @return true, if successful
   */
  @Override
  public boolean insertRuleInPosition(String datasetSchemaId, String ruleId, int position) {
    Rule rule = rulesRepository.findRule(new ObjectId(datasetSchemaId), new ObjectId(ruleId));
    if (null != rule) {
      if (rulesRepository.deleteRuleById(new ObjectId(datasetSchemaId), new ObjectId(ruleId))) {
        if (rulesRepository.insertRuleInPosition(new ObjectId(datasetSchemaId), rule, position)) {
          LOG.info("Rule {} reordered in datasetSchemaId {}", ruleId, datasetSchemaId);
          return true;
        }
        LOG_ERROR.error("Error inserting Rule {} in datasetSchemaId {} in position {}", ruleId,
            datasetSchemaId, position);
        return false;
      }
      LOG_ERROR.error("Error deleting Rule {} in datasetSchemaId {}", ruleId, datasetSchemaId);
      return false;
    }
    LOG_ERROR.error("Rule {} not found", ruleId);
    return false;
  }

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
}
