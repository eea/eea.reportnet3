package org.eea.validation.service;

import org.eea.interfaces.vo.validation.TaskVO;

import java.util.List;

public interface TaskService {

    /**
     * Saves task
     * @param taskVO
     */
    TaskVO saveTask(TaskVO taskVO);

    /**
     * Updates task
     * @param taskVO
     * @return
     */
    void updateTask(TaskVO taskVO);

    /**
     * Finds release task by json
     * @param json
     * @return
     */
    TaskVO findReleaseTaskByJson(String json);

    /**
     * Finds tasks by processId
     * @param processId
     * @return
     */
    List<TaskVO> findTaskByProcessId(String processId);
}
