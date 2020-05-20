package org.eea.dataset.persistence.schemas.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * The Interface UniqueConstraintRepository.
 */
public interface UniqueConstraintRepository
    extends MongoRepository<UniqueConstraintSchema, ObjectId> {


  /**
   * Find by dataset schema id.
   *
   * @param datasetSchemaId the dataset schema id
   * @return the list
   */
  List<UniqueConstraintSchema> findByDatasetSchemaId(ObjectId datasetSchemaId);

  /**
   * Delete by unique id.
   *
   * @param id the id
   * @return true, if successful
   */
  Long deleteByUniqueId(ObjectId id);

  /**
   * Update.
   *
   * @param uniqueConstraint the unique constraint
   * @return the boolean
   */
  Long update(UniqueConstraintSchema uniqueConstraint);

}
