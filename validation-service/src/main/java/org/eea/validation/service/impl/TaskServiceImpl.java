package org.eea.validation.service.impl;

import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.validation.mapper.TaskMapper;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.service.TaskService;
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

    @Override
    public TaskVO findTaskBySplitFileName(String splitFileName) {
        Task task = taskRepository.findBySplitFileNameContains(splitFileName);
        return taskMapper.entityToClass(task);
    }

    @Override
    public List<TaskVO> findTaskByProcessId(String processId) {
        List<Task> tasks = taskRepository.findByProcessId(processId);
        return taskMapper.entityListToClass(tasks);
    }
}
