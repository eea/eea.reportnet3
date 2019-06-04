package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.configuration.KieBaseManager;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.persistence.rules.model.RuleScope;
import org.eea.validation.persistence.rules.repository.DataFlowRulesRepository;
import org.eea.validation.service.ValidationService;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

/**
 * The Class ValidationService.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

  /** The kafka sender. */
  @Autowired
  private KafkaSender kafkaSender;

  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

  /** The data flow rules repository. */
  @Autowired
  private DataFlowRulesRepository dataFlowRulesRepository;

  /**
   * Gets the element lenght.
   *
   * @param dataFlowRules the data flow rules
   * @return the element lenght
   */
  @Override
  public DataFlowRule getDataFlowRule(DataFlowRule dataFlowRules) {
    KieSession kieSession;
    try {
      kieSession = kieBaseManager.reloadRules(1L).newKieSession();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    kieSession.insert(dataFlowRules);
    kieSession.fireAllRules();
    kieSession.dispose();
    return dataFlowRules;
  }


  @Override
  public List<Map<String, String>> getRules() {
    Iterable<DataFlowRule> preRepositoryDB = dataFlowRulesRepository.findAll();
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

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(EventType.VALIDATION_FINISHED_EVENT);
    final Map<String, Object> dataOutput = new HashMap<>();
    // TODO ADD DATA TO MAP
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }


  @Override
  public void saveRule(DataFlowRule dataFlowRules) {
    dataFlowRules.setDataFlowId(1L);
    dataFlowRules.setRuleId(new ObjectId());
    dataFlowRules.setRuleName("nombre regla");
    dataFlowRules.setRuleScope(RuleScope.DATASET);
    dataFlowRules.setThenCondition("thencondition");
    dataFlowRules.setWhenCondition("whencondition");
    dataFlowRulesRepository.save(dataFlowRules);
  }


}
