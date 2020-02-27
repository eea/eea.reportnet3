/**
 *
 */
package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SchemasRepository.
 */
public interface RulesRepository
    extends MongoRepository<RulesSchema, ObjectId>, ExtendedRulesRepository {


  /**
   * Find by id dataset schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema
   */
  RulesSchema findByIdDatasetSchema(ObjectId idDatasetSchema);



  /**
   * Count rules by id dataset schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the integer
   */
  Integer countRulesByIdDatasetSchema(ObjectId idDatasetSchema);
}
