package org.eea.dataset.kafka.command;

import java.util.List;
import org.eea.dataset.service.BigDataDatasetService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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
 */
@Component
public class ParquetToIcebergConversionCommand extends AbstractEEAEventHandlerCommand {

  private static final Logger LOG = LoggerFactory.getLogger(ParquetToIcebergConversionCommand.class);

  @Autowired
  private DatasetSchemaService datasetSchemaService;

  @Autowired
  private BigDataDatasetService bigDataDatasetService;

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
    try {
      Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
      Long dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
      Long providerId = eeaEventVO.getData().get("providerId") != null
          ? Long.parseLong(String.valueOf(eeaEventVO.getData().get("providerId")))
          : null;
      List<String> tableSchemaIds = (List<String>) eeaEventVO.getData().get("tableSchemaIds");

      String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);

      for (String tableSchemaId : tableSchemaIds) {
        TableSchemaVO tableSchemaVO = datasetSchemaService.getTableSchemaVO(tableSchemaId, datasetSchemaId);
        if (tableSchemaVO != null) {
          bigDataDatasetService.convertParquetToIcebergTable(datasetId, dataflowId, providerId, tableSchemaVO, datasetSchemaId);
        } else {
          LOG.error("TableSchemaVO not found for tableSchemaId: {}", tableSchemaId);
        }
      }
    } catch (Exception e) {
      LOG.error("Error processing Parquet to Iceberg conversion Kafka event: {}", e.getMessage());
      throw new EEAException(e.getMessage());
    }
  }
}
