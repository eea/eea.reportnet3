package org.eea.recordstore.service.impl;

import cdjd.com.fasterxml.jackson.databind.JsonNode;
import cdjd.com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.orchestrator.JobCanceledValidationTaskVO;
import org.eea.interfaces.vo.orchestrator.JobCanceledValidationTasksVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.recordstore.mapper.TaskMapper;
import org.eea.recordstore.persistence.domain.Task;
import org.eea.recordstore.persistence.repository.TaskRepository;
import org.eea.recordstore.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);

    private TaskRepository taskRepository;
    private TaskMapper taskMapper;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    @Override
    public TaskVO saveTask(TaskVO taskVO) {
        Task task = taskMapper.classToEntity(taskVO);
        taskRepository.save(task);
        taskRepository.flush();
        return taskMapper.entityToClass(task);
    }

    /**
     * Updates task status and finished date
     * @param status
     * @param dateFinish
     * @param taskId
     */
    @Transactional
    @Override
    public void updateStatusAndFinishedDate(String status, Date dateFinish, Long taskId) {
        taskRepository.updateStatusAndFinishDate(status, dateFinish, taskId);
    }

    /**
     *
     * @param splitFileName
     * @return
     */
    @Override
    public TaskVO findReleaseTaskBySplitFileNameAndProcessId(String splitFileName, String processId) {
        Task task = taskRepository.findByJsonSplitFileNameAndProcessId(splitFileName, processId);
        return taskMapper.entityToClass(task);
    }

    /**
     * Finds tasks by processId
     * @param processId
     * @return
     */
    @Override
    public List<TaskVO> findTaskByProcessId(String processId) {
        List<Task> tasks = taskRepository.findByProcessId(processId);
        return taskMapper.entityListToClass(tasks);
    }

    /**
     * Finds tasks with type IMPORT_TASK and status IN_PROGRESS
     * @return the tasks
     */
    @Override
    public List<TaskVO> findImportTasksInProgress(){
        List<Task> tasks = taskRepository.findByTaskTypeAndStatus(TaskType.IMPORT_TASK, ProcessStatusEnum.IN_PROGRESS);
        return taskMapper.entityListToClass(tasks);
    }

    /**
     * Finds tasks coming from list of processIds and status statusEnum
     * @return the tasks
     */
    @Override
    public JobCanceledValidationTasksVO findTasksByProcessIdsAndStatus(
            List<String> processIds, ProcessStatusEnum statusEnum, int pageNum, int pageSize) {

        List<Task> canceledTasks = taskRepository.findByProcessIdInAndStatus(processIds, statusEnum);

        int totalRecords = canceledTasks.size();
        if (totalRecords == 0) {
            // Return an empty response if no tasks
            return new JobCanceledValidationTasksVO(
                    Collections.emptyList(), // tasksList
                    0L,                      // totalRecords
                    0L,                      // filteredRecords
                    0L                       // remainingTasks
            );
        }

        // Manual pagination
        int fromIndex = pageNum * pageSize;
        if (fromIndex >= totalRecords) {
            // Requested page is out of range: return empty list, but total still included
            return new JobCanceledValidationTasksVO(
                    Collections.emptyList(),
                    (long) totalRecords,
                    0L,
                    0L
            );
        }

        int toIndex = Math.min(fromIndex + pageSize, totalRecords);
        List<Task> paginatedTasks = canceledTasks.subList(fromIndex, toIndex);

        //Convert paginated Task entities to JobCanceledValidationTaskVO
        List<JobCanceledValidationTaskVO> tasksList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (Task task : paginatedTasks) {
            JobCanceledValidationTaskVO taskVO = new JobCanceledValidationTaskVO();
            taskVO.setTaskId(task.getId());

            if (task.getJson() != null) {
                try {
                    JsonNode dataNode = objectMapper.readTree(task.getJson()).path("data");
                    taskVO.setRuleCode(dataNode.path("ruleCode").asText(null));
                    taskVO.setRuleId(dataNode.path("ruleId").asText(null));
                    taskVO.setRuleLevelError(dataNode.path("ruleLevelError").asText(null));
                } catch (Exception e) {
                    LOG.warn("Failed to parse JSON for taskId {}: {}", task.getId(), e.getMessage());
                }
            }
            tasksList.add(taskVO);
        }

        long filteredRecords = tasksList.size();
        long alreadyFetched = (long) pageNum * pageSize + filteredRecords;
        long remainingTasks = totalRecords - alreadyFetched;
        if (remainingTasks < 0) {
            remainingTasks = 0;
        }

        return new JobCanceledValidationTasksVO(
                tasksList,                 // tasksList
                (long) totalRecords,       // totalRecords
                filteredRecords,           // filteredRecords
                remainingTasks             // remainingTasks
        );
    }
}
