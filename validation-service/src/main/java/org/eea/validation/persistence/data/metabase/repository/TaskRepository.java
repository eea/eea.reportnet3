package org.eea.validation.persistence.data.metabase.repository;

import java.util.Date;
import java.util.List;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
      + " as ntasks, process_id from task where status= 'IN_QUEUE' group by task.process_id),\r\n"
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
      + " as ntasks, process_id from task where status= 'IN_QUEUE' group by task.process_id),\r\n"
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
      value = "select case when (exists (select id from task where process_id=:processId and (status !='FINISHED' and status !='CANCELED') limit 1)) then FALSE else TRUE end")
  boolean isProcessFinished(@Param("processId") String processId);

  /**
   * Find by id in.
   *
   * @param ids the ids
   * @return the list
   */
  @Query(nativeQuery = true, value = "select * from task WHERE id in(:ids)")
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
      value = "select case when (exists (select id from task where process_id=:processId and status ='IN_QUEUE' limit 1)) then FALSE else TRUE end")
  boolean isProcessEnding(@Param("processId") String processId);
}


