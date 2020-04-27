package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.FieldNoValidationMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.persistence.data.SortFieldsHelper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
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
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
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
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.multitenancy.DatasetId;
import org.eea.multitenancy.TenantResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

/** The type Dataset service. */
@Service("datasetService")
public class DatasetServiceImpl implements DatasetService {

  /** The Constant ROOT. */
  private static final String ROOT = "root";

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The field max length. */
  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

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

  /** The data collection repository. */
  @Autowired
  private DataCollectionRepository dataCollectionRepository;

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

  /** The parse common. */
  @Autowired
  private FileCommonUtils fileCommon;

  /** The file parser factory. */
  @Autowired
  private IFileParserFactory fileParserFactory;

  /** The file export factory. */
  @Autowired
  private IFileExportFactory fileExportFactory;

  /** The record no validation. */
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
      final PartitionDataSetMetabase partition = obtainPartition(datasetId, ROOT);

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
   * @param listaGeneral the lista general
   */
  @Override
  @Transactional
  public void saveAllRecords(@DatasetId Long datasetId, List<RecordValue> listaGeneral) {
    recordRepository.saveAll(listaGeneral);
  }


  /**
   * Save table.
   *
   * @param datasetId the dataset id
   * @param tableValue the dataset
   */
  @Override
  @Transactional
  public void saveTable(@DatasetId Long datasetId, TableValue tableValue) {
    TenantResolver.setTenantName(String.format("dataset_%s", datasetId));
    DatasetValue datasetValue = datasetRepository.findById(datasetId).get();
    tableValue.setDatasetId(datasetValue);
    tableRepository.saveAndFlush(tableValue);
  }


