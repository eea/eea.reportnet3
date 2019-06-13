package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.RecordValidation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface ValidationRecordRepository.
 */
public interface ValidationRecordRepository extends JpaRepository<RecordValidation, Long> {

}
