package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.springframework.data.mongodb.repository.MongoRepository;



/**
 * The Interface WebformConfigRepository.
 */
public interface WebformConfigRepository
    extends MongoRepository<WebformConfig, ObjectId>, ExtendedWebformConfigRepository {

  /**
   * Find by id referenced.
   *
   * @param id the id
   * @return the webform config
   */
  WebformConfig findByIdReferenced(Long id);

}
