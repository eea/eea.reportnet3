package org.eea.validation.util;

import javax.persistence.EntityManager;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * The Class SQLValitaionUtils.
 */
@Component
public class SQLValitaionUtils {



  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(KieBaseManager.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant AS: {@value}. */
  private static final String AS = "') AS ";


  private static final String DATASET_QUERY = "dataset_";

  private static final String UNDERSCORE = "_";

  private static final String DOT = ".";


  /** The entity manager. */
  // @PersistenceContext
  private static EntityManager entityManager;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  private static DatasetRepository datasetRepository;

  private static RulesRepository rulesRepository;

  @Autowired
  synchronized void setDatasetRepository(DatasetRepository datasetRepository) {
    SQLValitaionUtils.datasetRepository = datasetRepository;
  }

  @Autowired
  synchronized void setRulesRepository(RulesRepository rulesRepository) {
    SQLValitaionUtils.rulesRepository = rulesRepository;
  }

  /**
   * Validate SQL rule.
   *
   * @param datasetSchemaId the dataset schema id
   * @param rule the rule
   */
  @Async
  @SuppressWarnings("unchecked")
  public void validateSQLRule(String datasetSchemaId, Rule rule) {

    EventType notificationEventType = null;
    NotificationVO notificationVO = NotificationVO.builder()
        .user((String) ThreadPropertiesManager.getVariable("user")).datasetSchemaId(datasetSchemaId)
        .shortCode(rule.getShortCode()).error("The QC Rule is disabled").build();

    if (null != null) {
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


  private static void executeValidationSQLRule(Long datasetId, String ruleId) {
    String sqlExample =
        "select t25.campo_de_contaminacion from dataset_256.table_25 t25 inner join dataset_125.table_del_aire ta on t25.id as ta.id";

    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    RulesSchema rulechema =
        rulesRepository.getActiveAndVerifiedRules(new ObjectId(datasetSchemaId));



    datasetRepository.queryRSExecution(sqlExample);
  }

}
