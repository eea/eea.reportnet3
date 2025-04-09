package org.eea.recordstore.service.impl;

import cdjd.com.fasterxml.jackson.databind.JsonNode;
import cdjd.com.fasterxml.jackson.databind.ObjectMapper;
import org.eea.interfaces.vo.metabase.TaskType;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.interfaces.vo.orchestrator.JobCanceledValidationTasksVO;
import org.eea.recordstore.mapper.TaskMapper;
import org.eea.recordstore.persistence.domain.Task;
import org.eea.recordstore.persistence.repository.TaskRepository;
import org.eea.recordstore.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

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
     * Finds tasks from processIds and status statusEnum
     * @return the tasks
     */
    @Override
    public List<JobCanceledValidationTasksVO> findTasksByProcessIdsAndStatus(
            List<String> processIds, ProcessStatusEnum statusEnum, int pageNum, int pageSize) {

        List<Task> canceledTasks = taskRepository.findByProcessIdInAndStatus(processIds, statusEnum);

        if (canceledTasks.isEmpty()) {
            return Collections.emptyList();
        }

        int fromIndex = pageNum * pageSize;
        if (fromIndex >= canceledTasks.size()) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(fromIndex + pageSize, canceledTasks.size());
        List<Task> paginatedTasks = canceledTasks.subList(fromIndex, toIndex);

        List<JobCanceledValidationTasksVO> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        for (Task task : paginatedTasks) {
            JobCanceledValidationTasksVO taskVO = new JobCanceledValidationTasksVO();
            taskVO.setTaskId(task.getId());

            if (task.getJson() != null) {
                try {
                    JsonNode taskJsonRootNode = objectMapper.readTree(task.getJson());
                    JsonNode taskJsonDataNode = taskJsonRootNode.path("data");

                    if (taskJsonDataNode.has("ruleCode")) {
                        taskVO.setRuleCode(taskJsonDataNode.get("ruleCode").asText());
                    }
                    if (taskJsonDataNode.has("ruleId")) {
                        taskVO.setRuleId(taskJsonDataNode.get("ruleId").asText());
                    }
                    if (taskJsonDataNode.has("ruleLevelError")) {
                        taskVO.setRuleLevelError(taskJsonDataNode.get("ruleLevelError").asText());
                    }

                } catch (Exception e) {
                }
            }

            result.add(taskVO);
        }

        return result;
    }

}
