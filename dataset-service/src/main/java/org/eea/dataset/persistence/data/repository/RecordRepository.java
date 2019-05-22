package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.RecordValue;
import org.springframework.data.repository.CrudRepository;

public interface RecordRepository extends CrudRepository<RecordValue, Integer> {


}
