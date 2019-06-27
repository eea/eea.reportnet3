package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.DatasetValidation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface DatasetValidationRepository extends CrudRepository<DatasetValidation, Long> {

  @Query(
      "SELECT dv FROM DatasetValidation dv  WHERE dv.validation.id in(:ids) ")
  List<DatasetValidation> findByValidationIds(List<Long> ids);

}
