/**
 *
 */
package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * The Interface SchemasRepository.
 *
 * @author Mario Severa
 */
public interface SchemasRepository
    extends MongoRepository<DataSetSchema, ObjectId>, ExtendedSchemaRepository {


  /**
   * Find by id data flow and id data set schema.
   *
   * @param idFlow the id flow
   * @param idDataSetSchema the id data set schema
   * @return the data set schema
   */
  DataSetSchema findByIdDataFlowAndIdDataSetSchema(Long idFlow, ObjectId idDataSetSchema);


  /**
   * Find by id data set schema.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the data set schema
   */
  DataSetSchema findByIdDataSetSchema(ObjectId idDatasetSchema);

}
