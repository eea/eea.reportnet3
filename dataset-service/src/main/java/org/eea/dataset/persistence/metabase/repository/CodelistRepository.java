package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.Codelist;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * The Interface CodelistRepository.
 */
public interface CodelistRepository extends JpaRepository<Codelist, Long> {
}
