package org.eea.validation.service;

import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;

/**
 * The Class ValidationService.
 */
public interface RulesService {

  /**
   * Gets the rules schema by dataset id.
   *
   * @param idDatasetSchema the dataset id
   * @return the rules schema by dataset id
   */
  RulesSchemaVO getRulesSchemaByDatasetId(String idDatasetSchema);

}
