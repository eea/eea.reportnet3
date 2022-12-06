package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.JobProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * The Interface JobProcessRepository.
 */
public interface JobProcessRepository extends JpaRepository<JobProcess, Long> {

    JobProcess findByProcessId(String processId);

    @Query(nativeQuery = true, value = "select jp.process_id from job_process jp where jp.job_id= :jobId")
    List<String> findProcessesByJobId(@Param("jobId") Long jobId);

}
