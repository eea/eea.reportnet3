package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.JobProcess;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface JobProcessRepository.
 */
public interface JobProcessRepository extends JpaRepository<JobProcess, Long> {

    JobProcess findByProcessId(String processId);

}
