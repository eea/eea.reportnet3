package org.eea.dataflow.persistence.repository;

import org.eea.dataflow.persistence.domain.Document;
import org.springframework.data.repository.CrudRepository;

/**
 * The Interface DocumentRepository.
 */
public interface DocumentRepository extends CrudRepository<Document, Long> {

  /**
   * Find first by dataflow id and name and language.
   *
   * @param dataflowId the dataflow id
   * @param name the name
   * @param language the language
   * @return the document
   */
  Document findFirstByDataflowIdAndNameAndLanguage(final Long dataflowId, final String name,
      final String language);
}
