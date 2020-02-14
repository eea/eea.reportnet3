package org.eea.validation.service;

import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;

/**
 * The Class ValidationService.
 */
public interface RulesService {

  /**
   * Creates the empty rules scehma.
   *
   * @param schemaId the schema id
   * @param ruleSchemaId the rule schema id
   */
  void createEmptyRulesSchema(ObjectId schemaId, ObjectId ruleSchemaId);

  /**
   * Gets the rules schema by dataset id.
   *
   * @param idDatasetSchema the dataset id
   * @return the rules schema by dataset id
   */
  RulesSchemaVO getRulesSchemaByDatasetId(String idDatasetSchema);



  /**
   * Creates the empty rules scehma.
   *
   * @param schemaId the schema id
   */
  void deleteEmptyRulesScehma(ObjectId schemaId);

}
