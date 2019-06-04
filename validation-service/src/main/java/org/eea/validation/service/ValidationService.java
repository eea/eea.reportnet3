package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import org.eea.validation.persistence.rules.model.DataFlowRules;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service
public interface ValidationService {

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
  DataFlowRules getDataFlowRule(DataFlowRules dataFlowRules);

  /**
   * Gets the rules.
   *
   * @return the rules
   */
  List<Map<String, String>> getRules();


  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  void saveRule(DataFlowRules dataFlowRules);
}
