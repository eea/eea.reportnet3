package org.eea.dataset.io.kafka;

import javax.sql.DataSource;
import org.eea.dataset.multitenancy.MultiTenantDataSource;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.handler.EEAEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The type Event handler.
 */
@Service
public class EventHandler implements EEAEventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

  @Autowired
  private DataSource dataSource;

  @Override
  public Class<EEAEventVO> getType() {
    return EEAEventVO.class;
  }


  @Override
  public void processMessage(final EEAEventVO eeaEventVO) {
    LOG.info("Data set has received this message from Kafka {}", eeaEventVO);

    if (EventType.CONNECTION_CREATED_EVENT.equals(eeaEventVO.getEventType())) {
      ((MultiTenantDataSource) dataSource)
          .addDataSource((ConnectionDataVO) eeaEventVO.getData().get("connectionDataVO"));
    }
  }
}
