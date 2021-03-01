package org.eea.dataflow.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataflow.persistence.domain.Weblink;
import org.springframework.data.repository.CrudRepository;

/**
 * The interface Contributor repository.
 */
public interface WebLinkRepository extends CrudRepository<Weblink, Long> {

  /**
   * Find all by dataflow id.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  List<Weblink> findAllByDataflow_Id(Long idDataflow);

  /**
   * Exists by url.
   *
   * @param url the url
   * @return true, if successful
   */
  Optional<Weblink> findByUrlAndDataflowId(String url, Long dataflowId);
}
