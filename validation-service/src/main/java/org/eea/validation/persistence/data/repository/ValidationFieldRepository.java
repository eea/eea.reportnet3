package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.FieldValidation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface ValidationFieldRepository.
 */
public interface ValidationFieldRepository extends JpaRepository<FieldValidation, Long> {

}
