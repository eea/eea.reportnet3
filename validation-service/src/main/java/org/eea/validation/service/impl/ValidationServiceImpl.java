package org.eea.validation.service.impl;


import com.opencsv.CSVWriter;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.StringUtils;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3ConvertService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.controller.ums.ResourceManagementController.ResourceManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.FailedValidationsDatasetVO;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.interfaces.vo.ums.ResourceInfoVO;
import org.eea.interfaces.vo.ums.enums.ResourceGroupEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.multitenancy.TenantResolver;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.eea.validation.exception.EEAInvalidSQLException;
import org.eea.validation.persistence.data.domain.*;
import org.eea.validation.persistence.data.metabase.repository.TaskRepository;
import org.eea.validation.persistence.data.repository.*;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.DataLakeValidationService;
import org.eea.validation.service.ValidationService;
import org.eea.validation.util.KieBaseManager;
import org.eea.validation.util.RulesErrorUtils;
import org.eea.validation.util.SQLValidationUtils;
import org.joda.time.LocalDate;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.io.*;
import java.util.*;
import java.util.concurrent.Future;

import static org.eea.utils.LiteralConstants.S3_VALIDATION;
import static org.eea.utils.LiteralConstants.S3_VALIDATION_TABLE_PATH;

/**
 * The Class ValidationServiceImpl.
 */
