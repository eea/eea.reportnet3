package org.eea.validation.util;

import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.drools.template.ObjectDataCompiler;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.util.drools.compose.ConditionsDrools;
import org.eea.validation.util.drools.compose.SchemasDrools;
import org.eea.validation.util.drools.compose.TypeValidation;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class KieBaseManager.
 */
@Component
public class KieBaseManager {

  /**
   * The Constant REGULATION_TEMPLATE_FILE.
   */
  private static final String REGULATION_TEMPLATE_FILE = "/templateRules.drl";


  /** The Constant RULE_CHECK_TEMPLATE. */
  private static final String RULE_CHECK_TEMPLATE = "/ruleCheckTemplate.drl";

  /** The Constant timeZone. */
  private static final ZoneId timeZone = ZoneId.of("UTC");

  /** The Constant dateFormatter. */
  private static final DateTimeFormatter dateFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;
  /**
   * The dataset metabase controller.
   */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;


  /**
   * Reload rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchema the dataset schema
   * @return the kie base
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long datasetId, String datasetSchema) throws FileNotFoundException {
    DataSetMetabaseVO dataSetMetabaseVO =
        datasetMetabaseController.findDatasetMetabaseById(datasetId);

    // we take all actives kiebase
    RulesSchema schemaRules =
        rulesRepository.getRulesWithActiveCriteria(new ObjectId(datasetSchema), true);

    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    ObjectDataCompiler compiler = new ObjectDataCompiler();
    KieServices kieServices = KieServices.Factory.get();
    KieHelper kieHelper = new KieHelper();
    // we take the dataset value for salve all fail validations
    DatasetValue datasetValue = datasetRepository.findById(datasetId).orElse(null);

    // we bring the datasetschema
    DataSetSchema dataSetSchema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchema));

    // here we have the mothod who compose the field in template
    if (null != schemaRules.getRules() && !schemaRules.getRules().isEmpty()) {
      schemaRules.getRules().stream().forEach(rule -> {
        String schemasDrools = "";
        String originName = "";
        StringBuilder expression = new StringBuilder("");
        TypeValidation typeValidation = null;
        switch (rule.getType()) {
          case DATASET:
            schemasDrools = SchemasDrools.ID_DATASET_SCHEMA.getValue();
            typeValidation = TypeValidation.DATASET;
            originName = dataSetMetabaseVO.getDataSetName();
            break;
          case TABLE:
            schemasDrools = SchemasDrools.ID_TABLE_SCHEMA.getValue();
            typeValidation = TypeValidation.TABLE;
            for (TableSchema table : dataSetSchema.getTableSchemas()) {
              if (table.getIdTableSchema().equals(rule.getReferenceId())) {
                originName = table.getNameTableSchema();
              }
            }

            break;
          case RECORD:
            schemasDrools = SchemasDrools.ID_RECORD_SCHEMA.getValue();
            typeValidation = TypeValidation.RECORD;
            for (TableSchema table : dataSetSchema.getTableSchemas()) {
              if (table.getRecordSchema().getIdRecordSchema().equals(rule.getReferenceId())) {
                originName = table.getNameTableSchema();
              }
            }
            break;
          case FIELD:
            schemasDrools = SchemasDrools.ID_FIELD_SCHEMA.getValue();
            typeValidation = TypeValidation.FIELD;
            for (TableSchema table : dataSetSchema.getTableSchemas()) {
              for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
                if (field.getIdFieldSchema().equals(rule.getReferenceId())) {
                  originName = table.getNameTableSchema();
                }
              }
            }

            // if the type is field and isnt automatic we create the rules to validate check if
            // the
            // data are correct
            Document documentField =
                schemasRepository.findFieldSchema(datasetSchema, rule.getReferenceId().toString());
            DataType datatype = DataType.valueOf(documentField.get("typeData").toString());

            // that switch clear the validations , and check if the datas in values are correct
            if (null != datatype && !rule.isAutomatic()) {
              switch (datatype) {
                case NUMBER:
                  // expression.append("( !isBlank(value) || isNumber(value) && ");
                  rule.setWhenCondition(
                      rule.getWhenCondition().replaceAll("value", "doubleData(value)"));
                  break;
                case DATE:
                  expression.append("( !isBlank(value) || isDateYYYYMMDD(value) && ");
                  rule.setWhenCondition(rule.getWhenCondition().replaceAll("EQUALS", "=="));
                  break;
                case BOOLEAN:
                  expression.append("( !isBlank(value) || isBoolean(value) && ");
                  rule.setWhenCondition(rule.getWhenCondition().replaceAll("EQUALS", "=="));
                  break;
                case COORDINATE_LAT:
                  expression.append("( !isBlank(value) || isCordenateLat(value) && ");
                  rule.setWhenCondition(
                      rule.getWhenCondition().replaceAll("value", "doubleData(value)"));
                  break;
                case COORDINATE_LONG:
                  expression.append("( !isBlank(value) || isCordenateLong(value) && ");
                  rule.setWhenCondition(
                      rule.getWhenCondition().replaceAll("value", "doubleData(value)"));
                  break;
                default:
                  break;
              }
            }
            if (!StringUtils.isBlank(expression.toString())) {
              String whenConditionWithParenthesis = new StringBuilder("").append("(")
                  .append(rule.getWhenCondition()).append(")").toString();
              rule.setWhenCondition(
                  expression.append(whenConditionWithParenthesis).append(")").toString());
            }
        }
        // that method clear the datas and delete rules who are bad compose(bad syntax)
        cleanRules(originName, ruleAttributes, compiler, kieServices, datasetValue, rule,
            schemasDrools, typeValidation);
      });
    }

    String generatedDRL =
        compiler.compile(ruleAttributes, getClass().getResourceAsStream(REGULATION_TEMPLATE_FILE));
    // multiple such resoures/rules can be added
    byte[] b1 = generatedDRL.getBytes();
    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    kieHelper.addResource(resource1, ResourceType.DRL);
    // this is a shared variable in a single instanced object.
    return kieHelper.build();
  }

  /**
   * Clean rules.
   *
   * @param originName the origin name
   * @param ruleAttributes the rule attributes
   * @param compiler the compiler
   * @param kieServices the kie services
   * @param datasetValue the dataset value
   * @param rule the rule
   * @param schemasDrools the schemas drools
   * @param typeValidation the type validation
   */
  private void cleanRules(String originName, List<Map<String, String>> ruleAttributes,
      ObjectDataCompiler compiler, KieServices kieServices, DatasetValue datasetValue, Rule rule,
      String schemasDrools, TypeValidation typeValidation) {
    // Check rules before add to Rules Knowledge DB
    List<Map<String, String>> ruleAttributesHelper = new ArrayList<>();
    ruleAttributesHelper.add(passDataToMap(rule.getReferenceId().toString(),
        rule.getRuleId().toString(), typeValidation, schemasDrools, rule.getWhenCondition(),
        rule.getThenCondition().get(0), rule.getThenCondition().get(1), originName));

    // that method create another kiebase one by one and verify all rules are correct
    String generatedDRLTest = compiler.compile(ruleAttributesHelper,
        getClass().getResourceAsStream(REGULATION_TEMPLATE_FILE));
    byte[] b1 = generatedDRLTest.getBytes();
    Resource resourceTest = kieServices.getResources().newByteArrayResource(b1);
    KieHelper kieHelperTest = new KieHelper();
    kieHelperTest.addResource(resourceTest, ResourceType.DRL);
    Results results = kieHelperTest.verify();
    // if one rule is not correct he delete it and create a dataset validation about that rule
    if (results.hasMessages(Message.Level.ERROR)) {

      Validation ruleValidation = new Validation();
      ruleValidation.setIdRule(rule.getRuleId().toString());
      ruleValidation.setLevelError(ErrorTypeEnum.BLOCKER);
      ruleValidation.setMessage("The rule: " + rule.getShortCode()
          + ", does not meet the required format to validate the data, please review it.");
      ruleValidation.setValidationDate(ZonedDateTime.now(timeZone).format(dateFormatter));
      ruleValidation.setTypeEntity(EntityTypeEnum.DATASET);
      ruleValidation.setOriginName(originName);

      DatasetValidation ruleDSValidation = new DatasetValidation();
      ruleDSValidation.setValidation(ruleValidation);
      List<DatasetValidation> ruleDSValidations = new ArrayList<>();
      ruleDSValidations.add(ruleDSValidation);
      datasetValue.setDatasetValidations(ruleDSValidations);

      datasetRepository.save(datasetValue);

    } else {

      ruleAttributes.add(passDataToMap(rule.getReferenceId().toString(),
          rule.getRuleId().toString(), typeValidation, schemasDrools, rule.getWhenCondition(),
          rule.getThenCondition().get(0), rule.getThenCondition().get(1), originName));

    }
  }

