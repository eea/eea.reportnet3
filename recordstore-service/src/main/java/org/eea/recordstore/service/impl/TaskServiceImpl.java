package org.eea.recordstore.service.impl;

import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.recordstore.mapper.TaskMapper;
import org.eea.recordstore.persistence.domain.Task;
import org.eea.recordstore.persistence.repository.TaskRepository;
import org.eea.recordstore.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
     * Updates task
     * @param taskVO
     * @return
     */
    @Transactional
    @Override
    public void updateTask(TaskVO taskVO) {
        taskRepository.updateStatusAndFinishDate(taskVO.getId(), taskVO.getStatus().toString(), taskVO.getFinishDate());
    }

    /**
     *
     * @param json
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
}
