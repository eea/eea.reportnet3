package org.eea.dataflow.io.kafka.command;

import java.time.Instant;
import java.util.Date;
import org.eea.dataflow.service.DataflowDocumentService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.document.DocumentVO;
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
public class LoadDocumentCommand extends AbstractEEAEventHandlerCommand {

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
    return EventType.LOAD_DOCUMENT_COMPLETED_EVENT;
  }

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    DocumentVO documentVO = new DocumentVO();
    documentVO.setDataflowId((Long) eeaEventVO.getData().get("dataflow_id"));
    eeaEventVO.getData().get("filename");
    documentVO.setLanguage((String) eeaEventVO.getData().get("language"));
    documentVO.setDescription((String) eeaEventVO.getData().get("description"));
    documentVO.setSize((String) eeaEventVO.getData().get("size"));
    documentVO.setDate(Date.from((Instant) eeaEventVO.getData().get("date")));

    try {
      dataflowService.insertDocument(documentVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error inserting document for dataflow {} due to exception {}",
          documentVO.getDataflowId(), e);
    }
  }
}
