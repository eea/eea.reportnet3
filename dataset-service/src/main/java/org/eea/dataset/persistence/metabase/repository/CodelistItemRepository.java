package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.CodelistItem;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * The Interface CodelistRepository.
 */
@Deprecated
public interface CodelistItemRepository extends JpaRepository<CodelistItem, Long> {
}
