package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Representative;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface RepresentativeRepository.
 */
public interface RepresentativeRepository extends CrudRepository<Representative, Long> {

  /**
   * Find all by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<Representative> findAllByDataflow_Id(Long dataflowId);
}
