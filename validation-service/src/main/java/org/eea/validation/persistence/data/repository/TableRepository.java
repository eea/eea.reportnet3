package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.TableValue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface TableRepository.
 */
public interface TableRepository extends JpaRepository<TableValue, Long> {

  /**
   * Find by id table schema.
   *
   * @param idTableSchema the id table schema
   * @return the table value
   */
  TableValue findByIdTableSchema(String idTableSchema);

}


