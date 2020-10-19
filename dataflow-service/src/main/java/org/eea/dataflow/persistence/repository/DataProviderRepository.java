package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.eea.dataflow.persistence.domain.DataProviderCode;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

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
   * Gets the code by id.
   *
   * @param id the id
   * @return the code by id
   */
  @Query("SELECT dp.code FROM DataProvider dp WHERE dp.id = :id")
  String getCodeById(@Param("id") Long id);
}
