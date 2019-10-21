package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.DataSetTablesMapper;
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
import org.eea.dataset.persistence.data.domain.Statistics;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.StatisticsRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.ReportingDataset;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.ReportingDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.eea.multitenancy.DatasetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * The type Dataset service.
 */
@Service("datasetService")
public class DatasetServiceImpl implements DatasetService {

  /**
   * The Constant ROOT.
   */
  private static final String ROOT = "root";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The data set mapper.
   */
  @Autowired
  private DataSetMapper dataSetMapper;

  /**
   * The data set tables mapper.
   */
  @Autowired
  private DataSetTablesMapper dataSetTablesMapper;

  /**
   * The record mapper.
   */
  @Autowired
  private RecordMapper recordMapper;

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
   * The parse common.
   */
  @Autowired
  private FileCommonUtils fileCommon;


  /**
   * The reporting dataset repository.
   */
  @Autowired
  private ReportingDatasetRepository reportingDatasetRepository;

  /**
   * The partition data set metabase repository.
   */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /**
   * The data set metabase repository.
   */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;
  /**
   * The dataset repository.
   */
  @Autowired
  private DatasetRepository datasetRepository;

  /**
   * The data set metabase table collection.
   */
  @Autowired
  private DataSetMetabaseTableRepository dataSetMetabaseTableCollection;

  /**
   * The schemas repository.
   */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * The file parser factory.
   */
  @Autowired
  private IFileParserFactory fileParserFactory;

  /**
   * The file export factory.
   */
  @Autowired
  private IFileExportFactory fileExportFactory;


  /**
   * The field repository.
   */
  @Autowired
  private FieldRepository fieldRepository;

  /**
   * The record no validation.
   */
  @Autowired
  private RecordNoValidationMapper recordNoValidationMapper;

  /**
   * The field validation repository.
   */
  @Autowired
  private FieldValidationRepository fieldValidationRepository;

  /**
   * The record validation repository.
   */
  @Autowired
  private RecordValidationRepository recordValidationRepository;

  /**
   * The field validation mapper.
   */
  @Autowired
  private FieldValidationMapper fieldValidationMapper;

  /**
   * The record validation mapper.
   */
  @Autowired
  private RecordValidationMapper recordValidationMapper;


  /** The statistics repository. */
  @Autowired
  private StatisticsRepository statisticsRepository;