@Service("validationService")
public class ValidationServiceImpl implements ValidationService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(RulesServiceImpl.class);

  /** The Constant ENTITY: {@value}. */
  private static final String ENTITY = "Entity";

  /** The Constant TABLE: {@value}. */
  private static final String TABLE = "Table";

  /** The Constant FIELD: {@value}. */
  private static final String FIELD = "Field";

  /** The Constant CODE: {@value}. */
  private static final String CODE = "Code";

  /** The Constant CODENAME: {@value}. */
  private static final String CODENAME = "QC Name";

  /** The Constant CODEDESC: {@value}. */
  private static final String CODEDESC = "QC Description";

  /** The Constant LEVELERROR: {@value}. */
  private static final String LEVELERROR = "Level error";

  /** The Constant MESSAGE: {@value}. */
  private static final String MESSAGE = "Message";

  /** The Constant NUMBEROFRECORDS: {@value}. */
  private static final String NUMBEROFRECORDS = "Number of records";


  /** The Constant EXCEPTIONERRORSTRING: {@value}. */
  private static final String EXCEPTIONERRORSTRING =
      "Trying to download a file generated during the export dataset validation data process but the file is not found, datasetID: %s + filename: %s";


  /** The delimiter. */
  @Value("${exportDataDelimiter}")
  private char delimiter;

  /** The path public file. */
  @Value("${validationExportPathFile}")
  private String pathPublicFile;

  /**
   * The path export DL.
   */
  @Value("${exportDLPath}")
  private String exportDLPath;

  /** The kie base manager. */
  @Autowired
  private KieBaseManager kieBaseManager;

  /** The record validation repository. */
  @Autowired
  private RecordValidationRepository recordValidationRepository;

  /** The validation dataset repository. */
  @Autowired
  private ValidationDatasetRepository validationDatasetRepository;

  /** The table validation repository. */
  @Autowired
  private TableValidationRepository tableValidationRepository;

  /** The validation field repository. */
  @Autowired
  private FieldValidationRepository validationFieldRepository;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The validation repository. */
  @Autowired
  private ValidationRepository validationRepository;

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The task repository. */
  @Autowired
  private TaskRepository taskRepository;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The resource management controller. */
  @Autowired
  private ResourceManagementControllerZull resourceManagementController;

  /** The dataset schema controller. */
  @Autowired
  private DatasetSchemaControllerZuul datasetSchemaController;

  /** The data set controller zuul. */
  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;

  /** The rules error utils. */
  @Autowired
  private RulesErrorUtils rulesErrorUtils;

  /** The ruleservice. */
  @Autowired
  private RulesServiceImpl ruleservice;

  /** The sql validation utils. */
  @Autowired
  private SQLValidationUtils sqlValidationUtils;
  

  @Autowired
  private S3ConvertService s3ConvertService;

  @Autowired
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Autowired
  private S3Helper s3Helper;

  @Autowired
  private DremioHelperService dremioHelperService;

  @Autowired
  private DataFlowControllerZuul dataFlowControllerZuul;

  @Autowired
  private DataLakeValidationService dataLakeValidationService;

  /**
   * Run dataset validations.
   *
   * @param dataset the dataset
   * @param kieSession the kie session
   * @return the list
   */
  @Override
  public List<DatasetValidation> runDatasetValidations(DatasetValue dataset,
      KieSession kieSession) {
    List<DatasetValidation> result = new ArrayList<>();
    kieSession.insert(dataset);
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG.error("The Dataset Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(dataset, e);
    }
    try {
      if (null != dataset.getDatasetValidations() && !dataset.getDatasetValidations().isEmpty()) {
        result = dataset.getDatasetValidations();
      }
    } catch (Exception e) {
      LOG.error("Problem accessing to the db getting the datasetValidations: { } ",
          e.getMessage(), e);
    }
    return result;
  }

  /**
   * Run table validations.
   *
   * @param table the table
   * @param kieSession the kie session
   * @return the list
   */
  @Override
  public List<TableValidation> runTableValidations(TableValue table, KieSession kieSession) {
    List<TableValidation> result = new ArrayList<>();
    kieSession.insert(table);
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG.error("The Table Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(table, e);
    }
    try {
      if (null != table.getTableValidations() && !table.getTableValidations().isEmpty()) {
        result = table.getTableValidations();
      }
    } catch (Exception e) {
      LOG.error("Problem accessing to the db getting the tableValidations: { } ",
          e.getMessage(), e);
    }
    return result;
  }

  /**
   * Run record validations.
   *
   * @param record the record
   * @param kieSession the kie session
   * @return the list
   */
  @Override
  @Transactional
  public List<RecordValidation> runRecordValidations(RecordValue record, KieSession kieSession) {
    List<RecordValidation> result = new ArrayList<>();
    if (StringUtils.isNotBlank(record.getIdRecordSchema())) {
      kieSession.insert(record);
    }
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG.error("The Record Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(record, e);
    }

    try {
      if (null != record.getRecordValidations() && !record.getRecordValidations().isEmpty()) {
        result = record.getRecordValidations();
      }
    } catch (Exception e) {
      LOG.error("Problem accessing to the db getting the datasetValidations: { } ",
          e.getMessage(), e);
    }
    return result;
  }

  /**
   * Run field validations.
   *
   * @param field the field
   * @param kieSession the kie session
   * @return the list
   */
  @Override
  public List<FieldValidation> runFieldValidations(FieldValue field, KieSession kieSession) {
    List<FieldValidation> result = new ArrayList<>();
    if (StringUtils.isNotBlank(field.getIdFieldSchema())) {
      kieSession.insert(field);
    }
    try {
      kieSession.fireAllRules();
    } catch (RuntimeException e) {
      LOG.error("The Field Validation failed: {}", e.getMessage(), e);
      rulesErrorUtils.createRuleErrorException(field, e);
    }
    try {
      if (null != field.getFieldValidations() && !field.getFieldValidations().isEmpty()) {
        result = field.getFieldValidations();
      }
    } catch (Exception e) {
      LOG.error("Problem accessing to the db  getting the fieldValidations: { } ",
          e.getMessage(), e);
    }
    return result;
  }


  /**
   * Load rules knowledge base.
   *
   * @param datasetId the dataset id
   * @param rule the rule
   * @return the kie base
   * @throws EEAException the EEA exception
   */
  @Override
  public KieBase loadRulesKnowledgeBase(Long datasetId, String rule) throws EEAException {
    KieBase kieBase;
    try {
      kieBase = kieBaseManager.reloadRules(datasetId,
          datasetSchemaController.getDatasetSchemaId(datasetId), rule);
    } catch (FileNotFoundException e) {
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND, e);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      throw new EEAException(EEAErrorMessage.VALIDATION_SESSION_ERROR, e);
    }
    return kieBase;
  }

  /**
   * Validate data set.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param taskId the task id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void validateDataSet(Long datasetId, KieBase kieBase, Long taskId) throws EEAException {
    KieSession session;
    try {
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
      if (dataset == null) {
        throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
      }
      List<DatasetValidation> validations = new ArrayList<>();
      session = kieBase.newKieSession();
      try {
        validations = runDatasetValidations(dataset, session);

        validationDatasetRepository.saveAll(validations);
      } finally {
        session.destroy();
        validations = null;
        dataset = null;
        System.gc();
      }
    } catch (Exception e) {
      taskRepository.updateStatusAndFinishDate(taskId, ProcessStatusEnum.IN_QUEUE.toString(),
          new Date());
    }
  }

  /**
   * Validate table.
   *
   * @param datasetId the dataset id
   * @param idTable the id table
   * @param kieBase the kie base
   * @param sqlRule the sql rule
   * @param dataProviderId the data provider id
   * @param taskId the task id
   */
  @Override
  @Transactional
  public void validateTable(Long datasetId, Long idTable, KieBase kieBase, String sqlRule,
      String dataProviderId, Long taskId) {
    // Validating tables
    TableValue table = null;
    KieSession session = null;
    try {
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      if (!"null".equals(sqlRule)) {
        sqlValidationUtils.executeValidationSQLRule(datasetId, sqlRule, dataProviderId);
      } else {
        table = tableRepository.findById(idTable).orElse(null);
        session = kieBase.newKieSession();
        if (table != null) {
          tableValidationRepository.saveAll(runTableValidations(table, session));
        }
      }
    } catch (EEAInvalidSQLException e) {
      LOG.error("The Table Validation failed: {}", e.getMessage(), e);
      try {
        table = tableRepository.findById(idTable).orElse(null);
        if (table != null) {
          rulesErrorUtils.createRuleErrorException(table, new RuntimeException(e.getMessage()));
        }
      } catch (Exception ex1) {
        LOG.error("Saving blockers for the table validation failed: {}", ex1.getMessage(), ex1);
      }
      taskRepository.updateStatusAndFinishDate(taskId, ProcessStatusEnum.IN_QUEUE.toString(),
          new Date());
    } finally {
      table = null;
      if (session != null) {
        session.destroy();
      }
      System.gc();
    }
  }

  /**
   * Validate record.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param pageable the pageable
   * @param taskId the task id
   */
  @Override
  @Transactional
  public void validateRecord(Long datasetId, KieBase kieBase, Pageable pageable, Long taskId) {
    KieSession session;
    try {
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      List<RecordValue> records = recordRepository.findRecordsPageable(pageable);
      session = kieBase.newKieSession();
      try {
        for (RecordValue row : records) {
          TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
          recordValidationRepository.saveAll(runRecordValidations(row, session));
        }
      } finally {
        records = null;
        session.destroy();
        System.gc();
      }
    } catch (Exception e) {
      taskRepository.updateStatusAndFinishDate(taskId, ProcessStatusEnum.IN_QUEUE.toString(),
          new Date());
    }
  }

  /**
   * Validate fields.
   *
   * @param datasetId the dataset id
   * @param kieBase the kie base
   * @param pageable the pageable
   * @param onlyEmptyFields the only empty fields
   * @param taskId the task id
   */
  @Override
  @Transactional
  public void validateFields(Long datasetId, KieBase kieBase, Pageable pageable,
      boolean onlyEmptyFields, Long taskId) {
    KieSession session;
    try {
      TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
      Page<FieldValue> fields = onlyEmptyFields ? fieldRepository.findEmptyFields(pageable)
          : fieldRepository.findAll(pageable);
      session = kieBase.newKieSession();
      try {
        for (FieldValue field : fields) {
          TenantResolver.setTenantName(LiteralConstants.DATASET_PREFIX + datasetId);
          validationFieldRepository.saveAll(runFieldValidations(field, session));
        }
      } finally {
        session.destroy();
      }
    } catch (Exception e) {
      taskRepository.updateStatusAndFinishDate(taskId, ProcessStatusEnum.IN_QUEUE.toString(),
          new Date());
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
      error.setIdTableSchema(
          fieldValidation.getFieldValue().getRecord().getTableValue().getIdTableSchema());
      error.setNameFieldSchema(fieldValidation.getValidation().getFieldName());

      refillErrorValidation(fieldValidation.getValidation(), error);
      errors.put(fieldValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
  }


  /**
   * Gets the record errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
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
      refillErrorValidation(recordValidation.getValidation(), error);
      errors.put(recordValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
  }


  /**
   * Gets the table errors.
   *
   * @param datasetId the dataset id
   * @param idValidations the id validations
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
      error.setIdTableSchema(tableValidation.getTableValue().getIdTableSchema());
      refillErrorValidation(tableValidation.getValidation(), error);

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
      error.setIdTableSchema(dataset.getIdDatasetSchema());
      refillErrorValidation(datasetValidation.getValidation(), error);

      errors.put(datasetValidation.getValidation().getId(), error);
    }

    return new AsyncResult<>(errors);
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

  /**
   * Export validation file.
   *
   * @param datasetId the dataset id
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Async
  @Override
  public void exportValidationFile(Long datasetId) throws EEAException, IOException {
    DatasetTypeEnum datasetType = dataSetControllerZuul.getDatasetType(datasetId);

    // Sets the validation file name and it's root directory
    String composedFileName = "dataset-" + datasetId + "-validations";
    String fileNameWithExtension = composedFileName + "." + FileTypeEnum.CSV.getValue();
    String creatingFileError =
        String.format("Failed generating CSV file with name %s using datasetID %s",
            fileNameWithExtension, datasetId);

    File fileFolder = new File(pathPublicFile, composedFileName);

    fileFolder.mkdirs();

    // Creates notification VO and passes the datasetID, the filename and the datasetType
    NotificationVO notificationVO = NotificationVO.builder()
        .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
        .fileName(fileNameWithExtension).datasetType(datasetType).error(creatingFileError).build();

    // We create the CSV
    StringWriter stringWriter = new StringWriter();

    try (CSVWriter csvWriter =
        new CSVWriter(stringWriter, delimiter, CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

      // Creates an array list containing all the column names from the CSV defined as constants
      List<String> headers = new ArrayList<>(Arrays.asList(ENTITY, TABLE, FIELD, CODE, CODENAME,
          CODEDESC, LEVELERROR, MESSAGE, NUMBEROFRECORDS));

      // Writes the column names into the CSV Writer and sets the array String to headers size so it
      // only writes at most the number of columns as variables per row
      csvWriter.writeNext(headers.stream().toArray(String[]::new), false);
      int nHeaders = 9;

      if (getDatasetValuebyId(datasetId) != null)
        fillValidationDataCSV(datasetId, nHeaders, csvWriter, notificationVO);
      else
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, creatingFileError);

    }
    catch (IOException e) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DOWNLOAD_VALIDATIONS_FAILED_EVENT,
          null, notificationVO);
      LOG.error(EEAErrorMessage.CSV_FILE_ERROR, e);
      return;
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in exportValidationFile for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
    }

    // Convert the writer data to a bytes array to write it into a file
    String csv = stringWriter.getBuffer().toString();
    byte[] file = csv.getBytes();

    File fileWrite = new File(new File(pathPublicFile, composedFileName), fileNameWithExtension);

    // Tries to write the data obtained into the file, if it's successful, throws a notification
    // event completed
    try (OutputStream out = new FileOutputStream(fileWrite.toString())) {
      out.write(file);
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DOWNLOAD_VALIDATIONS_COMPLETED_EVENT,
          null, notificationVO);
    } catch (Exception e) {
      LOG.error("Unexpected error! Error in exportValidationFile when releasing notification (DOWNLOAD_VALIDATIONS_COMPLETED_EVENT) for datasetId {}. Message: {}", datasetId, e.getMessage());
      throw e;
    }
  }

  @Override
  public void exportValidationFileDL(Long datasetId) throws EEAException {
    DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
    S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId()!=null ? dataset.getDataProviderId() : 0, dataset.getId(), S3_VALIDATION);
    s3PathResolver.setPath(S3_VALIDATION_TABLE_PATH);
    if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH) && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
      String creatingFileError =
              String.format("Failed generating CSV file with name %s using datasetID %s",
                      S3_VALIDATION, datasetId);
      // Creates notification VO and passes the datasetID, the filename and the datasetType
      NotificationVO notificationVO = NotificationVO.builder()
              .user(SecurityContextHolder.getContext().getAuthentication().getName()).datasetId(datasetId)
              .fileName(S3_VALIDATION).datasetType(dataset.getDatasetTypeEnum()).error(creatingFileError).build();
      try {

        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DOWNLOAD_VALIDATIONS_COMPLETED_EVENT,
                null, notificationVO);
      } catch (Exception e) {
        LOG.error("Error exporting validation table data from dataset Id {}.", datasetId);
        kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DOWNLOAD_VALIDATIONS_FAILED_EVENT,
                null, notificationVO);
        throw e;
      }
    }
  }

  /**
   * Download exported file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ResponseStatusException the response status exception
   */
  @Override
  public File downloadExportedFile(Long datasetId, String fileName)
      throws IOException, ResponseStatusException {

    // we compound the route and create the file
    File file =
        new File(new File(pathPublicFile, "dataset-" + datasetId + "-validations"), fileName);
    if (!file.exists()) {

      LOG.error(String.format(EXCEPTIONERRORSTRING, datasetId, fileName));
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          String.format(EXCEPTIONERRORSTRING, datasetId, fileName));
    }

    return file;

  }

  /**
   * Gets the validations by dataset value.
   *
   * @param dataset the dataset
   * @param datasetId the dataset id
   * @return the validations by dataset value
   */
  private FailedValidationsDatasetVO getValidationsByDatasetValue(DatasetValue dataset,
      Long datasetId) {
    FailedValidationsDatasetVO validations = new FailedValidationsDatasetVO();

    // Recovers the errors from the dataset Validations and the total records so we can cycle
    // through them later
    validations.setErrors(new ArrayList<>());
    validations.setIdDatasetSchema(dataset.getIdDatasetSchema());
    validations.setIdDataset(datasetId);

    List<GroupValidationVO> errors = validationRepository.findGroupRecordsByFilter(datasetId,
        new ArrayList<>(), new ArrayList<>(), "", "", null, "", false, false);

    getRuleMessage(dataset, errors);
    validations.setErrors(errors);

    validations.setTotalRecords(Long.valueOf(errors.size()));

    return validations;
  }

  private FailedValidationsDatasetVO getBigDataValidationsByDataset(DataSetMetabaseVO dataSetMetabaseVO) {
    FailedValidationsDatasetVO validations = new FailedValidationsDatasetVO();

    validations.setErrors(new ArrayList<>());
    validations.setIdDatasetSchema(dataSetMetabaseVO.getDatasetSchema());
    validations.setIdDataset(dataSetMetabaseVO.getId());
    List<GroupValidationVO> errors = new ArrayList<>();

    S3PathResolver s3PathResolver = new S3PathResolver(dataSetMetabaseVO.getDataflowId(), dataSetMetabaseVO.getDataProviderId()!=null ? dataSetMetabaseVO.getDataProviderId() : 0, dataSetMetabaseVO.getId(), S3_VALIDATION);
    if (s3Helper.checkFolderExist(s3PathResolver, S3_VALIDATION_TABLE_PATH) && dremioHelperService.checkFolderPromoted(s3PathResolver, s3PathResolver.getTableName())) {
       errors = dataLakeValidationService.findGroupRecordsByFilter(s3PathResolver, null, null, "",
              "", null, "", false, false);
    }

    getRuleMessageDL(dataSetMetabaseVO.getDatasetSchema(), errors);
    validations.setErrors(errors);

    validations.setTotalRecords(Long.valueOf(errors.size()));

    return validations;
  }

  /**
   * Gets the rule message.
   *
   * @param dataset the dataset
   * @param errors the errors
   * @return the rule message
   */
  @Override
  public void getRuleMessage(DatasetValue dataset, List<GroupValidationVO> errors) {
    RulesSchema rules =
        rulesRepository.findByIdDatasetSchema(new ObjectId(dataset.getIdDatasetSchema()));
    if (null != rules && null != rules.getRules()) {
      for (GroupValidationVO validation : errors) {
        for (Rule rule : rules.getRules()) {
          if ((EntityTypeEnum.FIELD == validation.getTypeEntity()
              || EntityTypeEnum.RECORD == validation.getTypeEntity())
              && validation.getIdRule().equals(rule.getRuleId().toString())) {
            validation.setMessage(replacePlaceHolders(rule.getThenCondition().get(0)));
          }
        }
      }
    }
  }

  /**
   * Gets the rule message.
   *
   * @param datasetSchema
   * @param errors the errors
   * @return the rule message
   */
  @Override
  public void getRuleMessageDL(String datasetSchema, List<GroupValidationVO> errors) {
    RulesSchema rules =
            rulesRepository.findByIdDatasetSchema(new ObjectId(datasetSchema));
    if (null != rules && null != rules.getRules()) {
      for (GroupValidationVO validation : errors) {
        for (Rule rule : rules.getRules()) {
          if ((EntityTypeEnum.FIELD == validation.getTypeEntity()
                  || EntityTypeEnum.RECORD == validation.getTypeEntity()
                  || EntityTypeEnum.TABLE == validation.getTypeEntity())
                  && validation.getShortCode().equals(rule.getShortCode())) {
            validation.setMessage(replacePlaceHolders(rule.getThenCondition().get(0)));
          }
        }
      }
    }
  }

  /**
   * Replace place holders.
   *
   * @param message the message
   * @return the string
   */
  private String replacePlaceHolders(String message) {
    return message.replace("{%", "<").replace("%}", ">");
  }



  /**
   * Fill validation error data.
   *
   * @param error the error
   * @param dataset the dataset
   * @param nHeaders the n headers
   * @return the string[]
   */
  private String[] fillValidationErrorData(GroupValidationVO error, String datasetSchema,
      int nHeaders) {
    RulesSchemaVO rulesVO =
        ruleservice.getActiveRulesSchemaByDatasetId(datasetSchema);
    List<RuleVO> ruleListVO = rulesVO.getRules();
    String[] fieldsToWrite = new String[nHeaders];

    // Compares the current error shortCode with all the active rules shortCode for the
    // current dataSet to check which rule caused the error to occur
    RuleVO ruleVO =
        ruleListVO.stream().filter(rule -> rule.getShortCode().equals(error.getShortCode()))
            .findFirst().orElse(new RuleVO());

    String ruleName = "";
    String ruleDescription = "";
    if (ruleVO.getShortCode()!=null) {
      ruleName = ruleVO.getRuleName().startsWith("=") ? " " + ruleVO.getRuleName() : ruleVO.getRuleName();
      ruleDescription = ruleVO.getDescription().startsWith("=") ? " " + ruleVO.getDescription() : ruleVO.getDescription();
    }
    // Sets all the data which is later going to be written into the CSV

    fieldsToWrite[0] = error.getTypeEntity().toString();
    fieldsToWrite[1] = error.getNameTableSchema();
    fieldsToWrite[2] = error.getNameFieldSchema();
    fieldsToWrite[3] = error.getShortCode().startsWith("=") ? " " + error.getShortCode() : error.getShortCode();
    fieldsToWrite[4] = ruleName;
    fieldsToWrite[5] = ruleDescription;
    fieldsToWrite[6] = error.getLevelError().toString();
    fieldsToWrite[7] = error.getMessage().startsWith("=") ? " " + error.getMessage() : error.getMessage();
    fieldsToWrite[8] = error.getNumberOfRecords().toString();

    return fieldsToWrite;
  }

  /**
   * Fill validation data CSV.
   *
   * @param datasetId the dataset id
   * @param nHeaders the n headers
   * @param csvWriter the csv writer
   * @param notificationVO the notification VO
   * @throws EEAException the EEA exception
   */
  private void fillValidationDataCSV(Long datasetId, int nHeaders, CSVWriter csvWriter,
      NotificationVO notificationVO) throws EEAException {
    try {
      FailedValidationsDatasetVO validations = null;
      DataSetMetabaseVO dataSetMetabaseVO = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
      DataFlowVO dataFlowVO = dataFlowControllerZuul.getMetabaseById(dataSetMetabaseVO.getDataflowId());
      if (dataFlowVO.getBigData()!=null && dataFlowVO.getBigData()) {
        validations = getBigDataValidationsByDataset(dataSetMetabaseVO);
      } else {
        DatasetValue dataset = getDatasetValuebyId(datasetId);
        validations = getValidationsByDatasetValue(dataset, datasetId);
      }

      if (CollectionUtils.isEmpty(validations.getErrors())) {
        LOG.error(
            "Tried to create validations export from an empty validations dataset so it delivered an empty file.");
      }

      else {
        for (Object error : validations.getErrors()) {
          // Casts validations.getErrors which is List<?> to an Object so we can later cast it to
          // GroupValidationVO which allows us to set the error properties

          GroupValidationVO castedError = (GroupValidationVO) error;

          csvWriter.writeNext(fillValidationErrorData(castedError, dataSetMetabaseVO.getDatasetSchema(), nHeaders), false);
        }
      }
    }

    // If any of the exceptions is catched, throws the notification of failed event
    catch (EEAException e) {
      kafkaSenderUtils.releaseNotificableKafkaEvent(EventType.DOWNLOAD_VALIDATIONS_FAILED_EVENT,
          null, notificationVO);
      LOG.error("Failed while filling the CSV file with data. Error message: {}",
          e.getMessage());
    }
  }

  /**
   * Refill error validation.
   *
   * @param validation the validation
   * @param error the error
   */
  private void refillErrorValidation(Validation validation, ErrorsValidationVO error) {
    error.setIdValidation(validation.getId());
    error.setLevelError(validation.getLevelError().name());
    error.setMessage(validation.getMessage());
    error.setNameTableSchema(validation.getTableName());
    error.setTypeEntity(validation.getTypeEntity().name());
    error.setValidationDate(validation.getValidationDate());
    error.setShortCode(validation.getShortCode());
  }
}
