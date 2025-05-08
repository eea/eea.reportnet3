package org.eea.dataset.kafka.command;

import java.util.List;
import org.eea.dataset.service.BigDataDatasetService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class IcebergToParquetConversionCommand extends AbstractEEAEventHandlerCommand {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergToParquetConversionCommand.class);

  @Lazy
  @Autowired
  private DatasetSchemaService datasetSchemaService;

  @Lazy
  @Autowired
  private BigDataDatasetService bigDataDatasetService;

  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  @Override
  public EventType getEventType() {
    return EventType.COMMAND_ICEBERG_TO_PARQUET_CONVERSION;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    String user = eeaEventVO.getData().get("user") != null
        ? String.valueOf(eeaEventVO.getData().get("user"))
        : SecurityContextHolder.getContext().getAuthentication().getName();

    Long datasetId = null;
    Long dataflowId = null;

    try {
      datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
      dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
      Long providerId = eeaEventVO.getData().get("providerId") != null
          ? Long.parseLong(String.valueOf(eeaEventVO.getData().get("providerId")))
          : null;
      List<String> tableSchemaIds = (List<String>) eeaEventVO.getData().get("tableSchemaIds");

      String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);

      for (String tableSchemaId : tableSchemaIds) {
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

        if (tableSchemaVO != null) {
          bigDataDatasetService.convertIcebergToParquetTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId);
        } else {
          LOG.error("TableSchemaVO not found for tableSchemaId: {}", tableSchemaId);
        }
      }

      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.ICEBERG_TO_PARQUET_CONVERSION_COMPLETED_EVENT,
          null,
          NotificationVO.builder()
              .user(user)
              .dataflowId(dataflowId)
              .datasetId(datasetId)
              .providerId(providerId)
              .build()
      );

      LOG.info("Successfully completed Iceberg to Parquet conversion for datasetId: {}", datasetId);


    } catch (Exception e) {
      LOG.error("Error processing Kafka event for converting Iceberg to Parquet: {}", e.getMessage());

      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.ICEBERG_TO_PARQUET_CONVERSION_FAILED_EVENT,
          null,
          NotificationVO.builder()
              .user(user)
              .dataflowId(dataflowId)
              .datasetId(datasetId)
              .build()
      );

      throw new EEAException(e.getMessage());
    }

  }

}
