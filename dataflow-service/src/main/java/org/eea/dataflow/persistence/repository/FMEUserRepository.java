package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.FMEUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface FMEUserRepository.
 */
public interface FMEUserRepository extends JpaRepository<FMEUser, Long> {

}
