package org.eea.dataset.persistence.metabase.repository;

import org.eea.dataset.persistence.metabase.domain.CodelistCategory;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * The Interface CodelistRepository.
 */
@Deprecated
public interface CodelistCategoryRepository extends JpaRepository<CodelistCategory, Long> {
}
