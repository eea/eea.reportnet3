package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataProviderRepository.
 */
public interface DataProviderRepository extends CrudRepository<DataProvider, Long> {

  /**
   * Find distinct code.
   *
   * @return the list
   */
  @Query("SELECT DISTINCT r.groupId as dataProviderGroupId, r.type as label FROM DataProvider r")
  List<DataProviderCode> findDistinctCode();

  /**
   * Find all by group id.
   *
   * @param groupId the group id
   * @return the list
   */
  List<DataProvider> findAllByGroupId(Long groupId);

  /**
   * Find by code.
   *
   * @param code the code
   * @return the list
   */
  List<DataProvider> findByCode(String code);
}
