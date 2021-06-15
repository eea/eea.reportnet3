package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.data.repository.FieldValidationRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.RecordValidationRepository;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.data.repository.TableValidationRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.KieBaseManager;
import org.eea.validation.util.RulesErrorUtils;
import org.joda.time.LocalDate;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service("validationService")
public class ValidationServiceImpl implements ValidationService {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The kie base manager.
   */
  @Autowired
  private KieBaseManager kieBaseManager;

  /**
   * The validation record repository.
   */
  @Autowired
  private RecordValidationRepository recordValidationRepository;

  /**
   * The validation dataset repository.
   */
  @Autowired
  private ValidationDatasetRepository validationDatasetRepository;

  /**
   * The validation table repository.
   */
  @Autowired
  private TableValidationRepository tableValidationRepository;

  /**
   * The validation field repository.
   */
  @Autowired
  private FieldValidationRepository validationFieldRepository;

  /**
   * The dataset repository.
   */
  @Autowired
  private DatasetRepository datasetRepository;

  /**
   * The record repository.
   */
  @Autowired
  private RecordRepository recordRepository;

  /**
   * The table repository.
   */
  @Autowired
  private TableRepository tableRepository;

  /**
   * The field repository.
   */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * The schemas repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The user management controller.
   */
  @Autowired
  private ResourceManagementControllerZull resourceManagementController;

  /**
   * The dataset schema controller.
   */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;

  /** The rules error utils. */
  @Autowired
  private RulesErrorUtils rulesErrorUtils;

  /**
   * Gets the element lenght.
   *
   * @param dataset the dataset
   * @param kieSession the kie session
   *
   * @return the element lenght
   */
  @Override
  public List<DatasetValidation> runDatasetValidations(DatasetValue dataset,
      KieSession kieSession) {
    kieSession.insert(dataset);
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG_ERROR.error("The Dataset Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(dataset, e);
    }
    return dataset.getDatasetValidations();
  }

  /**
   * Run table validations.
   *
   * @param table the table
   * @param kieSession the kie session
   *
   * @return the list
   */
  @Override
  public List<TableValidation> runTableValidations(TableValue table, KieSession kieSession) {
    kieSession.insert(table);
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG_ERROR.error("The Table Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(table, e);
    }
    return table.getTableValidations() == null ? new ArrayList<>() : table.getTableValidations();
  }

  /**
   * Run record validations.
   *
   * @param record the record
   * @param kieSession the kie session
   *
   * @return the list
   */
  @Override
  @Transactional
  public List<RecordValidation> runRecordValidations(RecordValue record, KieSession kieSession) {
    if (StringUtils.isNotBlank(record.getIdRecordSchema())) {
      kieSession.insert(record);
    }
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG_ERROR.error("The Record Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(record, e);
    }

    return null == record.getRecordValidations() || record.getRecordValidations().isEmpty()
        ? new ArrayList<>()
        : record.getRecordValidations();
  }

  /**
   * Run field validations.
   *
   * @param field the field
   * @param kieSession the kie session
   *
   * @return the list
   */
  @Override
  public List<FieldValidation> runFieldValidations(FieldValue field, KieSession kieSession) {
    if (StringUtils.isNotBlank(field.getIdFieldSchema())) {
      kieSession.insert(field);
    }
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG_ERROR.error("The Field Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(field, e);
    }
    return null == field.getFieldValidations() || field.getFieldValidations().isEmpty()
        ? new ArrayList<>()
        : field.getFieldValidations();

  }


