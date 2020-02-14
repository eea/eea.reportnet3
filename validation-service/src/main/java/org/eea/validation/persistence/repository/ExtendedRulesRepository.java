package org.eea.validation.persistence.repository;

import org.bson.types.ObjectId;

/**
 * The Interface ExtendedRulesRepository.
 */
public interface ExtendedRulesRepository {


  void deleteByIdDatasetSchema(ObjectId rulesSchemaId);
}
