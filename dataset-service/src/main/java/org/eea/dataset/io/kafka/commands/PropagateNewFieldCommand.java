package org.eea.dataset.io.kafka.commands;

import java.util.UUID;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PropagateNewFieldCommand.
 */
@Component
public class PropagateNewFieldCommand extends AbstractEEAEventHandlerCommand {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The update record helper. */
  @Autowired
  private UpdateRecordHelper updateRecordHelper;


  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_NEW_DESIGN_FIELD_PROPAGATION;
  }


  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) {
    Long datasetId = (Long) eeaEventVO.getData().get("dataset_id");
    Integer sizeRecords = (Integer) eeaEventVO.getData().get("sizeRecords");
    String idTableSchema = (String) eeaEventVO.getData().get("idTableSchema");
    Integer numPag = (Integer) eeaEventVO.getData().get("numPag");
    String idFieldSchema = (String) eeaEventVO.getData().get("idFieldSchema");
    DataType typeField = (DataType) eeaEventVO.getData().get("typeField");

    updateRecordHelper.propagateNewFieldDesign(datasetId, idTableSchema, sizeRecords, numPag,
        UUID.randomUUID().toString(), idFieldSchema, typeField);

  }


}
