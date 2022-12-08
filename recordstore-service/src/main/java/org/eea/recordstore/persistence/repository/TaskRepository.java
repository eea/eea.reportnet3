package org.eea.recordstore.persistence.repository;

import org.eea.recordstore.persistence.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(nativeQuery = true,
    value = "select id from task where status='IN_PROGRESS' and task_type='RELEASE_TASK' and (extract(epoch from LOCALTIMESTAMP - date_start) / 60) > :timeInMinutes")
    List<BigInteger> getTasksInProgress(@Param("timeInMinutes") long timeInMinutes);

    /**
     * Finds tasks by processId
     * @param processId
     * @return
     */
    List<Task> findByProcessId(String processId);

    /**
     * Finds task by json
     * @param splitFileName
     * @return
     */
    @Query(nativeQuery = true, value = "select * from task t where t.\"json\"\\:\\:json->>'splitFileName' like :splitFileName")
    Task findByJsonSplitFileName(@Param("splitFileName") String splitFileName);

    /**
     * Update status and finish date.
     *
     * @param taskId the task id
     * @param status the status
     * @param dateFinish the date finish
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "update task set status= :status ,date_finish= :dateFinish where id=:taskId ")
    void updateStatusAndFinishDate(@Param("taskId") Long taskId, @Param("status") String status,
                                   @Param("dateFinish") Date dateFinish);

    /**
     * Update status and starting date.
     *
     * @param taskId the task id
     * @param status the status
     * @param dateStart the date finish
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "update task set status= :status ,date_start= :dateStart where id=:taskId ")
    void updateStatusAndSAndStartingDate(@Param("taskId") Long taskId, @Param("status") String status,
        @Param("dateStart") Date dateStart);
}
