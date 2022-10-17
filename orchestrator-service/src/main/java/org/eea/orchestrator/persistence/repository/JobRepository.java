package org.eea.orchestrator.persistence.repository;

import org.eea.orchestrator.persistence.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface JobRepository.
 */
public interface JobRepository extends JpaRepository<Job, Long> {

}