  /**
   * Text rule correct.
   *
   * @param rule the rule
   * @return true, if successful
   */
  public boolean textRuleCorrect(Rule rule) {

    KieServices kieServices = KieServices.Factory.get();
    ObjectDataCompiler compiler = new ObjectDataCompiler();
    Boolean correctRules = Boolean.TRUE;
    List<Map<String, String>> ruleAttribute = new ArrayList<>();
    TypeValidation typeValidation = null;
    String schemasDrools = "";
    switch (rule.getType()) {
      case DATASET:
        schemasDrools = SchemasDrools.ID_DATASET_SCHEMA.getValue();
        typeValidation = TypeValidation.DATASET;
        break;
      case TABLE:
        schemasDrools = SchemasDrools.ID_TABLE_SCHEMA.getValue();
        typeValidation = TypeValidation.TABLE;
        break;
      case RECORD:
        schemasDrools = SchemasDrools.ID_RECORD_SCHEMA.getValue();
        typeValidation = TypeValidation.RECORD;
        break;
      case FIELD:
        schemasDrools = SchemasDrools.ID_FIELD_SCHEMA.getValue();
        typeValidation = TypeValidation.FIELD;
    }

    ruleAttribute.add(passDataToMap(rule.getReferenceId().toString(), rule.getRuleId().toString(),
        typeValidation, schemasDrools, rule.getWhenCondition(), rule.getThenCondition().get(0),
        rule.getThenCondition().get(1), "orig"));


    String generatedDRLTest =
        compiler.compile(ruleAttribute, getClass().getResourceAsStream(RULE_CHECK_TEMPLATE));

    byte[] b1 = generatedDRLTest.getBytes();
    Resource resourceTest = kieServices.getResources().newByteArrayResource(b1);
    KieHelper kieHelperTest = new KieHelper();
    kieHelperTest.addResource(resourceTest, ResourceType.DRL);
    Results results = kieHelperTest.verify();
    // if one rule is not correct he delete it and create a dataset validation about that rule
    if (results.hasMessages(Message.Level.ERROR)) {
      correctRules = Boolean.FALSE;
    }
    return correctRules;
  }


  /**
   * Pass data to map.
   *
   * @param idSchema the id schema
   * @param idRule the id rule
   * @param typeValidation the type validation
   * @param schemaName the schema name
   * @param whenCondition the when condition
   * @param message the message
   * @param error the error
   * @param tableSchemaName the table schema name
   * @return the map
   */
  private Map<String, String> passDataToMap(String idSchema, String idRule,
      TypeValidation typeValidation, String schemaName, String whenCondition, String message,
      String error, String tableSchemaName) {
    Map<String, String> ruleAdd = new HashMap<>();
    ruleAdd.put(ConditionsDrools.DATASCHEMA_ID.getValue(), idSchema);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.SCHEMA_NAME.getValue(), schemaName);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.TYPE_VALIDATION.getValue(), typeValidation.getValue());
    ruleAdd.put(ConditionsDrools.WHEN_CONDITION.getValue(), whenCondition);
    ruleAdd.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(), message);
    ruleAdd.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(), error);
    ruleAdd.put(ConditionsDrools.ORIGIN_NAME.getValue(),
        tableSchemaName != null ? tableSchemaName : "");
    return ruleAdd;
  }

}
