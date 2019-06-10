package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * The Interface TableRepository.
 */
public interface TableRepository extends JpaRepository<TableValue, Long> {


  
  
  /**
   * Find all tables.
   *
   * @return the list
   */
  @Query(value = "SELECT u FROM TableValue u")
  List<TableValue> findAllTables();
  
  /**
   * Count records by id table.
   *
   * @param id the id
   * @return the long
   */
  @Query("SELECT COUNT(rv) FROM TableValue tv INNER JOIN tv.records rv WHERE tv.id=?1")
  Long countRecordsByIdTable(Long id);
}
