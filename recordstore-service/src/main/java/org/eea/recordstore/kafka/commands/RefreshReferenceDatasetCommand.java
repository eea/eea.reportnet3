package org.eea.recordstore.kafka.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.recordstore.service.RecordStoreService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The Class RefreshReferenceDatasetCommand.
 */
@Component
public class RefreshReferenceDatasetCommand extends AbstractEEAEventHandlerCommand {

  /** The database management service. */
  @Autowired
  private RecordStoreService recordStoreService;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.REFRESH_MATERIALIZED_VIEW_EVENT;
  }

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Execute.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId =
        Long.parseLong(String.valueOf(eeaEventVO.getData().get(LiteralConstants.DATASET_ID)));
    String user = String.valueOf(eeaEventVO.getData().get(LiteralConstants.USER));
    Boolean released = Boolean.parseBoolean(String.valueOf(eeaEventVO.getData().get("released")));
    List<Integer> referenceDatasets =
        (List<Integer>) eeaEventVO.getData().get("referencesToRefresh");

    List<Long> referencesToRefresh = referenceDatasets.stream().mapToLong(Integer::longValue)
        .boxed().collect(Collectors.toList());

    if (referencesToRefresh != null && !CollectionUtils.isEmpty(referencesToRefresh)) {
      recordStoreService.refreshMaterializedQuery(referencesToRefresh, true, released, datasetId);
    } else {
      Map<String, Object> values = new HashMap<>();
      values.put(LiteralConstants.DATASET_ID, datasetId);
      values.put("released", released);
      kafkaSenderUtils.releaseKafkaEvent(EventType.UPDATE_MATERIALIZED_VIEW_EVENT, values);
    }
  }

}
