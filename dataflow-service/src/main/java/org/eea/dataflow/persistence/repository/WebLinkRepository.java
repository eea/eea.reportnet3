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
   * @param description the description
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  Optional<Weblink> findByUrlAndDescriptionAndDataflowId(String url, String description,
      Long dataflowId);
}
