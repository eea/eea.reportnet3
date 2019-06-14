package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.TableValidation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;



/**
 * The Interface TableValidationRepository.
 */
public interface TableValidationRepository extends CrudRepository<TableValidation, Integer> {

  /**
   * Find table validations by id dataset.
   *
   * @param datasetId the dataset id
   * @return the list
   */
  @Query("SELECT tval FROM TableValidation tval INNER JOIN FETCH tval.validation INNER JOIN tval.tableValue tv "
      + "WHERE tv.datasetId.id=?1")
  List<TableValidation> findTableValidationsByIdDataset(Long datasetId); 
  
}
