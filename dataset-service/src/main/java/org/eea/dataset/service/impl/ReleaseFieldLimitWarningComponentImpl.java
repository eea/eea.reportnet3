package org.eea.dataset.service.impl;

import org.eea.datalake.service.model.SpatialFieldInfo;
import org.eea.dataset.service.ReleaseFieldLimitWarningComponent;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ReleaseFieldLimitWarningComponentImpl implements ReleaseFieldLimitWarningComponent {

  private final KafkaSenderUtils kafkaSenderUtils;

  public ReleaseFieldLimitWarningComponentImpl(KafkaSenderUtils kafkaSenderUtils) {
    this.kafkaSenderUtils = kafkaSenderUtils;
  }

  @Override
  public void releaseFieldSizeNotification(SpatialFieldInfo spatialFieldInfo, Long dataflowId, Long datasetId) throws EEAException {
    if (spatialFieldInfo != null && !spatialFieldInfo.getRecordLines().isEmpty()) {
      NotificationVO notificationVO = NotificationVO.builder()
          .dataflowId(dataflowId)
          .datasetId(datasetId)
          .build();
      String recordLines = spatialFieldInfo.getRecordLines().stream()
          .map(String::valueOf)
          .collect(Collectors.joining(","));

      notificationVO.setTableName(spatialFieldInfo.getTableName());
      notificationVO.setFieldName(spatialFieldInfo.getFieldName());
      notificationVO.setRecordLines(recordLines);

      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.IMPORT_FIELD_SIZE_EXCEEDS_LIMIT_WARNING_EVENT, null, notificationVO);
    }
  }
}
