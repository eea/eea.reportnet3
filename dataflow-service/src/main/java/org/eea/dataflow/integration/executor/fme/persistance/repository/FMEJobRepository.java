package org.eea.dataflow.integration.executor.fme.persistance.repository;

import org.eea.dataflow.integration.executor.fme.persistance.domain.FMEJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FMEJobRepository extends JpaRepository<FMEJob, Long> {

}
