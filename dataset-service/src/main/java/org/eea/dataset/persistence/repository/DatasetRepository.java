package org.eea.dataset.persistence.repository;

public interface DatasetRepository /* extends CrudRepository<DataSetVO, Integer> */ {
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.data.repository.CrudRepository;

public interface DatasetRepository extends CrudRepository<DataSetVO, Integer> {
}
