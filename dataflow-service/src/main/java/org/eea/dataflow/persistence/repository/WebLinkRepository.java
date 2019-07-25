package org.eea.dataflow.persistence.repository;

import java.util.List;
import org.eea.dataflow.persistence.domain.Weblink;
import org.springframework.data.repository.CrudRepository;

/**
 * The interface Contributor repository.
 */
public interface WebLinkRepository extends CrudRepository<Weblink, Long> {

  // to find all dataflow
  List<Weblink> findAllByDataflow_Id(Long idDataflow);

}
