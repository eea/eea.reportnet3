package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface ValidationDatasetRepository.
 */
public interface ValidationDatasetRepository extends JpaRepository<DatasetValidation, Long> {

}
