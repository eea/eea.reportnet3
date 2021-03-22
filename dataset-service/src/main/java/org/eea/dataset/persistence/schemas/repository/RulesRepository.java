/**
 *
 */
package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.rule.RulesSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface RulesRepository.
 */
public interface RulesRepository extends MongoRepository<RulesSchema, ObjectId> {


  /**
   * Find by id dataset schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema
   */
  RulesSchema findByIdDatasetSchema(ObjectId idDatasetSchema);


}
