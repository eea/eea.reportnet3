package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.TableValidation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationTableRepository extends JpaRepository<TableValidation, Long> {


}
