package org.eea.dataset.service.impl;

import org.eea.dataset.service.ReleaseFieldLimitWarningComponent;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReleaseFieldLimitWarningComponentImpl implements ReleaseFieldLimitWarningComponent {

  private final KafkaSenderUtils kafkaSenderUtils;

  public ReleaseFieldLimitWarningComponentImpl(KafkaSenderUtils kafkaSenderUtils) {
    this.kafkaSenderUtils = kafkaSenderUtils;
  }

  @Override
  public void releaseFieldSizeNotification(List<Long> recordLines, Long dataflowId, Long datasetId) throws EEAException {
    if (!recordLines.isEmpty()) {
      NotificationVO notificationVO = NotificationVO.builder()
          .dataflowId(dataflowId)
          .datasetId(datasetId)
          .build();
      String result = recordLines.stream()
          .map(String::valueOf)
          .collect(Collectors.joining(","));
      notificationVO.setRecordLines(result);

      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_FIELD_SIZE_EXCEEDS_LIMIT_WARNING_EVENT, null, notificationVO);
    }
  }
}
