package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Document;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DocumentRepository.
 */
public interface DocumentRepository extends CrudRepository<Document, Long> {
}