  /**
   * Process file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the is
   * @param idTableSchema the id table schema
   * @return the data set VO
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
      final ReportingDataset reportingDataset = obtainReportingDataset(datasetId);

      // create the right file parser for the file type
      final IFileParseContext context = fileParserFactory.createContext(mimeType);
      final DataSetVO datasetVO =
          context.parse(is, reportingDataset.getDataflowId(), partition.getId(), idTableSchema);

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
    datasetRepository.removeDatasetData(dataSetId);

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
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public TableVO getTableValuesById(final Long datasetId, final String idTableSchema,
      Pageable pageable, final String fields) throws EEAException {
    List<String> commonShortFields = new ArrayList<>();
    Map<String, Integer> mapFields = new HashMap<>();
    List<SortField> sortFieldsArray = new ArrayList<>();
    List<RecordValue> records = null;

    Long totalRecords = tableRepository.countRecordsByIdTableSchema(idTableSchema);

    // Check if we need to put all the records without pagination
    if (pageable == null && totalRecords > 0) {
      pageable = PageRequest.of(0, totalRecords.intValue());
    }
    if (pageable == null && totalRecords == 0) {
      pageable = PageRequest.of(0, 20);
    }

    if (null == fields) {

      records = recordRepository.findByTableValueNoOrder(idTableSchema, pageable);

    } else {

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
        sortField.setTypefield(typefield.getType());
        sortFieldsArray.add(sortField);
      }
      records = recordRepository.findByTableValueWithOrder(idTableSchema, pageable,
          sortFieldsArray.stream().toArray(SortField[]::new));

    }

    if (records == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    TableVO result = new TableVO();
    if (records.isEmpty()) {
      result.setTotalRecords(0L);
      result.setRecords(new ArrayList<>());
      LOG.info("No records founded in datasetId {}, idTableSchema {}", datasetId, idTableSchema);

    } else {

      List<RecordVO> recordVOs = recordNoValidationMapper.entityListToClass(records);
      result.setRecords(recordVOs);
      result.setTotalRecords(totalRecords);

      LOG.info(
          "Total records found in datasetId {} idTableSchema {}: {}. Now in page {}, {} records by page",
          datasetId, idTableSchema, recordVOs.size(), pageable.getPageNumber(),
          pageable.getPageSize());
      if (null != fields) {
        LOG.info("Ordered by idFieldSchema {}", commonShortFields);
      }

      // 5º retrieve validations to set them into the final result
      List<Long> recordIds = recordVOs.stream().map(RecordVO::getId).collect(Collectors.toList());
      Map<Long, List<FieldValidation>> fieldValidations = this.getFieldValidations(recordIds);
      Map<Long, List<RecordValidation>> recordValidations = this.getRecordValidations(recordIds);
      recordVOs.stream().forEach(record -> {
        record.getFields().stream().forEach(field -> {
          List<FieldValidationVO> validations =
              this.fieldValidationMapper.entityListToClass(fieldValidations.get(field.getId()));
          field.setFieldValidations(validations);
          if (null != validations && !validations.isEmpty()) {
            field.setLevelError(
                validations.stream().map(validation -> validation.getValidation().getLevelError())
                    .filter(error -> error.equals(TypeErrorEnum.ERROR)).findFirst()
                    .orElse(TypeErrorEnum.WARNING));
          }
        });

        List<RecordValidationVO> validations =
            this.recordValidationMapper.entityListToClass(recordValidations.get(record.getId()));
        record.setRecordValidations(validations);
        if (null != validations && !validations.isEmpty()) {
          record.setLevelError(
              validations.stream().map(validation -> validation.getValidation().getLevelError())
                  .filter(error -> error.equals(TypeErrorEnum.ERROR)).findFirst()
                  .orElse(TypeErrorEnum.WARNING));
        }
      });

    }

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
      Set<Long> processedRecords = new HashSet<>();
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
   * Sets the dataschema tables.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableCollectionVO the table collection VO
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void setDataschemaTables(final Long datasetId, final Long dataFlowId,
      final TableCollectionVO tableCollectionVO) throws EEAException {
    final TableCollection tableCollection = dataSetTablesMapper.classToEntity(tableCollectionVO);
    tableCollection.setDataSetId(datasetId);
    tableCollection.setDataFlowId(dataFlowId);

    dataSetMetabaseTableCollection.save(tableCollection);
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
  public ValidationLinkVO getPositionFromAnyObjectId(final Long id, final Long idDataset,
      final TypeEntityEnum type) throws EEAException {

    ValidationLinkVO validationLink = new ValidationLinkVO();
    RecordValue record = new RecordValue();
    List<RecordValue> records = new ArrayList<>();

    // TABLE
    if (TypeEntityEnum.TABLE == type) {
      TableValue table = tableRepository.findByIdAndDatasetId_Id(id, idDataset);
      records = recordRepository.findByTableValueNoOrder(table.getIdTableSchema(), null);
      if (records != null && !records.isEmpty()) {
        record = records.get(0);
      }
    }

    // RECORD
    if (TypeEntityEnum.RECORD == type) {
      record = recordRepository.findByIdAndTableValue_DatasetId_Id(id, idDataset);
      records =
          recordRepository.findByTableValueNoOrder(record.getTableValue().getIdTableSchema(), null);
    }

    // FIELD
    if (TypeEntityEnum.FIELD == type) {

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

      DataSetSchema schema = schemasRepository.findByIdDataSetSchema(
          new ObjectId(record.getTableValue().getDatasetId().getIdDatasetSchema()));
      for (TableSchema tableSchema : schema.getTableSchemas()) {
        if (validationLink.getIdTableSchema().equals(tableSchema.getIdTableSchema().toString())) {
          validationLink.setNameTableSchema(tableSchema.getNameTableSchema());
          break;
        }
      }
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
   * @return the list
   */
  private List<Statistics> processTableStats(final TableValue tableValue, final Long datasetId,
      final Map<String, String> mapIdNameDatasetSchema) {


    Set<Long> recordIdsFromRecordWithValidationError =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.ERROR);

