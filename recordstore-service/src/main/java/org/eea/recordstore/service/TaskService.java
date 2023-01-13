package org.eea.recordstore.service;

import org.eea.interfaces.vo.validation.TaskVO;
import org.eea.recordstore.persistence.domain.Task;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

public interface TaskService {

    /**
     * Saves task
     * @param taskVO
     */
    TaskVO saveTask(TaskVO taskVO);

    /**
     * Updates task status and dateFinish
     * @param status
     * @param dateFinish
     * @param taskId
     */
    void updateStatusAndFinishedDate(String status, Date dateFinish, Long taskId);

    /**
     * Finds release task by json
     * @param splitFileName
     * @return
     */
    TaskVO findReleaseTaskBySplitFileNameAndProcessId(String splitFileName, String processId);

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

    /**
     * Finds tasks with type IMPORT_TASK and status IN_PROGRESS
     * @return the tasks
     */
    List<TaskVO> findImportTasksInProgress();

    /**
     * Restarts a task
     */
    void restartTask(Long taskId);
}
