package org.eea.validation.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import org.eea.validation.service.RulesService;
import org.eea.validation.util.AutomaticRules;
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


  /** The rules schema mapper. */
  @Autowired
  private RulesSchemaMapper rulesSchemaMapper;

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RulesServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant FC_DESCRIPTION. */
  private static final String FC_DESCRIPTION = "Checks if the field is missing or empty";

  /** The Constant FT_DESCRIPTION. */
  private static final String FT_DESCRIPTION = "Checks if the field is a valid ";

  /** The Constant FIELD_TYPE. */
  private static final String FIELD_TYPE = "Field type ";

  /**
   * Gets the rules schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema by dataset id
   */
  @Override
  public RulesSchemaVO getRulesSchemaByDatasetId(String idDatasetSchema) {
    RulesSchema rulesSchema =
        rulesRepository.getRulesWithActiveCriteria(new ObjectId(idDatasetSchema), false);
    return rulesSchema == null ? null : rulesSchemaMapper.entityToClass(rulesSchema);
  }


  /**
   * Gets the active rules schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the active rules schema by dataset id
   */
  @Override
  public RulesSchemaVO getActiveRulesSchemaByDatasetId(String idDatasetSchema) {
    RulesSchema rulesSchema =
        rulesRepository.getRulesWithActiveCriteria(new ObjectId(idDatasetSchema), true);
    return rulesSchema == null ? null : rulesSchemaMapper.entityToClass(rulesSchema);

  }

  /**
   * Creates the empty rules scehma.
   *
   * @param schemaId the schema id
   * @param ruleSchemaId the rule schema id
   */
  @Override
  public void createEmptyRulesSchema(ObjectId schemaId, ObjectId ruleSchemaId) {
    RulesSchema rSchema = new RulesSchema();
    rSchema.setIdDatasetSchema(schemaId);
    rSchema.setRulesSchemaId(ruleSchemaId);
    rSchema.setRules(new ArrayList<Rule>());
    rulesRepository.save(rSchema);
  }

  /**
   * Delete empty rules scehma.
   *
   * @param schemaId the schema id
   */
  @Override
  public void deleteEmptyRulesScehma(ObjectId schemaId) {

    RulesSchema ruleSchema = rulesRepository.findByIdDatasetSchema(schemaId);
    if (null != ruleSchema) {
      rulesRepository.deleteByIdDatasetSchema(ruleSchema.getRulesSchemaId());
    }
  }

  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteRuleById(String idDatasetSchema, String ruleId) throws EEAException {
    rulesRepository.deleteRuleById(idDatasetSchema, ruleId);
  }

  /**
   * Delete rule by reference id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteRuleByReferenceId(String idDatasetSchema, String referenceId)
      throws EEAException {
    rulesRepository.deleteRuleByReferenceId(idDatasetSchema, referenceId);

  }


  /**
   * Creates the new rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param rule the rule
   * @return the update result
   * @throws EEAException the EEA exception
   */
  @Override
  public void createNewRule(String idDatasetSchema, Rule rule) throws EEAException {
    if (rule.getRuleId() == null) {
      rule.setRuleId(new ObjectId());
    }
    rulesRepository.createNewRule(idDatasetSchema, rule);
  }

  /**
   * Count rulesin schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @param required the required
   * @param shortcode the shortcode
   * @return the string
   */
  private String countRulesinSchema(String idDatasetSchema, Boolean required, String shortcode) {
    RulesSchema rules =
        rulesRepository.getRulesWithTypeRuleCriteria(new ObjectId(idDatasetSchema), required);
    List<Rule> rulesList = rules.getRules();
    if (!rulesList.isEmpty()) {
      String code = rulesList.get(rulesList.size() - 1).getShortCode();
      String text = (code).substring(2, code.length());
      int rulesSize = Integer.valueOf(text) + 1;
      shortcode = String.format("%02d", rulesSize);
    }
    return shortcode;
  }

  /**
   * Creates the automatic rules. When Input argument "required" is true it is created an automatic
   * rule to validate if the field has been informed or not. Otherwise, a Rule based on the field's
   * DataType (number, boolean, codelist...) is created
   *
   * @param idDatasetSchema the id dataset schema
   * @param referenceId the reference id
   * @param typeData the type data
   * @param typeEntityEnum the type entity enum
   * @param required the required
   * @throws EEAException the EEA exception
   */
  @Override
  public void createAutomaticRules(String idDatasetSchema, String referenceId, DataType typeData,
      EntityTypeEnum typeEntityEnum, Boolean required) throws EEAException {
    Rule rule = new Rule();
    // we use that if to differentiate beetween a rule required and rule for any other type(Boolean,
    // number etc)
    String shortcode = "01";
    if (Boolean.TRUE.equals(required)) {
      shortcode = countRulesinSchema(idDatasetSchema, required, shortcode);
      rule = AutomaticRules.createRequiredRule(referenceId, typeEntityEnum, "Field cardinality",
          "FC" + shortcode, FC_DESCRIPTION);
    } else {
      shortcode = countRulesinSchema(idDatasetSchema, required, shortcode);
      switch (typeData) {
        case NUMBER:
          rule = AutomaticRules.createNumberAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData);
          break;
        case DATE:
          rule = AutomaticRules.createDateAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData);
          break;
        case BOOLEAN:
          rule = AutomaticRules.createBooleanAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData);
          break;
        case COORDINATE_LAT:
          rule = AutomaticRules.createLatAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData);
          break;
        case COORDINATE_LONG:
          rule = AutomaticRules.createLongAutomaticRule(referenceId, typeEntityEnum,
              FIELD_TYPE + typeData, "FT" + shortcode, FT_DESCRIPTION + typeData);
          break;
        case CODELIST:
          // we find the idcodelist to create this validate
          Document document = schemasRepository.findFieldSchema(idDatasetSchema, referenceId);
          rule = AutomaticRules.createCodelistAutomaticRule(referenceId, typeEntityEnum,
              UUID.randomUUID().toString(), document.get("idCodeList").toString(), "FT" + shortcode,
              FT_DESCRIPTION + typeData);
          break;
        default:
          rule = null;
          LOG.info("This Data Type has not automatic rule {}", typeData.getValue());
          break;
      }
    }
    if (null != rule) {
      rulesRepository.createNewRule(idDatasetSchema, rule);
    }

  }

  /**
   * Delete rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   */
  @Override
  public void deleteRuleRequired(String datasetSchemaId, String referenceId) {
    rulesRepository.deleteRuleRequired(datasetSchemaId, referenceId);
  }

  /**
   * Exists rule required.
   *
   * @param datasetSchemaId the dataset schema id
   * @param referenceId the reference id
   * @return the boolean
   */
  @Override
  public Boolean existsRuleRequired(String datasetSchemaId, String referenceId) {
    return rulesRepository.existsRuleRequired(datasetSchemaId, referenceId);
  }


  /**
   * Update rule.
   *
   * @param idDatasetSchema the id dataset schema
   * @param rule the rule
   * @return true, if successful
   */
  @Override
  public boolean updateRule(String idDatasetSchema, Rule rule) {
    if (null != rule) {
      if (rulesRepository.updateRule(idDatasetSchema, rule)) {
        LOG.info("Rule {} reordered in datasetSchemaId {}", rule.getRuleId(), idDatasetSchema);
        return true;
      }
      LOG_ERROR.error("Error updating rule for idDatasetSchema {} ", idDatasetSchema);
      return false;
    }
    LOG_ERROR.error("Error updating rule for idDatasetSchema {} ", idDatasetSchema);
    return false;
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
    Rule rule = rulesRepository.findRule(datasetSchemaId, ruleId);
    if (null != rule) {
      if (rulesRepository.deleteRule(datasetSchemaId, ruleId)) {
        if (rulesRepository.insertRuleInPosition(datasetSchemaId, rule, position)) {
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

}
