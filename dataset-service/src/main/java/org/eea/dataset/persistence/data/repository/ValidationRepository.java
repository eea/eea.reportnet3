package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.Validation;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The interface Validation repository.
 */
public interface ValidationRepository extends PagingAndSortingRepository<Validation, Long> {


}
