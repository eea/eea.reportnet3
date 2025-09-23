package org.eea.dataset.kafka.command;

import java.util.*;

import org.apache.commons.lang3.BooleanUtils;
import org.eea.datalake.service.impl.DremioHelperServiceImpl;
import org.eea.datalake.service.impl.S3HelperImpl;
import org.eea.datalake.service.impl.S3ServiceImpl;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.metabase.domain.DatasetTable;
import org.eea.dataset.service.BigDataDatasetService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetTableService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX;

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

  @Autowired
  private DatasetTableService datasetTableService;

  @Autowired
  private DremioHelperServiceImpl dremioHelperService;

  @Autowired
  private S3HelperImpl s3HelperPrivate;

  @Autowired
  private S3ServiceImpl s3ServicePrivate;

  @Lazy
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

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
    String datasetName = null;

    try {
      datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
      dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
      lockValue = (String) eeaEventVO.getData().get("lockValue");

      Long providerId = eeaEventVO.getData().get("providerId") != null
          ? Long.parseLong(String.valueOf(eeaEventVO.getData().get("providerId")))
          : null;
      List<String> tableSchemaIds = (List<String>) eeaEventVO.getData().get("tableSchemaIds");

      DataSetMetabaseVO dataSetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
      datasetName = dataSetMetabaseVO.getDataSetName();
      String datasetSchemaId = dataSetMetabaseVO.getDatasetSchema();

      List<TableSchemaVO> availableForConversionTables = new ArrayList<>();

      for (String tableSchemaId : tableSchemaIds) {
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);

        if (tableSchemaVO != null) {
          Boolean availableForConversion = bigDataDatasetService.convertIcebergToParquetTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId, lockValue);
          if(BooleanUtils.isTrue(availableForConversion)){
            availableForConversionTables.add(tableSchemaVO);
          }
        } else {
          LOG.error("TableSchemaVO not found for tableSchemaId: {}", tableSchemaId);
        }
      }

      //iceberg enabled should be updated to false at the end iceberg files should be deleted also at the end of the conversion to ensure that all available tables were converted.
      for (TableSchemaVO table : availableForConversionTables) {
        Long usedProviderId = (providerId != null) ? providerId : 0L;
        S3PathResolver s3IcebergTablePathResolver = new S3PathResolver(dataflowId, usedProviderId, datasetId, table.getNameTableSchema(), table.getNameTableSchema(), S3_TABLE_AS_FOLDER_QUERY_PATH);
        s3IcebergTablePathResolver.setIsIcebergTable(true);
        String icebergTablePath = s3ServicePrivate.getTableAsFolderQueryPath(s3IcebergTablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);

        //remove iceberg table
        LOG.info("Removing iceberg files for table in path {}", icebergTablePath);
        if (s3HelperPrivate.checkFolderExist(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX)){
          dremioHelperService.demoteFolderOrFile(s3IcebergTablePathResolver, table.getNameTableSchema());
          s3HelperPrivate.deleteFolder(s3IcebergTablePathResolver, S3_TABLE_NAME_FOLDER_PATH_FOR_VALID_PREFIX);
        }

        DatasetTable datasetTableEntry = new DatasetTable(datasetId, datasetSchemaId, table.getIdTableSchema(), false);
        datasetTableService.saveOrUpdateDatasetTableEntry(datasetTableEntry);
      }

      String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
      redisLockService.releaseLock(lockKey, lockValue);
      LOG.info("Released lock {} with value {}", lockKey, lockValue);

      kafkaSenderUtils.releaseNotificableKafkaEvent(
          EventType.ICEBERG_TO_PARQUET_CONVERSION_COMPLETED_EVENT,
              null,
          NotificationVO.builder()
              .user(user)
              .dataflowId(dataflowId)
              .datasetId(datasetId)
              .providerId(providerId)
              .datasetName(datasetName)
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
              .datasetName(datasetName)
              .build()
      );

      throw new EEAException(e.getMessage());
    }

  }

}
