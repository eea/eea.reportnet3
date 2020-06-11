/**
 *
 */
package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SchemasRepository.
 */
public interface IntegritySchemaRepository
    extends MongoRepository<IntegritySchema, ObjectId>, ExtendedIntegritySchemaRepository {

}
