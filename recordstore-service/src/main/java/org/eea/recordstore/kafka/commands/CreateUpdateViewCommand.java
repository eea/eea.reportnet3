package org.eea.recordstore.kafka.commands;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateUpdateViewCommand extends AbstractEEAEventHandlerCommand {

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The database management service. */
  @Autowired
  private RecordStoreService recordStoreService;

  @Override
  public EventType getEventType() {
    return EventType.CREATE_UPDATE_VIEW_EVENT;
  }

  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {

    Long datasetId =
        Long.parseLong(String.valueOf(eeaEventVO.getData().get(LiteralConstants.DATASET_ID)));
    String user = String.valueOf(eeaEventVO.getData().get(LiteralConstants.USER));
    Boolean isMaterialized =
        Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("isMaterialized")));
    Boolean checkSQL = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("checkSQL")));

    recordStoreService.createUpdateQueryView(datasetId, isMaterialized);

    if (checkSQL) {
      releaseValidateManualQCEvent(datasetId, user, true);
    }
  }

  /**
   * Release validate manual QC event.
   *
   * @param datasetId the dataset id
   * @param checkNoSQL the check no SQL
   */
  private void releaseValidateManualQCEvent(Long datasetId, String user, boolean checkNoSQL) {
    Map<String, Object> result = new HashMap<>();
    result.put(LiteralConstants.DATASET_ID, datasetId);
    result.put("checkNoSQL", checkNoSQL);
    result.put(LiteralConstants.USER, user);
    kafkaSenderUtils.releaseKafkaEvent(EventType.VALIDATE_MANUAL_QC_COMMAND, result);
  }

}
