package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.Validation;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The interface Validation repository.
 */
public interface ValidationRepository extends PagingAndSortingRepository<Validation, Long> {

  /**
   * Find by level error.
   *
   * @param levelError the level error
   * @return the list
   */
  List<Validation> findByLevelError(ErrorTypeEnum levelError);

}
