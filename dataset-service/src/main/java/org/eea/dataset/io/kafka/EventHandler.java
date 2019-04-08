package org.eea.dataset.io.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventHandler //implements EEAEventHandler
{

  private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

//  @Override
//  public Class<EEAEventVO> getType() {
//    return null;
//  }
//
//  @Override
//  public void processMessage(EEAEventVO eeaEventVO) {
//    LOG.info("Data set has received this message from Kafka {}", eeaEventVO);
//  }
}
