package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationDatasetRepository extends JpaRepository<DatasetValidation, Long> {

}
