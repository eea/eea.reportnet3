package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.rules.model.DataFlowRule;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.KieBaseManager;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service("validationService")
public class ValidationServiceImpl implements ValidationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);

  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

  /** The data flow rules repository. */

  @Autowired
  private KafkaSender kafkaSender;


  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

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
  public DatasetValue runDatasetValidations(DatasetValue dataset, Long DataflowId) {
    KieSession kieSession;
    System.err.println(System.currentTimeMillis());
    try {
      kieSession = kieBaseManager.reloadRules(DataflowId).newKieSession();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
    System.err.println(System.currentTimeMillis());
    kieSession.insert(dataset);
    // for (TableVO tableData : datasetVO.getTableVO()) {
    // kieSession.insert(tableData);
    // for (RecordVO recordData : tableData.getRecords()) {
    // kieSession.insert(recordData);
    // for (FieldVO FieldData : recordData.getFields()) {
    // kieSession.insert(FieldData);
    // }
    // }
    // }
    kieSession.fireAllRules();
    kieSession.dispose();
    System.err.println(System.currentTimeMillis());
    return dataset;
  }

  /**
   * Gets the rules.
   *
   * @return the rules
   */
  @Override
  public List<Map<String, String>> getRulesByDataFlowId(Long idDataflow) {


    return null;
  }

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void validateDataSetData(Long datasetId) {
    // read Dataset Data
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
    // // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    // Execute rules validation
    DatasetValue result = runDatasetValidations(dataset, dataflowId);
    // Save results to the db
    // datasetController.saveValidations(result);
    datasetRepository.saveAndFlush(dataset);

    releaseKafkaEvent(kafkaSender, EventType.VALIDATION_FINISHED_EVENT, dataset.getId());

  }

  /**
   * Release kafka event.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  private static void releaseKafkaEvent(final KafkaSender kafkaSender, final EventType eventType,
      final Long datasetId) {

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }

  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  @Override
  public void saveRule(DataFlowRule dataFlowRules) {

  }

}
