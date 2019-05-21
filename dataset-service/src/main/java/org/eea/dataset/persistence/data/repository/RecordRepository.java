package org.eea.dataset.persistence.data.repository;

import java.util.List;
import org.eea.dataset.persistence.data.domain.Record;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RecordRepository extends CrudRepository<Record, Integer> {

  @Query("SELECT r from Record r")
  List<Record> specialFind(Long datasetId);
}