    Set<Long> recordIdsFromRecordWithValidationWarning =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.WARNING);

    Set<Long> recordIdsFromFieldWithValidationError =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.ERROR);

    Set<Long> recordIdsFromFieldWithValidationWarning =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.WARNING);

    Set<Long> idsErrors = new HashSet<>();
    idsErrors.addAll(recordIdsFromRecordWithValidationError);
    idsErrors.addAll(recordIdsFromFieldWithValidationError);

    Set<Long> idsWarnings = new HashSet<>();
    idsWarnings.addAll(recordIdsFromRecordWithValidationWarning);
    idsWarnings.addAll(recordIdsFromFieldWithValidationWarning);

    idsWarnings.removeAll(idsErrors);

    Long totalRecordsWithErrors = Long.valueOf(idsErrors.size());
    Long totalRecordsWithWarnings = Long.valueOf(idsWarnings.size());


    TableStatisticsVO tableStats = new TableStatisticsVO();
    tableStats.setIdTableSchema(tableValue.getIdTableSchema());
    Long countRecords = tableRepository.countRecordsByIdTableSchema(tableValue.getIdTableSchema());
    tableStats.setTotalRecords(countRecords);

    Long totalTableErrors = totalRecordsWithErrors + totalRecordsWithWarnings;

    totalTableErrors = totalTableErrors + tableValue.getTableValidations().size();

    tableStats.setNameTableSchema(mapIdNameDatasetSchema.get(tableValue.getIdTableSchema()));
    tableStats.setTotalErrors(totalTableErrors);
    tableStats.setTotalRecordsWithErrors(totalRecordsWithErrors);
    tableStats.setTotalRecordsWithWarnings(totalRecordsWithWarnings);
    tableStats.setTableErrors(totalTableErrors > 0 ? true : false);

    List<Statistics> stats = new ArrayList<>();

    Statistics statsIdTable =
        fillStat(tableValue.getIdTableSchema(), "idTableSchema", tableValue.getIdTableSchema());

    Statistics statsNameTable = fillStat(tableValue.getIdTableSchema(), "nameTableSchema",
        mapIdNameDatasetSchema.get(tableValue.getIdTableSchema()));

    Statistics statsTotalTableError =
        fillStat(tableValue.getIdTableSchema(), "totalErrors", totalTableErrors.toString());

    Statistics statsTotalRecords =
        fillStat(tableValue.getIdTableSchema(), "totalRecords", countRecords.toString());

    Statistics statsTotalRecordsWithErrors = fillStat(tableValue.getIdTableSchema(),
        "totalRecordsWithErrors", totalRecordsWithErrors.toString());

    Statistics statsTotalRecordsWithWarnings = fillStat(tableValue.getIdTableSchema(),
        "totalRecordsWithWarnings", totalRecordsWithWarnings.toString());

    Statistics statsTableErrors = new Statistics();
    statsTableErrors.setIdTableSchema(tableValue.getIdTableSchema());
    statsTableErrors.setStatName("tableErrors");
    if (tableValue.getTableValidations() != null && tableValue.getTableValidations().size() > 0) {
      statsTableErrors.setValue("true");
    } else {
      statsTableErrors.setValue(totalTableErrors > 0 ? "true" : "false");
    }

    stats.add(statsIdTable);
    stats.add(statsNameTable);
    stats.add(statsTotalTableError);
    stats.add(statsTotalRecords);
    stats.add(statsTotalRecordsWithErrors);
    stats.add(statsTotalRecordsWithWarnings);
    stats.add(statsTableErrors);


    return stats;

  }


  /**
   * Fill table stat.
   *
   * @param idTableSchema the id table schema
   * @param statName the stat name
   * @param value the value
   * @return the statistics
   */
  private Statistics fillStat(String idTableSchema, String statName, String value) {

    Statistics stat = new Statistics();
    stat.setIdTableSchema(idTableSchema);
    stat.setStatName(statName);
    stat.setValue(value);

    return stat;
  }


  /**
   * Save statistics.
   *
   * @param datasetId the dataset id
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
          fillStat(null, "idDataSetSchema", dataset.getIdDatasetSchema());
      statsList.add(statsIdDatasetSchema);

      Statistics statsNameDatasetSchema =
          fillStat(null, "nameDataSetSchema", datasetMb.getDataSetName());
      statsList.add(statsNameDatasetSchema);

      Statistics statsDatasetErrors = fillStat(null, "datasetErrors", datasetErrors.toString());
      statsList.add(statsDatasetErrors);

      statisticsRepository.deleteAll();
      statisticsRepository.saveAll(statsList);


      LOG.info("Statistics save to datasetId {}.", datasetId);
    } else {
      LOG_ERROR.error("No dataset found to save statistics. DatasetId:{}", datasetId);
    }

  }


  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   * @return the statistics
   * @throws EEAException the EEA exception
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  @Override
  @Transactional
  public StatisticsVO getStatistics(final Long datasetId)
      throws EEAException, InstantiationException, IllegalAccessException {

    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
    StatisticsVO stats = new StatisticsVO();

    List<Statistics> statistics = statisticsRepository.findAllStatistics();
    List<Statistics> statisticsTables = statistics.stream()
        .filter(s -> StringUtils.isNotBlank(s.getIdTableSchema())).collect(Collectors.toList());
    List<Statistics> statisticsDataset = statistics.stream()
        .filter(s -> StringUtils.isBlank(s.getIdTableSchema())).collect(Collectors.toList());


    Map<String, List<Statistics>> tablesMap = statisticsTables.stream()
        .collect(Collectors.groupingBy(Statistics::getIdTableSchema, Collectors.toList()));

    // Dataset level stats
    Class<?> clazzStats = stats.getClass();
    Object instance = clazzStats.newInstance();
    statisticsDataset.stream().forEach(s -> {
      setEntityProperty(instance, s.getStatName(), s.getValue());
    });
    stats = (StatisticsVO) instance;


    // Table statistics
    stats.setTables(new ArrayList<>());
    for (List<Statistics> listStats : tablesMap.values()) {
      Class<?> clazzTable = TableStatisticsVO.class;
      Object instanceTable = clazzTable.newInstance();
      listStats.stream().forEach(s -> {
        setEntityProperty(instanceTable, s.getStatName(), s.getValue());
      });
      stats.getTables().add((TableStatisticsVO) instanceTable);
    }



    // Check if there are empty tables
    DataSetSchema schema =
        schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getIdDatasetSchema()));
    List<String> listIdsDataSetSchema = new ArrayList<>();
    Map<String, String> mapIdNameDatasetSchema = new HashMap<>();
    for (TableSchema tableSchema : schema.getTableSchemas()) {
      listIdsDataSetSchema.add(tableSchema.getIdTableSchema().toString());
      mapIdNameDatasetSchema.put(tableSchema.getIdTableSchema().toString(),
          tableSchema.getNameTableSchema());
    }
    List<String> listIdDataSetSchema = new ArrayList<>();
    List<TableValue> allTableValues = dataset.getTableValues();
    for (TableValue tableValue : allTableValues) {
      listIdDataSetSchema.add(tableValue.getIdTableSchema());
    }
    listIdsDataSetSchema.removeAll(listIdDataSetSchema);
    for (String idTableSchem : listIdsDataSetSchema) {
      stats.getTables()
          .add(new TableStatisticsVO(idTableSchem, mapIdNameDatasetSchema.get(idTableSchem)));
    }

    return stats;
  }


  /**
   * Sets the entity property.
   *
   * @param object the object
   * @param fieldName the field name
   * @param fieldValue the field value
   * @return the boolean
   */
  public static Boolean setEntityProperty(Object object, String fieldName, String fieldValue) {
    Class<?> clazz = object.getClass();
    while (clazz != null) {
      try {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        if (field.getType().equals(Long.class)) {
          field.set(object, Long.valueOf(fieldValue));
        } else if (field.getType().equals(Boolean.class)) {
          field.set(object, Boolean.valueOf(fieldValue));
        } else {
          field.set(object, fieldValue);
        }

        return true;
      } catch (NoSuchFieldException e) {
        clazz = clazz.getSuperclass();
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
    }
    return false;
  }



  /**
   * Returns map with key = IdField value=List of FieldValidation.
   *
   * @param recordIds the record ids
   *
   * @return the Map
   */
  private Map<Long, List<FieldValidation>> getFieldValidations(final List<Long> recordIds) {
    List<FieldValidation> fieldValidations =
        this.fieldValidationRepository.findByFieldValue_RecordIdIn(recordIds);

    Map<Long, List<FieldValidation>> result = new HashMap<>();

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
  private Map<Long, List<RecordValidation>> getRecordValidations(final List<Long> recordIds) {

    List<RecordValidation> recordValidations =
        this.recordValidationRepository.findByRecordValueIdIn(recordIds);

    Map<Long, List<RecordValidation>> result = new HashMap<>();

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
    List<RecordValue> recordValue = recordMapper.classListToEntity(records);
    List<FieldValue> fields = recordValue.parallelStream().map(RecordValue::getFields)
        .filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
    fieldRepository.saveAll(fields);
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
    TableValue table = new TableValue();
    table.setId(tableId);
    recordValue.parallelStream().forEach(record -> {
      if (record.getDatasetPartitionId() == null) {
        try {
          record.setDatasetPartitionId(this.obtainPartition(datasetId, "root").getId());
        } catch (EEAException e) {
          LOG_ERROR.error(e.getMessage());
        }
      }
      record.setTableValue(table);
      record.getFields().stream().filter(field -> field.getValue() == null).forEach(field -> {
        field.setValue("");
      });

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
  public void deleteRecord(final Long datasetId, final Long recordId) throws EEAException {
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
    final DataSetMetabase datasetMetabase = obtainReportingDataset(datasetId);

    final IFileExportContext context = fileExportFactory.createContext(mimeType);
    LOG.info("End of exportFile");
    return context.fileWriter(datasetMetabase.getDataflowId(), datasetId, idTableSchema);

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
    DataSetSchemaVO dataSetSchema = fileCommon.getDataSetSchema(datasetMetabase.getDataflowId());
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
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void deleteRecordValuesToRestoreSnapshot(Long datasetId, Long idPartition)
      throws EEAException {
    recordRepository.deleteRecordValuesToRestoreSnapshot(idPartition);
  }

  /**
   * Save table propagation.
   *
   * @param datasetId the dataset id
   * @param tableSchema the table schema
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public void saveTablePropagation(Long datasetId, TableSchemaVO tableSchema) throws EEAException {
    TableValue table = new TableValue();
    Optional<DatasetValue> dataset = datasetRepository.findById(datasetId);
    if (dataset.isPresent()) {
      table.setIdTableSchema(tableSchema.getIdTableSchema());
      table.setDatasetId(dataset.get());
      saveTable(datasetId, table);
    } else {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
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

  }


}
