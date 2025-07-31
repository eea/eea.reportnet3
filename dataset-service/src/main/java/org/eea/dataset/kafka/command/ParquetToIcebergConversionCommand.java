package org.eea.dataset.kafka.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.service.BigDataDatasetService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetTableService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
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
 * The Class ParquetToIcebergConversionCommand. Handles the conversion from Parquet to Iceberg.
 */
@Component
public class ParquetToIcebergConversionCommand extends AbstractEEAEventHandlerCommand {

  private static final Logger LOG = LoggerFactory.getLogger(ParquetToIcebergConversionCommand.class);

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

  @Autowired
  private DatasetTableService datasetTableService;

  @Override
  public EventType getEventType() {
    return EventType.COMMAND_PARQUET_TO_ICEBERG_CONVERSION;
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

      List<TableSchemaVO> availableForConversionTables = new ArrayList<>();

      for (String tableSchemaId : tableSchemaIds) {
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

        if (tableSchemaVO != null) {
          Boolean availableForConversion = bigDataDatasetService.convertParquetToIcebergTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId, lockValue);
          if(BooleanUtils.isTrue(availableForConversion)){
            availableForConversionTables.add(tableSchemaVO);
          }
        } else {
          LOG.error("TableSchemaVO not found for tableSchemaId: {}", tableSchemaId);
        }
      }

      //iceberg enabled should be updated at the end of the conversion to ensure that all available tables were converted.
      for (TableSchemaVO table : availableForConversionTables) {
        DatasetTable datasetTableEntry = new DatasetTable(datasetId, datasetSchemaId, table.getIdTableSchema(), true);
        datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
      }

      String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
      redisLockService.releaseLock(lockKey, lockValue);
      LOG.info("Released lock {} with value {}", lockKey, lockValue);

      // Notify completion event
      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.PARQUET_TO_ICEBERG_CONVERSION_COMPLETED_EVENT,
          null,
          NotificationVO.builder()
              .user(user)
              .dataflowId(dataflowId)
              .datasetId(datasetId)
              .providerId(providerId)
              .build()
      );

      LOG.info("Successfully completed Parquet to Iceberg conversion for datasetId: {} and user {}", datasetId, user);

    } catch (Exception e) {
      LOG.error("Error processing Kafka event for converting Parquet to Iceberg for datasetId: {} and user {} : {}", datasetId, user, e.getMessage());

      String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
      redisLockService.releaseLock(lockKey, lockValue);
      LOG.info("Released lock {} with value {}", lockKey, lockValue);

      // Notify failure event
      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.PARQUET_TO_ICEBERG_CONVERSION_FAILED_EVENT,
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