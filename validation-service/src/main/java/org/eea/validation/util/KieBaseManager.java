package org.eea.validation.util;

import java.io.FileNotFoundException;
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
import org.springframework.scheduling.annotation.Async;
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

  /**
   * The rules repository.
   */
  @Autowired
  private RulesRepository rulesRepository;
  /**
   * The dataset metabase controller.
   */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;

  /**
   * The schemas repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;


  /**
   * Reload rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchema the dataset schema
   *
   * @return the kie base
   *
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
                case NUMBER_INTEGER:
                  expression.append("( !isBlank(value) || isNumberInteger(value) && ");
                  rule.setWhenCondition(
                      rule.getWhenCondition().replaceAll("value", "doubleData(value)"));
                  break;
                case NUMBER_DECIMAL:
                  expression.append("( !isBlank(value) || isNumberDecimal(value) && ");
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
                  expression.append("( !isBlank(value) || ");
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
        ruleAttributes.add(passDataToMap(rule.getReferenceId().toString(),
            rule.getRuleId().toString(), typeValidation, schemasDrools, rule.getWhenCondition(),
            rule.getThenCondition().get(0), rule.getThenCondition().get(1), originName));
      });
    }

    KieHelper kieHelper = kiebaseAssemble(compiler, kieServices, ruleAttributes);
    // this is a shared variable in a single instanced object.
    return kieHelper.build();
  }

  /**
   * Text rule and put disable if is the rule have not correct format
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   *
   * @return true, if successful
   */
  @Async
  public void textRuleCorrect(String datasetSchemaId, Rule rule) {

    KieServices kieServices = KieServices.Factory.get();
    ObjectDataCompiler compiler = new ObjectDataCompiler();
    List<Map<String, String>> ruleAttribute = new ArrayList<>();
    TypeValidation typeValidation = TypeValidation.DATASET;
    String schemasDrools = "";
    String whenCondition = rule.getWhenCondition();
    StringBuilder expression = new StringBuilder("");
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
        break;
    }

    Document documentField =
        schemasRepository.findFieldSchema(datasetSchemaId, rule.getReferenceId().toString());
    DataType datatype = DataType.valueOf(documentField.get("typeData").toString());

    // we do the same thing like in kiebase validation part
    if (null != datatype) {
      switch (datatype) {
        case NUMBER_INTEGER:
          expression.append("( !isBlank(value) || isNumberInteger(value) && ");
          whenCondition = whenCondition.replaceAll("value", "doubleData(value)");
          break;
        case NUMBER_DECIMAL:
          expression.append("( !isBlank(value) || isNumberDecimal(value) && ");
          whenCondition = whenCondition.replaceAll("value", "doubleData(value)");
          break;
        case DATE:
          expression.append("( !isBlank(value) || isDateYYYYMMDD(value) && ");
          whenCondition = whenCondition.replaceAll("EQUALS", "==");
          break;
        case BOOLEAN:
          expression.append("( !isBlank(value) || isBoolean(value) && ");
          whenCondition = whenCondition.replaceAll("EQUALS", "==");
          break;
        case COORDINATE_LAT:
          expression.append("( !isBlank(value) || isCordenateLat(value) && ");
          whenCondition = whenCondition.replaceAll("value", "doubleData(value)");
          break;
        case COORDINATE_LONG:
          expression.append("( !isBlank(value) || isCordenateLong(value) && ");
          whenCondition = whenCondition.replaceAll("value", "doubleData(value)");
          break;
        default:
          expression.append("( !isBlank(value) || ");
          break;
      }
    }
    if (!StringUtils.isBlank(expression.toString())) {
      String whenConditionWithParenthesis =
          new StringBuilder("").append("(").append(whenCondition).append(")").toString();
      expression.append(whenConditionWithParenthesis).append(")").toString();
    }
    ruleAttribute.add(passDataToMap(rule.getReferenceId().toString(), rule.getRuleId().toString(),
        typeValidation, schemasDrools, expression.toString(), rule.getThenCondition().get(0),
        rule.getThenCondition().get(1), ""));

    // We create the same text like in kiebase and with that part we check if the rule is correct
    KieHelper kieHelperTest = kiebaseAssemble(compiler, kieServices, ruleAttribute);

    Results results = kieHelperTest.verify();
    // if one rule is not correct we return a false and the rule
    // will not be created
    if (results.hasMessages(Message.Level.ERROR)) {
      rule.setEnabled(Boolean.FALSE);
      rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);

    }
  }

  /**
   * Kiebase assemble. In this method we create the kiebase
   *
   * @param compiler the compiler
   * @param kieServices the kie services
   * @param ruleAttributesHelper the rule attributes helper
   *
   * @return the kie helper
   */
  private KieHelper kiebaseAssemble(ObjectDataCompiler compiler, KieServices kieServices,
      List<Map<String, String>> ruleAttributesHelper) {

    String generatedDRLTest = compiler.compile(ruleAttributesHelper,
        getClass().getResourceAsStream(REGULATION_TEMPLATE_FILE));
    byte[] b1 = generatedDRLTest.getBytes();
    Resource resourceTest = kieServices.getResources().newByteArrayResource(b1);
    KieHelper kieHelperTest = new KieHelper();
    kieHelperTest.addResource(resourceTest, ResourceType.DRL);
    return kieHelperTest;
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
   *
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
