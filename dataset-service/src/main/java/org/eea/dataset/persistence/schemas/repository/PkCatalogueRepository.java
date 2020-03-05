package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * The Interface PkCatalogueRepository.
 */
public interface PkCatalogueRepository extends MongoRepository<PkCatalogueSchema, ObjectId> {



}
