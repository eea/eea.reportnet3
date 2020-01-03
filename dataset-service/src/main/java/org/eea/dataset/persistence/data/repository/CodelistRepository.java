package org.eea.dataset.persistence.data.repository;

import org.eea.dataset.persistence.data.domain.Codelist;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * The Interface CodelistRepository.
 */
public interface CodelistRepository extends JpaRepository<Codelist, Long> {
}
