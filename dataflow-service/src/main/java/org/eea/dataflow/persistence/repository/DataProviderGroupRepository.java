package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderGroup;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataProviderRepository.
 */
public interface DataProviderGroupRepository extends CrudRepository<DataProviderGroup, Long> {

  /**
   * Find distinct code.
   *
   * @return the list
   */
  @Query("SELECT DISTINCT r FROM DataProviderGroup r WHERE r.type = :type")
  List<DataProviderGroup> findDistinctCode(TypeDataProviderEnum type);

  /**
   * Find by code.
   *
   * @param code the code
   * @return the list
   */
  List<DataProvider> findByType(TypeDataProviderEnum type);
}
