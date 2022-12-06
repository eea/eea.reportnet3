package org.eea.recordstore.persistence.repository;

import org.eea.recordstore.persistence.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(nativeQuery = true,
        value = "select t.id from task t, procces p where t.procces_id = p.procces_id and p.process_type= 'RELEASE' and t.status='IN_PROGRESS' and (extract(epoch from LOCALTIMESTAMP - t.date_start) / 60) > :timeInMinutes")
    List<BigInteger> getTasksInProgress(@Param("timeInMinutes") long timeInMinutes);

}
