package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.dataflow.DataFlowController;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.validation.configuration.KieBaseManager;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.persistence.rules.repository.DataFlowRulesRepository;
import org.eea.validation.service.ValidationService;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

/**
 * The Class ValidationService.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);

  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

  /** The data flow rules repository. */
  @Autowired
  private DataFlowRulesRepository dataFlowRulesRepository;


  @Autowired
  private DataFlowController dataFlowController;
  /** The dataset controller. */
  @Autowired
  private DataSetControllerZuul datasetController;

  /**
   * Gets the element lenght.
   *
   * @param dataFlowRules the data flow rules
   * @return the element lenght
   */
  @Override
  public DataSetVO getDataFlowRule(DataSetVO datasetVO, Long DataflowId) {
    KieSession kieSession;
    try {
      kieSession = kieBaseManager.reloadRules(DataflowId).newKieSession();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    // for (DataFlowRule dataFlowRule2 : dataFlowRule) {
    // kieSession.insert(dataFlowRule2);
    // }
    kieSession.insert(datasetVO);
    kieSession.fireAllRules();
    kieSession.dispose();
    return datasetVO;
  }

  /**
   * Gets the rules.
   *
   * @return the rules
   */
  @Override
  public List<Map<String, String>> getRulesByDataFlowId(Long idDataflow) {
    Iterable<DataFlowRule> preRepositoryDB =
        dataFlowRulesRepository.findAllByDataFlowId(idDataflow);
    List<DataFlowRule> preRepository = Lists.newArrayList(preRepositoryDB);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();
    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();
      rule1.put("ruleid", preRepository.get(i).getRuleName());
      ruleAttributes.add(rule1);
    }
    return ruleAttributes;
  }

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void validateDataSetData(Long datasetId) {
    // read Dataset Data
    DataSetVO dataset = datasetController.getById(datasetId);
    // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    // Execute rules validation
    DataSetVO result = runDatasetValidations(dataset, dataflowId);
    // Save results to the db
    datasetController.saveValidations(result);

  }

  /**
   * Run dataset validations.
   *
   * @param datasetVO the dataset VO
   * @param dataflowId the dataflow id
   * @return the data set VO
   */
  private DataSetVO runDatasetValidations(DataSetVO datasetVO, Long dataflowId) {
    datasetVO.setIdDatasetSchema("tralara");
    return datasetVO;
  }


  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  @Override
  public void saveRule(DataFlowRule dataFlowRules) {
    dataFlowRulesRepository.save(dataFlowRules);
  }

}
