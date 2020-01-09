package org.eea.dataset.persistence.metabase.repository;

import java.util.List;
import java.util.Optional;
import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * The Interface CodelistRepository.
 */
public interface CodelistRepository extends JpaRepository<Codelist, Long> {

  Optional<List<Codelist>> findAllByNameAndVersion(String name, Long version);
}
