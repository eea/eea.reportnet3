/**
 *
 */
package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SchemasRepository.
 *
 * @author Mario Severa
 */
public interface RulesRepository extends MongoRepository<RulesSchema, ObjectId> {


  /**
   * Find by id data set schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the data set schema
   */
  RulesSchema findByIdDatasetSchema(ObjectId idDatasetSchema);
}
