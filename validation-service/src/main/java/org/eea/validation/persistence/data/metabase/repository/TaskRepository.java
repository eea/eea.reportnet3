package org.eea.validation.persistence.data.metabase.repository;

import java.util.List;
import org.eea.validation.persistence.data.metabase.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface TaskRepository.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

  @Query(nativeQuery = true, value = "with numberOfPendingTaks as (select count(id)\r\n"
      + " as ntasks, process_id from task where status= 'IN_QUEUE' group by task.process_id),\r\n"
      + " taskPriorityId as (\r\n"
      + "select  t.id, p.priority ,npt.ntasks from process p join numberOfPendingTaks npt on p.process_id=npt.process_id join task t on t.process_id= p.process_id where t.status= 'IN_QUEUE' order by p.priority, npt.ntasks asc \r\n"
      + ")\r\n" + "select t.id from taskPriorityId t limit :numberTasks ;")
  List<Long> findLastTask(@Param("numberTasks") int numberTasks);

  @Query(nativeQuery = true, value = "with numberOfPendingTaks as (select count(id)\r\n"
      + " as ntasks, process_id from task where status= 'IN_QUEUE' group by task.process_id),\r\n"
      + " taskPriorityId as (\r\n"
      + "select  t.id, p.priority ,npt.ntasks from process p join numberOfPendingTaks npt on p.process_id=npt.process_id join task t on t.process_id= p.process_id where t.status= 'IN_QUEUE' and p.priority>50 order by p.priority, npt.ntasks asc \r\n"
      + ")\r\n" + "select t.id from taskPriorityId t limit :numberTasks ;")
  List<Long> findLastLowPriorityTask(@Param("numberTasks") int numberTasks);

  @Query(nativeQuery = true,
      value = "select case when (exists (select id from task where process_id=:processId and status !='FINISHED' limit 1)) then FALSE else TRUE end")
  boolean isProcessFinished(@Param("processId") String processId);

  @Query(nativeQuery = true, value = "select * from task WHERE id in(:ids)")
  List<Task> findByIdIn(@Param("ids") List<Long> ids);
}


