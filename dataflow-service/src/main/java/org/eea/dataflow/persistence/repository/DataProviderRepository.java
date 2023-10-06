package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.DataProvider;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

/**
 * The Interface DataProviderRepository.
 */
public interface DataProviderRepository extends CrudRepository<DataProvider, Long> {

  /**
   * Find all by data provider group id.
   *
   * @param id the id
   * @return the list
   */
  List<DataProvider> findAllByDataProviderGroup_id(@Param("id") Long id);

  /**
   * Find by code.
   *
   * @param code the code
   * @return the list
   */
  List<DataProvider> findByCode(String code);

  /**
   * Save a DataProvider object.
   *
   * @param label The label of the DataProvider
   * @param code The code of the DataProvider
   * @param groupId The group ID of the DataProvider
   */
  @Modifying
  @Transactional
  @Query(value = "INSERT INTO data_provider(id, label, code, group_id) " +
          "VALUES ((SELECT COALESCE(MAX(id), 0) + 1 FROM data_provider), ?, ?, ?)",
          nativeQuery = true)
  void saveDataProvider(String label, String code, Long groupId);
}
