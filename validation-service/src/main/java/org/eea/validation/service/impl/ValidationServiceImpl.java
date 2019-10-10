package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.ums.UserManagementController;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.validation.persistence.data.domain.DatasetValidation;
import org.eea.validation.persistence.data.domain.DatasetValue;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.persistence.data.domain.TableValidation;
import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.data.domain.Validation;
import org.eea.validation.persistence.data.repository.DatasetRepository;
import org.eea.validation.persistence.data.repository.DatasetRepositoryImpl;
import org.eea.validation.persistence.data.repository.FieldRepository;
import org.eea.validation.persistence.data.repository.FieldValidationRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.RecordValidationRepository;
import org.eea.validation.persistence.data.repository.RecordValidationRepository.EntityErrors;
import org.eea.validation.persistence.data.repository.TableRepository;
import org.eea.validation.persistence.data.repository.TableValidationQuerysDroolsRepository;
import org.eea.validation.persistence.data.repository.TableValidationRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.KieBaseManager;
import org.joda.time.LocalDate;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * The schemas repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * The table validation querys drools repository.
   */
  @Autowired
  private TableValidationQuerysDroolsRepository tableValidationQuerysDroolsRepository;

  /**
   * The kafka sender utils.
   */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /**
   * The user management controller.
   */
  @Autowired
  private UserManagementController userManagementController;

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
    kieSession.fireAllRules();
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
    kieSession.fireAllRules();
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
    kieSession.fireAllRules();

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
    kieSession.fireAllRules();
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
      kieBase = kieBaseManager.reloadRules(datasetId);
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
    TenantResolver.setTenantName("dataset_" + datasetId);
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    KieSession session = kieBase.newKieSession();
    try {
      List<DatasetValidation> validations = runDatasetValidations(dataset, session);

      validations.stream().forEach(validation -> validation.setDatasetValue(dataset));
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
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void validateTable(Long datasetId, Long idTable, KieBase kieBase) throws EEAException {
    // Validating tables
    TenantResolver.setTenantName("dataset_" + datasetId);
    TableValue table = tableRepository.findById(idTable).orElse(null);
    // dataset.getTableValues().stream().forEach(table -> {
    KieSession session = kieBase.newKieSession();
    try {
      List<TableValidation> validations = runTableValidations(table, session);
      if (table.getTableValidations() != null) {
        table.getTableValidations().stream().filter(Objects::nonNull).forEach(tableValidation -> {
          tableValidation.setTableValue(table);
        });
      }
      tableValidationRepository.saveAll(validations);
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
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void validateRecord(Long datasetId, KieBase kieBase, Pageable pageable)
      throws EEAException {

    TenantResolver.setTenantName("dataset_" + datasetId);
    List<RecordValue> records = this.recordRepository.findAll(pageable).getContent();
    List<RecordValidation> recordValidations = new ArrayList<>();
    KieSession session = kieBase.newKieSession();
    try {
      records.stream().filter(Objects::nonNull).forEach(row -> {

        runRecordValidations(row, session);
        List<RecordValidation> validations = row.getRecordValidations();
        if (null != validations) {
          validations.stream().filter(Objects::nonNull).forEach(rowValidation -> {
            rowValidation.setRecordValue(row);
          });
        }
        TenantResolver.setTenantName("dataset_" + datasetId);
        recordValidations.addAll(validations);
      });
      if (recordValidations.size() > 0) {
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
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void validateFields(Long datasetId, KieBase kieBase, Pageable pageable)
      throws EEAException {

    List<FieldValue> fields = fieldRepository.findAll(pageable).getContent();
    List<FieldValidation> fieldValidations = new ArrayList<>();
    KieSession session = kieBase.newKieSession();
    try {
      fields.stream().filter(Objects::nonNull).forEach(field -> {

        List<FieldValidation> resultFields = runFieldValidations(field, session);

        if (null != field.getFieldValidations()) {
          field.getFieldValidations().stream().filter(Objects::nonNull).forEach(fieldVal -> {
            fieldVal.setFieldValue(field);
          });
        }
        TenantResolver.setTenantName("dataset_" + datasetId);
        fieldValidations.addAll(resultFields);
      });
      if (fieldValidations.size() > 0) {
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
    ResourceInfoVO resourceInfoVO =
        userManagementController.getResourceDetail(datasetId, ResourceGroupEnum.DATASET_PROVIDER);
    String countryCode = "''";
    String dataCallYear = "" + new LocalDate().getYear();
    if (null != resourceInfoVO.getAttributes() && resourceInfoVO.getAttributes().size() > 0) {
      if (resourceInfoVO.getAttributes().containsKey("countryCode")) {
        countryCode = resourceInfoVO.getAttributes().get("countryCode").get(0);
      }

      if (resourceInfoVO.getAttributes().containsKey("dataCallYear")) {
        dataCallYear = resourceInfoVO.getAttributes().get("dataCallYear").get(0);
      }
    }
    ThreadPropertiesManager.setVariable("dataCallYear", dataCallYear);
    ThreadPropertiesManager.setVariable("countryCode", countryCode);
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
      error.setNameTableSchema(fieldValidation.getValidation().getOriginName());

      error.setIdTableSchema(
          fieldValidation.getFieldValue().getRecord().getTableValue().getIdTableSchema());

      error.setTypeEntity(fieldValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(fieldValidation.getValidation().getValidationDate());

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
      error.setIdObject(recordValidation.getRecordValue().getId());
      error.setIdValidation(recordValidation.getValidation().getId());
      error.setLevelError(recordValidation.getValidation().getLevelError().name());
      error.setMessage(recordValidation.getValidation().getMessage());
      error.setNameTableSchema(recordValidation.getValidation().getOriginName());

      error.setIdTableSchema(recordValidation.getRecordValue().getTableValue().getIdTableSchema());

      error.setTypeEntity(recordValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(recordValidation.getValidation().getValidationDate());

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
      error.setIdObject(tableValidation.getTableValue().getId());
      error.setIdValidation(tableValidation.getValidation().getId());
      error.setLevelError(tableValidation.getValidation().getLevelError().name());
      error.setMessage(tableValidation.getValidation().getMessage());
      error.setNameTableSchema(tableValidation.getValidation().getOriginName());

      error.setIdTableSchema(tableValidation.getTableValue().getIdTableSchema());

      error.setTypeEntity(tableValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(tableValidation.getValidation().getValidationDate());

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
      error.setIdObject(datasetValidation.getDatasetValue().getId());
      error.setIdValidation(datasetValidation.getValidation().getId());
      error.setLevelError(datasetValidation.getValidation().getLevelError().name());
      error.setMessage(datasetValidation.getValidation().getMessage());
      error.setNameTableSchema(datasetValidation.getValidation().getOriginName());
      error.setIdTableSchema(dataset.getIdDatasetSchema());
      error.setTypeEntity(datasetValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(datasetValidation.getValidation().getValidationDate());

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
   * Error scale.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void errorScale(Long datasetId) throws EEAException {
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }

    Map<BigInteger, EntityErrors> map = new LinkedHashMap<>();
    List<EntityErrors> recordErrorList = recordValidationRepository.findFailedRecords();
    recordErrorList.stream().forEach(object -> map.put(object.getId(), object));
    List<RecordValidation> recordValidations = new ArrayList<>();
    map.entrySet().stream().forEach(entry -> {
      RecordValidation recordVal = new RecordValidation();
      recordVal.setValidation(new Validation());
      RecordValue recordValue = new RecordValue();
      recordValue.setId(Long.valueOf(entry.getValue().getId().toString()));
      recordVal.setRecordValue(recordValue);
      if (TypeErrorEnum.ERROR.getValue().equals(entry.getValue().getError())) {
        recordVal.getValidation().setLevelError(TypeErrorEnum.ERROR);
        recordVal.getValidation().setMessage("ONE OR MORE FIELDS HAVE ERRORS");
      } else {
        recordVal.getValidation().setLevelError(TypeErrorEnum.WARNING);
        recordVal.getValidation().setMessage("ONE OR MORE FIELDS HAVE WARNINGS");
      }
      recordVal.getValidation().setIdRule(new ObjectId().toString());
      recordVal.getValidation().setTypeEntity(TypeEntityEnum.RECORD);
      recordVal.getValidation().setValidationDate(new Date().toString());
      recordVal.getValidation().setOriginName(entry.getValue().getOrigin());
      recordValidations.add(recordVal);
    });
    this.recordValidationRepository.saveAll(recordValidations);

    Map<BigInteger, EntityErrors> map2 = new LinkedHashMap<>();
    List<EntityErrors> tableErrorList = recordValidationRepository.findFailedTables();
    tableErrorList.stream().forEach(object -> map2.put(object.getId(), object));
    List<TableValidation> tableValidations = new ArrayList<>();
    map2.entrySet().stream().forEach(entry -> {
      TableValidation tableVal = new TableValidation();
      tableVal.setValidation(new Validation());
      TableValue tableValue = new TableValue();
      tableValue.setId(Long.valueOf(entry.getValue().getId().toString()));
      tableVal.setTableValue(tableValue);
      if (TypeErrorEnum.ERROR.getValue().equals(entry.getValue().getError())) {
        tableVal.getValidation().setLevelError(TypeErrorEnum.ERROR);
        tableVal.getValidation().setMessage("ONE OR MORE RECORDS HAVE ERRORS");
      } else {
        tableVal.getValidation().setLevelError(TypeErrorEnum.WARNING);
        tableVal.getValidation().setMessage("ONE OR MORE RECORDS HAVE WARNINGS");
      }
      tableVal.getValidation().setIdRule(new ObjectId().toString());
      tableVal.getValidation().setTypeEntity(TypeEntityEnum.TABLE);
      tableVal.getValidation().setValidationDate(new Date().toString());
      tableVal.getValidation().setOriginName(entry.getValue().getOrigin());
      tableValidations.add(tableVal);
    });
    tableValidationRepository.saveAll(tableValidations);

    Map<BigInteger, EntityErrors> map3 = new LinkedHashMap<>();
    List<EntityErrors> datasetErrorList = recordValidationRepository.findFailedDatasets();
    datasetErrorList.stream().forEach(object -> map3.put(object.getId(), object));
    List<DatasetValidation> datasetValidations = new ArrayList<>();
    map3.entrySet().stream().forEach(entry -> {
      DatasetValidation datasetVal = new DatasetValidation();
      datasetVal.setValidation(new Validation());
      DatasetValue datasetValue = new DatasetValue();
      datasetValue.setId(Long.valueOf(entry.getValue().getId().toString()));
      datasetVal.setDatasetValue(datasetValue);
      if (TypeErrorEnum.ERROR.getValue().equals(entry.getValue().getError())) {
        datasetVal.getValidation().setLevelError(TypeErrorEnum.ERROR);
        datasetVal.getValidation().setMessage("ONE OR MORE TABLES HAVE ERRORS");
      } else {
        datasetVal.getValidation().setLevelError(TypeErrorEnum.WARNING);
        datasetVal.getValidation().setMessage("ONE OR MORE TABLES HAVE WARNINGS");
      }
      datasetVal.getValidation().setIdRule(new ObjectId().toString());
      datasetVal.getValidation().setTypeEntity(TypeEntityEnum.DATASET);
      datasetVal.getValidation().setValidationDate(new Date().toString());
      datasetVal.getValidation().setOriginName(entry.getValue().getOrigin());
      datasetValidations.add(datasetVal);
    });
    validationDatasetRepository.saveAll(datasetValidations);
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
   * @return the integer
   */
  @Override
  public Integer countFieldsDataset(Long datasetId) {
    return recordRepository.countFieldsDataset();
  }


  /**
   * The dataset repository.
   */
  @Autowired
  private DatasetRepositoryImpl datasetRepositoryImpl;

  /**
   * Dataset validation DO 02 query.
   *
   * @param DO02 the do02
   *
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDO02Query(String DO02) {
    return datasetRepositoryImpl.datasetValidationQuery(DO02);
  }

  /**
   * Dataset validation DO 03 query.
   *
   * @param DO03 the do03
   *
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDO03Query(String DO03) {
    return datasetRepositoryImpl.datasetValidationQuery(DO03);
  }

  /**
   * Dataset validation DC 01 A query.
   *
   * @param DC01A the dc01a
   *
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDC01AQuery(String DC01A) {
    return datasetRepositoryImpl.datasetValidationQuery(DC01A);
  }

  /**
   * Dataset validation DC 01 B query.
   *
   * @param DC01B the dc01b
   *
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDC01BQuery(String DC01B) {
    return datasetRepositoryImpl.datasetValidationQuery(DC01B);
  }

  /**
   * Dataset validation DC 02 query.
   *
   * @param DC02 the dc02
   *
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDC02Query(String DC02) {
    return datasetRepositoryImpl.datasetValidationQuery(DC02);
  }

  /**
   * Dataset validation DC 03 query.
   *
   * @param DC03 the dc03
   *
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDC03Query(String DC03) {
    return datasetRepositoryImpl.datasetValidationQuery(DC03);
  }

  /**
   * Dataset validation DC 02 B query.
   *
   * @param DC03 the dc03
   * @return the boolean
   */
  @Override
  public Boolean datasetValidationDC02BQuery(String DC03) {
    return datasetRepositoryImpl.datasetValidationQuery(DC03);
  }

  /**
   * Table validation DR 01 AB query.
   *
   * @param DR01A the dr01a
   * @param previous the previous
   *
   * @return the boolean
   */
  // TABLE PART
  @Override
  public Boolean tableValidationDR01ABQuery(String DR01A, Boolean previous) {
    return tableValidationQuerysDroolsRepository.tableValidationDR01ABQuery(DR01A, previous);
  }

  /**
   * Table validation query non return result.
   *
   * @param QUERY the query
   *
   * @return the boolean
   */
  @Override
  public Boolean tableValidationQueryNonReturnResult(String QUERY) {
    return tableValidationQuerysDroolsRepository.tableValidationQueryNonReturnResult(QUERY);
  }

  /**
   * Table validation query return result.
   *
   * @param QUERY the query
   * @return the boolean
   */
  @Override
  public Boolean tableValidationQueryReturnResult(String QUERY) {
    return tableValidationQuerysDroolsRepository.tableValidationQueryReturnResult(QUERY);
  }


  /**
   * Table record R ids.
   *
   * @param queryValidate the query validate
   * @param MessageError the message error
   * @param typeError the type error
   * @param originName the origin name
   * @return the boolean
   */
  @Override
  public Boolean tableRecordRIds(String queryValidate, String MessageError, TypeErrorEnum typeError,
      String originName) {
    List<BigInteger> listRecords =
        tableValidationQuerysDroolsRepository.tableValidationQueryReturnListIds(queryValidate);
    if (null != listRecords && !listRecords.isEmpty()) {
      for (BigInteger recordId : listRecords) {
        Optional<RecordValue> record = recordRepository.findByIdValidation(recordId.longValue());
        RecordValidation recordVal = new RecordValidation();
        Validation validation = new Validation();
        recordVal.setValidation(validation);
        recordVal.setRecordValue(record.get());
        recordVal.getValidation().setLevelError(typeError);
        recordVal.getValidation().setMessage(MessageError);
        recordVal.getValidation().setIdRule(new ObjectId().toString());
        recordVal.getValidation().setTypeEntity(TypeEntityEnum.RECORD);
        recordVal.getValidation().setValidationDate(new Date().toString());
        recordVal.getValidation().setOriginName(originName);
        recordValidationRepository.save(recordVal);
      }
    }
    return true;
  }


}
