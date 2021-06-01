package org.eea.dataset.persistence.schemas.repository;

import java.util.List;
import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * The Interface PkCatalogueRepository.
 */
public interface PkCatalogueRepository extends MongoRepository<PkCatalogueSchema, ObjectId> {

  /**
   * Find by id pk.
   *
   * @param idPk the id pk
   * @return the pk catalogue schema
   */
  PkCatalogueSchema findByIdPk(ObjectId idPk);

  /**
   * Delete by id pk.
   *
   * @param idPk the id pk
   */
  void deleteByIdPk(ObjectId idPk);

  /**
   * Find by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  List<PkCatalogueSchema> findByDataflowId(Long dataflowId);
}
