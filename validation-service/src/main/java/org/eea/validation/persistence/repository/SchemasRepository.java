/**
 *
 */
package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * The Interface SchemasRepository.
 */
public interface SchemasRepository
    extends MongoRepository<DataSetSchema, ObjectId>, ExtendedSchemaRepository {

  /**
   * Find by id data set schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the data set schema
   */
  DataSetSchema findByIdDataSetSchema(ObjectId idDatasetSchema);

}
