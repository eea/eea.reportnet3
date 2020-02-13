package org.eea.validation.service;

import org.bson.types.ObjectId;

/**
 * The Class ValidationService.
 */
public interface RulesService {

  void createEmptyRulesScehma(ObjectId schemaId, ObjectId ruleSchemaId);

}
