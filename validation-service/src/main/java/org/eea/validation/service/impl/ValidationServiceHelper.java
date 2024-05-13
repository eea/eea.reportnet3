package org.eea.validation.service.impl;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.drools.template.ObjectDataCompiler;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RuleExpressionService;
import org.eea.validation.util.drools.compose.ConditionsDrools;
import org.eea.validation.util.drools.compose.SchemasDrools;
import org.eea.validation.util.drools.compose.TypeValidation;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidationServiceHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceHelper.class);
  private static final String REGULATION_TEMPLATE_FILE = "/templateRules.drl";
  private static final String TYPE_DATA = "typeData";
  private static final String KEYWORDS = "DELETE,INSERT,DROP";

  @Autowired
  private DatasetMetabaseController datasetMetabaseController;
  @Autowired
  private DatasetSchemaController datasetSchemaController;
  @Autowired
  private RuleExpressionService ruleExpressionService;
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;
  @Autowired
  private RulesRepository rulesRepository;
  @Autowired
  private SchemasRepository schemasRepository;
  @Autowired
  private DatasetRepository datasetRepository;

  public void validateAllRules(Long datasetId, boolean checkNoSQL, Object user) {
    try {
      ThreadPropertiesManager.setVariable("user", user);
      List<Rule> errorRulesList = new ArrayList<>();

      String datasetSchemaId = datasetMetabaseController.findDatasetSchemaIdById(datasetId);
      Long dataflowId = datasetMetabaseController.findDatasetMetabaseById(datasetId).getDataflowId();

      RulesSchema rulesSchema = rulesRepository.findByIdDatasetSchema(new ObjectId(datasetSchemaId));
      List<Rule> rulesSQLSchema = rulesRepository.findSqlRules(new ObjectId(datasetSchemaId));

      if (null != rulesSchema && !rulesSchema.getRules().isEmpty()) {
        rulesSchema.getRules().stream().filter(rule -> !rule.isAutomatic()).forEach(rule -> {
          updateRuleState(errorRulesList, datasetId, checkNoSQL, datasetSchemaId, rulesSQLSchema, rule);
        });
      }

      if (!errorRulesList.isEmpty()) {
        RulesSchema rulesdisabled = rulesRepository.getAllDisabledRules(new ObjectId(datasetSchemaId));
        RulesSchema rulesUnchecked = rulesRepository.getAllUncheckedRules(new ObjectId(datasetSchemaId));
        NotificationVO notificationVO = NotificationVO.builder()
            .user(SecurityContextHolder.getContext().getAuthentication().getName())
            .datasetId(datasetId).dataflowId(dataflowId)
            .invalidRules(rulesUnchecked.getRules().size())
            .disabledRules(rulesdisabled.getRules().size())
            .build();
        LOG.info("SQL rules contains errors");
        releaseNotification(EventType.DISABLE_NAMES_TYPES_RULES_ERROR_EVENT, notificationVO);
      }
    } catch (Exception e) {
      LOG.error("Unexpected error! Error validating all sql rules for datasetId: {}, checkNoSQL: {}, user: {}. Message: {}", datasetId, checkNoSQL, user, e.getMessage());
      throw e;
    }
  }

  /**
   * Update rule state.
   *
   * @param errorRulesList  the error rules list
   * @param datasetId       the dataset id
   * @param checkNoSQL      the check no SQL
   * @param datasetSchemaId the dataset schema id
   * @param rulesSQLSchema  the rules SQL schema
   * @param rule            the rule
   */
  private void updateRuleState(List<Rule> errorRulesList, Long datasetId, boolean checkNoSQL, String datasetSchemaId, List<Rule> rulesSQLSchema, Rule rule) {
    try {
      Boolean valid = null;
      if (checkNoSQL && null == rule.getSqlSentence()) {
        valid = validateRule(datasetSchemaId, rule);
      }
      if (rulesSQLSchema.contains(rule) && null != rule.getSqlSentence()) {
        valid = validateSQLRule(rule.getSqlSentence(), datasetId, rule);
      }
      if (Boolean.TRUE.equals(valid)) {
        rule.setVerified(true);
      }
      if (Boolean.FALSE.equals(valid)) {
        if (rule.getVerified().equals(Boolean.TRUE)) {
          rule.setVerified(false);
          rule.setEnabled(false);
          errorRulesList.add(rule);
        } else {
          rule.setEnabled(false);
        }
      }
      rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    } catch (Exception e) {
      ObjectId ruleId = (rule != null) ? rule.getRuleId() : null;
      LOG.error("Unexpected error! Error updating rule state for datasetId {} and ruleId {}. Message: {}", datasetId, ruleId, e.getMessage());
      throw e;
    }
  }

  /**
   * Validate rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule            the rule
   * @return the boolean
   */
  private Boolean validateRule(String datasetSchemaId, Rule rule) {
    try {
      Map<String, DataType> dataTypeMap = new HashMap<>();
      EntityTypeEnum ruleType = rule.getType();
      String ruleExpressionString = rule.getWhenCondition();
      boolean isCorrect = false;
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
          return validateWithKiebase(rule);
      }
      if (ruleExpressionService.isDataTypeCompatible(ruleExpressionString, ruleType, dataTypeMap)) {
        isCorrect = true;
        LOG.info("Rule validation passed: {}", rule);
      } else {
        isCorrect = false;
        LOG.info("Rule validation not passed: {}", rule);
      }
      return isCorrect;
    } catch (Exception e) {
      ObjectId ruleId = (rule != null) ? rule.getRuleId() : null;
      LOG.error("Unexpected error! Error validating rule state for datasetSchemaId {} and ruleId {}. Message: {}", datasetSchemaId, ruleId, e.getMessage());
      throw e;
    }
  }

  /**
   * Validate SQL rule.
   *
   * @param query     the query
   * @param datasetId the dataset id
   * @param rule      the rule
   * @return the boolean
   */
  private Boolean validateSQLRule(String query, Long datasetId, Rule rule) {
    boolean isSQLCorrect = true;
    // validate query
    if (!StringUtils.isBlank(query)) {
      // validate query sintax
      if (checkQuerySyntax(query)) {
        try {
          String preparedquery = query.contains(";") ? query.replace(";", "") + " limit 5" : query + " limit 5";
          launchValidationQuery(preparedquery, datasetId, rule);
        } catch (EEAInvalidSQLException e) {
          LOG.info("SQL is not correct: {}, {}", e.getMessage(), e);
          isSQLCorrect = false;
        }
      } else {
        isSQLCorrect = false;
      }
    } else {
      isSQLCorrect = false;
    }
    if (!isSQLCorrect) {
      LOG.info("Rule validation not passed: {}", rule);
    } else {
      LOG.info("Rule validation passed: {}", rule);
    }
    return isSQLCorrect;
  }

  /**
   * Validate with kiebase.
   *
   * @param rule the rule
   * @return the boolean
   */
  private Boolean validateWithKiebase(Rule rule) {
    boolean isCorrect = false;
    try {
      if (EntityTypeEnum.TABLE.equals(rule.getType())) {
        KieServices kieServices = KieServices.Factory.get();
        ObjectDataCompiler compiler = new ObjectDataCompiler();
        List<Map<String, String>> ruleAttribute = new ArrayList<>();
        ruleAttribute.add(passDataToMap(rule.getReferenceId().toString(), rule.getRuleId().toString(), TypeValidation.TABLE, SchemasDrools.ID_TABLE_SCHEMA.getValue(), rule.getWhenCondition(), rule.getThenCondition().get(0), rule.getThenCondition().get(1), "tableName", "shortcode", "fieldName"));
        // We create the same text like in kiebase and with that part we check if the rule is correct
        KieHelper kieHelperTest = kiebaseAssemble(compiler, kieServices, ruleAttribute);
        // Check rule integrity
        Results results = kieHelperTest.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
          isCorrect = false;
          LOG.info("Rule validation with Kiebase not passed: {}", rule);
        } else {
          isCorrect = true;
          LOG.info("Rule validation with Kiebase passed: {}", rule);
        }
      } else {
        LOG.info("Not a validable rule: {}", rule);
      }
      return isCorrect;
    } catch (Exception e) {
      ObjectId ruleId = (rule != null) ? rule.getRuleId() : null;
      LOG.error("Unexpected error! Error validating rule with kiebase for ruleId {}. Message: {}", ruleId, e.getMessage());
      throw e;
    }
  }

  /**
   * Kiebase assemble.
   *
   * @param compiler             the compiler
   * @param kieServices          the kie services
   * @param ruleAttributesHelper the rule attributes helper
   * @return the kie helper
   */
  private KieHelper kiebaseAssemble(ObjectDataCompiler compiler, KieServices kieServices, List<Map<String, String>> ruleAttributesHelper) {
    String generatedDRLTest = compiler.compile(ruleAttributesHelper, getClass().getResourceAsStream(REGULATION_TEMPLATE_FILE));
    byte[] b1 = generatedDRLTest.getBytes();
    Resource resourceTest = kieServices.getResources().newByteArrayResource(b1);
    KieHelper kieHelperTest = new KieHelper();
    kieHelperTest.addResource(resourceTest, ResourceType.DRL);
    return kieHelperTest;
  }

  /**
   * Check query syntax.
   *
   * @param query the query
   * @return the boolean
   */
  private boolean checkQuerySyntax(String query) {
    boolean queryContainsKeyword = true;
    String[] queryKeywords = KEYWORDS.split(",");
    for (String word : queryKeywords) {
      if (query.toLowerCase().contains(word.toLowerCase())) {
        queryContainsKeyword = false;
        break;
      }
    }
    return queryContainsKeyword;
  }

  /**
   * Release notification.
   *
   * @param eventType      the event type
   * @param notificationVO the notification VO
   */
  private void releaseNotification(EventType eventType, NotificationVO notificationVO) {
    try {
      kafkaSenderUtils.releaseNotificableKafkaEvent(eventType, null, notificationVO);
    } catch (EEAException e) {
      LOG.error("Unable to release notification: {}, {}", eventType, notificationVO);
    } catch (Exception e) {
      String eventTopic = (eventType != null) ? eventType.getTopic() : null;
      Long datasetId = (notificationVO != null) ? notificationVO.getDatasetId() : null;
      LOG.error("Unexpected error! Error releasing notification for event topic {} and datasetId {}. Message: {}", eventTopic, datasetId, e.getMessage());
      throw e;
    }
  }

  /**
   * Retrieve table data.
   *
   * @param query     the query
   * @param datasetId the dataset id
   * @param rule      the rule
   * @return the table value
   * @throws PSQLException          the PSQL exception
   * @throws EEAInvalidSQLException
   */
  private void launchValidationQuery(String query, Long datasetId, Rule rule) throws EEAInvalidSQLException {
    DataSetSchemaVO schema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);
    String entityName = "";
    Long idTable = null;
    String newQuery = query;
    switch (rule.getType()) {
      case FIELD:
        entityName = retriveFieldName(schema, rule.getReferenceId().toString());
        idTable = getTableId(schema, rule.getReferenceId().toString(), datasetId);
        break;
      case TABLE:
        entityName = retriveTableName(schema, rule.getReferenceId().toString());
        idTable = datasetRepository.getTableId(rule.getReferenceId().toString(), datasetId);
        break;
      case RECORD:
        idTable = retriveIsTableFromRecordSchema(schema, rule.getReferenceId().toString(), datasetId);
        break;
      default:
        break;
    }
    LOG.info("Query to be executed: {}", newQuery);
    datasetRepository.queryRSExecution(newQuery, rule.getType(), entityName, datasetId, idTable);
  }

  /**
   * Pass data to map.
   *
   * @param idSchema        the id schema
   * @param idRule          the id rule
   * @param typeValidation  the type validation
   * @param schemaName      the schema name
   * @param whenCondition   the when condition
   * @param message         the message
   * @param error           the error
   * @param tableSchemaName the table schema name
   * @param shortCode       the short code
   * @param fieldName       the field name
   * @return the map
   */
  private Map<String, String> passDataToMap(String idSchema, String idRule, TypeValidation typeValidation, String schemaName, String whenCondition, String message, String error, String tableSchemaName, String shortCode, String fieldName) {
    Map<String, String> ruleAdd = new HashMap<>();
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
    ruleAdd.put(ConditionsDrools.TABLE_NAME.getValue(), null != tableSchemaName && !tableSchemaName.isEmpty() ? tableSchemaName : "Dataset Table Name");
    ruleAdd.put(ConditionsDrools.FIELD_NAME.getValue(), null != tableSchemaName && !fieldName.isEmpty() ? fieldName : "None Field Name");
    ruleAdd.put(ConditionsDrools.SHORT_CODE.getValue(), shortCode);
    return ruleAdd;
  }

  /**
   * Retrive field name.
   *
   * @param schema        the schema
   * @param idFieldSchema the id field schema
   * @return the string
   */
  private String retriveFieldName(DataSetSchemaVO schema, String idFieldSchema) {
    String fieldName = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
        if (field.getId().equals(idFieldSchema)) {
          fieldName = field.getName();
        }
      }
    }
    return fieldName;
  }

  /**
   * Retrive is table from field schema.
   *
   * @param schema        the schema
   * @param fieldSchemaId the field schema id
   * @param datasetId     the dataset id
   * @return the long
   */
  private Long getTableId(DataSetSchemaVO schema, String fieldSchemaId, Long datasetId) {
    String tableSchemaId = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
        if (field.getId().equals(fieldSchemaId)) {
          tableSchemaId = table.getIdTableSchema();
          break;
        }
      }
    }
    return datasetRepository.getTableId(tableSchemaId, datasetId);
  }

  /**
   * Retrive table name.
   *
   * @param schema        the schema
   * @param idTableSchema the id table schema
   * @return the string
   */
  private String retriveTableName(DataSetSchemaVO schema, String idTableSchema) {
    String tableName = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      if (table.getIdTableSchema().equals(idTableSchema)) {
        tableName = table.getNameTableSchema();
      }
    }
    return tableName;
  }

  /**
   * Retrive is table from record schema.
   *
   * @param schema         the schema
   * @param recordSchemaId the record schema id
   * @param datasetId      the dataset id
   * @return the long
   */
  private Long retriveIsTableFromRecordSchema(DataSetSchemaVO schema, String recordSchemaId, Long datasetId) {
    String tableSchemaId = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      if (table.getRecordSchema().getIdRecordSchema().equals(recordSchemaId)) {
        tableSchemaId = table.getIdTableSchema();
      }
    }
    return datasetRepository.getTableId(tableSchemaId, datasetId);
  }
}
