package org.eea.validation.kafka.command;

import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
import org.kie.api.KieBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ExecuteTableValidationCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The validation helper.
   */
  @Autowired
  private ValidationHelper validationHelper;
  /**
   * The validation service.
   */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATE_TABLE;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(final EEAEventVO eeaEventVO) throws EEAException {
    final Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
    TenantResolver.setTenantName("dataset_" + datasetId);
    final Long idTable = (Long) eeaEventVO.getData().get("idTable");
    try {
      KieBase kieBase = validationService.loadRulesKnowledgeBase(datasetId);
      validationService.validateTable(datasetId, idTable, kieBase);
    } catch (EEAException e) {
      LOG_ERROR.error("Error processing validations for dataset {} due to exception {}", datasetId,
          e);
      eeaEventVO.getData().put("error", e);
    } finally {
      final String uuid = (String) eeaEventVO.getData().get("uuid");
      //if this is the coordinator validation  instance, then no need to send message, just updates expected validations and verify if process is finished
      ConcurrentHashMap<String, Integer> processMap = validationHelper.getProcessesMap();
      synchronized (processMap) {
        if (processMap.containsKey(uuid)) {
          processMap.merge(uuid, -1, Integer::sum);
          validationHelper.checkFinishedValidations(datasetId, uuid);
        } else {//send the message to coordinator validation instance
          kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATED_TABLE_COMPLETED,
              eeaEventVO.getData());
        }

      }
    }
  }

}
