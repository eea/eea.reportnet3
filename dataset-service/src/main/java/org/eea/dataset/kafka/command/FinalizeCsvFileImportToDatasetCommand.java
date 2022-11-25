package org.eea.dataset.kafka.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class FinalizeCsvFileImportToDatasetCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The validation helper.
   */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private FileCommonUtils fileCommon;

  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

  /** The batch record save. */
  @Value("${dataset.import.batchRecordSave}")
  private int batchRecordSave;

    @Value("${loadDataDelimiter}")
  private char loadDataDelimiter;

  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_FINALIZE_CSV_FILE_IMPORT_TO_DATASET;
  }

  /**
   * Perform action.
   *
   * @param eeaEventVO the eea event VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void execute(EEAEventVO eeaEventVO) throws EEAException {
    Long datasetId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("datasetId")));
    ThreadPropertiesManager.setVariable("user", eeaEventVO.getData().get("user"));
    Object replace = eeaEventVO.getData().get("replace");
    String processId = String.valueOf(eeaEventVO.getData().get("processId"));
    boolean replacebool = !(replace instanceof Boolean) || (boolean) replace;

    String filePath = String.valueOf(eeaEventVO.getData().get("filePath"));

    String fileName = String.valueOf(eeaEventVO.getData().get("fileName"));
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    DataSetSchema dataSetSchema =  mapper.convertValue(eeaEventVO.getData().get("DataSetSchema"), new TypeReference<DataSetSchema>() { });
    String idTableSchema = String.valueOf(eeaEventVO.getData().get("idTableSchema"));
    String delimiter = String.valueOf(eeaEventVO.getData().get("delimiter"));
    Long startLine = Long.parseLong((String) eeaEventVO.getData().get("startLine"));
    Long endLine = Long.parseLong((String) eeaEventVO.getData().get("endLine"));
    Long partitionId = Long.parseLong((String) eeaEventVO.getData().get("partitionId"));
    final Long taskId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("task_id")));

    try (InputStream inputStream = Files.newInputStream(Path.of(filePath))) {
      fileTreatmentHelper.reinitializeCsvSegmentedReaderStrategy(delimiter != null ? delimiter.charAt(0) : loadDataDelimiter,fileCommon,datasetId,fieldMaxLength,null,batchRecordSave);

      fileTreatmentHelper.importCsvFileChunk(datasetId,  fileName, inputStream,partitionId,
               idTableSchema,  replacebool,  dataSetSchema,  delimiter, startLine, endLine);

      fileTreatmentHelper.updateGeometry(datasetId, dataSetSchema);
      fileTreatmentHelper.finishImportProcess(taskId,datasetId,processId, idTableSchema, fileName, "error", false);

    } catch (IOException | EEAException e) {
      LOG_ERROR.error("RN3-Import file task failed: fileName={}, idTableSchema={},  taskId={}", fileName,
              idTableSchema,taskId, e);
      fileTreatmentHelper.updateTask(taskId, ProcessStatusEnum.CANCELED,new Date());
    }

  }

}