  /**
   * Load rules knowledge base.
   *
   * @param datasetId the dataset id
   *
   * @return the kie session
   *
   * @throws EEAException the EEA exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   */
  @Override
  public KieBase loadRulesKnowledgeBase(Long datasetId) throws EEAException {
    KieBase kieBase;
    try {
      kieBase = kieBaseManager.reloadRules(datasetId,
          datasetSchemaController.getDatasetSchemaId(datasetId));
    } catch (FileNotFoundException e) {
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND, e);
    } catch (Exception e) {
      LOG_ERROR.error(e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.VALIDATION_SESSION_ERROR, e);
    }
    return kieBase;
  }

  /**
   * Validate data set.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void validateDataSet(Long datasetId, KieBase kieBase) throws EEAException {
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    KieSession session = kieBase.newKieSession();
    try {
      List<DatasetValidation> validations = runDatasetValidations(dataset, session);

      validationDatasetRepository.saveAll(validations);
    } finally {
      session.destroy();
    }

  }

  /**
   * Validate table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @param kieBase the kie base
   */
  @Override
  @Transactional
  public void validateTable(Long datasetId, Long idTable, KieBase kieBase) {
    // Validating tables
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    TableValue table = tableRepository.findById(idTable).orElse(null);
    KieSession session = kieBase.newKieSession();

    try {
      if (table != null) {
        List<TableValidation> validations = runTableValidations(table, session);
        tableValidationRepository.saveAll(validations);
      }
    } finally {
      session.destroy();
    }
  }

  /**
   * Validate record.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param pageable the pageable
   */
  @Override
  @Transactional
  public void validateRecord(Long datasetId, KieBase kieBase, Pageable pageable) {

    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    List<RecordValue> records = recordRepository.findRecordsPageable(pageable);
    List<RecordValidation> recordValidations = new ArrayList<>();
    KieSession session = kieBase.newKieSession();
    try {
      for (RecordValue row : records) {
        runRecordValidations(row, session);
        List<RecordValidation> validations = row.getRecordValidations();
        recordValidations.addAll(validations);
      }
      if (!recordValidations.isEmpty()) {
        TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
        recordValidationRepository.saveAll(recordValidations);
      }
    } finally {
      session.destroy();
    }
  }

  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param pageable the pageable
   * @param onlyEmptyFields the only empty fields
   */
  @Override
  @Transactional
  public void validateFields(Long datasetId, KieBase kieBase, Pageable pageable,
      boolean onlyEmptyFields) {
    TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
    Page<FieldValue> fields = onlyEmptyFields ? fieldRepository.findEmptyFields(pageable)
        : fieldRepository.findAll(pageable);
    List<FieldValidation> fieldValidations = new ArrayList<>();
    KieSession session = kieBase.newKieSession();
    try {
      fields.forEach(field -> {

        List<FieldValidation> resultFields = runFieldValidations(field, session);

        fieldValidations.addAll(resultFields);
      });
      if (!fieldValidations.isEmpty()) {
        TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
        validationFieldRepository.saveAll(fieldValidations);
      }
    } finally {
      session.destroy();
    }
  }

  /**
   * Delete all validation.
   *
   * @param datasetId the dataset id
   */
  @Transactional
  @Override
  public void deleteAllValidation(Long datasetId) {
    datasetRepository.deleteValidationTable();
    initVariablesToValidate(datasetId);
  }

  /**
   * Inits the variables to validate.
   *
   * @param datasetId the dataset id
   */
  private void initVariablesToValidate(Long datasetId) {
    ResourceInfoVO resourceInfoVO = resourceManagementController.getResourceDetail(datasetId,
        ResourceGroupEnum.DATASET_LEAD_REPORTER);
    String countryCode = "''";
    String dataCallYear = "" + new LocalDate().getYear();
    if (null != resourceInfoVO.getAttributes() && resourceInfoVO.getAttributes().size() > 0) {
      if (resourceInfoVO.getAttributes().containsKey(LiteralConstants.COUNTRY_CODE)) {
        countryCode = resourceInfoVO.getAttributes().get(LiteralConstants.COUNTRY_CODE).get(0);
      }

      if (resourceInfoVO.getAttributes().containsKey(LiteralConstants.DATA_CALL_YEAR)) {
        dataCallYear = resourceInfoVO.getAttributes().get(LiteralConstants.DATA_CALL_YEAR).get(0);
      }
    }
    ThreadPropertiesManager.setVariable(LiteralConstants.DATA_CALL_YEAR, dataCallYear);
    ThreadPropertiesManager.setVariable(LiteralConstants.COUNTRY_CODE, countryCode);
  }

  /**
   * Gets the field errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   *
   * @return the field errors
   */
  @Override
  public Future<Map<Long, ErrorsValidationVO>> getFieldErrors(final Long datasetId,
      final List<Long> idValidations) {
    List<FieldValidation> fieldValidations =
        validationFieldRepository.findByValidationIds(idValidations);
    Map<Long, ErrorsValidationVO> errors = new HashMap<>();
    for (FieldValidation fieldValidation : fieldValidations) {

      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(fieldValidation.getFieldValue().getId());
      error.setIdValidation(fieldValidation.getValidation().getId());
      error.setLevelError(fieldValidation.getValidation().getLevelError().name());
      error.setMessage(fieldValidation.getValidation().getMessage());
      error.setNameTableSchema(fieldValidation.getValidation().getTableName());
      error.setIdTableSchema(
          fieldValidation.getFieldValue().getRecord().getTableValue().getIdTableSchema());
      error.setTypeEntity(fieldValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(fieldValidation.getValidation().getValidationDate());
      error.setShortCode(fieldValidation.getValidation().getShortCode());
      error.setNameFieldSchema(fieldValidation.getValidation().getFieldName());

      errors.put(fieldValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
  }


  /**
   * Gets the record errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   *
   * @return the record errors
   */
  @Override
  public Future<Map<Long, ErrorsValidationVO>> getRecordErrors(final Long datasetId,
      final List<Long> idValidations) {
    List<RecordValidation> recordValidations =
        recordValidationRepository.findByValidationIds(idValidations);
    Map<Long, ErrorsValidationVO> errors = new HashMap<>();
    for (RecordValidation recordValidation : recordValidations) {

      ErrorsValidationVO error = new ErrorsValidationVO();
      if (recordValidation.getRecordValue() != null) {
        error.setIdObject(recordValidation.getRecordValue().getId());
        error
            .setIdTableSchema(recordValidation.getRecordValue().getTableValue().getIdTableSchema());
      }
      error.setIdValidation(recordValidation.getValidation().getId());
      error.setLevelError(recordValidation.getValidation().getLevelError().name());
      error.setMessage(recordValidation.getValidation().getMessage());
      error.setNameTableSchema(recordValidation.getValidation().getTableName());
      error.setTypeEntity(recordValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(recordValidation.getValidation().getValidationDate());
      error.setShortCode(recordValidation.getValidation().getShortCode());

      errors.put(recordValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
  }


  /**
   * Gets the table errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
   *
   * @return the table errors
   */
  @Override
  public Future<Map<Long, ErrorsValidationVO>> getTableErrors(final Long datasetId,
      final List<Long> idValidations) {
    List<TableValidation> tableValidations =
        tableValidationRepository.findByValidationIds(idValidations);
    Map<Long, ErrorsValidationVO> errors = new HashMap<>();
    for (TableValidation tableValidation : tableValidations) {

      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(tableValidation.getTableValue().getId().toString());
      error.setIdValidation(tableValidation.getValidation().getId());
      error.setLevelError(tableValidation.getValidation().getLevelError().name());
      error.setMessage(tableValidation.getValidation().getMessage());
      error.setNameTableSchema(tableValidation.getValidation().getTableName());
      error.setIdTableSchema(tableValidation.getTableValue().getIdTableSchema());
      error.setTypeEntity(tableValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(tableValidation.getValidation().getValidationDate());
      error.setShortCode((tableValidation.getValidation().getShortCode()));

      errors.put(tableValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
  }


  /**
   * Gets the dataset errors.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   * @param idValidations the id validations
   *
   * @return the dataset errors
   */
  @Override
  public Future<Map<Long, ErrorsValidationVO>> getDatasetErrors(final Long datasetId,
      final DatasetValue dataset, final List<Long> idValidations) {
    Map<Long, ErrorsValidationVO> errors = new HashMap<>();
    List<DatasetValidation> datasetValidations =
        validationDatasetRepository.findByValidationIds(idValidations);
    for (DatasetValidation datasetValidation : datasetValidations) {
      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(datasetValidation.getDatasetValue().getId().toString());
      error.setIdValidation(datasetValidation.getValidation().getId());
      error.setLevelError(datasetValidation.getValidation().getLevelError().name());
      error.setMessage(datasetValidation.getValidation().getMessage());
      error.setNameTableSchema(datasetValidation.getValidation().getTableName());
      error.setIdTableSchema(dataset.getIdDatasetSchema());
      error.setTypeEntity(datasetValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(datasetValidation.getValidation().getValidationDate());
      error.setShortCode(datasetValidation.getValidation().getShortCode());

      errors.put(datasetValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
  }

  /**
   * Gets the dataset valueby id.
   *
   * @param datasetId the dataset id
   *
   * @return the dataset valueby id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public DatasetValue getDatasetValuebyId(final Long datasetId) throws EEAException {
    if (datasetId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return datasetRepository.findById(datasetId).orElse(new DatasetValue());
  }

  /**
   * Gets the find by id data set schema.
   *
   * @param datasetId the dataset id
   * @param datasetSchemaId the dataset schema id
   *
   * @return the find by id data set schema
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public DataSetSchema getfindByIdDataSetSchema(final Long datasetId,
      final ObjectId datasetSchemaId) throws EEAException {
    if (datasetId == null || datasetSchemaId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return schemasRepository.findByIdDataSetSchema(datasetSchemaId);
  }


  /**
   * Force validations.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void forceValidations(Long datasetId) {
    kafkaSenderUtils.releaseDatasetKafkaEvent(EventType.COMMAND_EXECUTE_VALIDATION, datasetId);
  }

  /**
   * Count records dataset.
   *
   * @param datasetId the dataset id
   *
   * @return the integer
   */
  @Override
  public Integer countRecordsDataset(Long datasetId) {
    return recordRepository.countRecordsDataset();
  }

  /**
   * Count fields dataset.
   *
   * @param datasetId the dataset id
   *
   * @return the integer
   */
  @Override
  public Integer countFieldsDataset(Long datasetId) {
    return fieldRepository.countFieldsDataset();
  }

  /**
   * Count empty fields dataset.
   *
   * @param datasetId the dataset id
   * @return the integer
   */
  @Override
  public Integer countEmptyFieldsDataset(Long datasetId) {
    return fieldRepository.countEmptyFieldsDataset();
  }

}
