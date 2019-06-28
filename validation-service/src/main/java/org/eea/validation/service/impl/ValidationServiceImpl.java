package org.eea.validation.service.impl;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
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
import org.eea.validation.persistence.data.repository.FieldValidationRepository;
import org.eea.validation.persistence.data.repository.RecordRepository;
import org.eea.validation.persistence.data.repository.RecordValidationRepository;
import org.eea.validation.persistence.data.repository.TableValidationRepository;
import org.eea.validation.persistence.data.repository.ValidationDatasetRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
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

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceImpl.class);
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
  private RecordValidationRepository validationRecordRepository;

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

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * The dataset controller.
   */
  @Autowired
  private DataSetControllerZuul datasetController;

  /** The dataset validation repository. */
  @Autowired
  private ValidationDatasetRepository datasetValidationRepository;


  /**
   * Gets the element lenght.
   *
   * @param dataset the dataset
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
   * @param tableValues the table values
   *
   * @return the list
   */
  @Override
  public List<TableValidation> runTableValidations(List<TableValue> tableValues,
      KieSession kieSession) {
    tableValues.stream().forEach(table -> kieSession.insert(table));
    kieSession.fireAllRules();
    return tableValues.isEmpty() ? new ArrayList<>()
        : tableValues.get(0).getTableValidations() == null ? new ArrayList<>()
            : tableValues.get(0).getTableValidations();
  }

  /**
   * Run record validations.
   *
   * @param records the records
   *
   * @return the list
   */
  @Override
  public List<RecordValidation> runRecordValidations(List<RecordValue> records,
      KieSession kieSession) {
    records.stream().forEach(record -> kieSession.insert(record));
    kieSession.fireAllRules();
    return records.isEmpty() ? new ArrayList<>() : records.get(0).getRecordValidations();
  }

  /**
   * Run field validations.
   *
   * @param fields the fields
   *
   * @return the list
   */
  @Override
  public List<FieldValidation> runFieldValidations(List<FieldValue> fields, KieSession kieSession) {
    fields.stream()
        .filter(field -> field.getIdFieldSchema() != null
            && StringUtils.isNotBlank(field.getIdFieldSchema()))
        .forEach(field -> kieSession.insert(field));
    kieSession.fireAllRules();
    return null == fields.get(0).getFieldValidations()
        || fields.get(0).getFieldValidations().isEmpty() ? new ArrayList<>()
            : fields.get(0).getFieldValidations();
  }


  /**
   * Load rules knowledge base.
   *
   * @param dataflowId the dataflow id
   *
   * @return the kie session
   *
   * @throws EEAException
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   */
  public KieSession loadRulesKnowledgeBase(Long dataflowId) throws EEAException {
    KieSession kieSession;
    try {
      kieSession = kieBaseManager.reloadRules(dataflowId).newKieSession();
    } catch (FileNotFoundException e) {
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND, e);
    } catch (Exception e) {
      LOG_ERROR.error(e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.VALIDATION_SESSION_ERROR, e);
    }
    return kieSession;
  }



  @Override
  @Transactional
  public void validateDataSet(Long datasetId) throws EEAException {
    // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    if (dataflowId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }

  @Override
  @Transactional
  public void validateTable(Long datasetId) throws EEAException {
    // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    if (dataflowId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }

  }

  @Override
  @Transactional
  public void validateRecord(Long datasetId) throws EEAException {
    // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    if (dataflowId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    dataset.getTableValues().stream().forEach(table -> {
      List<RecordValidation> recordValList = new ArrayList<RecordValidation>();
      List<RecordValue> validatedFields =
          sanitizeRecordsValidations(recordRepository.findAllRecordsByTableValueId(table.getId()));
      validatedFields.stream().forEach(row -> {
        List<TypeErrorEnum> errorsList = new ArrayList<>();
        List<String> orig = new ArrayList<>();
        row.getFields().stream().filter(field -> null != field.getFieldValidations())
            .forEach(field -> {
              field.getFieldValidations().stream().forEach(fval -> {
                orig.add(fval.getValidation().getOriginName());
                if (fval.getValidation().getLevelError().equals(TypeErrorEnum.ERROR)) {
                  errorsList.add(TypeErrorEnum.ERROR);
                } else {
                  errorsList.add(TypeErrorEnum.WARNING);
                }
              });
            });
        if (null != errorsList) {
          RecordValidation recordVal = new RecordValidation();
          Validation validation = new Validation();
          if (errorsList.contains(TypeErrorEnum.ERROR)) {
            validation.setLevelError(TypeErrorEnum.ERROR);
            validation.setMessage("ONE OR MORE FIELDS HAVE ERRORS");
          } else {
            if (errorsList.contains(TypeErrorEnum.WARNING)
                && !errorsList.contains(TypeErrorEnum.ERROR)) {
              validation.setLevelError(TypeErrorEnum.WARNING);
              validation.setMessage("ONE OR MORE FIELDS HAVE WARNINGS");
            } else {
              return;
            }
          }
          validation.setIdRule(new ObjectId().toString());
          validation.setTypeEntity(TypeEntityEnum.RECORD);
          validation.setValidationDate(new Date().toString());
          validation.setOriginName(orig.get(0));
          recordVal.setValidation(validation);
          recordVal.setRecordValue(row);
          row.getRecordValidations().add(recordVal);
          recordValList.add(recordVal);
        }
      });
      validationRecordRepository.saveAll((Iterable<RecordValidation>) recordValList);
    });
  }


  /**
   * Validate data set data.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException
   */
  @Override
  @Transactional
  public void validateFields(Long datasetId) throws EEAException {
    // Get Dataflow id
    Long dataflowId = datasetController.getDataFlowIdById(datasetId);
    if (dataflowId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    // Get the session for the rules validation
    KieSession session = loadRulesKnowledgeBase(dataflowId);

    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    // Records
    for (TableValue tableValue : dataset.getTableValues()) {
      Long tableId = tableValue.getId();
      // read Dataset records Data for each table
      List<RecordValue> recordsByTable =
          sanitizeRecords(recordRepository.findAllRecordsByTableValueId(tableId));

      // Execute field rules validation
      recordsByTable.stream().filter(Objects::nonNull).forEach(row -> {
        if (null != row.getRecordValidations()) {
          row.getFields().stream().filter(Objects::nonNull).forEach(field -> {
            List<FieldValidation> resultFields = runFieldValidations(row.getFields(), session);
            if (null != field.getFieldValidations()) {
              field.getFieldValidations().stream().filter(Objects::nonNull).forEach(fieldValue -> {
                fieldValue.setFieldValue(field);
              });
              validationFieldRepository.saveAll((Iterable<FieldValidation>) resultFields);
            }
          });
        }
      });
    }
  }



  /**
   * Populate record validations.
   *
   * @param recordsByTable the records by table
   */
  private void populateRecordValidations(List<RecordValue> recordsByTable) {
    recordsByTable.stream().filter(Objects::nonNull).forEach(row -> {
      if (null != row.getRecordValidations()) {
        row.getRecordValidations().stream().filter(Objects::nonNull).forEach(rowValue -> {
          rowValue.setRecordValue(row);
        });
      }
    });
  }

  /**
   * Execute table validations.
   *
   * @param session the session
   * @param dataset the dataset
   *
   * @return the list
   */
  private List<TableValidation> executeTableValidations(KieSession session, DatasetValue dataset) {
    List<TableValidation> resultTable = runTableValidations(dataset.getTableValues(), session);
    // Asign ID Table
    dataset.getTableValues().stream().forEach(table -> {
      if (null != table.getTableValidations()) {
        table.getTableValidations().stream().forEach(tableValue -> {
          tableValue.setTableValue(table);
        });
      }
    });
    return resultTable;
  }

  /**
   * Execute dataset validations.
   *
   * @param session the session
   * @param dataset the dataset
   *
   * @return the list
   */
  private List<DatasetValidation> executeDatasetValidations(KieSession session,
      DatasetValue dataset) {
    List<DatasetValidation> resultDataset = runDatasetValidations(dataset, session);
    // Asign ID Dataset
    if (null != resultDataset && !resultDataset.isEmpty()) {
      resultDataset.stream().forEach(datasetValue -> {
        datasetValue.setDatasetValue(dataset);
      });
    }
    return resultDataset;
  }


  /**
   * Sanitize records.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    Set<Long> processedRecords = new HashSet<>();
    for (RecordValue recordValue : records) {
      if (!processedRecords.contains(recordValue.getId())) {
        processedRecords.add(recordValue.getId());
        recordValue.getFields().stream().forEach(field -> field.setFieldValidations(null));
        sanitizedRecords.add(recordValue);
      }

    }
    return sanitizedRecords;

  }

  private List<RecordValue> sanitizeRecordsValidations(List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    Set<Long> processedRecords = new HashSet<>();
    for (RecordValue recordValue : records) {
      if (!processedRecords.contains(recordValue.getId())) {
        processedRecords.add(recordValue.getId());
        sanitizedRecords.add(recordValue);
      }
    }
    return sanitizedRecords;
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
  }

  /**
   * Gets the field errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the field errors
   */
  @Override
  public List<ErrorsValidationVO> getFieldErrors(final Long datasetId,
      final List<Long> idValidations) {
    List<FieldValidation> fieldValidations =
        validationFieldRepository.findByValidationIds(idValidations);
    List<ErrorsValidationVO> errors = new ArrayList<>();
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

      errors.add(error);
    }
    return errors;
  }


  /**
   * Gets the record errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the record errors
   */
  @Override
  public List<ErrorsValidationVO> getRecordErrors(final Long datasetId,
      final List<Long> idValidations) {
    List<RecordValidation> recordValidations =
        validationRecordRepository.findByValidationIds(idValidations);
    List<ErrorsValidationVO> errors = new ArrayList<>();
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

      errors.add(error);
    }
    return errors;
  }


  /**
   * Gets the table errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the table errors
   */
  @Override
  public List<ErrorsValidationVO> getTableErrors(final Long datasetId,
      final List<Long> idValidations) {
    List<TableValidation> tableValidations =
        tableValidationRepository.findByValidationIds(idValidations);
    List<ErrorsValidationVO> errors = new ArrayList<>();
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

      errors.add(error);
    }
    return errors;
  }


  /**
   * Gets the dataset errors.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   * @param idValidations the id validations
   * @return the dataset errors
   */
  @Override
  public List<ErrorsValidationVO> getDatasetErrors(final Long datasetId, final DatasetValue dataset,
      final List<Long> idValidations) {
    List<ErrorsValidationVO> errors = new ArrayList<>();
    List<DatasetValidation> datasetValidations =
        datasetValidationRepository.findByValidationIds(idValidations);
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

      errors.add(error);
    }
    return errors;
  }

  /**
   * Gets the dataset valueby id.
   *
   * @param datasetId the dataset id
   * @return the dataset valueby id
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
   * @return the find by id data set schema
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

}
