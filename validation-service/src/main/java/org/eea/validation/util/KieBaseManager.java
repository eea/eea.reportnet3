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
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleExpressionVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
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

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Reload rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema
   *
   * @return the kie base
   *
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long datasetId, String datasetSchemaId) throws FileNotFoundException {

    ObjectId datasetSchemaOId = new ObjectId(datasetSchemaId);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    ObjectDataCompiler compiler = new ObjectDataCompiler();
    KieServices kieServices = KieServices.Factory.get();

    // Get enabled and verified rules
    RulesSchema schemaRules = rulesRepository.getActiveAndVerifiedRules(datasetSchemaOId);

    // we bring the datasetschema
    DataSetSchema dataSetSchema = schemasRepository.findByIdDataSetSchema(datasetSchemaOId);

    // here we have the mothod who compose the field in template
    if (null != schemaRules && null != schemaRules.getRules()
        && !schemaRules.getRules().isEmpty()) {
      schemaRules.getRules().stream().forEach(rule -> {
        String schemasDrools = "";
        String originName = "";
        StringBuilder expression = new StringBuilder("");
        TypeValidation typeValidation = null;
        switch (rule.getType()) {
          case DATASET:
            schemasDrools = SchemasDrools.ID_DATASET_SCHEMA.getValue();
            typeValidation = TypeValidation.DATASET;
            originName =
                datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataSetName();
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
            Document documentField = schemasRepository.findFieldSchema(datasetSchemaId,
                rule.getReferenceId().toString());
            DataType dataType = DataType.valueOf(documentField.get("typeData").toString());

            // that switch clear the validations , and check if the datas in values are correct
            if (null != dataType && !rule.isAutomatic()) {
              switch (dataType) {
                case NUMBER_INTEGER:
                  expression.append("( !isBlank(value) || isNumberInteger(value) && ");
                  break;
                case NUMBER_DECIMAL:
                  expression.append("( !isBlank(value) || isNumberDecimal(value) && ");
                  break;
                case DATE:
                  expression.append("( !isBlank(value) || isDateYYYYMMDD(value) && ");
                  break;
                case BOOLEAN:
                  expression.append("( !isBlank(value) || isBoolean(value) && ");;
                  break;
                case COORDINATE_LAT:
                  expression.append("( !isBlank(value) || isCordenateLat(value) && ");
                  break;
                case COORDINATE_LONG:
                  expression.append("( !isBlank(value) || isCordenateLong(value) && ");
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
   * @throws EEAException
   */
  @Async
  public void textRuleCorrect(String datasetSchemaId, Rule rule) throws EEAException {
    DataType dataType = null;
    Map<String, DataType> dataTypeMap = new HashMap<>();
    RuleExpressionVO ruleExpressionVO = new RuleExpressionVO(rule.getWhenCondition());
    switch (rule.getType()) {
      case DATASET:
        break;
      case TABLE:
        break;
      case RECORD:

        break;
      case FIELD:
        Document document =
            schemasRepository.findFieldSchema(datasetSchemaId, rule.getReferenceId().toString());
        dataType = DataType.valueOf(document.get("typeData").toString());
        dataTypeMap.put("VALUE", dataType);
        break;
    }


    if (!ruleExpressionVO.isDataTypeCompatible(rule.getType(), dataTypeMap)) {
      rule.setVerified(false);
      rule.setEnabled(false);
      rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.INVALIDATED_QC_RULE_EVENT, null,
          NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
              .datasetSchemaId(datasetSchemaId).error("The QC Rule is disabled")
              .shortCode(rule.getShortCode()).build());
    }
    // ruleAttribute.add(passDataToMap(rule.getReferenceId().toString(),
    // rule.getRuleId().toString(),
    // typeValidation, schemasDrools, expression.toString(), rule.getThenCondition().get(0),
    // rule.getThenCondition().get(1), ""));
    //
    // // We create the same text like in kiebase and with that part we check if the rule is correct
    // KieHelper kieHelperTest = kiebaseAssemble(compiler, kieServices, ruleAttribute);
    //
    // // Check rule integrity
    // Results results = kieHelperTest.verify();
    //
    // if (results.hasMessages(Message.Level.ERROR)) {
    // rule.setVerified(false);
    // rule.setEnabled(false);
    // rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    // kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.INVALIDATED_QC_RULE_EVENT, null,
    // NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
    // .datasetSchemaId(datasetSchemaId).error("The QC Rule is disabled")
    // .shortCode(rule.getShortCode()).build());
    // } else {
    // rule.setVerified(true);
    // rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    // kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.VALIDATED_QC_RULE_EVENT, null,
    // NotificationVO.builder().user((String) ThreadPropertiesManager.getVariable("user"))
    // .datasetSchemaId(datasetSchemaId).shortCode(rule.getShortCode()).build());
    // }
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
