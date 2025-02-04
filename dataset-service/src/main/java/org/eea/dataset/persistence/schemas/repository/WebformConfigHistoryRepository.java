package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfigHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebformConfigHistoryRepository extends MongoRepository<WebformConfigHistory, ObjectId> {
  // Find a specific version by webFormId and version number
  Optional<WebformConfigHistory> findByIdWebformConfigSchemaAndVersion(ObjectId idWebformConfigSchema, Long version);

  // Find the latest version
  Optional<WebformConfigHistory> findFirstByIdWebformConfigSchemaOrderByVersionDesc(ObjectId idWebformConfigSchema);
}

