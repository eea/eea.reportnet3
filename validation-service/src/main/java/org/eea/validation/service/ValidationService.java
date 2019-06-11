package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.rules.model.DataFlowRule;

/**
 * The Class ValidationService.
 */
public interface ValidationService {


  /**
   * Gets the rules by data flow id.
   *
   * @param idDataflow the id dataflow
   * @return the rules by data flow id
   */
  List<Map<String, String>> getRulesByDataFlowId(Long idDataflow);

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  void validateDataSetData(Long datasetId);


  /**
   * Gets the element lenght.
   *
   * @param dataFlowRules the data flow rules
   * @return the element lenght
   */
  DatasetValue runDatasetValidations(DatasetValue dataset, Long DataflowId);

  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  void saveRule(DataFlowRule dataFlowRules);

}
