package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<DatasetValue, Long> {

  // DatasetValue findByDatasetMetabaseId(Long datasetMetabaseId);

}
