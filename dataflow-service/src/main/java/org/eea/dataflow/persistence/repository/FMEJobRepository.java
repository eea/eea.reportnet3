package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.FMEJob;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface FMEJobRepository.
 */
public interface FMEJobRepository extends JpaRepository<FMEJob, Long> {

}
