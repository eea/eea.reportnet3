package org.eea.validation.persistence.data.repository;

import org.eea.validation.persistence.data.domain.Validation;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The Interface ValidationRepository.
 */
public interface ValidationRepository
    extends PagingAndSortingRepository<Validation, Long>, ValidationRepositoryPaginated {

}
