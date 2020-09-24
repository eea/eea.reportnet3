package org.eea.validation.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
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
import org.eea.validation.util.KieBaseManager;
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

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(KieBaseManager.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant FIRST_QUERY_PART: {@value}. */
  private static final String FIRST_QUERY_PART =
      "ID, ID_RECORD_SCHEMA, ID_TABLE, DATASET_PARTITION_ID, DATA_PROVIDER_CODE";

  /** The Constant COMMA: {@value}. */
  private static final String COMMA = ", ";

  /** The Constant SELECT: {@value}. */
  private static final String SELECT = "SELECT";

  /** The Constant FROM: {@value}. */
  private static final String FROM = "FROM";

  /** The Constant WHERE: {@value}. */
  private static final String WHERE = "WHERE";

  /** The Constant KEYWORDS: {@value}. */
  private static final String KEYWORDS = "INNER,JOIN,COALESCE,DELETE,INSERT,DROP";

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaController datasetSchemaController;

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
  public void validateSQLRule(Long datasetId, String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user((String) ThreadPropertiesManager.getVariable("user")).datasetSchemaId(datasetSchemaId)
        .shortCode(rule.getShortCode()).error("The QC Rule is disabled").build();

    if (validateRule(rule.getSqlSentence(), datasetId).equals(Boolean.TRUE)) {
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
   * @param query the query
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Override
  public void validateSQLRuleFromDatacollection(String query, Long datasetId,
      String datasetSchemaId, RuleVO ruleVO) {
    if (validateRule(query, datasetId).equals(Boolean.FALSE)) {
      Rule rule = ruleMapper.classToEntity(ruleVO);
      rule.setVerified(false);
      rule.setEnabled(false);
      LOG.info("Rule validation not passed before pass to datacollection: {}", rule);
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
   * @return the boolean
   */
  private Boolean validateRule(String query, Long datasetId) {
    Boolean isSQLCorrect = Boolean.TRUE;
    // validate query sintax
    if (checkQuerySintax(query)) {
      String preparedquery = queryTreat(query, datasetId) + " limit 5";
      try {
        retrivedata(preparedquery, datasetId);
      } catch (SQLException e) {
        LOG_ERROR.error("SQL is not correct: {}, {}", e.getMessage(), e);
        isSQLCorrect = Boolean.FALSE;
      }
    } else {
      isSQLCorrect = Boolean.FALSE;
    }
    return isSQLCorrect;
  }

  /**
   * Check query sintax.
   *
   * @param query the query
   * @return the boolean
   */
  private Boolean checkQuerySintax(String query) {
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
   * Gets the table name.
   *
   * @param query the query
   * @return the table name
   */
  private String getTableName(String query) {
    int from = query.indexOf(FROM);
    List<String> tables = getTablesFromRuleQuery(query.substring(from));
    for (int index = 0; index < tables.size(); index++) {
      if (!tables.get(index).trim().equalsIgnoreCase("INNER")
          && !tables.get(index).trim().equalsIgnoreCase("JOIN")
          && !tables.get(index).trim().equalsIgnoreCase(" ")
          && !tables.get(index).trim().equalsIgnoreCase("CROSS")
          && !tables.get(index).trim().equalsIgnoreCase("LEFT")
          && !tables.get(index).trim().equalsIgnoreCase("RIGHT")
          && !tables.get(index).trim().equalsIgnoreCase(FROM)) {
        return tables.get(index);
      }
    }
    return "";
  }

  /**
   * Query treat.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @return the string
   */
  @Override
  @Transactional
  public String queryTreat(String query, Long datasetId) {
    String queryUpperCase = query.toUpperCase();
    List<String> userQueryColumnList = getColumsFromRuleQuery(queryUpperCase);
    String tableName = getTableName(queryUpperCase);
    List<String> tableColumnList = getColumnsNameFromSchema(datasetId, tableName);
    List<String> queryColumnList = extractQueryColumns(userQueryColumnList, tableColumnList);
    String queryLastPart = getLastPartFromQuery(datasetId, queryUpperCase);

    StringBuilder preparedStatement = new StringBuilder(SELECT + " ");
    if (queryColumnList.isEmpty()) {
      preparedStatement.append(" * ");
    } else {
      preparedStatement.append(FIRST_QUERY_PART);
      preparedStatement.append(COMMA);
      Iterator<String> iterator = queryColumnList.iterator();
      while (iterator.hasNext()) {
        String column = iterator.next();
        StringBuilder mustcolumns = new StringBuilder();
        // name
        mustcolumns.append(column);
        mustcolumns.append(COMMA);
        // name_id
        mustcolumns.append(column + "_ID");
        mustcolumns.append(COMMA);
        // name_id_field_schema
        mustcolumns.append(column + "_ID_FIELD_SCHEMA");
        mustcolumns.append(COMMA);
        // name_type
        mustcolumns.append(column + "_TYPE");
        if (iterator.hasNext()) {
          mustcolumns.append(COMMA);
        }
      }
    }
    preparedStatement.append(queryLastPart);
    try {
      retrivedata(preparedStatement.toString(), datasetId);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return preparedStatement.toString();
  }


  /**
   * Gets the last part from query.
   *
   * @param datasetId the dataset id
   * @param query the query
   * @return the last part from query
   */
  private String getLastPartFromQuery(Long datasetId, String query) {
    int from = query.indexOf(FROM);
    List<String> tables = getTablesFromRuleQuery(query.substring(from));
    StringBuilder finalQueryPart = new StringBuilder(FROM);
    for (int index = 0; index < tables.size(); index++) {
      if (!tables.get(index).trim().equalsIgnoreCase("INNER")
          && !tables.get(index).trim().equalsIgnoreCase("JOIN")
          && !tables.get(index).trim().equalsIgnoreCase(" ")
          && !tables.get(index).trim().equalsIgnoreCase("CROSS")
          && !tables.get(index).trim().equalsIgnoreCase("LEFT")
          && !tables.get(index).trim().equalsIgnoreCase("RIGHT")
          && !tables.get(index).trim().equalsIgnoreCase(FROM)) {
        finalQueryPart.append(" dataset_" + datasetId + "." + tables.get(index).trim());
      }
      if (query.contains(WHERE)) {
        if (!tables.get(index).trim().equalsIgnoreCase(WHERE)) {
          finalQueryPart.append(" ");
          finalQueryPart.append(query.substring(query.indexOf(WHERE)));
          break;
        }
      }
    }
    return finalQueryPart.toString();
  }


  /**
   * Gets the tables from rule query.
   *
   * @param query the query
   * @return the tables from rule query
   */
  private List<String> getTablesFromRuleQuery(String query) {
    List<String> tableList = new ArrayList<>();
    String preparedQuery = query.replaceAll(",", ", ");
    String[] tables = preparedQuery.substring(preparedQuery.indexOf(FROM)).split("(?=\\s)");
    for (String table : tables) {
      if (!table.trim().equalsIgnoreCase(FROM)) {
        tableList.add(table);
      }
      if (table.trim().equals(WHERE) && table.trim().equals("LIMIT")
          && table.trim().equals("OFFSET")) {
        break;
      }
    }
    return tableList;
  }

  /**
   * Extract query columns.
   *
   * @param userQueryColumnList the user query column list
   * @param tableColumnList the table column list
   * @return the list
   */
  private List<String> extractQueryColumns(List<String> userQueryColumnList,
      List<String> tableColumnList) {
    List<String> queryColumns = new ArrayList<>();
    userQueryColumnList.stream().forEach(column -> {
      if (tableColumnList.contains(column)) {
        queryColumns.add(column);
      }
    });
    return queryColumns;
  }

  /**
   * Gets the colums from rule query.
   *
   * @param query the query
   * @return the colums from rule query
   */
  private List<String> getColumsFromRuleQuery(String query) {
    if (query.contains("*")) {
      return new ArrayList<>();
    } else {
      List<String> columnList = new ArrayList<>();
      String preparedQuery = query.replaceAll(",", ", ");
      String[] fields = preparedQuery.split("(?=\\s)");
      for (String field : fields) {
        if (!field.trim().equalsIgnoreCase(SELECT) && !field.trim().equalsIgnoreCase(FROM)) {
          columnList.add(field);
        }
        if (field.trim().equalsIgnoreCase(FROM)) {
          break;
        }
      }
      return columnList;
    }
  }

  /**
   * Gets the columns name from schema.
   *
   * @param datasetId the dataset id
   * @param tableName the table name
   * @return the columns name from schema
   */
  private List<String> getColumnsNameFromSchema(Long datasetId, String tableName) {
    DataSetSchemaVO schema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);
    List<String> fieldNameList = new ArrayList<>();
    schema.getTableSchemas().stream().filter(table -> table.getNameTableSchema().equals(tableName))
        .forEach(table -> {
          table.getRecordSchema().getFieldSchema().stream().forEach(field -> {
            fieldNameList.add(field.getName());
          });
        });
    return fieldNameList;
  }


  /**
   * Gets the rule.
   *
   * @param datasetId the dataset id
   * @param ruleId the rule id
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
   * Retrivedata.
   *
   * @param query the query
   * @return the table value
   * @throws SQLException the SQL exception
   */

  @Override
  public TableValue retrivedata(String query, Long datasetId) throws SQLException {
    TableValue table = datasetRepository.queryRSExecution(query);
    if (null != table.getRecords() && table.getRecords().isEmpty()) {
      retrieveValidations(table.getRecords(), datasetId);
    }
    return table;
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
