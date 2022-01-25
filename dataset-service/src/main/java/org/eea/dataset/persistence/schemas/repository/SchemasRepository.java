/**
 *
 */
package org.eea.dataset.persistence.schemas.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.webform.Webform;
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

  /**
   * Find by id data flow.
   *
   * @param idDataflow the id dataflow
   * @return the list
   */
  List<DataSetSchema> findByIdDataFlow(Long idDataflow);

  /**
   * Find by webform.
   *
   * @param webform the webform
   * @return the list
   */
  List<DataSetSchema> findByWebform(Webform webform);
}
