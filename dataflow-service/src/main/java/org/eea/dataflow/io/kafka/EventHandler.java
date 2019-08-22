package org.eea.dataflow.io.kafka;

import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAException;
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

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private DataflowDocumentService dataflowService;

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

    if (EventType.LOAD_DOCUMENT_COMPLETED_EVENT.equals(eeaEventVO.getEventType())) {
      Long dataflowId = (Long) eeaEventVO.getData().get("dataflow_id");
      String filename = (String) eeaEventVO.getData().get("filename");
      String language = (String) eeaEventVO.getData().get("language");
      String description = (String) eeaEventVO.getData().get("description");
      try {
        dataflowService.insertDocument(dataflowId, filename, language, description);
      } catch (EEAException e) {
        LOG_ERROR.error("Error inserting document for dataflow {} due to exception {}", dataflowId,
            e);
      }
    }

    if (EventType.DELETE_DOCUMENT_COMPLETED_EVENT.equals(eeaEventVO.getEventType())) {
      Long documentId = (Long) eeaEventVO.getData().get("documentId");
      try {
        dataflowService.deleteDocument(documentId);
      } catch (EEAException e) {
        LOG_ERROR.error("Error deleting document {} due to exception {}", documentId, e);
      }
    }

  }
}
