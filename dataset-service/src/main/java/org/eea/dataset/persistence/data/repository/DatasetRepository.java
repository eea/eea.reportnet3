package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.domain.Dataset;
import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<Dataset, Integer> {
}
