package org.eea.dataset.kafka.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.dataset.persistence.metabase.repository.TaskRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.kafka.commands.AbstractEEAEventHandlerCommand;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.thread.ThreadPropertiesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ImportCsvFileChunkToDatasetCommand extends AbstractEEAEventHandlerCommand {

  /**
   * The validation helper.
   */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  @Autowired
  private FileCommonUtils fileCommon;

  @Autowired
  private TaskRepository taskRepository;

  //@Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength=10000;

  /** The batch record save. */
  //@Value("${dataset.import.batchRecordSave}")
  private int batchRecordSave=2500;

//  @Value("${loadDataDelimiter}")
private char loadDataDelimiter=',';
  /**
   * Gets the event type.
   *
   * @return the event type
   */
  @Override
  public EventType getEventType() {
    return EventType.COMMAND_IMPORT_CSV_FILE_CHUNK_TO_DATASET;
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

    Long taskId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("task_id")));
    Optional<Task> task = taskRepository.findById(taskId);

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

    try (InputStream inputStream = Files.newInputStream(Path.of(filePath))) {

      fileTreatmentHelper.reinitializeCsvSegmentedReaderStrategy(delimiter != null ? delimiter.charAt(0) : loadDataDelimiter,fileCommon,datasetId,fieldMaxLength,null,batchRecordSave);
      fileTreatmentHelper.importCsvFileChunk(datasetId,  fileName, inputStream,partitionId,
               idTableSchema,  replacebool,  dataSetSchema,  delimiter, startLine, endLine);
      if(task.isPresent()){
        task.get().setStatus(ProcessStatusEnum.FINISHED);
        taskRepository.save(task.get());
      }
    } catch (IOException | EEAException e) {
      if(task.isPresent()){
        task.get().setStatus(ProcessStatusEnum.CANCELED);
        taskRepository.save(task.get());
      }
    }

  }

}
