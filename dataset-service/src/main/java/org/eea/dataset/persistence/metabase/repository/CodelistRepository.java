package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.springframework.data.repository.PagingAndSortingRepository;


/**
 * The Interface CodelistRepository.
 */
public interface CodelistRepository extends PagingAndSortingRepository<Codelist, Long> {

  /**
   * Find all by name and version.
   *
   * @param name the name
   * @param version the version
   * @return the optional
   */
  Optional<List<Codelist>> findAllByNameAndVersion(String name, Long version);

  /**
   * Find all by id in.
   *
   * @param codelistIds the codelist ids
   * @return the optional
   */
  Optional<List<Codelist>> findAllByIdIn(List<Long> codelistIds);
}
