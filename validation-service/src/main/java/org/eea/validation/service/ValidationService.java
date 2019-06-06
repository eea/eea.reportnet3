package org.eea.validation.service;


import java.util.List;
import java.util.Map;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service
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
  DataFlowRule getDataFlowRule(List<DataFlowRule> dataFlowRules);

  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  void saveRule(DataFlowRule dataFlowRules);
}
