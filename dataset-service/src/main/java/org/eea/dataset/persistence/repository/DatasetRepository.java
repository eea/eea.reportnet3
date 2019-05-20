package org.eea.dataset.persistence.repository;

import org.eea.dataset.persistence.domain.Dataset;
import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<Dataset, Integer> {
}
