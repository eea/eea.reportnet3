package org.eea.recordstore.service;

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
     * @param splitFileName
     * @return
     */
    TaskVO findReleaseTaskBySplitFileName(String splitFileName);

    /**
     * Finds tasks by processId
     * @param processId
     * @return
     */
    List<TaskVO> findTaskByProcessId(String processId);

    /**
     * Updates starting date of task
     * @param taskVO
     * @return
     */
    void updateTaskStartingDate(TaskVO taskVO);
}
