package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.util.ValidationHelper;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 *
 */
@Component
public class TableValidatedCommand extends AbstractEEAEventHandlerCommand {

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_VALIDATED_TABLE_COMPLETED;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException
   */
  @Override
  public void execute(final EEAEventVO eeaEventVO) throws EEAException {
    final String uuid = (String) eeaEventVO.getData().get("uuid");
    final Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
    final KieBase kieBase = (KieBase) eeaEventVO.getData().get("kieBase");
    if (validationHelper.getProcessesMap().containsKey(uuid)) {
      validationHelper.getProcessesMap().merge(uuid, -1, Integer::sum);
      validationHelper.checkFinishedValidations(datasetId, uuid);
    } else {
      kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_VALIDATED_TABLE_COMPLETED,
          eeaEventVO.getData());
    }
  }

}
