package org.eea.dataset.kafka.command;

import java.util.*;


import org.eea.dataset.service.BigDataDatasetService;
import org.eea.exception.EEAException;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * The Class IcebergToParquetConversionCommand. Handles the conversion from Iceberg to Parquet.
 */
@Component
public class IcebergToParquetConversionCommand extends AbstractEEAEventHandlerCommand {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergToParquetConversionCommand.class);

  @Autowired
  private BigDataDatasetService bigDataDatasetService;


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


    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
    Long dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));
    String lockValue = (String) eeaEventVO.getData().get("lockValue");

    Long providerId = eeaEventVO.getData().get("providerId") != null
            ? Long.parseLong(String.valueOf(eeaEventVO.getData().get("providerId")))
            : null;
    List<String> tableSchemaIds = (List<String>) eeaEventVO.getData().get("tableSchemaIds");

    try {
      bigDataDatasetService.convertIcebergToParquetTables(datasetId, dataflowId, providerId, tableSchemaIds, user, lockValue);
    }
    catch (Exception e){
      LOG.error("Could not call async method convertIcebergToParquetTables for datasetId {} Error: {}", datasetId, e.getMessage());
      throw new EEAException(e.getMessage());
    }
  }

}
