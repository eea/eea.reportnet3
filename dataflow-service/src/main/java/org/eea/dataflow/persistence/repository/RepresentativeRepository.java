package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Representative;
import org.springframework.data.repository.PagingAndSortingRepository;



/**
 * The Interface RepresentativeRepository.
 */
public interface RepresentativeRepository extends PagingAndSortingRepository<Representative, Long> {

  List<Representative> findAllByDataflow_Id(Long dataflowId);
}
