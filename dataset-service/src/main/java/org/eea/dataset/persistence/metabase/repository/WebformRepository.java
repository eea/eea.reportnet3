package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.WebformMetabase;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The Interface WebformRepository.
 */
public interface WebformRepository extends JpaRepository<WebformMetabase, Long> {
}
