package org.eea.validation.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.validation.persistence.rules.DataFlowRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.internal.utils.KieHelper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KieBaseManagerTest {

  @InjectMocks
  private KieBaseManager kieBaseManager;
  @Mock
  private KieHelper kieHelper;

  @Test
  public void testKieBaseManager() throws FileNotFoundException {

    List<DataFlowRule> list = new ArrayList<DataFlowRule>();
    DataFlowRule rule = new DataFlowRule();
    rule.setRuleScope(TypeEntityEnum.DATASET);
    rule.setRuleId(new ObjectId());
    rule.setWhenCondition("1234");
    List<String> listsString = new ArrayList<String>();
    listsString.add("ERROR");
    listsString.add("ERROR");
    rule.setThenCondition(listsString);
    list.add(rule);

    DataFlowRule rule2 = new DataFlowRule();
    rule2.setRuleScope(TypeEntityEnum.TABLE);
    rule2.setRuleId(new ObjectId());
    rule2.setWhenCondition("1234");
    rule2.setThenCondition(listsString);
    list.add(rule2);

    DataFlowRule rule3 = new DataFlowRule();
    rule3.setRuleScope(TypeEntityEnum.FIELD);
    rule3.setRuleId(new ObjectId());
    rule3.setWhenCondition("1234");
    rule3.setThenCondition(listsString);
    list.add(rule3);

    DataFlowRule rule4 = new DataFlowRule();
    rule4.setRuleScope(TypeEntityEnum.RECORD);
    rule4.setRuleId(new ObjectId());
    rule4.setWhenCondition("1234");
    rule4.setThenCondition(listsString);
    list.add(rule4);
    // when(dataFlowRulesRepository.findAllByDataFlowId(Mockito.any())).thenReturn(list);
    // kieBaseManager.reloadRules(1L);
  }

}
