package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * The Interface ValidationDatasetRepository.
 */
public interface ValidationDatasetRepository extends JpaRepository<DatasetValidation, Long> {

  /**
   * Find by validation ids.
   *
   * @param ids the ids
   * @return the list
   */
  @Query("SELECT dv FROM DatasetValidation dv  WHERE dv.validation.id in(:ids) ")
  List<DatasetValidation> findByValidationIds(@Param("ids") List<Long> ids);
}
