package org.eea.validation.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.mapper.RuleMapper;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.SqlRulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * The Class SqlRulesServiceImpl.
 */
@Service("SQLRulesService")
public class SqlRulesServiceImpl implements SqlRulesService {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SqlRulesServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The Constant KEYWORDS: {@value}.
   */
  private static final String KEYWORDS = "DELETE,INSERT,DROP";

  /**
   * The dataset repository.
   */
  @Autowired
  private DatasetRepository datasetRepository;

  /**
   * The rules repository.
   */
  @Autowired
  private RulesRepository rulesRepository;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The dataset schema controller.
   */
  @Autowired
  private DatasetSchemaController datasetSchemaController;

  /**
   * The rule mapper.
   */
  @Autowired
  private RuleMapper ruleMapper;

  /**
   * Validate SQL rule.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @Override
  @Transactional
  public void validateSQLRule(Long datasetId, String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user((String) ThreadPropertiesManager.getVariable("user")).datasetSchemaId(datasetSchemaId)
        .shortCode(rule.getShortCode()).error("The QC Rule is disabled").build();

    if (validateRule(rule.getSqlSentence(), datasetId, rule).equals(Boolean.TRUE)) {
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
   * Validate SQL rule from datacollection.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param ruleVO the rule VO
   */
  @Override
  public void validateSQLRuleFromDatacollection(Long datasetId, String datasetSchemaId,
      RuleVO ruleVO) {
    Rule rule = ruleMapper.classToEntity(ruleVO);
    if (validateRule(ruleVO.getSqlSentence(), datasetId, rule).equals(Boolean.FALSE)) {
      rule.setVerified(false);
      rule.setEnabled(false);
      rule.setWhenCondition(new StringBuilder().append("isSQLSentence(").append(datasetId)
          .append(",'").append(rule.getRuleId().toString()).append("')").toString());
      LOG.info("Rule validation not passed before pass to datacollection: {}", rule);
      rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    } else {
      rule.setEnabled(true);
      rule.setVerified(true);
      rulesRepository.updateRule(new ObjectId(datasetSchemaId), rule);
    }

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


  /**
   * Validate rule.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param rule the rule
   *
   * @return the boolean
   */

  private Boolean validateRule(String query, Long datasetId, Rule rule) {
    Boolean isSQLCorrect = Boolean.TRUE;
    // validate query
    if (!StringUtils.isBlank(query)) {
      // validate query sintax
      if (checkQuerySyntax(query)) {
        try {
          String preparedquery = "";
          if (query.contains(";")) {
            preparedquery = query.replace(";", "") + " limit 5";
          } else {
            preparedquery = query + " limit 5";
          }
          retrieveTableData(preparedquery, datasetId, rule);
        } catch (SQLException e) {
          LOG_ERROR.error("SQL is not correct: {}, {}", e.getMessage(), e);
          isSQLCorrect = Boolean.FALSE;

        } catch (Exception e) {
          LOG_ERROR.error("SQL is not correct: {}, {}", e.getMessage(), e);
          isSQLCorrect = Boolean.FALSE;
        }
      } else {
        isSQLCorrect = Boolean.FALSE;
      }
    } else {
      isSQLCorrect = Boolean.FALSE;
    }

    return isSQLCorrect;
  }


  /**
   * Check query syntax.
   *
   * @param query the query
   *
   * @return the boolean
   */
  private Boolean checkQuerySyntax(String query) {
    Boolean queryContainsKeyword = Boolean.TRUE;
    String[] queryKeywords = KEYWORDS.split(",");
    for (String word : queryKeywords) {
      if (query.toLowerCase().contains(word.toLowerCase())) {
        queryContainsKeyword = Boolean.FALSE;
        break;
      }
    }
    return queryContainsKeyword;
  }

  /**
   * Gets the rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
   *
   * @return the rule
   */
  @Override
  public Rule getRule(Long datasetId, String ruleId) {
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    RulesSchema rulechema =
        rulesRepository.getActiveAndVerifiedRules(new ObjectId(datasetSchemaId));
    for (Rule rule : rulechema.getRules()) {
      if (rule.getRuleId().equals(new ObjectId(ruleId))) {
        return rule;
      }
    }
    return null;
  }

  /**
   * Retrieve Table Data.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param rule the rule
   *
   * @return the table value
   *
   * @throws SQLException the SQL exception
   */

  @Override
  public TableValue retrieveTableData(String query, Long datasetId, Rule rule) throws SQLException {
    DataSetSchemaVO schema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);
    String entityName = "";
    Long idTable = null;
    switch (rule.getType()) {
      case FIELD:
        entityName = retriveFieldName(schema, rule.getReferenceId().toString());
        idTable =
            retriveIsTableFromFieldSchema(schema, rule.getReferenceId().toString(), datasetId);
        break;
      case TABLE:
        entityName = retriveTableName(schema, rule.getReferenceId().toString());
        idTable = datasetRepository.getTableId(rule.getReferenceId().toString(), datasetId);
        break;
      case RECORD:
        idTable =
            retriveIsTableFromRecordSchema(schema, rule.getReferenceId().toString(), datasetId);
        break;
      case DATASET:
        break;
    }
    TableValue table = new TableValue();
    try {
      table =
          datasetRepository.queryRSExecution(query, rule.getType(), entityName, datasetId, idTable);
    } catch (SQLException e) {
      LOG_ERROR.error("SQL can't be executed: {}", e.getMessage(), e);
    }
    if (null != table && null != table.getRecords() && !table.getRecords().isEmpty()) {
      retrieveValidations(table.getRecords(), datasetId);
    }
    return table;
  }

