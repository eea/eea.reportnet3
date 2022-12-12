package org.eea.recordstore.service.impl;

import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.recordstore.mapper.TaskMapper;
import org.eea.recordstore.persistence.domain.Task;
import org.eea.recordstore.persistence.repository.TaskRepository;
import org.eea.recordstore.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public TaskVO findReleaseTaskBySplitFileName(String splitFileName) {
        Task task = taskRepository.findByJsonSplitFileName(splitFileName);
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
     * Updates start date of task
     * @param taskVO
     * @return
     */
    @Transactional
    @Override
    public void updateTaskStartingDate(TaskVO taskVO) {
        taskRepository.updateStatusAndSAndStartingDate(taskVO.getId(), taskVO.getStatus().toString(), taskVO.getStartingDate());
    }
}
