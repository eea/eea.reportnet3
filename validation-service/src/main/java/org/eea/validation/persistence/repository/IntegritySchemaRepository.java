/**
 *
 */
package org.eea.validation.persistence.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SchemasRepository.
 */
public interface IntegritySchemaRepository
    extends MongoRepository<IntegritySchema, ObjectId>, ExtendedIntegritySchemaRepository {

  /**
   * Find by id dataset schema.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the rules schema
   */
  List<IntegritySchema> findByOriginDatasetId(Long originDatasetId);

  /**
   * Find by referenced dataset schema id.
   *
   * @param referencedDatasetSchemaId the referenced dataset schema id
   * @return the integrity schema
   */
  IntegritySchema findByReferencedDatasetId(Long referencedDatasetId);


}
