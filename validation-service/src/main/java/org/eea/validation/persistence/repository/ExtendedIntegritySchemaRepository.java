package org.eea.validation.persistence.repository;

import java.util.List;
import org.eea.validation.persistence.schemas.IntegritySchema;

/**
 * The Interface ExtendedIntegritySchemaRepository.
 */
public interface ExtendedIntegritySchemaRepository {


  /**
   * Find byid field schema orig or dest.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  List<IntegritySchema> findByidFieldSchemaOrigOrDest(String idFieldSchema);
}
