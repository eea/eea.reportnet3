package org.eea.dataset.persistence.data.repository;

import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<DataSetVO, Integer> {
}
