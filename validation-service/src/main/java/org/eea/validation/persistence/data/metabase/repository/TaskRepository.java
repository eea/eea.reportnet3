package org.eea.validation.persistence.data.metabase.repository;

import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The Interface TaskRepository.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

  /**
   * Find last task.
   *
   * @param numberTasks the number tasks
   * @return the list
   */
  @Query(nativeQuery = true, value = "with numberOfPendingTaks as (select count(id)\r\n"
      + " as ntasks, process_id from task where status= 'IN_QUEUE' and task_type='VALIDATION_TASK' group by task.process_id),\r\n"
      + " taskPriorityId as (\r\n"
      + "select  t.id, p.priority ,npt.ntasks from process p join numberOfPendingTaks npt on p.process_id=npt.process_id join task t on t.process_id= p.process_id where t.status= 'IN_QUEUE' order by p.priority, npt.ntasks asc \r\n"
      + ")\r\n" + "select t.id from taskPriorityId t limit :numberTasks ;")
  List<Long> findLastTask(@Param("numberTasks") int numberTasks);

  /**
   * Find last low priority task.
   *
   * @param numberTasks the number tasks
   * @return the list
   */
  @Query(nativeQuery = true, value = "with numberOfPendingTaks as (select count(id)\r\n"
      + " as ntasks, process_id from task where status= 'IN_QUEUE' and task_type='VALIDATION_TASK' group by task.process_id),\r\n"
      + " taskPriorityId as (\r\n"
      + "select  t.id, p.priority ,npt.ntasks from process p join numberOfPendingTaks npt on p.process_id=npt.process_id join task t on t.process_id= p.process_id where t.status= 'IN_QUEUE' and p.priority>50 order by p.priority, npt.ntasks asc \r\n"
      + ")\r\n" + "select t.id from taskPriorityId t limit :numberTasks ;")
  List<Long> findLastLowPriorityTask(@Param("numberTasks") int numberTasks);

  /**
   * Checks if is process finished.
   *
   * @param processId the process id
   * @return true, if is process finished
   */
  @Query(nativeQuery = true,
      value = "select case when (exists (select id from task where process_id=:processId and task_type='VALIDATION_TASK' and (status !='FINISHED' and status !='CANCELED') limit 1)) then FALSE else TRUE end")
  boolean isProcessFinished(@Param("processId") String processId);

  /**
   * Find by id in.
   *
   * @param ids the ids
   * @return the list
   */
  @Query(nativeQuery = true, value = "select * from task WHERE id in(:ids) and task_type='VALIDATION_TASK'")
  List<Task> findByIdIn(@Param("ids") List<Long> ids);

  /**
   * update status
   * @param taskId
   * @param status
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true, value = "update task set status= :status where id= :taskId")
  void updateStatus(@Param("taskId") Long taskId, @Param("status") String status);

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
   * Cancel status and finish date.
   *
   * @param taskId the task id
   * @param dateFinish the date finish
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true,
      value = "update task set status= (case when (select \"version\" from task where id=:taskId) >20 then 'CANCELED' else 'IN_QUEUE' END) ,date_finish= :dateFinish where id=:taskId ")
  void cancelStatusAndFinishDate(@Param("taskId") Long taskId,
      @Param("dateFinish") Date dateFinish);

  /**
   * Checks if is process ending.
   *
   * @param processId the process id
   * @return true, if is process ending
   */
  @Query(nativeQuery = true,
      value = "select case when (exists (select id from task where process_id=:processId and status ='IN_QUEUE' and task_type='VALIDATION_TASK' limit 1)) then FALSE else TRUE end")
  boolean isProcessEnding(@Param("processId") String processId);

  @Query(nativeQuery = true,
          value = "select id from task where status='IN_PROGRESS' and task_type='VALIDATION_TASK' and (extract(epoch from LOCALTIMESTAMP - date_start) / 60) > :timeInMinutes")
  List<BigInteger> getInProgressValidationTasksThatExceedTime(@Param("timeInMinutes") long timeInMinutes);

  /**
   * Finds tasks by processId
   * @param processId
   * @return
   */
  @Query(nativeQuery = true, value = "select id from task where process_id=:processId")
  List<BigInteger> findByProcessId(@Param("processId") String processId);

  /**
   * Finds tasks by processId and status
   * @param processId
   * @param status
   * @return
   */
  List<Task> findByProcessIdAndStatus(String processId, ProcessStatusEnum status);

  /**
   * Update status and finish date based on process id and current status.
   *
   * @param status
   * @param dateFinish
   * @param processId
   * @param statuses
   */
  @Modifying
  @Transactional
  @Query(nativeQuery = true,
          value = "update task set status= :status ,date_finish= :dateFinish where process_id=:processId and status in :statuses")
  void updateTaskStatusByProcessIdAndCurrentStatus(@Param("status") String status, @Param("dateFinish") Date dateFinish,
                                                   @Param("processId") String processId, @Param("statuses") Set<String> statuses);

  /**
   * Finds tasks by processId and status
   * @param processId
   * @param status
   * @return
   */
  @Query(nativeQuery = true,
          value = "select count(*) from task where process_id=:processId and status in :status")
  Integer findTasksCountByProcessIdAndStatusIn(@Param("processId") String processId,@Param("status") List<String> status);

  /**
   * Finds the latest task that is in a specific status for more than timeInMinutes minutes
   * @param processId
   * @param timeInMinutes
   * @param statuses
   * @param taskType
   * @return
   */
  @Query(nativeQuery = true,
          value = "select * from task t where t.id in (select id from task where status in :statuses and task_type= :taskType and process_id= :processId order by date_finish desc limit 1) and (extract(epoch from LOCALTIMESTAMP - t.date_finish) / 60) > :timeInMinutes")
  Task getTaskThatExceedsTimeByStatusesAndType(@Param("processId") String processId, @Param("timeInMinutes") long timeInMinutes,
                                                      @Param("statuses") Set<String> statuses, @Param("taskType") String taskType);

  Optional<Task> findById(Long id);

  /**
   * Checks if is process ending.
   *
   * @param processId the process id
   * @return true, if is process ending
   */
  @Query(nativeQuery = true,
      value = "select case when (exists (select id from task where process_id=:processId and status ='CANCELED' and task_type='VALIDATION_TASK' limit 1)) then TRUE else FALSE end")
  boolean hasProcessCanceledTasks(@Param("processId") String processId);
}


