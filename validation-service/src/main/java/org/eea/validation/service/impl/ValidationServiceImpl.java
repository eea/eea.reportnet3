package org.eea.validation.service.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.persistance.rules.model.DataFlowRules;
import org.eea.validation.persistance.rules.repository.DataFlowRulesRepository;
import org.eea.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);

  @Autowired
  private KafkaSender kafkaSender;

  @Autowired
  private DatasetController datasetController;

  @Autowired
  private DataFlowRulesRepository dataFlowRulesRepository;

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void validateDataSetData(Long datasetId) {
    // read Dataset Data
    DataSetVO dataset = datasetController.getById(datasetId);
    // Read Dataset rules
    List<DataFlowRules> rules = dataFlowRulesRepository.findAll();
    // Execute rules validation
    DataSetVO result = runDatasetValidations(dataset, rules);
    // Save results to the db
    datasetController.updateDataset(result);
    // Release notification event
    releaseKafkaEvent(EventType.VALIDATION_FINISHED_EVENT, datasetId);
    LOG.info("Dataset validated");
  }

  /**
   * Release kafka event.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  private void releaseKafkaEvent(final EventType eventType, final Long datasetId) {

    final EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    final Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }

  private DataSetVO runDatasetValidations(DataSetVO datasetVO, List<DataFlowRules> rules) {
    return datasetVO;
  }
}
