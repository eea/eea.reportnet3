package org.eea.dataset.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.persistence.data.SortFieldsHelper;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.AttachmentRepository;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.DesignDataset;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.Statistics;
import org.eea.dataset.persistence.metabase.repository.DataCollectionRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.metabase.repository.StatisticsRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.domain.pkcatalogue.PkCatalogueSchema;
import org.eea.dataset.persistence.schemas.repository.PkCatalogueRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.PaMService;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.IntegrationToolTypeEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.ETLFieldVO;
import org.eea.interfaces.vo.dataset.ETLRecordVO;
import org.eea.interfaces.vo.dataset.ETLTableVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.interfaces.vo.lock.LockVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.lock.enums.LockType;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

/**
 * The Class DatasetServiceImpl.
 */
@Service("datasetService")
public class DatasetServiceImpl implements DatasetService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant USER: {@value}. */
  private static final String USER = "root";

  /** The Constant HEADER_NAME: {@value}. */
  private static final String HEADER_NAME = "headerName";

  /** The Constant DATASET_ID: {@value}. */
  private static final String DATASET_ID = "dataset_%s";

  /** The Constant FILE_PUBLIC_DATASET_PATTERN_NAME. */
  private static final String FILE_PUBLIC_DATASET_PATTERN_NAME = "%s-%s";
  /** The field max length. */
  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

  /** The path public file. */
  @Value("${pathPublicFile}")
  private String pathPublicFile;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The partition data set metabase repository. */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The design dataset repository. */
  @Autowired
  private DesignDatasetRepository designDatasetRepository;

  /** The reporting dataset repository. */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /** The dataflow controller zull. */
  @Autowired
  private DataFlowControllerZuul dataflowControllerZuul;

  /** The table repository. */
  @Autowired
  private TableRepository tableRepository;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The record validation repository. */
  @Autowired
  private RecordValidationRepository recordValidationRepository;

  /** The field repository. */
  @Autowired
  private FieldRepository fieldRepository;

  /** The field validation repository. */
  @Autowired
  private FieldValidationRepository fieldValidationRepository;

  /** The statistics repository. */
  @Autowired
  private StatisticsRepository statisticsRepository;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The record mapper. */
  @Autowired
  private RecordMapper recordMapper;

  /** The file parser factory. */
  @Autowired
  private IFileParserFactory fileParserFactory;

  /** The file export factory. */
  @Autowired
  private IFileExportFactory fileExportFactory;

  /** The record no validation mapper. */
  @Autowired
  private RecordNoValidationMapper recordNoValidationMapper;

  /** The field validation mapper. */
  @Autowired
  private FieldValidationMapper fieldValidationMapper;

  /** The record validation mapper. */
  @Autowired
  private RecordValidationMapper recordValidationMapper;

  /** The kafka sender utils. */
  @Autowired
  private KafkaSenderUtils kafkaSenderUtils;

  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The representative controller zuul. */
  @Autowired
  private RepresentativeControllerZuul representativeControllerZuul;

  /** The field no validation mapper. */
  @Autowired
  private FieldNoValidationMapper fieldNoValidationMapper;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /** The integration controller. */
  @Autowired
  private IntegrationControllerZuul integrationController;

  /** The pa M service. */
  @Autowired
  private PaMService paMService;

  /** The attachment repository. */
  @Autowired
  private AttachmentRepository attachmentRepository;

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

  /** The pk catalogue repository. */
  @Autowired
  private PkCatalogueRepository pkCatalogueRepository;

  /**
   * Process file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the is
   * @param idTableSchema the id table schema
   *
   * @return the data set VO
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public DataSetVO processFile(final Long datasetId, final String fileName, final InputStream is,
      final String idTableSchema) throws EEAException, IOException {
    // obtains the file type from the extension
    if (fileName == null) {
      throw new EEAException(EEAErrorMessage.FILE_NAME);
    }
    final String mimeType = getMimetype(fileName);
    // validates file types for the data load
    validateFileType(mimeType);

    try {
      // Get the partition for the partiton id
      final PartitionDataSetMetabase partition = obtainPartition(datasetId, USER);

      // Get the dataFlowId from the metabase
      final Long dataflowId = getDataFlowIdById(datasetId);

      // create the right file parser for the file type
      final IFileParseContext context = fileParserFactory.createContext(mimeType, datasetId);

      final DataSetVO datasetVO = context.parse(is, dataflowId, partition.getId(), idTableSchema);

      if (datasetVO == null) {
        throw new IOException("Empty dataset");
      }

      return datasetVO;

    } finally {
      is.close();
    }
  }

  /**
   * Save all records.
   *
   * @param datasetId the dataset id
   * @param recordValues the record values
   */
  @Override
  @Transactional
  public void saveAllRecords(Long datasetId, List<RecordValue> recordValues) {
    recordRepository.saveAll(recordValues);
  }

  /**
   * Save table.
   *
   * @param datasetId the dataset id
   * @param tableValue the table value
   */
  @Override
  @Transactional
  public void saveTable(@DatasetId Long datasetId, TableValue tableValue) {
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
    Optional<DatasetValue> datasetValue = datasetRepository.findById(datasetId);
    if (datasetValue.isPresent()) {
      tableValue.setDatasetId(datasetValue.get());
      tableRepository.saveAndFlush(tableValue);
    }
  }

  /**
   * Gets the mimetype.
   *
   * @param file the file
   *
   * @return the mimetype
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public String getMimetype(final String file) throws EEAException {
    String mimeType = null;
    final int location = file.lastIndexOf('.');
    if (location == -1) {
      throw new EEAException(EEAErrorMessage.FILE_EXTENSION);
    }
    mimeType = file.substring(location + 1);
    return mimeType;
  }

  /**
   * Delete data schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  public void deleteDataSchema(final String datasetId) {
    schemasRepository.deleteById(new ObjectId(datasetId));
  }

  /**
   * Delete table by schema.
   *
   * @param tableSchemaId the id table schema
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteTableBySchema(final String tableSchemaId, final Long datasetId) {
    deleteRecords(datasetId, tableSchemaId);
  }

  private void deleteRecords(Long datasetId, String tableSchemaId) {

    boolean singleTable = null != tableSchemaId;
    Long dataflowId = getDataFlowIdById(datasetId);
    TypeStatusEnum dataflowStatus = dataflowControllerZuul.getMetabaseById(dataflowId).getStatus();
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(
        new ObjectId(datasetMetabaseService.findDatasetSchemaIdById(datasetId)));

    for (TableSchema tableSchema : schema.getTableSchemas()) {

      String loopTableSchemaId = tableSchema.getIdTableSchema().toString();

      if (singleTable && !tableSchemaId.equals(loopTableSchemaId)) {
        continue;
      }

      if (TypeStatusEnum.DESIGN.equals(dataflowStatus)) {
        recordRepository.deleteRecordWithIdTableSchema(loopTableSchemaId);
        LOG.info("Executed deleteRecords: datasetId={}, tableSchemaId={}", datasetId,
            loopTableSchemaId);
      } else if (Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        LOG.info("Skipped deleteRecords: datasetId={}, tableSchemaId={}, tableSchema.readOnly={}",
            datasetId, loopTableSchemaId, tableSchema.getReadOnly());
      } else if (Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        List<String> fieldSchemasToClear = tableSchema.getRecordSchema().getFieldSchema().stream()
            .filter(fieldSchema -> Boolean.FALSE.equals(fieldSchema.getReadOnly()))
            .map(fieldSchema -> fieldSchema.getIdFieldSchema().toString())
            .collect(Collectors.toList());
        List<FieldValue> fieldValuesToClear =
            fieldRepository.findAllByIdFieldSchemaIn(fieldSchemasToClear);
        fieldValuesToClear.stream().forEach(fieldVal -> fieldVal.setValue(""));
        fieldRepository.saveAll(fieldValuesToClear);
        LOG.info("Overwritting fieldValues to blank: datasetId={}, tableSchemaId={}", datasetId,
            loopTableSchemaId);
      }

      if (singleTable) {
        return;
      }
    }

    try {
      saveStatistics(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error saving statistics after deleting all the dataset values. Error message: {}",
          e.getMessage(), e);
    }
  }


  /**
   * Delete import data.
   *
   * @param datasetId the data set id
   */
  @Override
  @Transactional
  public void deleteImportData(final Long datasetId) {
    deleteRecords(datasetId, null);
  }

  /**
   * Gets the table values by id.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public TableVO getTableValuesById(final Long datasetId, final String idTableSchema,
      Pageable pageable, final String fields, ErrorTypeEnum[] levelError, String[] idRules,
      String fieldSchema, String fieldValue) throws EEAException {
    List<String> commonShortFields = new ArrayList<>();
    Map<String, Integer> mapFields = new HashMap<>();
    List<SortField> sortFieldsArray = new ArrayList<>();
    SortField[] newFields = null;
    TableVO result = new TableVO();
    Long totalRecords = tableRepository.countRecordsByIdTableSchema(idTableSchema);

    // Check if we need to put all the records without pagination
    pageable = calculatePageable(pageable, totalRecords);

    result = calculatedErrorsAndRecordsToSee(datasetId, idTableSchema, pageable, fields, levelError,
        commonShortFields, mapFields, sortFieldsArray, newFields, result, idRules, fieldSchema,
        fieldValue);

    // Table with out values
    if (null == result.getRecords() || result.getRecords().isEmpty()) {
      result.setRecords(new ArrayList<>());
      LOG.info("No records founded in datasetId {}, idTableSchema {}", datasetId, idTableSchema);

    } else {
      List<RecordVO> recordVOs = result.getRecords();

      LOG.info(
          "Total records found in datasetId {} idTableSchema {}: {}. Now in page {}, {} records by page",
          datasetId, idTableSchema, recordVOs.size(),
          pageable != null ? pageable.getPageNumber() : null,
          pageable != null ? pageable.getPageSize() : null);
      if (null != fields) {
        LOG.info("Ordered by idFieldSchema {}", commonShortFields);
      }

      // 5Âº retrieve validations to set them into the final result
      retrieveValidations(recordVOs);

    }
    result.setTotalRecords(totalRecords);
    return result;
  }

  /**
   * Gets the by id.
   *
   * @param datasetId the dataset id
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  @Deprecated
  public DataSetVO getById(Long datasetId) throws EEAException {
    DatasetValue datasetValue = new DatasetValue();
    List<TableValue> allTableValues = tableRepository.findAllTables();
    datasetValue.setTableValues(allTableValues);
    datasetValue.setId(datasetId);
    datasetValue.setIdDatasetSchema(datasetRepository.findIdDatasetSchemaById(datasetId));
    for (TableValue tableValue : allTableValues) {
      tableValue
          .setRecords(sanitizeRecords(retrieveRecordValue(tableValue.getIdTableSchema(), null)));
    }
    LOG.info("Get dataset by id: {}", datasetId);
    return dataSetMapper.entityToClass(datasetValue);
  }

  /**
   * Update dataset.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateDataset(Long datasetId, DataSetVO dataset) throws EEAException {
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    DatasetValue datasetValue = dataSetMapper.classToEntity(dataset);
    datasetRepository.saveAndFlush(datasetValue);
  }

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   *
   * @return the data flow id by id
   */
  @Override
  public Long getDataFlowIdById(Long datasetId) {
    return dataSetMetabaseRepository.findDataflowIdById(datasetId);
  }

  /**
   * Gets the position from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param type the type
   *
   * @return the position from any object id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public ValidationLinkVO getPositionFromAnyObjectId(final String id, final Long idDataset,
      final EntityTypeEnum type) throws EEAException {

    ValidationLinkVO validationLink = new ValidationLinkVO();
    RecordValue record = new RecordValue();
    List<RecordValue> records = new ArrayList<>();

    // TABLE
    if (EntityTypeEnum.TABLE == type) {
      TableValue table = tableRepository.findByIdAndDatasetId_Id(Long.parseLong(id), idDataset);
      records = recordRepository.findByTableValueNoOrder(table.getIdTableSchema(), null);
      if (records != null && !records.isEmpty()) {
        record = records.get(0);
      }
    }

    // RECORD
    if (EntityTypeEnum.RECORD == type) {
      record = recordRepository.findByIdAndTableValue_DatasetId_Id(id, idDataset);
      records =
          recordRepository.findByTableValueNoOrder(record.getTableValue().getIdTableSchema(), null);
    }

    // FIELD
    if (EntityTypeEnum.FIELD == type) {
      FieldValue field = fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(id, idDataset);
      if (field != null && field.getRecord() != null && field.getRecord().getTableValue() != null) {
        record = field.getRecord();
        records = recordRepository
            .findByTableValueNoOrder(record.getTableValue().getIdTableSchema(), null);
      }
    }

    if (records != null && !records.isEmpty()) {
      int recordPosition = records.indexOf(record);
      validationLink.setIdTableSchema(record.getTableValue().getIdTableSchema());
      validationLink.setPosition(Long.valueOf(recordPosition));
      validationLink.setIdRecord(record.getId());
    }

    LOG.info(
        "Validation error with idObject {} clicked in dataset {}. The position is {} from table schema {}",
        id, idDataset, validationLink.getPosition(), validationLink.getIdTableSchema());
    return validationLink;
  }

  /**
   * Save statistics.
   *
   * @param datasetId the dataset id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void saveStatistics(final Long datasetId) throws EEAException {

    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());

    if (dataset.getId() != null && StringUtils.isNotBlank(dataset.getIdDatasetSchema())) {
      List<Statistics> statsList = new ArrayList<>();
      List<TableValue> allTableValues = dataset.getTableValues();

      DataSetMetabase datasetMb =
          dataSetMetabaseRepository.findById(datasetId).orElse(new DataSetMetabase());

      DataSetSchema schema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getIdDatasetSchema()));

      Map<String, String> mapIdNameDatasetSchema = new HashMap<>();
      for (TableSchema tableSchema : schema.getTableSchemas()) {

        mapIdNameDatasetSchema.put(tableSchema.getIdTableSchema().toString(),
            tableSchema.getNameTableSchema());
      }

      for (TableValue tableValue : allTableValues) {
        statsList.addAll(processTableStats(tableValue, datasetId, mapIdNameDatasetSchema));
      }

      // Check dataset validations
      Boolean datasetErrors = false;
      if (dataset.getDatasetValidations() != null && !dataset.getDatasetValidations().isEmpty()) {
        datasetErrors = true;
      } else {
        Optional<Statistics> opt = statsList.stream()
            .filter(s -> "tableErrors".equals(s.getStatName()) && "true".equals(s.getValue()))
            .findFirst();
        if (opt.isPresent()) {
          datasetErrors = true;
        }
      }

      Statistics statsIdDatasetSchema =
          fillStat(datasetId, null, "idDataSetSchema", dataset.getIdDatasetSchema());
      statsList.add(statsIdDatasetSchema);

      Statistics statsNameDatasetSchema =
          fillStat(datasetId, null, "nameDataSetSchema", datasetMb.getDataSetName());
      statsList.add(statsNameDatasetSchema);

      Statistics statsDatasetErrors =
          fillStat(datasetId, null, "datasetErrors", datasetErrors.toString());
      statsList.add(statsDatasetErrors);

      statisticsRepository.deleteStatsByIdDataset(datasetId);
      statisticsRepository.flush();
      statisticsRepository.saveAll(statsList);

      LOG.info("Statistics save to datasetId {}.", datasetId);
    } else {
      LOG_ERROR.error("No dataset found to save statistics. DatasetId:{}", datasetId);
    }
  }

  /**
   * Update records. if we fill updateCascadePk it update the pk in cascade in the dataset
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateRecords(final Long datasetId, final List<RecordVO> records,
      boolean updateCascadePK) throws EEAException {
    if (datasetId == null || records == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);

    }
    List<RecordValue> recordValues = recordMapper.classListToEntity(records);
    List<FieldValue> fieldValues = new ArrayList<>();

    if (updateCascadePK) {
      // we update the ids in cascade for any pk value changed
      for (RecordValue recordValue : recordValues) {
        updateCascadePK(recordValue.getId(), recordValue.getFields());
      }
    }
    String datasetSchemaId = dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
    DatasetTypeEnum datasetType = getDatasetType(datasetId);
    for (RecordValue recordValue : recordValues) {
      fieldValueUpdateRecordFor(fieldValues, datasetSchemaId, recordValue, datasetType);
    }
    fieldRepository.saveAll(fieldValues);
  }


  /**
   * Field value update record for.
   *
   * @param fieldValues the field values
   * @param datasetSchemaId the dataset schema id
   * @param recordValue the record value
   * @param datasetType the dataset type
   */
  private void fieldValueUpdateRecordFor(List<FieldValue> fieldValues, String datasetSchemaId,
      RecordValue recordValue, DatasetTypeEnum datasetType) {
    for (FieldValue fieldValue : recordValue.getFields()) {
      if (null == fieldValue.getValue()) {
        fieldValue.setValue("");
      } else {
        if (fieldValue.getValue().length() >= fieldMaxLength) {
          fieldValue.setValue(fieldValue.getValue().substring(0, fieldMaxLength));
        }
      }
      Document fieldSchema =
          schemasRepository.findFieldSchema(datasetSchemaId, fieldValue.getIdFieldSchema());
      if (!(fieldSchema != null && fieldSchema.get(LiteralConstants.READ_ONLY) != null
          && fieldSchema.getBoolean(LiteralConstants.READ_ONLY)
          && !DatasetTypeEnum.DESIGN.equals(datasetType))) {
        fieldValues.add(fieldValue);
      }
    }
  }

  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param recordVOs the record V os
   * @param tableSchemaId the table schema id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertRecords(Long datasetId, List<RecordVO> recordVOs, String tableSchemaId)
      throws EEAException {

    if (recordVOs == null || recordVOs.isEmpty()) {
      throw new EEAException(EEAErrorMessage.RECORD_REQUIRED);
    }

    DataSetMetabaseVO datasetMetabaseVO = datasetMetabaseService.findDatasetMetabase(datasetId);
    TableSchema tableSchema = getTableSchema(tableSchemaId, datasetMetabaseVO.getDatasetSchema());

    if (null == tableSchema) {
      throw new EEAException(EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }

    DatasetTypeEnum datasetType = getDatasetType(datasetId);
    String dataProviderCode = null != datasetMetabaseVO.getDataProviderId()
        ? representativeControllerZuul.findDataProviderById(datasetMetabaseVO.getDataProviderId())
            .getCode()
        : null;

    if (!DatasetTypeEnum.DESIGN.equals(datasetType)) {

      // Deny insert if the table is marked as read only. Not applies for DESIGN.
      if (Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        throw new EEAException(EEAErrorMessage.TABLE_READ_ONLY);
      }

      // Deny insert if the table is marked as fixed number of record. Not applies for DESIGN.
      if (Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        throw new EEAException(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS);
      }
    }

    recordRepository
        .saveAll(createRecords(datasetId, dataProviderCode, recordVOs, datasetType, tableSchema));
  }

  /**
   * Delete record. if we fill deleteCascadePk it delete the pk in cascade in the dataset
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @param deleteCascadePK the delete cascade
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteRecord(final Long datasetId, final String recordId,
      final boolean deleteCascadePK) throws EEAException {
    if (datasetId == null || recordId == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (deleteCascadePK) {
      deleteCascadePK(recordId);
    }
    recordRepository.deleteRecordWithId(recordId);
  }

  /**
   * Delete cascade PK.
   *
   * @param recordId the record id
   */
  private void deleteCascadePK(final String recordId) {
    RecordValue record = recordRepository.findById(recordId);
    Map<String, FieldValue> mapField = new HashMap<>();
    // get fields in record
    record.getFields().stream().forEach(field -> mapField.put(field.getIdFieldSchema(), field));
    // get the RecordSchema
    Document recordSchemaDocument = schemasRepository.findRecordSchema(
        record.getTableValue().getDatasetId().getIdDatasetSchema(),
        record.getTableValue().getIdTableSchema());
    // get fks to delete
    if (null != recordSchemaDocument) {
      List<Document> fieldSchemasList =
          (ArrayList) recordSchemaDocument.get(LiteralConstants.FIELD_SCHEMAS);

      List<String> recordsToDelete = findRecordsToDelete(mapField, fieldSchemasList);
      if (!recordsToDelete.isEmpty()) {
        LOG.info("records with fk's to delete {}", recordsToDelete);
        recordRepository.deleteRecordWithIdIn(recordsToDelete);
      }
    }
    // delete all fks
  }

  /**
   * Find records to delete.
   *
   * @param mapField the map field
   * @param fieldSchemasList the field schemas list
   *
   * @return the list
   */
  private List<String> findRecordsToDelete(Map<String, FieldValue> mapField,
      List<Document> fieldSchemasList) {
    List<String> recordsToDelete = new ArrayList<>();
    for (Object document : fieldSchemasList) {
      Document fieldSchemaDocument = (Document) document;
      if (fieldSchemaDocument.get(LiteralConstants.PK) != null
          && fieldSchemaDocument.getBoolean(LiteralConstants.PK)) {
        String idFieldSchema = (fieldSchemaDocument.get(LiteralConstants.ID)).toString();
        PkCatalogueSchema pkCatalogueSchema =
            pkCatalogueRepository.findByIdPk(new ObjectId(idFieldSchema));
        if (null != pkCatalogueSchema && pkCatalogueSchema.getReferenced() != null) {
          List<String> referenced = pkCatalogueSchema.getReferenced().stream()
              .map(ObjectId::toString).collect(Collectors.toList());
          List<FieldValue> fieldsValues = fieldRepository.findByIdFieldSchemaIn(referenced);

          FieldValue fieldV = mapField.get(idFieldSchema);
          fieldsValues.stream().forEach(field -> {
            if (fieldV != null && field.getValue().equals(fieldV.getValue())) {
              recordsToDelete.add(field.getRecord().getId());
            }
          });
          // delete pams list
          paMService.deleteGroups(fieldSchemasList, fieldV.getValue());
        }
      }
    }
    return recordsToDelete;
  }

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param tableSchemaId the table schema id
   *
   * @return the byte[]
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public byte[] exportFile(Long datasetId, String mimeType, final String tableSchemaId)
      throws EEAException, IOException {
    // Get the dataFlowId from the metabase
    Long idDataflow = getDataFlowIdById(datasetId);

    // Find if the dataset type is EU to include the countryCode
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
    boolean includeCountryCode = DatasetTypeEnum.EUDATASET.equals(datasetType);

    final IFileExportContext context = fileExportFactory.createContext(mimeType);
    LOG.info("End of exportFile");
    return context.fileWriter(idDataflow, datasetId, tableSchemaId, includeCountryCode);
  }

  /**
   * Export file through integration.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void exportFileThroughIntegration(Long datasetId, Long integrationId) throws EEAException {
    DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(datasetId)
        .orElseThrow(() -> new EEAException(EEAErrorMessage.DATASET_NOTFOUND));
    String datasetSchemaId = datasetMetabase.getDatasetSchema();
    IntegrationVO integrationVO =
        integrationController.findExportIntegration(datasetSchemaId, integrationId);
    integrationController.executeIntegrationProcess(IntegrationToolTypeEnum.FME,
        IntegrationOperationTypeEnum.EXPORT, null, datasetId, integrationVO);
  }

  /**
   * Insert schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void insertSchema(final Long datasetId, String idDatasetSchema) throws EEAException {

    // 1.Insert the dataset schema into DatasetValue
    DatasetValue ds = new DatasetValue();
    ds.setIdDatasetSchema(idDatasetSchema);
    ds.setId(datasetId);

    // 2.Search the table schemas of the dataset and then insert it into TableValue
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));
    List<TableValue> tableValues = new ArrayList<>();
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      TableValue tv = new TableValue();
      tv.setIdTableSchema(tableSchema.getIdTableSchema().toString());
      tv.setDatasetId(ds);
      tableValues.add(tv);
    }
    ds.setTableValues(tableValues);
    datasetRepository.save(ds);
  }

  /**
   * Update field. if cascade pk is true we update the pk in cascade
   *
   * @param datasetId the dataset id
   * @param field the field
   * @param updateCascadePK the update cascade PK
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateField(final Long datasetId, final FieldVO field, boolean updateCascadePK)
      throws EEAException {
    if (datasetId == null || field == null) {
      throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);

    }

    String datasetSchemaId = dataSetMetabaseRepository.findDatasetSchemaIdById(datasetId);
    Document fieldSchema =
        schemasRepository.findFieldSchema(datasetSchemaId, field.getIdFieldSchema());

    if (fieldSchema == null) {
      throw new EEAException(EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    }

    Boolean isLinkMultiselect = Boolean.FALSE;
    if (DataType.LINK.equals(field.getType())) {
      isLinkMultiselect = fieldSchema.get(LiteralConstants.PK_HAS_MULTIPLE_VALUES) != null
          && fieldSchema.getBoolean(LiteralConstants.PK_HAS_MULTIPLE_VALUES);
    }
    if (fieldSchema.get(LiteralConstants.READ_ONLY) != null
        && (Boolean) fieldSchema.get(LiteralConstants.READ_ONLY)
        && !DatasetTypeEnum.DESIGN.equals(getDatasetType(datasetId))) {
      throw new EEAException(EEAErrorMessage.FIELD_READ_ONLY);
    }
    // if the type is multiselect codelist or Link multiselect we sort the values in lexicographic
    // order
    if ((DataType.MULTISELECT_CODELIST.equals(field.getType()) || isLinkMultiselect)
        && null != field.getValue()) {
      List<String> values = new ArrayList<>();
      Arrays.asList(field.getValue().split(",")).stream()
          .forEach(value -> values.add(value.trim()));
      Collections.sort(values);
      field.setValue(values.toString().substring(1, values.toString().length() - 1));
    }
    if (updateCascadePK) {
      fieldValueUpdatePK(field, fieldSchema, datasetSchemaId);
    }

    if (null != field.getValue() && field.getValue().length() >= fieldMaxLength) {
      field.setValue(field.getValue().substring(0, fieldMaxLength));
    }

    fieldRepository.saveValue(field.getId(), field.getValue());
  }


  /**
   * Find table id by table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   *
   * @return the long
   */
  @Override
  public Long findTableIdByTableSchema(Long datasetId, String idTableSchema) {
    return tableRepository.findIdByIdTableSchema(idTableSchema);
  }

  /**
   * Delete record values to restore snapshot.
   *
   * @param datasetId the dataset id
   * @param idPartition the id partition
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @org.springframework.transaction.annotation.Transactional(propagation = Propagation.NESTED)
  public void deleteRecordValuesToRestoreSnapshot(Long datasetId, Long idPartition)
      throws EEAException {
    recordRepository.deleteRecordValuesToRestoreSnapshot(idPartition);
  }

  /**
   * Save table propagation.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void saveTablePropagation(Long datasetId, TableSchemaVO tableSchema) throws EEAException {
    TableValue table = new TableValue();
    TenantResolver.setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, datasetId));
    Optional<DatasetValue> dataset = datasetRepository.findById(datasetId);
    if (dataset.isPresent()) {
      table.setIdTableSchema(tableSchema.getIdTableSchema());
      table.setDatasetId(dataset.get());
      saveTable(datasetId, table);
    } else {
      LOG_ERROR.error("Saving table propagation failed because the dataset {} is not found",
          datasetId);
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
  }

  /**
   * Delete table value.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  @Override
  @Transactional
  public void deleteTableValue(Long datasetId, String idTableSchema) {
    tableRepository.deleteByIdTableSchema(idTableSchema);
  }

  /**
   * Delete field values.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   */
  @Override
  @Transactional
  public void deleteFieldValues(Long datasetId, String fieldSchemaId) {
    LOG.info(
        "Deleting the related field values from the datasetId {} due to deleting the fieldSchema {}",
        datasetId, fieldSchemaId);
    fieldRepository.deleteByIdFieldSchemaNative(fieldSchemaId);
  }

  /**
   * Update field value type.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   * @param type the type
   */
  @Override
  @Transactional
  public void updateFieldValueType(Long datasetId, String fieldSchemaId, DataType type) {
    fieldRepository.updateFieldValueType(fieldSchemaId, type.getValue());
  }

  /**
   * Delete all table values.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteAllTableValues(Long datasetId) {
    tableRepository.removeTableData(datasetId);
  }

  /**
   * Prepare new field propagation.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaVO the field schema VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void prepareNewFieldPropagation(Long datasetId, FieldSchemaVO fieldSchemaVO)
      throws EEAException {

    // Find, from the idFieldSchema, the idTableSchema to know the table and the records containing
    // it to propagate
    Integer sizeRecords = 0;
    Optional<DatasetValue> dataset = datasetRepository.findById(datasetId);
    if (dataset.isPresent()) {
      String idDatasetSchema = dataset.get().getIdDatasetSchema();
      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));

      Optional<TableSchema> tableSchema = schema.getTableSchemas().stream()
          .filter(t -> t.getRecordSchema().getFieldSchema().stream()
              .anyMatch(f -> f.getIdFieldSchema().equals(new ObjectId(fieldSchemaVO.getId()))))
          .findFirst();

      if (tableSchema.isPresent()) {
        String idTableSchema = tableSchema.get().getIdTableSchema().toString();

        Optional<TableValue> table = dataset.get().getTableValues().stream()
            .filter(t -> t.getIdTableSchema().equals(idTableSchema)).findFirst();

        if (table.isPresent()) {

          sizeRecords = table.get().getRecords().size();

          Map<String, Object> value = new HashMap<>();
          value.put("dataset_id", datasetId);
          value.put("sizeRecords", sizeRecords);
          value.put("idTableSchema", idTableSchema);
          value.put("idFieldSchema", fieldSchemaVO.getId());
          value.put("typeField", fieldSchemaVO.getType());
          kafkaSenderUtils.releaseKafkaEvent(EventType.COMMAND_NEW_DESIGN_FIELD_PROPAGATION, value);

        }
      }
    } else {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
  }

  /**
   * Save new field propagation.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param idFieldSchema the id field schema
   * @param typeField the type field
   */
  @Override
  @Transactional
  public void saveNewFieldPropagation(Long datasetId, String idTableSchema, Pageable pageable,
      String idFieldSchema, DataType typeField) {

    List<RecordValue> recordsPaginated =
        recordRepository.findByTableValue_IdTableSchema(idTableSchema, pageable);

    List<FieldValue> fields = new ArrayList<>();
    for (RecordValue r : recordsPaginated) {
      FieldValue field = new FieldValue();
      field.setIdFieldSchema(idFieldSchema);
      field.setType(typeField);
      field.setValue("");
      RecordValue recordAux = new RecordValue();
      TableValue tableAux = new TableValue();
      DatasetValue datasetAux = new DatasetValue();
      datasetAux.setId(datasetId);
      tableAux.setDatasetId(datasetAux);
      recordAux.setTableValue(tableAux);
      recordAux.setId(r.getId());
      field.setRecord(recordAux);

      fields.add(field);
    }

    fieldRepository.saveAll(fields);
  }

  /**
   * Delete record values by provider.
   *
   * @param datasetId the dataset id
   * @param providerCode the provider code
   */
  @Override
  @Transactional
  public void deleteRecordValuesByProvider(@DatasetId Long datasetId, String providerCode) {
    LOG.info("Deleting data with providerCode: {} ", providerCode);
    recordRepository.deleteByDataProviderCode(providerCode);
  }

  /**
   * Gets the field values referenced.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param conditionalValue the conditional value
   * @param searchValue the search value
   * @param resultsNumber the results number
   *
   * @return the field values referenced
   */
  @Override
  public List<FieldVO> getFieldValuesReferenced(Long datasetIdOrigin, String datasetSchemaId,
      String fieldSchemaId, String conditionalValue, String searchValue, Integer resultsNumber) {

    List<FieldVO> fieldsVO = new ArrayList<>();
    Document fieldSchema = schemasRepository.findFieldSchema(datasetSchemaId, fieldSchemaId);
    Document referenced = (Document) fieldSchema.get(LiteralConstants.REFERENCED_FIELD);
    if (referenced != null) {
      String idPk = referenced.get("idPk").toString();
      String labelSchemaId = null;
      String conditionalSchemaId = null;
      if (referenced.get("labelId") != null) {
        labelSchemaId = referenced.get("labelId").toString();
      } else {
        // In case there's no label selected, the label will the the same as the Pk
        labelSchemaId = idPk;
      }
      if (referenced.get("linkedConditionalFieldId") != null) {
        conditionalSchemaId = referenced.get("linkedConditionalFieldId").toString();
      }
      if (StringUtils.isBlank(searchValue)) {
        searchValue = "";
      }

      Long idDatasetDestination =
          datasetMetabaseService.getDatasetDestinationForeignRelation(datasetIdOrigin, idPk);

      TenantResolver
          .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, idDatasetDestination));
      // If the variable resultsNumbers is null, then the limit of results by default it will be 15.
      // Otherwise the limit will be
      // the number passed with a max of 100. That will be the results showed on screen.
      if (resultsNumber == null || resultsNumber == 0) {
        resultsNumber = 15;
      } else if (resultsNumber > 100) {
        resultsNumber = 100;
      }

      // The query returns the list of fieldsVO ordered by it's type and considering the possible
      // label and conditional values
      FieldValue fvPk = fieldRepository.findFirstTypeByIdFieldSchema(idPk);
      fieldsVO = fieldRepository.findByIdFieldSchemaWithTagOrdered(idPk, labelSchemaId, searchValue,
          conditionalSchemaId, conditionalValue, fvPk.getType(), resultsNumber);

    }
    return fieldsVO;
  }


  /**
   * Gets the referenced dataset id.
   *
   * @param datasetId the dataset id
   * @param idPk the id pk
   *
   * @return the referenced dataset id
   */
  @Override
  public Long getReferencedDatasetId(Long datasetId, String idPk) {
    return datasetMetabaseService.getDatasetDestinationForeignRelation(datasetId, idPk);
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   *
   * @return the ETL dataset VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public ETLDatasetVO etlExportDataset(@DatasetId Long datasetId) throws EEAException {

    // Get the datasetSchemaId by the datasetId
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    if (null == datasetSchemaId) {
      throw new EEAException(String.format(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND, datasetId));
    }

    // Get the datasetSchema by the datasetSchemaId
    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    if (null == datasetSchema) {
      throw new EEAException(
          String.format(EEAErrorMessage.DATASET_SCHEMA_NOT_FOUND, datasetSchemaId));
    }

    // Construct object to be returned
    ETLDatasetVO etlDatasetVO = new ETLDatasetVO();
    List<ETLTableVO> etlTableVOs = new ArrayList<>();
    etlDatasetVO.setTables(etlTableVOs);

    // Loop to fill ETLTableVOs
    for (TableSchema tableSchema : datasetSchema.getTableSchemas()) {

      // Match each fieldSchemaId with its headerName
      Map<String, String> fieldMap = new HashMap<>();
      for (FieldSchema field : tableSchema.getRecordSchema().getFieldSchema()) {
        fieldMap.put(field.getIdFieldSchema().toString(), field.getHeaderName());
      }

      ETLTableVO etlTableVO = new ETLTableVO();
      List<ETLRecordVO> etlRecordVOs = new ArrayList<>();
      etlTableVO.setTableName(tableSchema.getNameTableSchema());
      etlTableVO.setRecords(etlRecordVOs);
      etlTableVOs.add(etlTableVO);

      // Loop to fill ETLRecordVOs
      for (RecordValue record : recordRepository
          .findByTableValueNoOrder(tableSchema.getIdTableSchema().toString(), null)) {
        ETLRecordVO etlRecordVO = new ETLRecordVO();
        List<ETLFieldVO> etlFieldVOs = new ArrayList<>();
        etlRecordVO.setFields(etlFieldVOs);
        etlRecordVO.setCountryCode(record.getDataProviderCode());
        etlRecordVOs.add(etlRecordVO);

        // Loop to fill ETLFieldVOs
        for (FieldValue field : record.getFields()) {
          ETLFieldVO etlFieldVO = new ETLFieldVO();
          etlFieldVO.setFieldName(fieldMap.get(field.getIdFieldSchema()));
          etlFieldVO.setValue(field.getValue());
          etlFieldVOs.add(etlFieldVO);
        }
      }
    }
    return etlDatasetVO;
  }

  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param providerId the provider id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public void etlImportDataset(@DatasetId Long datasetId, ETLDatasetVO etlDatasetVO,
      Long providerId) throws EEAException {
    // Get the datasetSchemaId by the datasetId
    LOG.info("Import data into dataset {}", datasetId);
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    if (null == datasetSchemaId) {
      throw new EEAException(String.format(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND, datasetId));
    }

    // Get the datasetSchema by the datasetSchemaId
    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    if (null == datasetSchema) {
      throw new EEAException(
          String.format(EEAErrorMessage.DATASET_SCHEMA_NOT_FOUND, datasetSchemaId));
    }

    // Obtain the data provider code to insert into the record
    DataProviderVO provider =
        providerId != null ? representativeControllerZuul.findDataProviderById(providerId) : null;

    // Get the partition for the partiton id
    final PartitionDataSetMetabase partition = obtainPartition(datasetId, USER);

    // Construct Maps to relate ids
    Map<String, TableSchema> tableMap = new HashMap<>();
    Map<String, FieldSchema> fieldMap = new HashMap<>();
    Set<String> tableWithAttachmentFieldSet = new HashSet<>();
    for (TableSchema tableSchema : datasetSchema.getTableSchemas()) {
      tableMap.put(tableSchema.getNameTableSchema().toLowerCase(), tableSchema);
      // Match each fieldSchemaId with its headerName
      for (FieldSchema field : tableSchema.getRecordSchema().getFieldSchema()) {
        fieldMap.put(field.getHeaderName().toLowerCase() + tableSchema.getIdTableSchema(), field);
        if (DataType.ATTACHMENT.equals(field.getType())) {
          LOG.warn("Table with id schema {} contains attachment field, processing",
              tableSchema.getIdTableSchema());
          tableWithAttachmentFieldSet.add(tableSchema.getIdTableSchema().toString());
        }
      }
    }

    // Construct object to be save
    DatasetValue dataset = new DatasetValue();
    List<TableValue> tables = new ArrayList<>();
    List<String> readOnlyTables = new ArrayList<>();
    List<String> fixedNumberTables = new ArrayList<>();

    // Loops to build the entity
    dataset.setId(datasetId);
    DatasetTypeEnum datasetType = getDatasetType(dataset.getId());

    etlTableFor(etlDatasetVO, provider, partition, tableMap, fieldMap, dataset, tables,
        readOnlyTables, fixedNumberTables, datasetType);
    dataset.setTableValues(tables);
    dataset.setIdDatasetSchema(datasetSchemaId);

    List<RecordValue> allRecords = new ArrayList<>();

    tableValueFor(datasetId, dataset, readOnlyTables, fixedNumberTables, allRecords,
        tableWithAttachmentFieldSet);
    recordRepository.saveAll(allRecords);
    LOG.info("Data saved into dataset {}", datasetId);
  }

  /**
   * Table value for.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
   * @param readOnlyTables the read only tables
   * @param fixedNumberTables the fixed number tables
   * @param allRecords the all records
   * @param tableWithAttachmentFieldSet the table with attachment field set
   */
  private void tableValueFor(Long datasetId, DatasetValue dataset, List<String> readOnlyTables,
      List<String> fixedNumberTables, List<RecordValue> allRecords,
      Set<String> tableWithAttachmentFieldSet) {
    for (TableValue tableValue : dataset.getTableValues()) {
      // Check if the table with idTableSchema has been populated already
      Long oldTableId = findTableIdByTableSchema(datasetId, tableValue.getIdTableSchema());
      fillTableId(tableValue.getIdTableSchema(), dataset.getTableValues(), oldTableId);
      if (!readOnlyTables.contains(tableValue.getIdTableSchema())
          && !fixedNumberTables.contains(tableValue.getIdTableSchema())) {
        // Put an empty value to the field if it's an attachment type if and only if table has
        // fields of this type
        if (tableWithAttachmentFieldSet.contains(tableValue.getIdTableSchema())) {
          LOG.warn("Table {} and id schema {} contains attachment field, processing",
              tableValue.getId(), tableValue.getIdTableSchema());
          tableValue.getRecords().stream().forEach(r -> {
            r.getFields().stream().forEach(f -> {
              if (DataType.ATTACHMENT.equals(f.getType())) {
                f.setValue("");
              }
              if (null != f.getValue() && f.getValue().length() >= fieldMaxLength) {
                f.setValue(f.getValue().substring(0, fieldMaxLength));
              }
            });
          });
        }
        allRecords.addAll(tableValue.getRecords());
      }
      if (null == oldTableId) {
        tableRepository.saveAndFlush(tableValue);
      }
    }
  }

  /**
   * Etl table for.
   *
   * @param etlDatasetVO the etl dataset VO
   * @param provider the provider
   * @param partition the partition
   * @param tableMap the table map
   * @param fieldMap the field map
   * @param dataset the dataset
   * @param tables the tables
   * @param readOnlyTables the read only tables
   * @param fixedNumberTables the fixed number tables
   * @param datasetType the dataset type
   */
  private void etlTableFor(ETLDatasetVO etlDatasetVO, DataProviderVO provider,
      final PartitionDataSetMetabase partition, Map<String, TableSchema> tableMap,
      Map<String, FieldSchema> fieldMap, DatasetValue dataset, List<TableValue> tables,
      List<String> readOnlyTables, List<String> fixedNumberTables, DatasetTypeEnum datasetType) {
    for (ETLTableVO etlTable : etlDatasetVO.getTables()) {
      etlBuildEntity(provider, partition, tableMap, fieldMap, dataset, tables, etlTable,
          datasetType);
      // Check if table is read Only and save into a list
      TableSchema tableSchema = tableMap.get(etlTable.getTableName().toLowerCase());
      if (tableSchema != null && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        readOnlyTables.add(tableSchema.getIdTableSchema().toString());
      }
      if (tableSchema != null && Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        fixedNumberTables.add(tableSchema.getIdTableSchema().toString());
      }
    }
  }

  /**
   * Gets the table read only. Receives by parameter the datasetId, the objectId and the type
   * (table, record, field). In example, if receives an objectId that is a Record (that's a record
   * schema id), find the property readOnly of the table that belongs to the record
   *
   * @param datasetId the dataset id
   * @param objectId the object id
   * @param type the type
   *
   * @return the table read only
   */
  @Override
  public Boolean getTableReadOnly(Long datasetId, String objectId, EntityTypeEnum type) {
    Boolean readOnly = false;
    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));

    switch (type) {
      case TABLE:
        readOnly = tableForReadOnly(objectId, readOnly, schema);
        break;
      case RECORD:
        readOnly = recordForReadOnly(objectId, readOnly, schema);
        break;
      case FIELD:
        readOnly = fieldForReadOnly(objectId, readOnly, schema);
        break;
      default:
        break;
    }
    return readOnly;
  }

  /**
   * Checks if is dataset reportable. Dataset is reportable when is designDataset in dataflow with
   * status design or reportingDataset in state Draft.
   *
   * @param idDataset the id dataset
   *
   * @return the boolean
   */
  @Override
  public boolean isDatasetReportable(Long idDataset) {
    boolean result = false;
    // Check if dataset is a designDataset
    final Optional<DesignDataset> designDataset = designDatasetRepository.findById(idDataset);
    if (designDataset.isPresent()) {
      DataFlowVO dataflow = getDataflow(idDataset);
      if (TypeStatusEnum.DESIGN.equals(dataflow.getStatus())) {
        result = true;
      } else {
        LOG.info("DesignDataset {} is not reportable because are in dataflow {} with status {}",
            idDataset, dataflow.getId(), dataflow);
      }
    }
    // Check if dataset is a reportingDataset
    if (!result) {
      Optional<ReportingDataset> reportingDataset = reportingDatasetRepository.findById(idDataset);
      if (reportingDataset.isPresent()) {
        DataFlowVO dataflow = getDataflow(idDataset);
        if (TypeStatusEnum.DRAFT.equals(dataflow.getStatus())) {
          result = true;
        } else {
          LOG.info("DesignDataset {} is not reportable because are in dataflow {} with status {}",
              idDataset, dataflow.getId(), dataflow);
        }
      }
    }
    return result;
  }

  /**
   * Copy data.
   *
   * @param dictionaryOriginTargetDatasetsId the dictionary origin target datasets id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  @Override
  public void copyData(Map<Long, Long> dictionaryOriginTargetDatasetsId,
      Map<String, String> dictionaryOriginTargetObjectId) {
    // We've got the dictionary of origin datasetsId and it's new equivalent datasetId from the
    // copied ones
    // We'll load the data from the origin datasetId, modify it using the dictionary to accurate to
    // the target datasetId (like the tableschema) and finally save it
    dictionaryOriginTargetDatasetsId.forEach((Long originDataset, Long targetDataset) -> {

      DesignDataset originDesign =
          designDatasetRepository.findById(originDataset).orElse(new DesignDataset());
      if (StringUtils.isNoneBlank(originDesign.getDatasetSchema())) {

        List<TableSchema> listOfTablesFiltered = getTableFromSchema(originDesign);
        // if there are tables of the origin dataset with tables ToPrefill, then we'll copy the data
        if (!listOfTablesFiltered.isEmpty()) {
          LOG.info("There are data to copy. Copy data from datasetId {} to datasetId {}",
              originDataset, targetDataset);
          List<RecordValue> recordDesignValuesList = new ArrayList<>();
          List<AttachmentValue> attachments = new ArrayList<>();
          recordDesignValuesList = replaceData(originDataset, targetDataset, listOfTablesFiltered,
              dictionaryOriginTargetObjectId, attachments);

          if (!recordDesignValuesList.isEmpty()) {
            // save values
            TenantResolver
                .setTenantName(String.format(LiteralConstants.DATASET_FORMAT_NAME, targetDataset));
            recordRepository.saveAll(recordDesignValuesList);
            // copy attachments too
            if (!attachments.isEmpty()) {
              attachmentRepository.saveAll(attachments);
            }
          }
        }
      }
    });
  }

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the attachment
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public AttachmentValue getAttachment(Long datasetId, String idField) throws EEAException {
    return attachmentRepository.findByFieldValueId(idField);
  }

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteAttachment(Long datasetId, String fieldId) throws EEAException {
    // Delete the attachment
    attachmentRepository.deleteByFieldValueId(fieldId);
    // Put the field value name to null
    FieldValue field = fieldRepository.findById(fieldId);
    field.setValue("");
    fieldRepository.save(field);
  }

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   * @param fileName the file name
   * @param is the is
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public void updateAttachment(Long datasetId, String fieldId, String fileName, InputStream is)
      throws EEAException, IOException {

    FieldValue field = fieldRepository.findById(fieldId);
    // Check if the field is marked as read only
    Document fieldSchema = schemasRepository.findFieldSchema(
        field.getRecord().getTableValue().getDatasetId().getIdDatasetSchema(),
        field.getIdFieldSchema());
    if (!DatasetTypeEnum.DESIGN.equals(getDatasetType(datasetId)) && fieldSchema != null
        && fieldSchema.get(LiteralConstants.READ_ONLY) != null
        && fieldSchema.getBoolean(LiteralConstants.READ_ONLY)) {
      throw new EEAException(EEAErrorMessage.FIELD_READ_ONLY);
    }
    // Attachment table
    AttachmentValue attachment = attachmentRepository.findByFieldValueId(fieldId);
    if (null == attachment) {
      attachment = new AttachmentValue();
      attachment.setFieldValue(field);
    }
    attachment.setFileName(fileName);
    byte[] content;
    content = IOUtils.toByteArray(is);
    is.close();
    attachment.setContent(content);
    attachmentRepository.save(attachment);

    // Field table
    field.setValue(fileName);
    fieldRepository.save(field);
  }

  /**
   * Gets the field by id.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the field by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public FieldVO getFieldById(Long datasetId, String idField) throws EEAException {
    FieldValue fieldValue = fieldRepository.findById(idField);
    if (fieldValue == null) {
      throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);
    }
    return fieldNoValidationMapper.entityToClass(fieldValue);
  }

  /**
   * Delete attachment by field schema id.
   *
   * @param datasetId the dataset id
   * @param fieldSchemaId the field schema id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteAttachmentByFieldSchemaId(Long datasetId, String fieldSchemaId)
      throws EEAException {
    // Delete the attachment
    attachmentRepository.deleteByFieldValueIdFieldSchema(fieldSchemaId);
    // Put the field value name to null
    fieldRepository.clearFieldValue(fieldSchemaId);
  }


  /**
   * Gets the table fixed number of records.
   *
   * @param datasetId the dataset id
   * @param objectId the object id
   * @param type the type
   *
   * @return the table fixed number of records
   */
  @Override
  public Boolean getTableFixedNumberOfRecords(Long datasetId, String objectId,
      EntityTypeEnum type) {
    Boolean fixedNumber = false;
    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));

    switch (type) {
      case TABLE:
        fixedNumber = tableForFixedNumberOfRecords(objectId, fixedNumber, schema);
        break;
      case RECORD:
        fixedNumber = recordForFixedNumberOfRecords(objectId, fixedNumber, schema);
        break;
      case FIELD:
      default:
        break;
    }
    return fixedNumber;
  }


  /**
   * Find record schema id by id.
   *
   * @param datasetId the dataset id
   * @param idRecord the id record
   *
   * @return the string
   */
  @Override
  public String findRecordSchemaIdById(Long datasetId, String idRecord) {
    RecordValue record = recordRepository.findById(idRecord);
    return record.getIdRecordSchema();
  }


  /**
   * Find field schema id by id.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   *
   * @return the string
   */
  @Override
  public String findFieldSchemaIdById(Long datasetId, String idField) {
    FieldValue field = fieldRepository.findById(idField);
    return field.getIdFieldSchema();
  }


  /**
   * Spread data prefill.
   *
   * @param originDatasetDesign the designs
   * @param targetDatasetId the dataset id
   */
  @Override
  public void spreadDataPrefill(DesignDataset originDatasetDesign, Long targetDatasetId) {
    // get tables from schema
    List<TableSchema> listOfTablesFiltered =
        getTablesFromSchema(originDatasetDesign.getDatasetSchema());
    // get the data from designs datasets
    if (!listOfTablesFiltered.isEmpty()) {

      TenantResolver
          .setTenantName(String.format(DATASET_ID, originDatasetDesign.getId().toString()));

      List<RecordValue> targetRecords = new ArrayList<>();

      // fill the data
      DatasetValue ds = new DatasetValue();
      ds.setId(targetDatasetId);

      Optional<PartitionDataSetMetabase> datasetPartition =
          partitionDataSetMetabaseRepository.findFirstByIdDataSet_id(targetDatasetId);
      Long datasetPartitionId = datasetPartition.orElse(new PartitionDataSetMetabase()).getId();
      // attachment values
      List<AttachmentValue> attachments = new ArrayList<>();
      Iterable<AttachmentValue> iterableAttachments = attachmentRepository.findAll();
      iterableAttachments.forEach(attachments::add);
      recordDesingAssignation(targetDatasetId, originDatasetDesign, targetRecords,
          datasetPartitionId, attachments, listOfTablesFiltered);
      if (!targetRecords.isEmpty()) {
        // save values
        TenantResolver.setTenantName(String.format(DATASET_ID, targetDatasetId));
        recordRepository.saveAll(targetRecords);
        // copy attachments too
        if (!attachments.isEmpty()) {
          attachmentRepository.saveAll(attachments);
        }
      }
    }
  }


  /**
   * Creates the lock with signature.
   *
   * @param lockSignature the lock signature
   * @param mapCriteria the map criteria
   * @param userName the user name
   * @throws EEAException the EEA exception
   */
  @Override
  public void createLockWithSignature(LockSignature lockSignature, Map<String, Object> mapCriteria,
      String userName) throws EEAException {
    mapCriteria.put("signature", lockSignature.getValue());
    LockVO lockVO = lockService.findByCriteria(mapCriteria);
    if (lockVO == null) {
      lockService.createLock(new Timestamp(System.currentTimeMillis()), userName, LockType.METHOD,
          mapCriteria);
    }
  }


  /**
   * Record desing assignation.
   *
   * @param targetDatasetId the target dataset id
   * @param originDatasetDesign the origin dataset design
   * @param targetRecords the record design values list
   * @param datasetPartitionId the dataset partition id
   * @param attachments the attachments
   * @param listOfTablesFiltered the list of tables filtered
   */
  private void recordDesingAssignation(Long targetDatasetId, DesignDataset originDatasetDesign,
      List<RecordValue> targetRecords, Long datasetPartitionId, List<AttachmentValue> attachments,
      List<TableSchema> listOfTablesFiltered) {

    Map<String, AttachmentValue> dictionaryIdFieldAttachment = new HashMap<>();
    attachments.forEach(attachment -> {
      if (null != attachment.getFieldValue() && null != attachment.getFieldValue().getId()) {
        dictionaryIdFieldAttachment.put(attachment.getFieldValue().getId(), attachment);
      }
    });

    Long dataProviderId =
        datasetMetabaseService.findDatasetMetabase(targetDatasetId).getDataProviderId();
    final DataProviderVO dataproviderVO =
        null != dataProviderId ? representativeControllerZuul.findDataProviderById(dataProviderId)
            : new DataProviderVO();

    listOfTablesFiltered.stream().forEach(tableSchema -> {
      Integer numberOfFieldsInRecord = tableSchema.getRecordSchema().getFieldSchema().size();

      Pageable fieldValuePage = PageRequest.of(0, 1000 * numberOfFieldsInRecord);

      List<FieldValue> pagedFieldValues;
      Map<String, RecordValue> mapTargetRecordValues = new HashMap<>();
      TableValue targetTable =
          tableRepository.findByIdTableSchema(tableSchema.getIdTableSchema().toString());
      while ((pagedFieldValues = fieldRepository.findByRecord_IdRecordSchema(
          tableSchema.getRecordSchema().getIdRecordSchema().toString(), fieldValuePage))
              .size() > 0) {

        processRecordPage(pagedFieldValues, targetRecords, mapTargetRecordValues,
            dictionaryIdFieldAttachment, targetTable, numberOfFieldsInRecord, dataproviderVO,
            datasetPartitionId, null);
        fieldValuePage = fieldValuePage.next();
      }


    });

  }

  // Method invoked from recordDesingAssignation and replaceData methods, reducing duplicated code

  /**
   * Process record page.
   *
   * @param pagedFieldValues the paged field values
   * @param targetRecords the target records
   * @param mapTargetRecordValues the map target record values
   * @param dictionaryIdFieldAttachment the dictionary id field attachment
   * @param targetTable the target table
   * @param numberOfFieldsInRecord the number of fields in record
   * @param dataproviderVO the dataprovider VO
   * @param datasetPartitionId the dataset partition id
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   */
  // and Cognitive Complexity
  private void processRecordPage(List<FieldValue> pagedFieldValues, List<RecordValue> targetRecords,
      Map<String, RecordValue> mapTargetRecordValues,
      Map<String, AttachmentValue> dictionaryIdFieldAttachment, TableValue targetTable,
      Integer numberOfFieldsInRecord, DataProviderVO dataproviderVO, Long datasetPartitionId,
      Map<String, String> dictionaryOriginTargetObjectId) {
    // make list of field vaues grouped by their record id. The field values will be set with
    // the taget schemas id so they can be inserted
    pagedFieldValues.stream().forEach(field -> {
      FieldValue auxField = new FieldValue();
      auxField.setValue(field.getValue());
      String targetIdRecordSchema = null;

      if (null == dictionaryOriginTargetObjectId) {// called from recordDesingAssignation
        auxField.setIdFieldSchema(field.getIdFieldSchema());
        targetIdRecordSchema = field.getRecord().getIdRecordSchema();
      } else {// called from replaceData
        auxField.setIdFieldSchema(dictionaryOriginTargetObjectId.get(field.getIdFieldSchema()));
        // transform the grouping record in the target one. Do it only once
        targetIdRecordSchema =
            dictionaryOriginTargetObjectId.get(field.getRecord().getIdRecordSchema());
      }

      auxField.setType(field.getType());

      String originRecordId = field.getRecord().getId();
      if (!mapTargetRecordValues.containsKey(originRecordId)) {

        RecordValue targetRecordValue = new RecordValue();
        if (null != dataproviderVO) { // called from recordDesingAssignation
          targetRecordValue.setDataProviderCode(dataproviderVO.getCode());
        }
        targetRecordValue.setDatasetPartitionId(datasetPartitionId);
        targetRecordValue.setIdRecordSchema(targetIdRecordSchema);
        targetRecordValue.setTableValue(targetTable);
        targetRecordValue.setFields(new ArrayList<>());
        // using temporary recordId as grouping criteria, then it will be removed before giving
        // back
        mapTargetRecordValues.put(originRecordId, targetRecordValue);
        targetRecords.add(targetRecordValue);
      }

      auxField.setRecord(mapTargetRecordValues.get(originRecordId));

      if (DataType.ATTACHMENT.equals(field.getType())) {
        if (dictionaryIdFieldAttachment.containsKey(field.getId())) {
          dictionaryIdFieldAttachment.get(field.getId()).setFieldValue(auxField);
          dictionaryIdFieldAttachment.get(field.getId()).setId(null);
        }

      }
      auxField.setGeometry(field.getGeometry());
      mapTargetRecordValues.get(originRecordId).getFields().add(auxField);
      // when the record has reached the number of fields per record then remove from the map to
      // avoid rehashing
      if (mapTargetRecordValues.get(originRecordId).getFields().size() == numberOfFieldsInRecord) {
        mapTargetRecordValues.remove(originRecordId);
      }
    });
  }

  /**
   * Gets the tables from schema.
   *
   * @param idDatasetSchema the id dataset schema
   *
   * @return the tables from schema
   */
  private List<TableSchema> getTablesFromSchema(String idDatasetSchema) {
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(idDatasetSchema));
    List<TableSchema> listOfTables = schema.getTableSchemas();
    List<TableSchema> listOfTablesFiltered = new ArrayList<>();
    for (TableSchema desingTableToPrefill : listOfTables) {
      if (Boolean.TRUE.equals(desingTableToPrefill.getToPrefill())) {
        listOfTablesFiltered.add(desingTableToPrefill);
      }
    }
    return listOfTablesFiltered;
  }

  /**
   * Gets the dataflow.
   *
   * @param idDataset the id dataset
   *
   * @return the dataflow
   */
  private DataFlowVO getDataflow(Long idDataset) {
    // Get the dataFlowId from the metabase
    Long dataflowId = getDataFlowIdById(idDataset);
    // get de dataflow
    return dataflowControllerZuul.getMetabaseById(dataflowId);
  }

  /**
   * Creates field values for a given record.
   *
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param attachments the attachments
   * @param recordAux the record aux
   * @param fieldValues the field values
   * @param fieldValuesOnlyValues the field values only values
   */
  private void createFieldValueForRecord(Map<String, String> dictionaryOriginTargetObjectId,
      Map<String, AttachmentValue> attachments, RecordValue recordAux, List<FieldValue> fieldValues,
      List<FieldValue> fieldValuesOnlyValues) {
    for (FieldValue field : fieldValues) {
      FieldValue auxField = new FieldValue();
      auxField.setValue(field.getValue());
      auxField.setIdFieldSchema(dictionaryOriginTargetObjectId.get(field.getIdFieldSchema()));
      auxField.setType(field.getType());
      auxField.setRecord(recordAux);
      fieldValuesOnlyValues.add(auxField);
      if (DataType.ATTACHMENT.equals(field.getType())) {
        if (attachments.containsKey(field.getId())) {
          attachments.get(field.getId()).setFieldValue(auxField);
          attachments.get(field.getId()).setId(null);
        }

      }
    }
  }

  /**
   * Obtain partition.
   *
   * @param datasetId the dataset id
   * @param user the user
   *
   * @return the partition data set metabase
   *
   * @throws EEAException the EEA exception
   */
  private PartitionDataSetMetabase obtainPartition(final Long datasetId, final String user)
      throws EEAException {
    final PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
        .findFirstByIdDataSet_idAndUsername(datasetId, user).orElse(null);
    if (partition == null) {
      LOG_ERROR.error(EEAErrorMessage.PARTITION_ID_NOTFOUND);
      throw new EEAException(EEAErrorMessage.PARTITION_ID_NOTFOUND);
    }
    return partition;
  }

  /**
   * Validate file type.
   *
   * @param mimeType the mime type
   *
   * @throws EEAException the EEA exception
   */
  private void validateFileType(final String mimeType) throws EEAException {
    // files that will be accepted: csv, xml, xls, xlsx
    switch (mimeType) {
      case "csv":
        break;
      case "xml":
        break;
      case "xls":
        break;
      case "xlsx":
        break;
      default:
        throw new InvalidFileException(EEAErrorMessage.FILE_FORMAT);
    }
  }

  /**
   * Retrieve validations.
   *
   * @param recordVOs the record V os
   */
  private void retrieveValidations(List<RecordVO> recordVOs) {
    // retrieve validations to set them into the final result
    List<String> recordIds = recordVOs.stream().map(RecordVO::getId).collect(Collectors.toList());
    Map<String, List<FieldValidation>> fieldValidations = this.getFieldValidations(recordIds);
    Map<String, List<RecordValidation>> recordValidations = this.getRecordValidations(recordIds);
    recordVOs.stream().forEach(record -> {
      record.getFields().stream().forEach(field -> {
        List<FieldValidationVO> validations =
            fieldValidationMapper.entityListToClass(fieldValidations.get(field.getId()));
        field.setFieldValidations(validations);
        if (null != validations && !validations.isEmpty()) {
          field.setLevelError(
              validations.stream().map(validation -> validation.getValidation().getLevelError())
                  .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                  .orElse(ErrorTypeEnum.WARNING));
        }
      });

      List<RecordValidationVO> validations =
          recordValidationMapper.entityListToClass(recordValidations.get(record.getId()));
      record.setRecordValidations(validations);
      if (null != validations && !validations.isEmpty()) {
        record.setLevelError(
            validations.stream().map(validation -> validation.getValidation().getLevelError())
                .filter(error -> error.equals(ErrorTypeEnum.ERROR)).findFirst()
                .orElse(ErrorTypeEnum.WARNING));
      }
    });
  }

  /**
   * Calculate pageable.
   *
   * @param pageable the pageable
   * @param totalRecords the total records
   *
   * @return the pageable
   */
  private Pageable calculatePageable(Pageable pageable, Long totalRecords) {
    if (pageable == null && totalRecords > 0) {
      pageable = PageRequest.of(0, totalRecords.intValue());
    }
    if (pageable == null && totalRecords == 0) {
      pageable = PageRequest.of(0, 20);
    }
    return pageable;
  }

  /**
   * Calculated errors and records to see.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param commonShortFields the common short fields
   * @param mapFields the map fields
   * @param sortFieldsArray the sort fields array
   * @param newFields the new fields
   * @param result the result
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table VO
   */
  private TableVO calculatedErrorsAndRecordsToSee(final Long datasetId, final String idTableSchema,
      Pageable pageable, final String fields, ErrorTypeEnum[] levelError,
      List<String> commonShortFields, Map<String, Integer> mapFields,
      List<SortField> sortFieldsArray, SortField[] newFields, TableVO result, String[] idRules,
      String fieldSchema, String fieldValue) {
    List<RecordValue> records;
    if (null == fields && (null == levelError || levelError.length == 5)
        && (idRules == null || idRules.length == 0) && fieldSchema == null && fieldValue == null) {
      records = recordRepository.findByTableValueNoOrder(idTableSchema, pageable);
      List<RecordVO> recordVOs = recordNoValidationMapper.entityListToClass(records);
      result.setTotalFilteredRecords(0L);
      result.setRecords(recordVOs);
    } else {
      result = fieldsMap(datasetId, idTableSchema, pageable, fields, levelError, commonShortFields,
          mapFields, sortFieldsArray, newFields, idRules, fieldSchema, fieldValue);
    }
    return result;
  }

  /**
   * Fields map.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   * @param commonShortFields the common short fields
   * @param mapFields the map fields
   * @param sortFieldsArray the sort fields array
   * @param newFields the new fields
   * @param idRules the id rules
   * @param fieldSchema the field schema
   * @param fieldValue the field value
   *
   * @return the table VO
   */
  private TableVO fieldsMap(final Long datasetId, final String idTableSchema, Pageable pageable,
      final String fields, ErrorTypeEnum[] levelError, List<String> commonShortFields,
      Map<String, Integer> mapFields, List<SortField> sortFieldsArray, SortField[] newFields,
      String[] idRules, String fieldSchema, String fieldValue) {
    TableVO result;
    if (null != fields) {

      String[] pairs = fields.split(",");
      for (int i = 0; i < pairs.length; i++) {
        String pair = pairs[i];
        String[] keyValue = pair.split(":");
        mapFields.put(keyValue[0], Integer.valueOf(keyValue[1]));
        commonShortFields.add(keyValue[0]);
      }

      for (String nameField : commonShortFields) {
        FieldValue typefield = fieldRepository.findFirstTypeByIdFieldSchema(nameField);
        SortField sortField = new SortField();
        sortField.setFieldName(nameField);
        sortField.setAsc(mapFields.get(nameField) == 1);
        if (null == typefield) {
          sortField.setTypefield(DataType.TEXT);
        } else {
          sortField.setTypefield(typefield.getType());
        }

        sortFieldsArray.add(sortField);
      }
      newFields = sortFieldsArray.stream().toArray(SortField[]::new);
    }

    result = recordRepository.findByTableValueWithOrder(datasetId, idTableSchema,
        Arrays.asList(levelError), pageable, idRules != null ? Arrays.asList(idRules) : null,
        fieldSchema, fieldValue, newFields);
    return result;
  }

  /**
   * Retrieve record value.
   *
   * @param idTableSchema the id table schema
   * @param idFieldSchema the id field schema
   *
   * @return the list
   */
  @Deprecated
  private List<RecordValue> retrieveRecordValue(String idTableSchema, String idFieldSchema) {
    Optional.ofNullable(idFieldSchema).ifPresent(field -> SortFieldsHelper.setSortingField(field));
    List<RecordValue> records = null;
    try {
      records = recordRepository.findByTableValueIdTableSchema(idTableSchema);
    } finally {
      SortFieldsHelper.cleanSortingField();
    }

    return records;
  }

  /**
   * Sanitize records.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(final List<RecordValue> records) {
    List<RecordValue> sanitizedRecords = new ArrayList<>();
    if (records != null && !records.isEmpty()) {
      Set<String> processedRecords = new HashSet<>();
      for (RecordValue recordValue : records) {
        if (!processedRecords.contains(recordValue.getId())) {
          processedRecords.add(recordValue.getId());
          sanitizedRecords.add(recordValue);
        }

      }
    }
    return sanitizedRecords;
  }

  /**
   * Process table stats.
   *
   * @param tableValue the table value
   * @param datasetId the dataset id
   * @param mapIdNameDatasetSchema the map id name dataset schema
   *
   * @return the list
   */
  private List<Statistics> processTableStats(final TableValue tableValue, final Long datasetId,
      final Map<String, String> mapIdNameDatasetSchema) {

    Set<Long> recordIdsFromRecordWithValidationBlocker =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.BLOCKER);

    Set<Long> recordIdsFromFieldWithValidationBlocker =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.BLOCKER);

    Set<Long> recordIdsFromRecordWithValidationError =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.ERROR);

    Set<Long> recordIdsFromFieldWithValidationError =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.ERROR);

    Set<Long> recordIdsFromRecordWithValidationWarning =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.WARNING);

    Set<Long> recordIdsFromFieldWithValidationWarning =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.WARNING);

    Set<Long> recordIdsFromRecordWithValidationInfo =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.INFO);

    Set<Long> recordIdsFromFieldWithValidationInfo =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), ErrorTypeEnum.INFO);

    Set<Long> idsBlockers = new HashSet<>();
    idsBlockers.addAll(recordIdsFromRecordWithValidationBlocker);
    idsBlockers.addAll(recordIdsFromFieldWithValidationBlocker);

    Set<Long> idsErrors = new HashSet<>();
    idsErrors.addAll(recordIdsFromRecordWithValidationError);
    idsErrors.addAll(recordIdsFromFieldWithValidationError);

    Set<Long> idsWarnings = new HashSet<>();
    idsWarnings.addAll(recordIdsFromRecordWithValidationWarning);
    idsWarnings.addAll(recordIdsFromFieldWithValidationWarning);

    Set<Long> idsInfos = new HashSet<>();
    idsInfos.addAll(recordIdsFromRecordWithValidationInfo);
    idsInfos.addAll(recordIdsFromFieldWithValidationInfo);

    idsErrors.removeAll(idsBlockers);
    idsWarnings.removeAll(idsBlockers);
    idsWarnings.removeAll(idsErrors);
    idsInfos.removeAll(idsBlockers);
    idsInfos.removeAll(idsErrors);
    idsInfos.removeAll(idsWarnings);

    Long totalRecordsWithBlockers = Long.valueOf(idsBlockers.size());
    Long totalRecordsWithErrors = Long.valueOf(idsErrors.size());
    Long totalRecordsWithWarnings = Long.valueOf(idsWarnings.size());
    Long totalRecordsWithInfos = Long.valueOf(idsInfos.size());

    TableStatisticsVO tableStats = new TableStatisticsVO();
    tableStats.setIdTableSchema(tableValue.getIdTableSchema());
    Long countRecords = tableRepository.countRecordsByIdTableSchema(tableValue.getIdTableSchema());
    tableStats.setTotalRecords(countRecords);

    Long totalTableErrors = totalRecordsWithBlockers + totalRecordsWithErrors
        + totalRecordsWithWarnings + totalRecordsWithInfos;

    totalTableErrors = totalTableErrors + tableValue.getTableValidations().size();

    tableStats.setNameTableSchema(mapIdNameDatasetSchema.get(tableValue.getIdTableSchema()));
    tableStats.setTotalErrors(totalTableErrors);
    tableStats.setTotalRecordsWithBlockers(totalRecordsWithBlockers);
    tableStats.setTotalRecordsWithErrors(totalRecordsWithErrors);
    tableStats.setTotalRecordsWithWarnings(totalRecordsWithWarnings);
    tableStats.setTotalRecordsWithInfos(totalRecordsWithInfos);
    tableStats.setTableErrors(totalTableErrors > 0);

    List<Statistics> stats = new ArrayList<>();

    Statistics statsIdTable = fillStat(datasetId, tableValue.getIdTableSchema(), "idTableSchema",
        tableValue.getIdTableSchema());

    Statistics statsNameTable = fillStat(datasetId, tableValue.getIdTableSchema(),
        "nameTableSchema", mapIdNameDatasetSchema.get(tableValue.getIdTableSchema()));

    Statistics statsTotalTableError = fillStat(datasetId, tableValue.getIdTableSchema(),
        "totalErrors", totalTableErrors.toString());

    Statistics statsTotalRecords =
        fillStat(datasetId, tableValue.getIdTableSchema(), "totalRecords", countRecords.toString());

    Statistics statsTotalRecordsWithBlockers = fillStat(datasetId, tableValue.getIdTableSchema(),
        "totalRecordsWithBlockers", totalRecordsWithBlockers.toString());

    Statistics statsTotalRecordsWithErrors = fillStat(datasetId, tableValue.getIdTableSchema(),
        "totalRecordsWithErrors", totalRecordsWithErrors.toString());

    Statistics statsTotalRecordsWithWarnings = fillStat(datasetId, tableValue.getIdTableSchema(),
        "totalRecordsWithWarnings", totalRecordsWithWarnings.toString());

    Statistics statsTotalRecordsWithInfos = fillStat(datasetId, tableValue.getIdTableSchema(),
        "totalRecordsWithInfos", totalRecordsWithInfos.toString());

    Statistics statsTableErrors = new Statistics();
    statsTableErrors.setIdTableSchema(tableValue.getIdTableSchema());
    statsTableErrors.setStatName("tableErrors");
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(datasetId);
    statsTableErrors.setDataset(reporting);
    if (tableValue.getTableValidations() != null && !tableValue.getTableValidations().isEmpty()) {
      statsTableErrors.setValue("true");
    } else {
      statsTableErrors.setValue(totalTableErrors > 0 ? "true" : "false");
    }

    stats.add(statsIdTable);
    stats.add(statsNameTable);
    stats.add(statsTotalTableError);
    stats.add(statsTotalRecords);
    stats.add(statsTotalRecordsWithBlockers);
    stats.add(statsTotalRecordsWithErrors);
    stats.add(statsTotalRecordsWithWarnings);
    stats.add(statsTotalRecordsWithInfos);
    stats.add(statsTableErrors);

    return stats;
  }

  /**
   * Fill stat.
   *
   * @param idDataset the id dataset
   * @param idTableSchema the id table schema
   * @param statName the stat name
   * @param value the value
   *
   * @return the statistics
   */
  private Statistics fillStat(Long idDataset, String idTableSchema, String statName, String value) {

    Statistics stat = new Statistics();
    ReportingDataset reporting = new ReportingDataset();
    reporting.setId(idDataset);
    stat.setDataset(reporting);
    stat.setIdTableSchema(idTableSchema);
    stat.setStatName(statName);
    stat.setValue(value);

    return stat;
  }

  /**
   * Gets the field validations.
   *
   * @param recordIds the record ids
   *
   * @return the field validations
   */
  private Map<String, List<FieldValidation>> getFieldValidations(final List<String> recordIds) {
    List<FieldValidation> fieldValidations =
        this.fieldValidationRepository.findByFieldValue_RecordIdIn(recordIds);

    Map<String, List<FieldValidation>> result = new HashMap<>();

    fieldValidations.stream().forEach(fieldValidation -> {
      if (!result.containsKey(fieldValidation.getFieldValue().getId())) {
        result.put(fieldValidation.getFieldValue().getId(), new ArrayList<>());
      }
      result.get(fieldValidation.getFieldValue().getId()).add(fieldValidation);
    });

    return result;
  }

  /**
   * Gets the record validations.
   *
   * @param recordIds the record ids
   *
   * @return the record validations
   */
  private Map<String, List<RecordValidation>> getRecordValidations(final List<String> recordIds) {

    List<RecordValidation> recordValidations =
        this.recordValidationRepository.findByRecordValueIdIn(recordIds);

    Map<String, List<RecordValidation>> result = new HashMap<>();

    recordValidations.stream().forEach(recordValidation -> {
      if (!result.containsKey(recordValidation.getRecordValue().getId())) {
        result.put(recordValidation.getRecordValue().getId(), new ArrayList<>());
      }
      result.get(recordValidation.getRecordValue().getId()).add(recordValidation);
    });

    return result;
  }

  /**
   * Etl build entity.
   *
   * @param provider the provider
   * @param partition the partition
   * @param tableMap the table map
   * @param fieldMap the field map
   * @param dataset the dataset
   * @param tables the tables
   * @param etlTable the etl table
   * @param datasetType the dataset type
   */
  private void etlBuildEntity(DataProviderVO provider, final PartitionDataSetMetabase partition,
      Map<String, TableSchema> tableMap, Map<String, FieldSchema> fieldMap, DatasetValue dataset,
      List<TableValue> tables, ETLTableVO etlTable, DatasetTypeEnum datasetType) {
    TableValue table = new TableValue();
    TableSchema tableSchema = tableMap.get(etlTable.getTableName().toLowerCase());
    if (tableSchema != null) {
      table.setIdTableSchema(tableSchema.getIdTableSchema().toString());
      List<RecordValue> records = new ArrayList<>();
      for (ETLRecordVO etlRecord : etlTable.getRecords()) {
        RecordValue recordValue = new RecordValue();
        recordValue.setIdRecordSchema(tableMap.get(etlTable.getTableName().toLowerCase())
            .getRecordSchema().getIdRecordSchema().toString());
        recordValue.setTableValue(table);
        List<FieldValue> fieldValues = new ArrayList<>();
        List<String> idSchema = new ArrayList<>();
        etlFieldBuildFor(fieldMap, dataset, tableSchema, etlRecord, recordValue, fieldValues,
            idSchema, datasetType);
        // set the fields if not declared in the records
        setMissingField(
            tableMap.get(etlTable.getTableName().toLowerCase()).getRecordSchema().getFieldSchema(),
            fieldValues, idSchema, recordValue);
        recordValue.setFields(fieldValues);
        recordValue.setDatasetPartitionId(partition.getId());
        recordValue.setDataProviderCode(provider != null ? provider.getCode() : null);
        records.add(recordValue);
      }
      table.setRecords(records);
      tables.add(table);
      table.setDatasetId(dataset);
    }
  }

  /**
   * Etl field build for.
   *
   * @param fieldMap the field map
   * @param dataset the dataset
   * @param tableSchema the table schema
   * @param etlRecord the etl record
   * @param recordValue the record value
   * @param fieldValues the field values
   * @param idFieldSchemas the id schema
   * @param datasetType the dataset type
   */
  private void etlFieldBuildFor(Map<String, FieldSchema> fieldMap, DatasetValue dataset,
      TableSchema tableSchema, ETLRecordVO etlRecord, RecordValue recordValue,
      List<FieldValue> fieldValues, List<String> idFieldSchemas, DatasetTypeEnum datasetType) {
    for (ETLFieldVO etlField : etlRecord.getFields()) {
      FieldValue field = new FieldValue();
      FieldSchema fieldSchema =
          fieldMap.get(etlField.getFieldName().toLowerCase() + tableSchema.getIdTableSchema());
      // Fill if is a design dataset or if not readonly
      if (fieldSchema != null && (DatasetTypeEnum.DESIGN.equals(datasetType)
          || !Boolean.TRUE.equals(fieldSchema.getReadOnly()))) {
        field.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
        field.setType(fieldSchema.getType());
        field.setValue(etlField.getValue());
        field.setRecord(recordValue);
        fieldValues.add(field);
        idFieldSchemas.add(field.getIdFieldSchema());
      }
    }
  }

  /**
   * Fill table id.
   *
   * @param idTableSchema the id table schema
   * @param listTableValues the list table values
   * @param oldTableId the old table id
   */
  private void fillTableId(final String idTableSchema, final List<TableValue> listTableValues,
      Long oldTableId) {
    if (oldTableId != null) {
      listTableValues.stream()
          .filter(tableValue -> tableValue.getIdTableSchema().equals(idTableSchema))
          .forEach(tableValue -> tableValue.setId(oldTableId));
    }
  }

  /**
   * Sets the missing field.
   *
   * @param headersSchema the headers schema
   * @param fields the fields
   * @param idSchema the id schema
   * @param recordValue the record value
   */
  private void setMissingField(List<FieldSchema> headersSchema, final List<FieldValue> fields,
      List<String> idSchema, RecordValue recordValue) {
    headersSchema.stream().forEach(header -> {
      if (!idSchema.contains(header.getIdFieldSchema().toString())) {
        final FieldValue field = new FieldValue();
        field.setIdFieldSchema(header.getIdFieldSchema().toString());
        field.setType(header.getType());
        field.setValue("");
        field.setRecord(recordValue);
        fields.add(field);
      }
    });
  }

  /**
   * Table for read only.
   *
   * @param objectId the object id
   * @param readOnly the read only
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean tableForReadOnly(String objectId, Boolean readOnly, DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getIdTableSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        readOnly = true;
        break;
      }
    }
    return readOnly;
  }

  /**
   * Table for fixed number of records.
   *
   * @param objectId the object id
   * @param fixedNumber the fixed number
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean tableForFixedNumberOfRecords(String objectId, Boolean fixedNumber,
      DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getIdTableSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        fixedNumber = true;
        break;
      }
    }
    return fixedNumber;
  }

  /**
   * Record for read only.
   *
   * @param objectId the object id
   * @param readOnly the read only
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean recordForReadOnly(String objectId, Boolean readOnly, DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getRecordSchema().getIdRecordSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
        readOnly = true;
        break;
      }
    }
    return readOnly;
  }


  /**
   * Record for fixed number of records.
   *
   * @param objectId the object id
   * @param fixedNumber the fixed number
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean recordForFixedNumberOfRecords(String objectId, Boolean fixedNumber,
      DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (objectId.equals(tableSchema.getRecordSchema().getIdRecordSchema().toString())
          && Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
        fixedNumber = true;
        break;
      }
    }
    return fixedNumber;
  }


  /**
   * Field for read only.
   *
   * @param objectId the object id
   * @param readOnly the read only
   * @param schema the schema
   *
   * @return the boolean
   */
  private Boolean fieldForReadOnly(String objectId, Boolean readOnly, DataSetSchema schema) {
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
        if (objectId.equals(fieldSchema.getIdFieldSchema().toString())
            && Boolean.TRUE.equals(tableSchema.getReadOnly())) {
          readOnly = true;
          break;
        }
      }
    }
    return readOnly;
  }

  /**
   * Gets the table from schema.
   *
   * @param originDesign the origin design
   *
   * @return the table from schema
   */
  private List<TableSchema> getTableFromSchema(DesignDataset originDesign) {
    // get tables from schema
    DataSetSchema schema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(originDesign.getDatasetSchema()));
    List<TableSchema> listOfTables = schema.getTableSchemas();
    List<TableSchema> listOfTablesFiltered = new ArrayList<>();
    for (TableSchema desingTableToPrefill : listOfTables) {
      if (Boolean.TRUE.equals(desingTableToPrefill.getToPrefill())) {
        listOfTablesFiltered.add(desingTableToPrefill);
      }
    }
    return listOfTablesFiltered;
  }

  /**
   * Replace data.
   *
   * @param originDataset the origin dataset
   * @param targetDataset the target dataset
   * @param listOfTablesFiltered the list of tables filtered
   * @param dictionaryOriginTargetObjectId the dictionary origin target object id
   * @param attachments the attachments
   *
   * @return the list
   */
  private List<RecordValue> replaceData(Long originDataset, Long targetDataset,
      List<TableSchema> listOfTablesFiltered, Map<String, String> dictionaryOriginTargetObjectId,
      List<AttachmentValue> attachments) {
    List<RecordValue> result = new ArrayList<>();
    TenantResolver.setTenantName(
        String.format(LiteralConstants.DATASET_FORMAT_NAME, originDataset.toString()));

    // attachment values
    Iterable<AttachmentValue> iterableAttachments = attachmentRepository.findAll();
    Map<String, AttachmentValue> dictionaryIdFieldAttachment = new HashMap<>();
    iterableAttachments.forEach(attachment -> {
      if (null != attachment.getFieldValue() && null != attachment.getFieldValue().getId()) {
        dictionaryIdFieldAttachment.put(attachment.getFieldValue().getId(), attachment);
      }
    });

    // fill the data
    DatasetValue ds = new DatasetValue();
    ds.setId(targetDataset);

    Optional<PartitionDataSetMetabase> datasetPartition =
        partitionDataSetMetabaseRepository.findFirstByIdDataSet_id(targetDataset);
    final Long datasetPartitionId =
        datasetPartition.isPresent() ? datasetPartition.get().getId() : null;

    Map<String, RecordValue> mapTargetRecordValues = new HashMap<>();
    for (TableSchema desingTable : listOfTablesFiltered) {
      // Get number of fields per record. Doing it on origin table schema as origin and target has
      // the same structure
      Integer numberOfFieldsInRecord = desingTable.getRecordSchema().getFieldSchema().size();

      // get target table by translating origing table schema into target table schema and then
      // query to target database
      TenantResolver.setTenantName(
          String.format(LiteralConstants.DATASET_FORMAT_NAME, targetDataset.toString()));
      TableValue targetTable = tableRepository.findByIdTableSchema(
          dictionaryOriginTargetObjectId.get(desingTable.getIdTableSchema().toString()));

      TenantResolver.setTenantName(
          String.format(LiteralConstants.DATASET_FORMAT_NAME, originDataset.toString()));

      // creating a first page of 1000 records, this means 1000*Number Of Fields in a Record

      Pageable fieldValuePage = PageRequest.of(0, 1000 * numberOfFieldsInRecord);

      List<FieldValue> pagedFieldValues;

      // run through the origin table, getting its records and fields and translating them into the
      // new schema
      while ((pagedFieldValues = fieldRepository.findByRecord_IdRecordSchema(
          desingTable.getRecordSchema().getIdRecordSchema().toString(), fieldValuePage))
              .size() > 0) {
        LOG.info(
            "Processing page {} with {} records of {} fields from Table {} with table schema {} from Dataset {} and Target Dataset {} ",
            fieldValuePage.getPageNumber(), pagedFieldValues.size() / numberOfFieldsInRecord,
            numberOfFieldsInRecord, desingTable.getNameTableSchema(),
            desingTable.getIdTableSchema(), originDataset, targetDataset);
        // For this, the best is getting fields in big completed sets and assign them to the records
        // to avoid excessive queries to bd

        processRecordPage(pagedFieldValues, result, mapTargetRecordValues,
            dictionaryIdFieldAttachment, targetTable, numberOfFieldsInRecord, null,
            datasetPartitionId, dictionaryOriginTargetObjectId);

        fieldValuePage = fieldValuePage.next();
        TenantResolver.setTenantName(
            String.format(LiteralConstants.DATASET_FORMAT_NAME, originDataset.toString()));
      }

    }

    attachments.addAll(dictionaryIdFieldAttachment.values());
    return result;
  }


  /**
   * Creates the records.
   *
   * @param datasetId the dataset id
   * @param dataProviderCode the data provider code
   * @param recordVOs the record V os
   * @param datasetType the dataset type
   * @param tableSchema the table schema
   *
   * @return the list
   */
  private List<RecordValue> createRecords(Long datasetId, String dataProviderCode,
      List<RecordVO> recordVOs, DatasetTypeEnum datasetType, TableSchema tableSchema) {

    String tableSchemaId = tableSchema.getIdTableSchema().toString();
    Long datasetPartitionId =
        partitionDataSetMetabaseRepository.findFirstByIdDataSet_idAndUsername(datasetId, USER)
            .orElse(new PartitionDataSetMetabase()).getId();

    DatasetValue dataset = new DatasetValue();
    dataset.setId(datasetId);

    TableValue tableValue = new TableValue();
    tableValue.setId(tableRepository.findIdByIdTableSchema(tableSchemaId));
    tableValue.setDatasetId(dataset);

    List<RecordValue> recordValues = new ArrayList<>();

    // Rebuild each record to ensure it contains proper fields
    for (RecordVO recordVO : recordVOs) {
      List<FieldVO> fieldVOs = recordVO.getFields();
      List<FieldValue> fieldValues = new ArrayList<>();

      RecordValue recordValue = new RecordValue();
      recordValue.setDataProviderCode(dataProviderCode);
      recordValue.setDatasetPartitionId(datasetPartitionId);
      recordValue.setTableValue(tableValue);
      recordValue.setFields(fieldValues);
      recordValue.setIdRecordSchema(tableSchema.getRecordSchema().getIdRecordSchema().toString());
      recordValues.add(recordValue);

      for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
        FieldValue fieldValue = new FieldValue();
        fieldValue.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
        fieldValue.setType(fieldSchema.getType());
        fieldValue.setValue(filterFieldValue(fieldSchema, datasetType, fieldVOs));
        fieldValue.setRecord(recordValue);
        fieldValues.add(fieldValue);
      }
    }
    // Force last database pointer position
    recordRepository.findLastRecord();
    return recordValues;
  }

  /**
   * Filter field value.
   *
   * @param fieldSchema the field schema
   * @param datasetType the dataset type
   * @param fieldVOs the field V os
   *
   * @return the string
   */
  private String filterFieldValue(FieldSchema fieldSchema, DatasetTypeEnum datasetType,
      List<FieldVO> fieldVOs) {

    String value = "";
    String fieldSchemaId = fieldSchema.getIdFieldSchema().toString();
    DataType dataType = fieldSchema.getType();

    // Skip if write is not allowed
    if (Boolean.TRUE.equals(fieldSchema.getReadOnly())
        && !DatasetTypeEnum.DESIGN.equals(datasetType)) {
      return value;
    }

    // Find the fieldVO with the fieldSchemaId if exists
    for (FieldVO fieldVO : fieldVOs) {
      if (fieldSchemaId.equals(fieldVO.getIdFieldSchema())) {
        if (null != fieldVO.getValue()) {
          value = fieldVO.getValue();

          // Sort values if there are multiple
          if (DataType.MULTISELECT_CODELIST.equals(dataType) || (DataType.LINK.equals(dataType)
              && Boolean.TRUE.equals(fieldSchema.getPkHasMultipleValues()))) {
            String[] values = value.trim().split("\\s*,\\s*");
            Arrays.sort(values);
            value = Arrays.stream(values).collect(Collectors.joining(", "));
          }
        }
        fieldVOs.remove(fieldVO);
        break;
      }
    }

    // Cut string value to maximum length
    if (value.length() > fieldMaxLength) {
      value = value.substring(0, fieldMaxLength);
    }

    return value;
  }

  /**
   * Gets the dataset type, if it's a design, reporting, datacollection or eudataset .
   *
   * @param datasetId the dataset id
   *
   * @return the dataset type
   */
  @Override
  public DatasetTypeEnum getDatasetType(Long datasetId) {
    DatasetTypeEnum type = null;
    if (reportingDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.REPORTING;
    } else if (designDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.DESIGN;
    } else if (dataCollectionRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.COLLECTION;
    } else if (dataSetMetabaseRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.EUDATASET;
    }
    return type;
  }

  /**
   * Update cascade PK.
   *
   * @param recordValueId the record value id
   * @param fieldValues the field values
   *
   * @throws EEAException the EEA exception
   */
  private void updateCascadePK(final String recordValueId, final List<FieldValue> fieldValues)
      throws EEAException {

    // we took recordValue to take more information necesary to calculate pk
    RecordValue record = recordRepository.findById(recordValueId);

    // we bring the schema
    Document recordSchemaDocument = schemasRepository.findRecordSchema(
        record.getTableValue().getDatasetId().getIdDatasetSchema(),
        record.getTableValue().getIdTableSchema());
    // get fks to delete
    if (null != recordSchemaDocument) {
      List<?> fieldSchemasList =
          (ArrayList<?>) recordSchemaDocument.get(LiteralConstants.FIELD_SCHEMAS);

      Document fieldSchemaDocument = null;
      String idListOfSinglePamsField = null;
      for (Object document : fieldSchemasList) {
        if (((Document) document).get(LiteralConstants.PK) != null
            && ((Document) document).getBoolean(LiteralConstants.PK)) {
          fieldSchemaDocument = (Document) document;
        }
        if (((Document) document).get(HEADER_NAME) != null
            && ((Document) document).getString(HEADER_NAME).equals("ListOfSinglePams")) {
          idListOfSinglePamsField = ((Document) document).get("_id").toString();
        }
      }
      // we look if the record to update have any pk
      if (null != fieldSchemaDocument) {
        updatePKsValues(fieldValues, fieldSchemaDocument, idListOfSinglePamsField);
      }
    }
  }

  /**
   * Update Pks values in cascade when we update the id.
   *
   * @param fieldValues the field values
   * @param fieldSchemaDocument the field schema document
   * @param idListOfSinglePamsField the id list of single pams field
   *
   * @throws EEAException the EEA exception
   */
  private void updatePKsValues(final List<FieldValue> fieldValues, Document fieldSchemaDocument,
      String idListOfSinglePamsField) throws EEAException {
    // we took the both fields, database and filled in new record
    String idFieldSchema = fieldSchemaDocument.get(LiteralConstants.ID).toString();
    FieldValue fieldValueInRecord = fieldValues.stream()
        .filter(field -> field.getIdFieldSchema().equals(idFieldSchema)).findFirst().orElse(null);

    // we find if exist for this pk the same value in pk(in another field) and throw a error if
    // exist
    if (null != fieldValueInRecord) {
      FieldValue fieldValueDatabaseEquals = fieldRepository.findOneByIdFieldSchemaAndValue(
          fieldValueInRecord.getIdFieldSchema(), fieldValueInRecord.getValue());

      if (null != fieldValueDatabaseEquals
          && !fieldValueDatabaseEquals.getId().equalsIgnoreCase(fieldValueInRecord.getId())) {
        throw new EEAException(
            String.format(EEAErrorMessage.PK_ID_ALREADY_EXIST, fieldValueInRecord.getValue()));
      }
      FieldValue fieldValueDatabase = fieldRepository.findById(fieldValueInRecord.getId());
      // we compare if that value is diferent, if not we ignore the pk cascade
      PkCatalogueSchema pkCatalogueSchema =
          pkCatalogueRepository.findByIdPk(new ObjectId(idFieldSchema));
      if (null != pkCatalogueSchema && null != pkCatalogueSchema.getReferenced()) {
        List<String> referenced = pkCatalogueSchema.getReferenced().stream().map(ObjectId::toString)
            .collect(Collectors.toList());
        List<FieldValue> fieldsValues = fieldRepository.findByIdFieldSchemaIn(referenced);
        // we save if the data are the same th
        fieldsValues.stream().forEach(fieldValuePkOtherTable -> {
          if (fieldValueDatabase.getValue().equalsIgnoreCase(fieldValuePkOtherTable.getValue())) {
            fieldValuePkOtherTable.setValue(fieldValueInRecord.getValue());
            fieldRepository.saveValue(fieldValuePkOtherTable.getId(),
                fieldValuePkOtherTable.getValue());

          }
        });
      }
      // we call pams service
      paMService.updateGroups(idListOfSinglePamsField, fieldValueDatabase, fieldValueInRecord);
    }
  }

  /**
   * Field value update PK. we change in cascade when we update a pk value
   *
   * @param fieldValueVO the field value VO
   * @param fieldSchemaDocument the field schema document
   * @param datasetSchemaId the dataset schema id
   *
   * @throws EEAException the EEA exception
   */
  private void fieldValueUpdatePK(final FieldVO fieldValueVO, final Document fieldSchemaDocument,
      final String datasetSchemaId) throws EEAException {

    // we find if exist for this pk the same value in pk and throw a error if exist
    FieldValue fieldValueDatabaseEquals = fieldRepository
        .findOneByIdFieldSchemaAndValue(fieldValueVO.getIdFieldSchema(), fieldValueVO.getValue());
    if (null != fieldValueDatabaseEquals
        && !fieldValueDatabaseEquals.getId().equalsIgnoreCase(fieldValueVO.getId())) {
      throw new EEAException(
          String.format(EEAErrorMessage.PK_ID_ALREADY_EXIST, fieldValueVO.getValue()));
    }
    // we took fieldValue to take more information necesary to calculate pk
    FieldValue fieldValue = fieldRepository.findById(fieldValueVO.getId());

    // we bring the schema
    Document recordSchemaDocument = schemasRepository.findRecordSchema(datasetSchemaId,
        fieldValue.getRecord().getTableValue().getIdTableSchema());
    // get fks to delete
    if (null != recordSchemaDocument) {
      List<Document> fieldSchemasList =
          (ArrayList) recordSchemaDocument.get(LiteralConstants.FIELD_SCHEMAS);

      String idFieldSchema = fieldSchemaDocument.get(LiteralConstants.ID).toString();
      PkCatalogueSchema pkCatalogueSchema =
          pkCatalogueRepository.findByIdPk(new ObjectId(idFieldSchema));

      if (null != pkCatalogueSchema && null != pkCatalogueSchema.getReferenced()) {
        List<String> referenced = pkCatalogueSchema.getReferenced().stream().map(ObjectId::toString)
            .collect(Collectors.toList());
        // we update the rest of pk
        List<FieldValue> fieldsValues = fieldRepository.findByIdFieldSchemaIn(referenced);
        fieldsValues.stream().forEach(fieldValuePkOtherTable -> {
          if (fieldValue.getValue().equalsIgnoreCase(fieldValuePkOtherTable.getValue())) {
            fieldValuePkOtherTable.setValue(fieldValueVO.getValue());
            fieldRepository.saveValue(fieldValuePkOtherTable.getId(),
                fieldValuePkOtherTable.getValue());

          }
        });
        // we took the list of single pams value
        String idListOfSinglePamsField = null;
        for (Object document : fieldSchemasList) {
          if (((Document) document).get(HEADER_NAME) != null
              && ((Document) document).getString(HEADER_NAME).equals("ListOfSinglePams")) {
            idListOfSinglePamsField = ((Document) document).get("_id").toString();
            break;
          }
        }
        // we call pams service
        paMService.updateGroups(idListOfSinglePamsField, fieldValue,
            fieldNoValidationMapper.classToEntity(fieldValueVO));
      }
    }
  }

  /**
   * Gets the schema if reportable.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   *
   * @return the schema if reportable
   */
  @Override
  public DataSetSchema getSchemaIfReportable(Long datasetId, String tableSchemaId) {

    DataSetMetabase dataset = null;
    DataSetSchema schema = null;

    // Dataset: DESIGN
    dataset = designDatasetRepository.findById(datasetId).orElse(null);
    if (null != dataset) {
      if (TypeStatusEnum.DESIGN
          .equals(dataflowControllerZuul.getMetabaseById(dataset.getDataflowId()).getStatus())) {
        schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
      }
    }

    // Dataset: REPORTING
    else {
      dataset = reportingDatasetRepository.findById(datasetId).orElse(null);
      if (null != dataset && TypeStatusEnum.DRAFT
          .equals(dataflowControllerZuul.getMetabaseById(dataset.getDataflowId()).getStatus())) {
        schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
        if (null != tableSchemaId) {
          TableSchema tableSchema = schema.getTableSchemas().stream()
              .filter(t -> tableSchemaId.equals(t.getIdTableSchema().toString())).findFirst()
              .orElse(null);
          if (null == tableSchema || Boolean.TRUE.equals(tableSchema.getReadOnly())
              || Boolean.TRUE.equals(tableSchema.getFixedNumber())) {
            schema = null;
          }
        }
      }
    }

    return schema;
  }


  /**
   * Save public files.
   *
   * @param dataflowId the dataflow id
   * @param dataSetMetabase the data set metabase
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void savePublicFiles(Long dataflowId, DataSetMetabase dataSetMetabase) throws IOException {

    LOG.info("Start creating files. DataflowId: {} DataProviderId: {}", dataflowId,
        dataSetMetabase.getDataProviderId());

    List<RepresentativeVO> representativeList =
        representativeControllerZuul.findRepresentativesByIdDataFlow(dataflowId);

    // we took representative
    RepresentativeVO representative = representativeList.stream()
        .filter(data -> data.getDataProviderId() == dataSetMetabase.getDataProviderId()).findAny()
        .orElse(null);

    // we check if the representative have permit to do it
    if (null != representative && !representative.isRestrictFromPublic()) {

      // we create the dataflow folder to save it
      Path pathDataflow = Paths
          .get(new StringBuilder(pathPublicFile).append("dataflow-").append(dataflowId).toString());
      File directoryDataflow = new File(pathDataflow.toString());
      if (!directoryDataflow.exists()) {
        Files.createDirectories(pathDataflow);

        LOG.info("Folder {} created", pathDataflow);
      }

      // we create the dataprovider folder to save it andwe always delete it and put new files
      Path pathDataProvider =
          Paths.get(new StringBuilder(pathPublicFile).append("dataflow-").append(dataflowId)
              .append("\\dataProvider-").append(representative.getDataProviderId()).toString());
      if (directoryDataflow.exists()) {
        FileUtils.deleteDirectory(new File(pathDataProvider.toString()));
      }
      Files.createDirectories(pathDataProvider);
      LOG.info("Folder {} created", pathDataProvider);

      creeateAllDatasetFiles(dataSetMetabase, dataflowId, pathDataProvider,
          representative.getDataProviderId());
    }

  }


  /**
   * Export public file.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param fileName the file name
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  public byte[] exportPublicFile(Long dataflowId, Long dataProviderId, String fileName)
      throws IOException, EEAException {

    // we compound the route
    String location = new StringBuilder(pathPublicFile).append("dataflow-").append(dataflowId)
        .append("\\dataProvider-").append(dataProviderId).append("\\").append(fileName)
        .append(".xlsx").toString();

    byte[] dataBytes = null;
    File file = new File(location);
    if (!file.exists()) {
      throw new EEAException(EEAErrorMessage.FILE_NOT_FOUND);
    }
    dataBytes = FileUtils.readFileToByteArray(file);
    return dataBytes;
  }

  /**
   * Creeate all dataset files.
   *
   * @param dataset the dataset
   * @param dataflowId the dataflow id
   * @param pathDataProvider the path data provider
   * @param dataProviderId the data provider id
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void creeateAllDatasetFiles(DataSetMetabase dataset, Long dataflowId,
      Path pathDataProvider, Long dataProviderId) throws IOException {

    DataProviderVO dataProvider = representativeControllerZuul.findDataProviderById(dataProviderId);

    List<DataSetMetabase> datasetMetabaseList =
        dataSetMetabaseRepository.findByDataflowIdAndDataProviderId(dataflowId, dataProviderId);

    // now we create all files depends if they are avaliable
    for (DataSetMetabase datasetToFile : datasetMetabaseList) {
      if (datasetToFile.isAvailableInPublic()) {

        // we put the good in the correct field
        List<DesignDataset> desingDataset = designDatasetRepository.findByDataflowId(dataflowId);
        // we find the name of the dataset to asing it for file
        String datasetDesingName = "";
        for (DesignDataset designDatasetVO : desingDataset) {
          if (designDatasetVO.getDatasetSchema()
              .equalsIgnoreCase(datasetToFile.getDatasetSchema())) {
            datasetDesingName = designDatasetVO.getDataSetName();
          }
        }

        try {
          // 1º we create
          byte[] file = exportFile(datasetToFile.getId(), "xlsx", null);
          String nameFileUnique = String.format(FILE_PUBLIC_DATASET_PATTERN_NAME,
              dataProvider.getLabel(), datasetDesingName);

          // we save the file in its files
          String newFile = new StringBuilder(pathDataProvider.toString()).append("\\")
              .append(nameFileUnique).append(".xlsx").toString();
          FileUtils.writeByteArrayToFile(new File(newFile), file);

          // we save the file in metabase with the name without the route
          newFile = new StringBuilder("dataflow-").append(dataflowId).append("\\dataProvider-")
              .append(dataProvider.getId()).append("\\").append(nameFileUnique).toString();
          datasetToFile.setPublicFileName(nameFileUnique);
          dataSetMetabaseRepository.save(datasetToFile);

        } catch (EEAException e) {
          LOG_ERROR.error(
              "File not created in dataflow {} with dataprovider {} with datasetId {} message {}",
              dataset.getDataflowId(), dataset.getDataProviderId(), datasetToFile.getId(),
              e.getMessage(), e);
        }
        LOG.info("Start files created in DataflowId: {} with DataProviderId: {}",
            dataset.getDataflowId(), dataset.getDataProviderId());
      }
    }
  }


  /**
   * Gets the table schema.
   *
   * @param tableSchemaId the table schema id
   * @param datasetSchemaId the dataset schema id
   * @return the table schema
   */
  private TableSchema getTableSchema(String tableSchemaId, String datasetSchemaId) {

    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    TableSchema tableSchema = null;

    if (null != datasetSchema && null != datasetSchema.getTableSchemas()
        && ObjectId.isValid(tableSchemaId)) {
      ObjectId oid = new ObjectId(tableSchemaId);
      tableSchema = datasetSchema.getTableSchemas().stream()
          .filter(ts -> oid.equals(ts.getIdTableSchema())).findFirst().orElse(null);
    }

    return tableSchema;
  }



}
