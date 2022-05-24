package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.audit.Audit;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface AuditRepository.
 */
public interface AuditRepository extends MongoRepository<Audit, ObjectId>, ExtendedAuditRepository {

  /**
   * Delete by dataset id.
   *
   * @param datasetId the dataset id
   */
  void deleteByDatasetId(Long datasetId);

}
