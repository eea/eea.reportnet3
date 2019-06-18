package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Contributor;
import org.eea.dataflow.persistence.domain.Dataflow;
import org.springframework.data.repository.CrudRepository;


/**
 * The interface Dataflow repository.
 */
public interface DataflowRepository extends CrudRepository<Dataflow, Long> {


  /**
   * Find all by accepted true and by contributors.
   *
   * @param status the status
   * @param contributor the contributor
   * @return the dataflow
   */
  Dataflow findAllByAcceptedTrueAndByContributors(Boolean status, Contributor contributor);


  /**
   * Find all by accepted false and by contributors.
   *
   * @param status the status
   * @param contributor the contributor
   * @return the dataflow
   */
  Dataflow findAllByAcceptedFalseAndByContributors(Boolean status, Contributor contributor);

}
