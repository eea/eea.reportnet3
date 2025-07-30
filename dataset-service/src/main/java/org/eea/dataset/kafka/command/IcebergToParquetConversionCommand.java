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
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class IcebergToParquetConversionCommand. Handles the conversion from Iceberg to Parquet.
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

  @Autowired
  private RedisLockService redisLockService;


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
    String lockValue = null;

    try {
      datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
      dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
      lockValue = (String) eeaEventVO.getData().get("lockValue");

      Long providerId = eeaEventVO.getData().get("providerId") != null
          ? Long.parseLong(String.valueOf(eeaEventVO.getData().get("providerId")))
          : null;
      List<String> tableSchemaIds = (List<String>) eeaEventVO.getData().get("tableSchemaIds");

      String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);

      for (String tableSchemaId : tableSchemaIds) {
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

        if (tableSchemaVO != null) {
          bigDataDatasetService.convertIcebergToParquetTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId, lockValue);
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

      LOG.info("Successfully completed Iceberg to Parquet conversion for datasetId: {} and user {}", datasetId, user);


    } catch (Exception e) {
      LOG.error("Error processing Kafka event for converting Iceberg to Parquet for datasetId: {} and user {} : {}", datasetId, user, e.getMessage());

      String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
      redisLockService.releaseLock(lockKey, lockValue);
      LOG.info("Released lock {} with value {}", lockKey, lockValue);

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
