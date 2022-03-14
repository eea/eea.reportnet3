package org.eea.validation.persistence.data.metabase.repository;

import org.eea.validation.persistence.data.metabase.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface TaskRepository.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

  @Query(nativeQuery = true,
      value = "select * from task where status= 'IN_QUEUE' and process_id= (select process_id from (select count(id) as ntasks, process_id from task where status= 'IN_QUEUE' group by task.process_id order by ntasks limit 1) aux) limit 1")
  Task findLastTask();

  @Query(nativeQuery = true,
      value = "select case when (exists (select id from task where process_id=:processId and status !='COMPLETED' limit 1)) then FALSE else TRUE end")
  boolean isProcessFinished(@Param("processId") String processId);
}


