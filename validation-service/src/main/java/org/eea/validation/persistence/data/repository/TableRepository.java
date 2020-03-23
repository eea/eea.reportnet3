package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.TableValue;
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

}


