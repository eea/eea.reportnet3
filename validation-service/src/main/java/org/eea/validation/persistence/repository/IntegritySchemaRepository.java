/**
 *
 */
package org.eea.validation.persistence.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.IntegritySchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * The Interface SchemasRepository.
 */
public interface IntegritySchemaRepository extends MongoRepository<IntegritySchema, ObjectId> {

  /**
   * Find by origin or reference fields.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  @Query("{'$or': [{'originFields' : ?0 }, {'referencedFields' : ?0 }]}")
  List<IntegritySchema> findByOriginOrReferenceFields(ObjectId idFieldSchema);

  /**
   * Find by origin or reference dataset schema id.
   *
   * @param idFieldSchema the id field schema
   * @return the list
   */
  @Query("{'$or': [{'originDatasetSchemaId' : ?0 }, {'referencedDatasetSchemaId' : ?0 }]}")
  List<IntegritySchema> findByOriginOrReferenceDatasetSchemaId(ObjectId idFieldSchema);


  /**
   * Find by origin dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  @Query("{'$or': [{'originDatasetSchemaId' : ?0 }]}")
  List<IntegritySchema> findByOriginDatasetSchemaId(ObjectId datasetSchemaId);

  /**
   * Delete by origin dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   */
  void deleteByOriginDatasetSchemaId(ObjectId datasetSchemaId);
}
