package org.eea.dataflow.io.kafka.command;

import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 *
 */
@Component
public class DeleteDocumentCommand extends AbstractEEAEventHandlerCommand {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The dataflow service. */
  @Autowired
  private DataflowDocumentService dataflowService;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.DELETE_DOCUMENT_COMPLETED_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    Long documentId = (Long) eeaEventVO.getData().get("documentId");
    try {
      dataflowService.deleteDocument(documentId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting document {} due to exception {}", documentId, e);
    }
  }
}
