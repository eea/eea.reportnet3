package org.eea.validation.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.template.ObjectDataCompiler;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.persistence.rules.repository.DataFlowRulesRepository;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

@Component
public class KieBaseManager {

  private static final String REGULATION_TEMPLATE_FILE = "src/main/resources/template01.drl";

  private KieBase kieBase;

  @Autowired
  private DataFlowRulesRepository dataFlowRulesRepository;

  /**
   * Reload rules.
   *
   * @param dataflowId idDataflow to know the rules associates to the dataflow
   * @return Kiebase session object
   * @throws FileNotFoundException the file not found exception
   */
  public KieBase reloadRules(Long dataFlowId) throws FileNotFoundException {

    // Iterable<DataFlowRule> preRepositoryDB = dataFlowRulesRepository.findAll();
    Iterable<DataFlowRule> preRepositoryDB =
        dataFlowRulesRepository.findAllByDataFlowId(dataFlowId);
    List<DataFlowRule> preRepository = Lists.newArrayList(preRepositoryDB);
    List<Map<String, String>> ruleAttributes = new ArrayList<>();

    String LVTypeValidation = null;
    for (int i = 0; i < preRepository.size(); i++) {
      Map<String, String> rule1 = new HashMap<>();
      rule1.put("ruleid", preRepository.get(i).getRuleId().toString());
      switch (preRepository.get(i).getRuleScope()) {
        case DATASET:
          LVTypeValidation = "DataSetVO";
          break;
        case FIELD:
          LVTypeValidation = "FieldVO";
          break;
        case RECORD:
          LVTypeValidation = "RecordVO";
          break;
        case TABLE:
          LVTypeValidation = "TableVO";
          break;
        default:
          LVTypeValidation = "DataFlowRule";
          break;
      }
      rule1.put("typevalidation", LVTypeValidation);
      rule1.put("whencondition", preRepository.get(i).getWhenCondition().trim());
      rule1.put("condition1", preRepository.get(i).getThenCondition().get(0));
      rule1.put("condition2", preRepository.get(i).getThenCondition().get(1));
      ruleAttributes.add(rule1);
    }

    ObjectDataCompiler compiler = new ObjectDataCompiler();

    String generatedDRL =
        compiler.compile(ruleAttributes, new FileInputStream(REGULATION_TEMPLATE_FILE));

    KieServices kieServices = KieServices.Factory.get();

    KieHelper kieHelper = new KieHelper();

    // multiple such resoures/rules can be added
    byte[] b1 = generatedDRL.getBytes();
    Resource resource1 = kieServices.getResources().newByteArrayResource(b1);
    kieHelper.addResource(resource1, ResourceType.DRL);
    // this is a shared variable in a single instanced object.
    KieBase newBase = kieHelper.build();
    this.kieBase = newBase;
    return this.kieBase;
  }

  public static void fillValidation(DataSetVO dataSetVO, String message, String typeError,
      String ruleId) {

    ValidationVO newValidation = new ValidationVO();
    newValidation.setLevelError(
        typeError.equalsIgnoreCase("warning") ? TypeErrorEnum.WARNING : TypeErrorEnum.ERROR);
    newValidation.setMessage(message);
    newValidation.setTypeEntity(TypeEntityEnum.DATASET);
    newValidation.setIdRule(ruleId);
    Date hoy = new Date();
    newValidation.setValidationDate(hoy.toString());
    dataSetVO.getValidations().add(newValidation);
    System.err.println(dataSetVO.getClass());
  }

  public static void fillValidation(TableVO tableVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation = new ValidationVO();
    newValidation.setLevelError(
        typeError.equalsIgnoreCase("warning") ? TypeErrorEnum.WARNING : TypeErrorEnum.ERROR);
    newValidation.setMessage(message);
    newValidation.setTypeEntity(TypeEntityEnum.TABLE);
    newValidation.setIdRule(ruleId);
    Date hoy = new Date();
    newValidation.setValidationDate(hoy.toString());
    tableVO.getValidations().add(newValidation);
    System.err.println(tableVO.getClass());
  }

  public static void fillValidation(FieldVO fieldVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation = new ValidationVO();
    newValidation.setLevelError(
        typeError.equalsIgnoreCase("warning") ? TypeErrorEnum.WARNING : TypeErrorEnum.ERROR);
    newValidation.setMessage(message);
    newValidation.setTypeEntity(TypeEntityEnum.FIELD);
    newValidation.setIdRule(ruleId);
    Date hoy = new Date();
    newValidation.setValidationDate(hoy.toString());
    fieldVO.getValidations().add(newValidation);
    System.err.println(fieldVO.getClass());
  }

  public static void fillValidation(RecordVO recordVO, String message, String typeError,
      String ruleId) {
    ValidationVO newValidation = new ValidationVO();
    newValidation.setLevelError(
        typeError.equalsIgnoreCase("warning") ? TypeErrorEnum.WARNING : TypeErrorEnum.ERROR);
    newValidation.setMessage(message);
    newValidation.setTypeEntity(TypeEntityEnum.RECORD);
    newValidation.setIdRule(ruleId);
    Date hoy = new Date();
    newValidation.setValidationDate(hoy.toString());
    recordVO.getValidations().add(newValidation);
    System.err.println(recordVO.getClass());
  }

}
