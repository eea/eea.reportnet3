package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * The Interface CodelistRepository.
 */
public interface CodelistRepository extends PagingAndSortingRepository<Codelist, Long> {

  Optional<List<Codelist>> findAllByNameAndVersion(String name, Long version);
}
