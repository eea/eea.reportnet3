package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Dataflow;
import org.springframework.data.repository.CrudRepository;


/**
 * The interface Dataflow repository.
 */
public interface DataflowRepository extends CrudRepository<Dataflow, Long> {

}
