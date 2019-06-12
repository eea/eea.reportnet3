package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.RecordValidation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationRecordRepository extends JpaRepository<RecordValidation, Long> {

}
