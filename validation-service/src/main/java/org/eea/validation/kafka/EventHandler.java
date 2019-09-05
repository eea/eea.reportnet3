package org.eea.validation.kafka;

import javax.sql.DataSource;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.handler.EEAEventHandler;
import org.eea.multitenancy.MultiTenantDataSource;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.ValidationHelper;
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

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The validation service. */
  @Autowired
  @Qualifier("proxyValidationService")
  private ValidationService validationService;

  /** The validation helper. */
  @Autowired
  private ValidationHelper validationHelper;

  /** The data source. */
  @Autowired
  private DataSource dataSource;

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

    if (EventType.LOAD_DATA_COMPLETED_EVENT.equals(eeaEventVO.getEventType())
        || EventType.DELETED_TABLE.equals(eeaEventVO.getEventType())
        || EventType.SNAPSHOT_RESTORED_EVENT.equals(eeaEventVO.getEventType())) {
      Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
      try {
        validationHelper.executeValidation(datasetId);
      } catch (EEAException e) {
        LOG_ERROR.error("Error processing validations for dataset {} due to exception {}",
            datasetId, e);
      }
    }
    if (EventType.CONNECTION_CREATED_EVENT.equals(eeaEventVO.getEventType())) {
      ((MultiTenantDataSource) dataSource)
          .addDataSource((ConnectionDataVO) eeaEventVO.getData().get("connectionDataVO"));
    }
  }
}
