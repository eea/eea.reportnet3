package org.eea.dataset.kafka.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.dataset.persistence.metabase.repository.TaskRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobProcessController.JobProcessControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.CsvFileChunkRecoveryDetails;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.orchestrator.enums.JobInfoEnum;
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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;

/**
 * The Class EventHandlerCommand. Event Handler Command where we are encapsulating both
 * Object[EventHandlerReceiver] and the operation[Close] together as command.
 */
@Component
public class ImportCsvFileChunkToDatasetCommand extends AbstractEEAEventHandlerCommand {

  private static final Logger LOG = LoggerFactory.getLogger(ImportCsvFileChunkToDatasetCommand.class);

  /**
   * The validation helper.
   */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  @Autowired
  private FileCommonUtils fileCommon;

  @Autowired
  private TaskRepository taskRepository;

  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  @Autowired
  private JobControllerZuul jobControllerZuul;

  @Autowired
  private JobProcessControllerZuul jobProcessControllerZuul;

  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

  /** The batch record save. */
  @Value("${dataset.import.batchRecordSave}")
  private int batchRecordSave;

  /**
   * The delimiter.
   */
  @Value("${loadDataDelimiter}")
  private char loadDataDelimiter;


  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

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

    boolean replacebool = !(replace instanceof Boolean) || (boolean) replace;

    String filePath = String.valueOf(eeaEventVO.getData().get("filePath"));

    String fileName = String.valueOf(eeaEventVO.getData().get("fileName"));
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    DataSetSchema dataSetSchema =  mapper.convertValue(eeaEventVO.getData().get("DataSetSchema"), new TypeReference<DataSetSchema>() { });


    CsvFileChunkRecoveryDetails csvFileChunkRecoveryDetails =  mapper.convertValue(eeaEventVO.getData().get("CsvFileChunkRecoveryDetails"), new TypeReference<CsvFileChunkRecoveryDetails>() { });

    if(csvFileChunkRecoveryDetails==null){
      //Initialize it once and pass it around as a reference, to store the records and fields temp files names and then save them to the Task
      csvFileChunkRecoveryDetails = new CsvFileChunkRecoveryDetails();
    }
      csvFileChunkRecoveryDetails.setTaskId(taskId);


    String idTableSchema = String.valueOf(eeaEventVO.getData().get("idTableSchema"));
    String delimiter = (eeaEventVO.getData().get("delimiter") != null) ? String.valueOf(eeaEventVO.getData().get("delimiter")) : null;
    Long startLine = Long.parseLong((String) eeaEventVO.getData().get("startLine"));
    Long endLine = Long.parseLong((String) eeaEventVO.getData().get("endLine"));
    Long partitionId = Long.parseLong((String) eeaEventVO.getData().get("partitionId"));
    Boolean recoveryMode = true;

    String taskIdStr = (taskId != null) ? String.valueOf(taskId) : "null";
    LOG.info("Executing import task with id {} for datasetId {} filePath {}", taskIdStr, datasetId, filePath);

    try (InputStream inputStream = Files.newInputStream(Path.of(filePath))) {
      // Obtain the data provider code to insert into the record
      Long providerId = 0L;
      DataSetMetabaseVO metabase = datasetMetabaseService.findDatasetMetabase(datasetId);
      if (metabase.getDataProviderId() != null) {
        providerId = metabase.getDataProviderId();
      }
      DataProviderVO provider = representativeControllerZuul.findDataProviderById(providerId);

      fileTreatmentHelper.reinitializeCsvSegmentedReaderStrategy(delimiter != null ? delimiter.charAt(0) : loadDataDelimiter, fileCommon, datasetId, fieldMaxLength, provider.getCode(), batchRecordSave);
      fileTreatmentHelper.importCsvFileChunk(datasetId,  fileName, inputStream,partitionId,
               idTableSchema,  replacebool,  dataSetSchema,  delimiter, startLine, endLine, csvFileChunkRecoveryDetails);
      LOG.info("Updating status of task with id {} and datasetId {} to FINISHED.", taskIdStr, datasetId);
      Optional<Task> task = taskRepository.findById(taskId);
      if(task.isPresent()){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EEAEventVO eeaEventVOToUpdate = objectMapper.readValue(task.get().getJson(), EEAEventVO.class);
        eeaEventVOToUpdate.getData().put("CsvFileChunkRecoveryDetails",csvFileChunkRecoveryDetails);
        task.get().setJson(objectMapper.writeValueAsString(eeaEventVOToUpdate));
        taskRepository.updateStatusAndFinishDateAndJson(ProcessStatusEnum.FINISHED.toString(), new Date(), task.get().getJson(), task.get().getId());
      }
      else{
        LOG.error("Could not update task with id {} and datasetId {} because task is not present.", taskIdStr, datasetId);
      }
    } catch (Exception e) {
      LOG.error("Error Executing ImportCsvFileChunkToDatasetCommand for taskId {} and datasetId {} Updating task status to CANCELED", taskIdStr, datasetId, e);
      Optional<Task> task = taskRepository.findById(taskId);
      if(task.isPresent()){
        taskRepository.updateStatusAndFinishDate(task.get().getId(), ProcessStatusEnum.CANCELED.toString(), new Date());
        if(e.getMessage().equals(EEAErrorMessage.ERROR_FILE_NO_HEADERS_MATCHING)){
          Long jobId = jobProcessControllerZuul.findJobIdByProcessId(task.get().getProcessId());
          jobControllerZuul.updateJobInfo(jobId, JobInfoEnum.ERROR_NO_HEADERS_MATCHING, null);
        }
      }
      else{
        LOG.error("Could not update task with id {} and datasetId {} because task is not present.", taskIdStr, datasetId);
      }
    }

  }

}
