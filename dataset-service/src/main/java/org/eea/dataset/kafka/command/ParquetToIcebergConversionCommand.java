package org.eea.dataset.kafka.command;

import java.util.List;

import org.eea.dataset.service.BigDataDatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
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
    final String user = eeaEventVO.getData().get("user") != null
        ? String.valueOf(eeaEventVO.getData().get("user"))
        : SecurityContextHolder.getContext().getAuthentication().getName();

    final Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
    final Object tmp = eeaEventVO.getData().get("preparationCode");
    final String preparationCode = tmp == null ? null : String.valueOf(tmp);
    final Long dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
    final String lockValue = (String) eeaEventVO.getData().get("lockValue");


    final Long providerId = eeaEventVO.getData().get("providerId") != null
            ? Long.parseLong(String.valueOf(eeaEventVO.getData().get("providerId")))
            : null;
    final List<String> tableSchemaIds = (List<String>) eeaEventVO.getData().get("tableSchemaIds");

    try {
      bigDataDatasetService.convertParquetToIcebergTables(datasetId, preparationCode, dataflowId, providerId, tableSchemaIds, user, lockValue);
    }
    catch (Exception e){
      LOG.error("Could not call async method convertParquetToIcebergTables for datasetId {} preparationCode {} Error: {}", datasetId, preparationCode, e.getMessage());
      throw new EEAException(e.getMessage());
    }
  }

}