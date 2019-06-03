package org.eea.validation.kafka;

import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.handler.EEAEventHandler;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Event handler.
 */
@Service
public class EventHandler implements EEAEventHandler {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

  /** The validation service. */
  @Autowired
  private ValidationService validationService;

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }


  /**
   * Process message.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void processMessage(final EEAEventVO eeaEventVO) {
    LOG.info("ValidationService has received this message from Kafka {}", eeaEventVO);

    if (EventType.LOAD_DATA_COMPLETED_EVENT.equals(eeaEventVO.getEventType())) {
      validationService.validateDataSetData((Long) eeaEventVO.getData().get("dataSetId"));
    }
  }
}
