package org.eea.dataset.kafka.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.dataset.persistence.metabase.domain.Task;
import org.eea.dataset.persistence.metabase.repository.TaskRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
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
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private TaskRepository taskRepository;

    private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


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
        Long dataflowId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("dataflowId")));

        ThreadPropertiesManager.setVariable("user", eeaEventVO.getData().get("user"));
        String processId = String.valueOf(eeaEventVO.getData().get("processId"));
        Long taskId = Long.parseLong(String.valueOf(eeaEventVO.getData().get("task_id")));
        Optional<Task> task = this.taskRepository.findById(taskId);

        if (this.shouldAbortCommandExecutionIfCSVImportTasksLeftInProgress(processId, taskId)) {
            LOG_ERROR.error("Process with ID:" + processId + " has Tasks in Progress Status. Command: " + EventType.COMMAND_FINALIZE_CSV_FILE_IMPORT_TO_DATASET + " will abort execution");
            //return status of task in_queue
            if (task.isPresent()) {
                taskRepository.updateStatus(ProcessStatusEnum.IN_QUEUE.toString(), task.get().getId());
            }
            return;
        }


        String fileName = String.valueOf(eeaEventVO.getData().get("fileName"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DataSetSchema dataSetSchema = mapper.convertValue(eeaEventVO.getData().get("DataSetSchema"), new TypeReference<DataSetSchema>() {
        });
        String idTableSchema = String.valueOf(eeaEventVO.getData().get("idTableSchema"));

        try {
            if (this.shouldMarkCommandFailedBecauseImportTasksFailed(processId, taskId)) {
                fileTreatmentHelper.finishImportProcessV2(taskId, dataflowId, datasetId, processId, idTableSchema, fileName, "Error in import process subtasks", false);
            } else {
                fileTreatmentHelper.updateGeometry(datasetId, dataSetSchema);
                fileTreatmentHelper.finishImportProcessV2(taskId, dataflowId, datasetId, processId, idTableSchema, fileName, null, false);

            }

        } catch (Exception e) {
            LOG_ERROR.error("RN3-Import file task failed: fileName={}, idTableSchema={},  taskId={}", fileName,
                    idTableSchema, taskId, e);
            fileTreatmentHelper.updateTask(taskId, ProcessStatusEnum.CANCELED, new Date());
        }

    }

    protected Boolean shouldAbortCommandExecutionIfCSVImportTasksLeftInProgress(String processId, Long currentTaskId) {
        List<ProcessStatusEnum> statusesList = Arrays.asList(ProcessStatusEnum.IN_QUEUE,ProcessStatusEnum.IN_PROGRESS);
        List<Task> inProgressOrInQueueTasks = this.taskRepository.findAllByProcessIdAndStatusIn(processId, statusesList);

        if (inProgressOrInQueueTasks != null && inProgressOrInQueueTasks.size() > 1) {
            return true;
        }
        if (inProgressOrInQueueTasks != null && inProgressOrInQueueTasks.size() == 1) {
            //Case where, for one process ,only one task is left with IN_QUEUE or  IN_PROGRESS Status.
            //We check then if this task is the finalization one.
            if (inProgressOrInQueueTasks.get(0).getId().equals(currentTaskId)) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    protected Boolean shouldMarkCommandFailedBecauseImportTasksFailed(String processId, Long currentTaskId) {
        List<Task> canceledTasks = this.taskRepository.findAllByProcessIdAndStatus(processId, ProcessStatusEnum.CANCELED);
        if (canceledTasks != null && !canceledTasks.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
