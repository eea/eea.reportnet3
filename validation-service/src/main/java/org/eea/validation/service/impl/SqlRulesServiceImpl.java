package org.eea.validation.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
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

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Validate SQL rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @SuppressWarnings("unchecked")
  @Override
  public void validateSQLRule(String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user((String) ThreadPropertiesManager.getVariable("user")).datasetSchemaId(datasetSchemaId)
        .shortCode(rule.getShortCode()).error("The QC Rule is disabled").build();

    // if (null != null) {
    notificationEventType = EventType.VALIDATED_QC_RULE_EVENT;
    rule.setVerified(true);
    LOG.info("Rule validation passed: {}", rule);
    // } else {
    // notificationEventType = EventType.INVALIDATED_QC_RULE_EVENT;
    // rule.setVerified(false);
    // rule.setEnabled(false);
    // LOG.info("Rule validation not passed: {}", rule);
    // }

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
   * Query treat.
   *
   * @param query the query
   * @return the string
   */
  @Override
  public String queryTreat(String query) {

    List<String> columnList = getColumsFromRuleQuery(query);

    StringBuilder preparedStatement = new StringBuilder("SELECT ");
    if (columnList.isEmpty()) {
      preparedStatement.append(" * ");
    } else {
      preparedStatement.append(FIRST_QUERY_PART);
      preparedStatement.append(COMMA);
      Iterator<String> iterator = columnList.iterator();
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
    return preparedStatement.toString();
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
      String[] palabras = preparedQuery.split(" ");
      for (String palabra : palabras) {
        if (!palabra.toUpperCase().contains("SELECT")) {
          columnList.add(palabra);
        }
        if (!palabra.toUpperCase().contains("FROM")) {
          break;
        }
      }
      return columnList;
    }
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
   */
  @Transactional
  @Override
  public TableValue retrivedata(String query) {
    return datasetRepository.queryRSExecution(query);
  }


}
