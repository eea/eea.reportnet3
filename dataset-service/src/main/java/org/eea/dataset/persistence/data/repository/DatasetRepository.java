package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DatasetRepository.
 */
public interface DatasetRepository extends CrudRepository<DatasetValue, Long> {

}
