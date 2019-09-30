package org.eea.dataset.io.kafka;

import org.eea.dataset.service.DatasetService;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * The type Event handler.
 */
@Service
public class EventHandler implements EEAEventHandler {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * The dataset service.
   */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;


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
    LOG.info("Data set has received this message from Kafka {}", eeaEventVO);

  }
}
