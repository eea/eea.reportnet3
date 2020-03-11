package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.validation.util.ValidationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class CleanKyebaseCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(CleanKyebaseCommand.class);
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
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_CLEAN_KYEBASE;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(final EEAEventVO eeaEventVO) {
    final String uuid = (String) eeaEventVO.getData().get("uuid");
    LOG.info("Removing kieBase for process {}", uuid);
    validationHelper.removeKieBase(uuid);
  }


}
