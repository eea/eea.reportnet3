package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.DataflowReferencedSchema;
import org.springframework.data.mongodb.repository.MongoRepository;



/**
 * The Interface DataflowReferencedRepository.
 */
public interface DataflowReferencedRepository
    extends MongoRepository<DataflowReferencedSchema, ObjectId> {


  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  DataflowReferencedSchema findByDataflowId(Long dataflowId);


  /**
   * Delete by dataflow id.
   *
   * @param dataflowId the dataflow id
   */
  void deleteByDataflowId(Long dataflowId);
}
