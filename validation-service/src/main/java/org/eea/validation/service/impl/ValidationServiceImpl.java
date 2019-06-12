package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.multitenancy.DatasetId;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.data.repository.ValidationFieldRepository;
import org.eea.validation.persistence.data.repository.ValidationRecordRepository;
import org.eea.validation.persistence.data.repository.ValidationTableRepository;
import org.eea.validation.persistence.rules.DataFlowRule;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.KieBaseManager;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service("validationService")
public class ValidationServiceImpl implements ValidationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);
  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

  /** The validation record repository. */
  @Autowired
  private ValidationRecordRepository validationRecordRepository;

  /** The validation dataset repository. */
  @Autowired
  private ValidationDatasetRepository validationDatasetRepository;

  /** The validation table repository. */
  @Autowired
  private ValidationTableRepository validationTableRepository;

  /** The validation field repository. */
  @Autowired
  private ValidationFieldRepository validationFieldRepository;

  /** The kafka sender. */
  @Autowired
  private KafkaSender kafkaSender;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The dataset controller. */
  @Autowired
  private DataSetControllerZuul datasetController;

  /** The kie session. */
  private KieSession kieSession;

  /**
   * Gets the element lenght.
   *
   * @param dataset the dataset
   * @return the element lenght
   */
  @Override
  public List<DatasetValidation> runDatasetValidations(DatasetValue dataset) {
    kieSession.insert(dataset);
    kieSession.fireAllRules();
    return dataset.getDatasetValidations();
  }

  /**
   * Run table validations.
   *
   * @param tableValues the table values
   * @return the list
   */
  @Override
  public List<TableValidation> runTableValidations(List<TableValue> tableValues) {
    tableValues.stream().forEach(table -> kieSession.insert(table));
    kieSession.fireAllRules();
    return tableValues.isEmpty() ? new ArrayList<TableValidation>()
        : tableValues.get(0).getTableValidations();
  }

  /**
   * Run record validations.
   *
   * @param recordsPaged the records paged
   * @return the list
   */
  @Override
  public List<RecordValidation> runRecordValidations(List<RecordValue> records) {
    records.stream().forEach(record -> kieSession.insert(record));
    kieSession.fireAllRules();
    return records.isEmpty() ? new ArrayList<RecordValidation>()
        : records.get(0).getRecordValidations();
  }

  /**
   * Run field validations.
   *
   * @param fields the fields
   * @return the list
   */
  @Override
  public List<FieldValidation> runFieldValidations(List<FieldValue> fields) {
    fields.stream().forEach(field -> kieSession.insert(field));
    kieSession.fireAllRules();
    return fields.isEmpty() ? new ArrayList<FieldValidation>()
        : fields.get(0).getFieldValidations();
  }


  /**
   * Load rules knowledge base.
   *
   * @param DataflowId the dataflow id
   * @return the kie session
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   */
  public KieSession loadRulesKnowledgeBase(Long DataflowId) {
    try {
      kieSession = kieBaseManager.reloadRules(DataflowId).newKieSession();
    } catch (FileNotFoundException e) {
      LOG_ERROR.error(e.getMessage(), e);
      return null;
    }

    return kieSession;
  }

  /**
   * Gets the rules.
   *
   * @param idDataflow the id dataflow
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
  public void validateDataSetData(@DatasetId Long datasetId) {
    // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    loadRulesKnowledgeBase(dataflowId);
    // We delete all validation to delete before pass the new validations
    deleteAllValidation();
    // Dataset and TablesValue validations
    // read Dataset Data
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
    // Execute rules validation

    List<DatasetValidation> resultDataset = runDatasetValidations(dataset);
    // Save results to the db
    validationDatasetRepository.saveAll((Iterable<DatasetValidation>) resultDataset);

    List<TableValidation> resultTable = runTableValidations(dataset.getTableValues());
    // Save results to the db
    validationTableRepository.saveAll((Iterable<TableValidation>) resultTable);

    // Records validation
    for (TableValue tableValue : dataset.getTableValues()) {
      int i = 1;
      String tableIdTableSchema = tableValue.getIdTableSchema();
      Pageable pageable = PageRequest.of(i, 100);
      // read Dataset Data
      List<RecordValue> records = recordRepository.findRecordsPaged(tableIdTableSchema, pageable);

      while (records.size() != 0) {
        // Execute record rules validation
        List<RecordValidation> resultRecord = runRecordValidations(records);
        // Save results to the db
        validationRecordRepository.saveAll((Iterable<RecordValidation>) resultRecord);
        for (RecordValue record : records) {

          // Execute field rules validation
          List<FieldValidation> resultField = runFieldValidations(record.getFields());
          // Save results to the db
          validationFieldRepository.saveAll((Iterable<FieldValidation>) resultField);
        }
        pageable = PageRequest.of(i++, 100);
        records = recordRepository.findRecordsPaged(tableIdTableSchema, pageable);
      }
    }


    // release kafka event to notify that the dataset validations have been executed
    releaseKafkaEvent(kafkaSender, EventType.VALIDATION_FINISHED_EVENT, dataset.getId());
  }



  /**
   * Release kafka event.
   *
   * @param kafkaSender the kafka sender
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

  private void deleteAllValidation() {
    datasetRepository.deleteValidationTable();
  }

  /**
   * Save rule.
   *
   * @param dataFlowRules the data flow rules
   */
  @Override
  public void saveRule(DataFlowRule dataFlowRules) {}

}
