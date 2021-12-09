package org.eea.validation.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.drools.template.ObjectDataCompiler;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RuleExpressionService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** The Class KieBaseManager. */
@Component
public class KieBaseManager {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(KieBaseManager.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant REGULATION_TEMPLATE_FILE. */
  private static final String REGULATION_TEMPLATE_FILE = "/templateRules.drl";

  /** The Constant REGULATION_TEMPLATE_FILE. */
  private static final String TYPE_DATA = "typeData";

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The dataset metabase controller. */
  @Autowired
  private DatasetMetabaseController datasetMetabaseController;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The rule expression service. */
  @Autowired
  private RuleExpressionService ruleExpressionService;

  /**
   * Reload rules.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema
   * @param sqlRule the sql rule
   * @return the kie base
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long datasetId, String datasetSchemaId, Rule sqlRule)
      throws FileNotFoundException {

    ObjectId datasetSchemaOId = new ObjectId(datasetSchemaId);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    ObjectDataCompiler compiler = new ObjectDataCompiler();
    KieServices kieServices = KieServices.Factory.get();
    RulesSchema schemaRules = null;
    List<Rule> filteredRules = new ArrayList<>();
    if (sqlRule == null) {
      schemaRules = rulesRepository.getActiveAndVerifiedRules(datasetSchemaOId);
      filteredRules = schemaRules.getRules().stream()
          .filter(rule -> !rule.getWhenCondition().startsWith("isSQL"))
          .collect(Collectors.toList());
    } else {
      if (sqlRule.isEnabled()) {
        filteredRules.add(sqlRule);
      }
      schemaRules = new RulesSchema();
    }
    schemaRules.setRules(filteredRules);
    // Get enabled and verified rules

    // we bring the datasetschema
    DataSetSchema dataSetSchema = schemasRepository.findByIdDataSetSchema(datasetSchemaOId);

    // here we have the method who compose the field in template
    if (null != schemaRules.getRules() && !schemaRules.getRules().isEmpty()) {
      schemaRules.getRules().stream().forEach(rule -> {
        String schemasDrools = "";
        String tableName = "";
        String fieldName = "";
        TypeValidation typeValidation = null;
        switch (rule.getType()) {
          case DATASET:
            schemasDrools = SchemasDrools.ID_DATASET_SCHEMA.getValue();
            typeValidation = TypeValidation.DATASET;
            tableName =
                datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataSetName();
            break;
          case TABLE:
            schemasDrools = SchemasDrools.ID_TABLE_SCHEMA.getValue();
            typeValidation = TypeValidation.TABLE;
            tableName = fillTableOriginName(dataSetSchema, rule, tableName);
            break;
          case RECORD:
            // transform the rule to a tablerule
            if (isSqlSentence(rule)) {
              schemasDrools = SchemasDrools.ID_TABLE_SCHEMA.getValue();
              typeValidation = TypeValidation.TABLE;
              fillRecordReferenceID(dataSetSchema, rule);
            } else {
              schemasDrools = SchemasDrools.ID_RECORD_SCHEMA.getValue();
              typeValidation = TypeValidation.RECORD;
              tableName = fillRecordOriginName(dataSetSchema, rule, tableName);
            }
            break;
          case FIELD:
            // transform the rule to a tablerule
            if (isSqlSentence(rule)) {
              schemasDrools = SchemasDrools.ID_TABLE_SCHEMA.getValue();
              typeValidation = TypeValidation.TABLE;
              fillFieldReferenceId(dataSetSchema, rule);
            } else {
              schemasDrools = SchemasDrools.ID_FIELD_SCHEMA.getValue();
              typeValidation = TypeValidation.FIELD;
              tableName = fillFieldOriginName(dataSetSchema, rule).get(0);
              fieldName = fillFieldOriginName(dataSetSchema, rule).get(1);
            }
            break;
          default:
            break;
        }
        ruleAttributes.add(passDataToMap(rule.getReferenceId().toString(),
            rule.getRuleId().toString(), typeValidation, schemasDrools,
            "RuleOperators.setEntity(this) && " + rule.getWhenCondition(),
            rule.getThenCondition().get(0), rule.getThenCondition().get(1), tableName,
            rule.getShortCode(), fieldName));
      });
    }

    KieHelper kieHelper = kiebaseAssemble(compiler, kieServices, ruleAttributes);
    // this is a shared variable in a single instanced object.
    return kieHelper.build();
  }

  /**
   * Checks if is sql sentence.
   *
   * @param rule the rule
   * @return true, if is sql sentence
   */
  private boolean isSqlSentence(Rule rule) {
    return null != rule.getSqlSentence() && !rule.getSqlSentence().isEmpty();
  }


  /**
   * Fill record reference ID.
   *
   * @param dataSetSchema the data set schema
   * @param rule the rule
   */
  private void fillRecordReferenceID(DataSetSchema dataSetSchema, Rule rule) {
    for (TableSchema table : dataSetSchema.getTableSchemas()) {
      if (table.getRecordSchema().getIdRecordSchema().equals(rule.getReferenceId())) {
        rule.setReferenceId(table.getIdTableSchema());
      }
    }
  }


  /**
   * Fill field reference id.
   *
   * @param dataSetSchema the data set schema
   * @param rule the rule
   */
  private void fillFieldReferenceId(DataSetSchema dataSetSchema, Rule rule) {
    for (TableSchema table : dataSetSchema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().equals(rule.getReferenceId())) {
          rule.setReferenceId(table.getIdTableSchema());
        }
      }
    }
  }


  /**
   * Fill table origin name.
   *
   * @param dataSetSchema the data set schema
   * @param rule the rule
   * @param tableName the origin name
   * @return the string
   */
  private String fillTableOriginName(DataSetSchema dataSetSchema, Rule rule, String tableName) {
    for (TableSchema table : dataSetSchema.getTableSchemas()) {
      if (table.getIdTableSchema().equals(rule.getReferenceId())) {
        tableName = table.getNameTableSchema();
      }
    }
    return tableName;
  }

  /**
   * Fill record origin name.
   *
   * @param dataSetSchema the data set schema
   * @param rule the rule
   * @param tableName the origin name
   * @return the string
   */
  private String fillRecordOriginName(DataSetSchema dataSetSchema, Rule rule, String tableName) {
    for (TableSchema table : dataSetSchema.getTableSchemas()) {
      if (table.getRecordSchema().getIdRecordSchema().equals(rule.getReferenceId())) {
        tableName = table.getNameTableSchema();
      }
    }
    return tableName;
  }


  /**
   * Fill field origin name.
   *
   * @param dataSetSchema the data set schema
   * @param rule the rule
   * @return the list
   */
  private List<String> fillFieldOriginName(DataSetSchema dataSetSchema, Rule rule) {
    List<String> listValue = new ArrayList<>();
    for (TableSchema table : dataSetSchema.getTableSchemas()) {
      for (FieldSchema field : table.getRecordSchema().getFieldSchema()) {
        if (field.getIdFieldSchema().equals(rule.getReferenceId())) {
          listValue.add(table.getNameTableSchema());
          listValue.add(field.getHeaderName());
        }
      }
    }
    return listValue;
  }

  /**
   * Text rule correct.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @SuppressWarnings("unchecked")
  public void validateRule(String datasetSchemaId, Rule rule) {

    Map<String, DataType> dataTypeMap = new HashMap<>();
    EntityTypeEnum ruleType = rule.getType();
    String ruleExpressionString = rule.getWhenCondition();
    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName())
        .datasetSchemaId(datasetSchemaId).shortCode(rule.getShortCode())
        .error("The QC Rule is disabled").build();

    switch (ruleType) {
      case RECORD:
        String recordSchemaId = rule.getReferenceId().toString();
        Document recordSchema = schemasRepository.findRecordSchema(datasetSchemaId, recordSchemaId);
        for (Document field : (ArrayList<Document>) recordSchema.get("fieldSchemas")) {
          String fieldSchemaId = field.get("_id").toString();
          DataType fieldDataType = DataType.valueOf(field.get(TYPE_DATA).toString());
          dataTypeMap.put(fieldSchemaId, fieldDataType);
        }
        break;
      case FIELD:
        String fieldSchemaId = rule.getReferenceId().toString();
        Document fieldSchema = schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaId);
        dataTypeMap.put("VALUE", DataType.valueOf(fieldSchema.get(TYPE_DATA).toString()));
        break;
      default:
        validateWithKiebase(datasetSchemaId, rule, notificationVO);
        return;
    }

    if (ruleExpressionService.isDataTypeCompatible(ruleExpressionString, ruleType, dataTypeMap)) {
      notificationEventType = EventType.VALIDATED_QC_RULE_EVENT;
      rule.setVerified(true);
      LOG.info("Rule validation passed: {}", rule);
    } else {
      notificationEventType = EventType.INVALIDATED_QC_RULE_EVENT;
      rule.setVerified(false);
      rule.setEnabled(false);
      LOG.info("Rule validation not passed: {}", rule);
    }

    rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    releaseNotification(notificationEventType, notificationVO);
  }

  /**
   * Validate with kiebase.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   * @param notificationVO the notification VO
   */
  @Async
  public void validateWithKiebase(String datasetSchemaId, Rule rule,
      NotificationVO notificationVO) {

    if (EntityTypeEnum.TABLE.equals(rule.getType())) {
      EventType notificationEventType = null;
      KieServices kieServices = KieServices.Factory.get();
      ObjectDataCompiler compiler = new ObjectDataCompiler();
      List<Map<String, String>> ruleAttribute = new ArrayList<>();

      ruleAttribute.add(passDataToMap(rule.getReferenceId().toString(), rule.getRuleId().toString(),
          TypeValidation.TABLE, SchemasDrools.ID_TABLE_SCHEMA.getValue(), rule.getWhenCondition(),
          rule.getThenCondition().get(0), rule.getThenCondition().get(1), "tableName", "shortcode",
          "fieldName"));
      // We create the same text like in kiebase and with that part we check if the rule is correct
      KieHelper kieHelperTest = kiebaseAssemble(compiler, kieServices, ruleAttribute);

      // Check rule integrity
      Results results = kieHelperTest.verify();

      if (results.hasMessages(Message.Level.ERROR)) {
        notificationEventType = EventType.INVALIDATED_QC_RULE_EVENT;
        rule.setVerified(false);
        rule.setEnabled(false);
        LOG.info("Rule validation with Kiebase not passed: {}", rule);
      } else {
        notificationEventType = EventType.VALIDATED_QC_RULE_EVENT;
        rule.setVerified(true);
        LOG.info("Rule validation with Kiebase passed: {}", rule);
      }

      rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
      releaseNotification(notificationEventType, notificationVO);
    } else {
      LOG.info("Not a validable rule: {}", rule);
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
   * @param shortCode the short code
   * @param fieldName the field name
   * @return the map
   */
  private Map<String, String> passDataToMap(String idSchema, String idRule,
      TypeValidation typeValidation, String schemaName, String whenCondition, String message,
      String error, String tableSchemaName, String shortCode, String fieldName) {
    Map<String, String> ruleAdd = new HashMap<>();
    message = message.replaceAll("\"", "'");
    shortCode = shortCode.replaceAll("\"", "'");
    ruleAdd.put(ConditionsDrools.DATASCHEMA_ID.getValue(), idSchema);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.SCHEMA_NAME.getValue(), schemaName);
    ruleAdd.put(ConditionsDrools.RULE_ID.getValue(), idRule);
    ruleAdd.put(ConditionsDrools.TYPE_VALIDATION.getValue(), typeValidation.getValue());
    ruleAdd.put(ConditionsDrools.WHEN_CONDITION.getValue(), whenCondition);
    ruleAdd.put(ConditionsDrools.MESSAGE_FAIL_VALIDATION.getValue(), message);
    ruleAdd.put(ConditionsDrools.TYPE_FAIL_VALIDATION.getValue(), error);
    // the fieldname and table_name should have any value to put in the map because without it
    // drools doesnt
    // work
    ruleAdd.put(ConditionsDrools.TABLE_NAME.getValue(),
        null != tableSchemaName && !tableSchemaName.isEmpty() ? tableSchemaName
            : "Dataset Table Name");
    ruleAdd.put(ConditionsDrools.FIELD_NAME.getValue(),
        null != tableSchemaName && !fieldName.isEmpty() ? fieldName : "None Field Name");
    ruleAdd.put(ConditionsDrools.SHORT_CODE.getValue(), shortCode);

    return ruleAdd;
  }

  /**
   * Release notification.
   *
   * @param eventType the event type
   * @param notificationVO the notification VO
   */
  private void releaseNotification(EventType eventType, NotificationVO notificationVO) {
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Unable to release notification: {}, {}", eventType, notificationVO);
    }
  }
}
