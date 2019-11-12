/**
 *
 */
package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * The Interface SchemasRepository.
 *
 * @author Mario Severa
 */
public interface SchemasRepository
    extends MongoRepository<DataSetSchema, ObjectId>, ExtendedSchemaRepository {

  /**
   * Find schema by id flow.
   *
   * @param idFlow the id flow
   * @return the data set schema
   */
  @Query("{'idDataFlow': ?0}")
  DataSetSchema findSchemaByIdFlow(Long idFlow);


  /**
   * Find by id data set schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the data set schema
   */
  DataSetSchema findByIdDataSetSchema(ObjectId idDatasetSchema);
}
