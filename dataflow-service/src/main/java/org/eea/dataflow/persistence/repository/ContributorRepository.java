package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Contributor;
import org.springframework.data.repository.CrudRepository;

/**
 * The interface Contributor repository.
 */
public interface ContributorRepository extends CrudRepository<Contributor, Long> {

}
