package org.eea.validation.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
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

  private static final String SELECT = "SELECT";

  private static final String FROM = "FROM";

  /** The Constant KEYWORDS: {@value}. */
  private static final String KEYWORDS = "INNER,JOIN,COALESCE,DELETE,INSERT";

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

  /**
   * Validate SQL rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @SuppressWarnings("unchecked")
  @Override
  public void validateSQLRule(Long datasetId, String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user((String) ThreadPropertiesManager.getVariable("user")).datasetSchemaId(datasetSchemaId)
        .shortCode(rule.getShortCode()).error("The QC Rule is disabled").build();


    if (validateRule(rule.getSqlSentence(), datasetId, "") == Boolean.TRUE) {
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
   * @param idTableSchema the id table schema
   * @return the boolean
   */
  private Boolean validateRule(String query, Long datasetId, String idTableSchema) {
    Boolean isSQLCorrect = Boolean.TRUE;
    // validate query sintax
    if (checkQuerySintax(query)) {
      String preparedquery = queryTreat(query, datasetId, idTableSchema) + " limit 5";
      try {
        retrivedata(preparedquery);
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
   * Query treat.
   *
   * @param query the query
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @return the string
   */
  @Override
  @Transactional
  public String queryTreat(String query, Long datasetId, String idTableSchema) {
    String queryUpperCase = query.toUpperCase();
    List<String> userQueryColumnList = getColumsFromRuleQuery(queryUpperCase);
    List<String> tableColumnList = getColumnsNameFromSchema(datasetId, idTableSchema);
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
    return preparedStatement.toString();
  }


  /**
   * Gets the last part from query.
   * 
   * @param datasetId
   *
   * @param query the query
   * @return the last part from query
   */
  private String getLastPartFromQuery(Long datasetId, String query) {
    int from = query.indexOf(FROM);
    List<String> tables = getTablesFromRuleQuery(query.substring(from));

    for (int index = 0; index < tables.size(); index++) {
      if (!tables.get(index).trim().equalsIgnoreCase("INNER")
          && !tables.get(index).trim().equalsIgnoreCase("JOIN")
          && !tables.get(index).trim().equalsIgnoreCase(" ")
          && !tables.get(index).trim().equalsIgnoreCase("CROSS")
          && !tables.get(index).trim().equalsIgnoreCase("LEFT")
          && !tables.get(index).trim().equalsIgnoreCase("RIGHT")) {

      }
    }
    return query;
  }


  private List<String> getTablesFromRuleQuery(String query) {
    List<String> tableList = new ArrayList<>();
    String preparedQuery = query.replaceAll(",", ", ");
    String[] tables = preparedQuery.substring(preparedQuery.indexOf(FROM)).split("(?=\\s)");
    for (String table : tables) {
      if (!table.trim().equalsIgnoreCase(FROM)) {
        tableList.add(table);
      }
      if (!table.trim().equalsIgnoreCase("WHERE") && !table.trim().equalsIgnoreCase("LIMIT")
          && !table.trim().equalsIgnoreCase("OFFSET")) {
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
   * @param idTableSchema the id table schema
   * @return the columns name from schema
   */
  private List<String> getColumnsNameFromSchema(Long datasetId, String idTableSchema) {
    DataSetSchemaVO schema = datasetSchemaController.findDataSchemaByDatasetId(datasetId);
    List<String> fieldNameList = new ArrayList<>();
    schema.getTableSchemas().stream()
        .filter(table -> table.getIdTableSchema().equals(idTableSchema)).forEach(table -> {
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
   * @throws SQLException
   */

  @Override
  public TableValue retrivedata(String query) throws SQLException {
    return datasetRepository.queryRSExecution(query);
  }


}
