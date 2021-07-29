package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DataProviderRepository.
 */
public interface DataProviderRepository extends CrudRepository<DataProvider, Long> {


  /**
   * Find all by group id.
   *
   * @param groupId the group id
   * @return the list
   */
  List<DataProvider> findAllByDataProviderGroup_id(Long groupId);

  /**
   * Find by code.
   *
   * @param code the code
   * @return the list
   */
  List<DataProvider> findByCode(String code);
}