  /**
   * Retrive is table from field schema.
   *
   * @param schema the schema
   * @param fieldSchemaId the field schema id
   *
   * @return the long
   */
  @Transactional
  private Long retriveIsTableFromFieldSchema(DataSetSchemaVO schema, String fieldSchemaId,
      Long datasetId) {
    String tableSchemaId = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      for (FieldSchemaVO field : table.getRecordSchema().getFieldSchema()) {
        if (field.getId().equals(fieldSchemaId)) {
          tableSchemaId = table.getIdTableSchema();
        }
      }
    }
    return datasetRepository.getTableId(tableSchemaId, datasetId);
  }

  /**
   * Retrive is table from record schema.
   *
   * @param schema the schema
   * @param recordSchemaId the record schema id
   *
   * @return the long
   */
  @Transactional
  private Long retriveIsTableFromRecordSchema(DataSetSchemaVO schema, String recordSchemaId,
      Long datasetId) {
    String tableSchemaId = "";
    for (TableSchemaVO table : schema.getTableSchemas()) {
      if (table.getRecordSchema().getIdRecordSchema().equals(recordSchemaId)) {
        tableSchemaId = table.getIdTableSchema();
      }
    }
    return datasetRepository.getTableId(tableSchemaId, datasetId);
  }

  /**
   * Retrive first result.
   *
   * @param query the query
   * @param datasetId the dataset id
   *
   * @return the list
   */
  @Override
  public List<Object> retriveFirstResult(String query, Long datasetId) {

    List<Object> result = datasetRepository.queryUniqueResultExecution(query);

    return result;
  }

  /**
   * Retrive table name.
   *
   * @param schema the schema
   * @param idTableSchema the id table schema
   *
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
   * Retrive field name.
   *
   * @param schema the schema
   * @param idFieldSchema the id field schema
   *
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
   * Retrieve validations.
   *
   * @param records the records
   * @param datasetId the dataset id
   */
  private void retrieveValidations(List<RecordValue> records, Long datasetId) {
    // retrieve validations to set them into the final result
    List<String> recordIds = records.stream().map(RecordValue::getId).collect(Collectors.toList());
    Map<String, List<FieldValidation>> fieldValidations = getFieldValidations(recordIds, datasetId);
    Map<String, List<RecordValidation>> recordValidations =
        getRecordValidations(recordIds, datasetId);
    records.stream().forEach(record -> {
      record.getFields().stream().filter(field -> null != field).forEach(field -> {
        List<FieldValidation> validations = fieldValidations.get(field.getId());
        field.setFieldValidations(validations);
        if (null != validations && !validations.isEmpty()) {
          field.setLevelError(
              validations.stream().map(validation -> validation.getValidation().getLevelError())
                  .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                  .orElse(ErrorTypeEnum.WARNING));
        }
      });

      List<RecordValidation> validations = recordValidations.get(record.getId());
      record.setRecordValidations(validations);
      if (null != validations && !validations.isEmpty()) {
        record.setLevelError(
            validations.stream().map(validation -> validation.getValidation().getLevelError())
                .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                .orElse(ErrorTypeEnum.WARNING));
      }
    });
  }

  /**
   * Gets the field validations.
   *
   * @param recordIds the record ids
   * @param datasetId the dataset id
   *
   * @return the field validations
   */
  private Map<String, List<FieldValidation>> getFieldValidations(final List<String> recordIds,
      Long datasetId) {

    StringBuilder query = new StringBuilder("select fval.ID as field_val_id," + "v.ID as val_id,"
        + "fv.ID as field_id," + "fval.ID_FIELD as field_validation_id_field,"
        + "fval.ID_VALIDATION as field_validation_id_validation," + "v.ID_RULE as rule_id,"
        + "v.LEVEL_ERROR as level_error, " + "v.MESSAGE as message,"
        + "v.ORIGIN_NAME as origin_name," + "v.TYPE_ENTITY as type_entity,"
        + "v.VALIDATION_DATE as validation_date," + "fv.ID_FIELD_SCHEMA as id_field_schema,"
        + "fv.ID_RECORD as id_record," + "fv.TYPE as field_value_type," + "fv.VALUE as value "
        + "from dataset_" + datasetId + ".FIELD_VALIDATION fval " + "inner join dataset_"
        + datasetId + ".VALIDATION v " + "on fval.ID_VALIDATION=v.ID " + "inner join dataset_"
        + datasetId + ".FIELD_VALUE fv " + "on fval.ID_FIELD=fv.ID " + "where fv.ID_RECORD "
        + "in (");

    for (int i = 0; i < recordIds.size(); i++) {
      query.append("'" + recordIds.get(i) + "'");
      if (recordIds.size() > i + 1) {
        query.append(",");
      }
    }
    query.append(")");
    List<FieldValidation> fieldValidations =
        datasetRepository.queryFieldValidationExecution(query.toString());

    Map<String, List<FieldValidation>> result = new HashMap<>();

    fieldValidations.stream().forEach(fieldValidation -> {
      if (!result.containsKey(fieldValidation.getFieldValue().getId())) {
        result.put(fieldValidation.getFieldValue().getId(), new ArrayList<>());
      }
      result.get(fieldValidation.getFieldValue().getId()).add(fieldValidation);
    });

    return result;
  }

  /**
   * Gets the record validations.
   *
   * @param recordIds the record ids
   * @param datasetId the dataset id
   *
   * @return the record validations
   */
  private Map<String, List<RecordValidation>> getRecordValidations(final List<String> recordIds,
      Long datasetId) {

    StringBuilder query = new StringBuilder("select rval.ID as record_val_id," + "v.ID as val_id,"
        + "rv.ID as record_id," + "rval.ID_RECORD as record_validation_id_field,"
        + "rval.ID_VALIDATION as record_validation_id_validation," + "v.ID_RULE as rule_id,"
        + "v.LEVEL_ERROR as level_error," + "v.MESSAGE as message,"
        + "v.ORIGIN_NAME as origin_name," + "v.TYPE_ENTITY as type_entity,"
        + "v.VALIDATION_DATE as validation_date," + "rv.DATA_PROVIDER_CODE as data_provider_code,"
        + "rv.DATASET_PARTITION_ID as dataset_partition, "
        + "rv.ID_RECORD_SCHEMA as record_schema, " + "rv.ID_TABLE as id_table " + "from dataset_"
        + datasetId + ".RECORD_VALIDATION rval " + "inner join dataset_" + datasetId
        + ".VALIDATION v " + "on rval.ID_VALIDATION=v.ID " + "inner join dataset_" + datasetId
        + ".RECORD_VALUE rv " + "on rval.ID_RECORD=rv.ID " + "where rv.ID in (");

    for (int i = 0; i < recordIds.size(); i++) {
      query.append("'" + recordIds.get(i) + "'");
      if (recordIds.size() > i + 1) {
        query.append(",");
      }
    }
    query.append(")");
    List<RecordValidation> recordValidations =
        datasetRepository.queryRecordValidationExecution(query.toString());

    Map<String, List<RecordValidation>> result = new HashMap<>();

    recordValidations.stream().forEach(recordValidation -> {
      if (!result.containsKey(recordValidation.getRecordValue().getId())) {
        result.put(recordValidation.getRecordValue().getId(), new ArrayList<>());
      }
      result.get(recordValidation.getRecordValue().getId()).add(recordValidation);
    });

    return result;
  }

}
