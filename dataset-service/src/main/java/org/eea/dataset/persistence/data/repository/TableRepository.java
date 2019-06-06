package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TableRepository extends JpaRepository<TableValue, Long> {

  @Query(value = "SELECT u FROM TableValue u")
  List<TableValue> findAllTables();
}
