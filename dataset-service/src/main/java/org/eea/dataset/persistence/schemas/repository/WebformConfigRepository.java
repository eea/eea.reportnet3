package org.eea.dataset.persistence.schemas.repository;

import org.bson.types.ObjectId;
import org.eea.dataset.persistence.schemas.domain.webform.WebformConfig;
import org.springframework.data.mongodb.repository.MongoRepository;



public interface WebformConfigRepository extends MongoRepository<WebformConfig, ObjectId> {

  WebformConfig findByIdReferenced(Long id);

}