  /**
   * Obtain dataset metabase.
   *
   * @param datasetId the dataset id
   *
   * @return the data set metabase
   *
   * @throws EEAException the EEA exception
   */
  private ReportingDataset obtainReportingDataset(final Long datasetId) throws EEAException {
    final ReportingDataset reportingDataset =
        reportingDatasetRepository.findById(datasetId).orElse(null);
    if (reportingDataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return reportingDataset;
  }


  /**
   * Obtain partition id.
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
   * Gets the mimetype.
   *
   * @param file the file
   *
   * @return the mimetype
   *
   * @throws EEAException the EEA exception
   */
  private String getMimetype(final String file) throws EEAException {
    String mimeType = null;
    final int location = file.lastIndexOf('.');
    if (location == -1) {
      throw new EEAException(EEAErrorMessage.FILE_EXTENSION);
    }
    mimeType = file.substring(location + 1);
    return mimeType;
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
   * We delete the data imported.
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
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteTableBySchema(final String idTableSchema, final Long datasetId) {
    recordRepository.deleteRecordWithIdTableSchema(idTableSchema);
    LOG.info("Executed delete table with id {}, from dataset {}", idTableSchema, datasetId);
  }

  /**
   * Delete import data.
   *
   * @param dataSetId the data set id
   */
  @Override
  @Transactional
  public void deleteImportData(final Long dataSetId) {

    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(dataSetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));
    // Delete the records from the tables of the dataset that aren't marked as read only
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      if (tableSchema.getReadOnly() == null || !tableSchema.getReadOnly()) {
        recordRepository.deleteRecordWithIdTableSchema(tableSchema.getIdTableSchema().toString());
      }
    }
    try {
      this.saveStatistics(dataSetId);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "Error saving statistics after deleting all the dataset values. Error message: {}",
          e.getMessage(), e);
    }
    LOG.info("All data value deleted from dataSetId {}", dataSetId);
  }


  /**
   * Gets the table values by id.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageable the pageable
   * @param fields the fields
   * @param levelError the level error
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public TableVO getTableValuesById(final Long datasetId, final String idTableSchema,
      Pageable pageable, final String fields, ErrorTypeEnum[] levelError) throws EEAException {
    List<String> commonShortFields = new ArrayList<>();
    Map<String, Integer> mapFields = new HashMap<>();
    List<SortField> sortFieldsArray = new ArrayList<>();
    List<RecordValue> records = null;
    SortField[] newFields = null;
    TableVO result = new TableVO();
    Long totalRecords = tableRepository.countRecordsByIdTableSchema(idTableSchema);

    // Check if we need to put all the records without pagination
    if (pageable == null && totalRecords > 0) {
      pageable = PageRequest.of(0, totalRecords.intValue());
    }
    if (pageable == null && totalRecords == 0) {
      pageable = PageRequest.of(0, 20);
    }

    if (null == fields && (null == levelError || levelError.length == 5)) {

      records = recordRepository.findByTableValueNoOrder(idTableSchema, pageable);

      List<RecordVO> recordVOs = recordNoValidationMapper.entityListToClass(records);
      result.setTotalFilteredRecords(0L);
      result.setRecords(recordVOs);

    } else {

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
          sortField.setAsc((intToBoolean(mapFields.get(nameField))));
          if (null == typefield) {
            sortField.setTypefield(DataType.TEXT);
          } else {
            sortField.setTypefield(typefield.getType());
          }
          sortFieldsArray.add(sortField);
        }
        newFields = sortFieldsArray.stream().toArray(SortField[]::new);
      }

      result = recordRepository.findByTableValueWithOrder(idTableSchema, Arrays.asList(levelError),
          pageable, newFields);

    }

    // Table with out values
    if (null == result.getRecords() || result.getRecords().isEmpty()) {
      result.setRecords(new ArrayList<>());
      LOG.info("No records founded in datasetId {}, idTableSchema {}", datasetId, idTableSchema);

    } else {
      List<RecordVO> recordVOs = result.getRecords();

      LOG.info(
          "Total records found in datasetId {} idTableSchema {}: {}. Now in page {}, {} records by page",
          datasetId, idTableSchema, recordVOs.size(), pageable.getPageNumber(),
          pageable.getPageSize());
      if (null != fields) {
        LOG.info("Ordered by idFieldSchema {}", commonShortFields);
      }

      // 5º retrieve validations to set them into the final result
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
    result.setTotalRecords(totalRecords);
    return result;
  }

  /**
   * String to boolean.
   *
   * @param integer the integer
   *
   * @return the boolean
   */
  private Boolean intToBoolean(Integer integer) {
    return integer == 1;
  }


  /**
   * Retrieves in a controlled way the data from database
   *
   * This method ensures that Sorting Field Criteria is cleaned after every invocation.
   *
   * @param idTableSchema the id table schema
   * @param idFieldSchema the id field schema
   *
   * @return the list
   *
   * @deprecated this method is deprecated
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
   * Removes duplicated records in the query.
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
   * Gets the by id.
   *
   * @param datasetId the dataset id
   *
   * @return the by id
   *
   * @throws EEAException the EEA exception
   * @deprecated this method is deprecated
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
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public Long getDataFlowIdById(Long datasetId) throws EEAException {
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
   * Fill table stat.
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
          reportingDatasetRepository.findById(datasetId).orElse(new ReportingDataset());

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
   * Returns map with key = IdField value=List of FieldValidation.
   *
   * @param recordIds the record ids
   *
   * @return the Map
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
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateRecords(final Long datasetId, final List<RecordVO> records)
      throws EEAException {
    if (datasetId == null || records == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);

    }
    List<RecordValue> recordValues = recordMapper.classListToEntity(records);
    List<FieldValue> fieldValues = new ArrayList<>();
    for (RecordValue recordValue : recordValues) {
      for (FieldValue fieldValue : recordValue.getFields()) {
        if (null == fieldValue.getValue()) {
          fieldValue.setValue("");
        } else {
          if (fieldValue.getValue().length() >= fieldMaxLength) {
            fieldValue.setValue(fieldValue.getValue().substring(0, fieldMaxLength));
          }
        }
        fieldValues.add(fieldValue);
      }
    }
    fieldRepository.saveAll(fieldValues);
  }

  /**
   * Creates the records.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param idTableSchema the id table schema
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void createRecords(final Long datasetId, final List<RecordVO> records,
      final String idTableSchema) throws EEAException {
    if (datasetId == null || records == null || idTableSchema == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);
    }
    Long tableId = tableRepository.findIdByIdTableSchema(idTableSchema);
    if (null == tableId || tableId == 0) {
      throw new EEAException(EEAErrorMessage.TABLE_NOT_FOUND);
    }
    List<RecordValue> recordValue = recordMapper.classListToEntity(records);
    DatasetValue dataset = new DatasetValue();
    dataset.setId(datasetId);
    TableValue table = new TableValue();
    table.setId(tableId);

    // obtain the provider code (ie ES, FR, IT, etc)
    Long providerId = 0L;
    DataSetMetabaseVO metabase = datasetMetabaseService.findDatasetMetabase(datasetId);
    if (metabase.getDataProviderId() != null) {
      providerId = metabase.getDataProviderId();
    }
    DataProviderVO provider = representativeControllerZuul.findDataProviderById(providerId);

    // Set the provider code to create Hash
    recordValue.parallelStream().forEach(record -> {
      if (record.getDatasetPartitionId() == null) {
        try {
          record.setDatasetPartitionId(this.obtainPartition(datasetId, "root").getId());
        } catch (EEAException e) {
          LOG_ERROR.error(e.getMessage());
        }
      }
      table.setDatasetId(dataset);
      record.setTableValue(table);
      record.setDataProviderCode(provider.getCode());
      for (FieldValue field : record.getFields()) {
        if (null == field.getValue()) {
          field.setValue("");
        } else {
          if (field.getValue().length() >= fieldMaxLength) {
            field.setValue(field.getValue().substring(0, fieldMaxLength));
          }
        }
      }

    });
    recordRepository.saveAll(recordValue);
  }


  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteRecord(final Long datasetId, final String recordId) throws EEAException {
    if (datasetId == null || recordId == null) {
      throw new EEAException(EEAErrorMessage.RECORD_NOTFOUND);
    }
    recordRepository.deleteRecordWithId(recordId);
  }

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   * @param idTableSchema the id table schema
   *
   * @return the byte[]
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public byte[] exportFile(Long datasetId, String mimeType, final String idTableSchema)
      throws EEAException, IOException {
    // Get the partition
    // final PartitionDataSetMetabase partition = obtainPartition(datasetId, ROOT);

    // Get the dataFlowId from the metabase
    Long idDataflow = getDataFlowIdById(datasetId);

    final IFileExportContext context = fileExportFactory.createContext(mimeType);
    LOG.info("End of exportFile");
    return context.fileWriter(idDataflow, datasetId, idTableSchema);

  }

  /**
   * Gets the file name.
   *
   * @param mimeType the mime type
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   *
   * @return the file name
   *
   * @throws EEAException the EEA exception
   */
  @Override
  public String getFileName(String mimeType, String idTableSchema, Long datasetId)
      throws EEAException {
    final DataSetMetabase datasetMetabase = obtainReportingDataset(datasetId);
    DataSetSchemaVO dataSetSchema =
        fileCommon.getDataSetSchema(datasetMetabase.getDataflowId(), datasetId);
    return null == fileCommon.getFieldSchemas(idTableSchema, dataSetSchema)
        ? datasetMetabase.getDataSetName() + "." + mimeType
        : fileCommon.getTableName(idTableSchema, dataSetSchema) + "." + mimeType;

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
   * Update records.
   *
   * @param datasetId the dataset id
   * @param field the field
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void updateField(final Long datasetId, final FieldVO field) throws EEAException {
    if (datasetId == null || field == null) {
      throw new EEAException(EEAErrorMessage.FIELD_NOT_FOUND);

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
    TenantResolver.setTenantName(String.format("dataset_%s", datasetId));
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
   *
   * @return the datasetSchemaId
   */
  @Override
  @Transactional
  public void deleteFieldValues(Long datasetId, String fieldSchemaId) {
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
   * Delete table values.
   *
   * @param datasetId the dataset id
   */
  @Override
  @Transactional
  public void deleteAllTableValues(Long datasetId) {
    tableRepository.removeTableData(datasetId);
  }


  /**
   * Checks if is reporting dataset.
   *
   * @param datasetId the dataset id
   *
   * @return true, if is reporting dataset
   */
  @Override
  public boolean isReportingDataset(Long datasetId) {
    return reportingDatasetRepository.existsById(datasetId);
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
   * Delete record values.
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
   * @param datasetId the dataset id
   * @param idPk the id pk
   * @param searchValue the search value
   *
   * @return the field values referenced
   */
  @Override
  public List<FieldVO> getFieldValuesReferenced(Long datasetId, String idPk, String searchValue) {
    Long idDatasetDestination =
        datasetMetabaseService.getDatasetDestinationForeignRelation(datasetId, idPk);
    TenantResolver.setTenantName(String.format("dataset_%s", idDatasetDestination));
    // Pageable of 15 to take an equivalent to sql Limit. 15 because is the size of the results we
    // want to show on the screen
    List<FieldValue> fields = fieldRepository.findByIdFieldSchemaAndValueContaining(idPk,
        searchValue, PageRequest.of(0, 15));
    // Remove the duplicate values
    HashSet<String> seen = new HashSet<>();
    fields.removeIf(e -> !seen.add(e.getValue()));

    // Sort results
    List<FieldValue> sortedList = new ArrayList<>();
    if (!fields.isEmpty()) {
      sortedList = fields.stream().sorted(Comparator.comparing(FieldValue::getValue))
          .collect(Collectors.toList());
    }
    return fieldNoValidationMapper.entityListToClass(sortedList);

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
   * Gets the dataset type.
   *
   * @param datasetId the dataset id
   * @return the dataset type
   */
  @Override
  public DatasetTypeEnum getDatasetType(Long datasetId) {
    DatasetTypeEnum type = null;

    if (designDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.DESIGN;
    } else if (reportingDatasetRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.REPORTING;
    } else if (dataCollectionRepository.existsById(datasetId)) {
      type = DatasetTypeEnum.COLLECTION;
    }

    return type;
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @return the ETL dataset VO
   * @throws EEAException the EEA exception
   */
  @Override
  public ETLDatasetVO etlExportDataset(@DatasetId Long datasetId) throws EEAException {

    // Get the datasetSchemaId by the datasetId
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    if (null == datasetSchemaId) {
      throw new EEAException(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND + " " + datasetId);
    }

    // Get the datasetSchema by the datasetSchemaId
    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    if (null == datasetSchema) {
      throw new EEAException(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND + " " + datasetSchemaId);
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
   * @throws EEAException
   */
  @Override
  public void etlImportDataset(@DatasetId Long datasetId, ETLDatasetVO etlDatasetVO)
      throws EEAException {
    // Get the datasetSchemaId by the datasetId
    String datasetSchemaId = datasetRepository.findIdDatasetSchemaById(datasetId);
    if (null == datasetSchemaId) {
      throw new EEAException(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND + " " + datasetId);
    }

    // Get the datasetSchema by the datasetSchemaId
    DataSetSchema datasetSchema =
        schemasRepository.findById(new ObjectId(datasetSchemaId)).orElse(null);
    if (null == datasetSchema) {
      throw new EEAException(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND + " " + datasetSchemaId);
    }
    // Construct Maps to relate ids
    Map<String, TableSchema> tableMap = new HashMap<>();
    Map<String, FieldSchema> fieldMap = new HashMap<>();
    for (TableSchema tableSchema : datasetSchema.getTableSchemas()) {
      tableMap.put(tableSchema.getNameTableSchema(), tableSchema);
      // Match each fieldSchemaId with its headerName
      for (FieldSchema field : tableSchema.getRecordSchema().getFieldSchema()) {
        fieldMap.put(field.getHeaderName(), field);
      }
    }

    // Construct object to be save
    DatasetValue dataset = new DatasetValue();
    List<TableValue> tables = new ArrayList<>();

    // Loops to build the entity
    for (ETLTableVO etlTable : etlDatasetVO.getTables()) {
      TableValue table = new TableValue();
      TableSchema tableSchema = tableMap.get(etlTable.getTableName());
      if (tableSchema != null) {
        table.setIdTableSchema(tableSchema.getIdTableSchema().toString());
        List<RecordValue> records = new ArrayList<>();
        for (ETLRecordVO etlRecord : etlTable.getRecords()) {
          RecordValue recordValue = new RecordValue();
          recordValue.setIdRecordSchema(tableMap.get(etlTable.getTableName()).getRecordSchema()
              .getIdRecordSchema().toString());
          recordValue.setTableValue(table);
          List<FieldValue> fieldValues = new ArrayList<>();
          List<String> idSchema = new ArrayList<>();
          for (ETLFieldVO etlField : etlRecord.getFields()) {
            FieldValue field = new FieldValue();
            FieldSchema fieldSchema = fieldMap.get(etlField.getFieldName());
            if (fieldSchema != null) {
              field.setIdFieldSchema(fieldSchema.getIdFieldSchema().toString());
              field.setType(fieldSchema.getType());
              field.setValue(etlField.getValue());
              field.setRecord(recordValue);
              fieldValues.add(field);
              idSchema.add(field.getIdFieldSchema());
              setMissingField(
                  tableMap.get(etlTable.getTableName()).getRecordSchema().getFieldSchema(),
                  fieldValues, idSchema, recordValue);
            }
          }
          recordValue.setFields(fieldValues);
          records.add(recordValue);
        }
        table.setRecords(records);
        tables.add(table);
        table.setDatasetId(dataset);
      }
    }
    dataset.setTableValues(tables);
    dataset.setIdDatasetSchema(datasetSchemaId);
    dataset.setId(datasetId);

    datasetRepository.save(dataset);

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
   * Gets the table read only. Receives by parameter the datasetId, the objectId and the type
   * (table, record, field). In example, if receives an objectId that is a Record (that's a record
   * schema id), find the property readOnly of the table that belongs to the record
   * 
   * @param datasetId the dataset id
   * @param objectId the object id
   * @param type the type
   * @return the table read only
   */
  @Override
  public Boolean getTableReadOnly(Long datasetId, String objectId, EntityTypeEnum type) {
    Boolean readOnly = false;
    String datasetSchemaId = datasetMetabaseService.findDatasetSchemaIdById(datasetId);
    DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(datasetSchemaId));

    switch (type) {
      case TABLE:
        for (TableSchema tableSchema : schema.getTableSchemas()) {
          if (objectId.equals(tableSchema.getIdTableSchema().toString())
              && tableSchema.getReadOnly() != null && tableSchema.getReadOnly()) {
            readOnly = true;
            break;
          }
        }
        break;
      case RECORD:
        for (TableSchema tableSchema : schema.getTableSchemas()) {
          if (objectId.equals(tableSchema.getRecordSchema().getIdRecordSchema().toString())
              && tableSchema.getReadOnly() != null && tableSchema.getReadOnly()) {
            readOnly = true;
            break;
          }
        }
        break;
      case FIELD:
        for (TableSchema tableSchema : schema.getTableSchemas()) {
          for (FieldSchema fieldSchema : tableSchema.getRecordSchema().getFieldSchema()) {
            if (objectId.equals(fieldSchema.getIdFieldSchema().toString())
                && tableSchema.getReadOnly() != null && tableSchema.getReadOnly()) {
              readOnly = true;
              break;
            }
          }
        }
        break;
    }
    return readOnly;
  }

  /**
   * Release lock.
   *
   * @param criteria the criteria
   */
  @Override
  public void releaseLock(Object... criteria) {

    List<Object> criteriaList = new ArrayList<>();
    for (Object crit : criteria) {
      criteriaList.add(crit);
    }
    lockService.removeLockByCriteria(criteriaList);

  }

}
