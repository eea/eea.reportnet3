package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import org.eea.dataset.mapper.DataSetTablesMapper;
import org.eea.dataset.mapper.FieldValidationMapper;
import org.eea.dataset.mapper.RecordNoValidationMapper;
import org.eea.dataset.mapper.RecordValidationMapper;
import org.eea.dataset.persistence.data.SortFieldsHelper;
import org.eea.dataset.persistence.data.domain.DatasetValidation;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValidation;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.FieldValidationRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.RecordValidationRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
import org.eea.dataset.persistence.data.repository.TableValidationRepository;
import org.eea.dataset.persistence.data.util.SortField;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableRepository;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ErrorsValidationVO;
import org.eea.interfaces.vo.dataset.FieldValidationVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.RecordValidationVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
   * The data set metabase repository.
   */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /**
   * The partition data set metabase repository.
   */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

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
   * The record store controller zull.
   */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /**
   * The file parser factory.
   */
  @Autowired
  private IFileParserFactory fileParserFactory;

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

  /**
   * The table validation repository.
   */
  @Autowired
  private TableValidationRepository tableValidationRepository;

  /**
   * Creates the removeDatasetData dataset.
   *
   * @param datasetName the dataset name
   */
  @Override
  @Transactional
  public void createEmptyDataset(final String datasetName) {
    recordStoreControllerZull.createEmptyDataset("dataset_" + datasetName);
  }


  /**
   * Process file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param is the is
   * @param idTableSchema the id table schema
   *
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public void processFile(final Long datasetId, final String fileName, final InputStream is,
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
      if (partition == null) {
        throw new EEAException(EEAErrorMessage.PARTITION_ID_NOTFOUND);
      }
      // Get the dataFlowId from the metabase
      final DataSetMetabase datasetMetabase = obtainDatasetMetabase(datasetId);
      // create the right file parser for the file type
      final IFileParseContext context = fileParserFactory.createContext(mimeType);
      final DataSetVO datasetVO =
          context.parse(is, datasetMetabase.getDataflowId(), partition.getId(), idTableSchema);
      // map the VO to the entity
      if (datasetVO == null) {
        throw new IOException("Empty dataset");
      }
      datasetVO.setId(datasetId);

      final DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
      if (dataset == null) {
        throw new IOException("Error mapping file");
      }
      // Check if the table with idTableSchema has been populated already
      Long oldTableId = tableRepository.findIdByIdTableSchema(idTableSchema);
      fillTableId(idTableSchema, dataset.getTableValues(), oldTableId);
      // save dataset to the database
      datasetRepository.saveAndFlush(dataset);
      LOG.info("File processed and saved into DB");
    } finally {
      is.close();
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
   * Obtain dataset metabase.
   *
   * @param datasetId the dataset id
   *
   * @return the data set metabase
   *
   * @throws EEAException the EEA exception
   */
  private DataSetMetabase obtainDatasetMetabase(final Long datasetId) throws EEAException {
    final DataSetMetabase datasetMetabase =
        dataSetMetabaseRepository.findById(datasetId).orElse(null);
    if (datasetMetabase == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return datasetMetabase;
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
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
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
   * Delete import data.
   *
   * @param dataSetId the data set id
   */
  @Override
  @Transactional
  public void deleteImportData(Long dataSetId) {
    datasetRepository.removeDatasetData(dataSetId);
    LOG.info("All data value deleted from dataSetId {}", dataSetId);
  }


  /**
   * Gets the table values by id. It additionally can page the results and sort them
   *
   * sort is handmade since the criteria is the idFieldValue of the Fields inside the records.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the mongo ID
   * @param pageable the pageable
   * @param idFieldSchema the id field schema
   * @param asc the asc
   *
   * @return the table values by id
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public TableVO getTableValuesById(final Long datasetId, final String idTableSchema,
      final Pageable pageable, final String idFieldSchema, final Boolean asc) throws EEAException {
    List<RecordValue> records = null;
    if (StringUtils.isBlank(idFieldSchema)) {
      records = recordRepository.findByTableValueNoOrder(idTableSchema);
    } else {
      SortField sortField = new SortField();
      sortField.setFieldName(idFieldSchema);
      sortField.setAsc(asc);
      records = recordRepository.findByTableValueWithOrder(idTableSchema, sortField);
    }

    if (records == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    TableVO result = new TableVO();
    if (records.isEmpty()) {
      result.setTotalRecords(0L);
      result.setRecords(new ArrayList<>());
      LOG.info("No records founded in datasetId {}", datasetId);

    } else {

      int initIndex = pageable.getPageNumber() * pageable.getPageSize();
      int endIndex =
          (pageable.getPageNumber() + 1) * pageable.getPageSize() > records.size() ? records.size()
              : (pageable.getPageNumber() + 1) * pageable.getPageSize();
      List<RecordValue> pagedRecords = records.subList(initIndex, endIndex);
      List<RecordVO> recordVOs = recordNoValidationMapper.entityListToClass(pagedRecords);
      result.setRecords(recordVOs);
      result.setTotalRecords(Long.valueOf(records.size()));

      LOG.info("Total records found in datasetId {}: {}. Now in page {}, {} records by page",
          datasetId, recordVOs.size(), pageable.getPageNumber(), pageable.getPageSize());
      if (StringUtils.isNotBlank(idFieldSchema)) {
        LOG.info("Ordered by idFieldSchema {}", idFieldSchema);
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
   * Retrieves in a controlled way the data from database
   *
   * This method ensures that Sorting Field Criteria is cleaned after every invocation.
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
   * Removes duplicated records in the query.
   *
   * @param records the records
   *
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(List<RecordValue> records) {
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
   * @return the position from any object id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public ValidationLinkVO getPositionFromAnyObjectId(Long id, Long idDataset, TypeEntityEnum type)
      throws EEAException {

    ValidationLinkVO validationLink = new ValidationLinkVO();
    // TYPE 1 table; 2 record; 3 field
    RecordValue record = new RecordValue();
    List<RecordValue> records = new ArrayList<>();

    // TABLE
    if (TypeEntityEnum.TABLE == type) {
      TableValue table = tableRepository.findByIdAndDatasetId_Id(id, idDataset);
      records = table.getRecords();
      if (records != null && !records.isEmpty()) {
        record = records.get(0);
      }
    }

    // RECORD
    if (TypeEntityEnum.RECORD == type) {
      record = recordRepository.findByIdAndTableValue_DatasetId_Id(id, idDataset);
      records = record.getTableValue().getRecords();
    }

    // FIELD
    if (TypeEntityEnum.FIELD == type) {

      FieldValue field = fieldRepository.findByIdAndRecord_TableValue_DatasetId_Id(id, idDataset);
      if (field != null && field.getRecord() != null && field.getRecord().getTableValue() != null) {
        records = field.getRecord().getTableValue().getRecords();
        record = field.getRecord();
      }
    }

    if (records != null && !records.isEmpty()) {
      records = this.sanitizeRecords(records);
      int recordPosition = records.indexOf(record);

      validationLink.setIdTableSchema(record.getTableValue().getIdTableSchema());
      validationLink.setPosition(Long.valueOf(recordPosition));


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
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   *
   * @return the statistics
   *
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public StatisticsVO getStatistics(final Long datasetId) throws EEAException {

    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());
    StatisticsVO stats = new StatisticsVO();
    if (dataset.getId() != null && StringUtils.isNotBlank(dataset.getIdDatasetSchema())) {
      List<TableValue> allTableValues = dataset.getTableValues();
      stats.setIdDataSetSchema(dataset.getIdDatasetSchema());
      stats.setDatasetErrors(false);
      stats.setTables(new ArrayList<>());

      DataSetSchema schema =
          schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getIdDatasetSchema()));
      stats.setNameDataSetSchema(schema.getNameDataSetSchema());
      List<String> listIdsDataSetSchema = new ArrayList<>();
      Map<String, String> mapIdNameDatasetSchema = new HashMap<>();
      for (TableSchema tableSchema : schema.getTableSchemas()) {
        listIdsDataSetSchema.add(tableSchema.getIdTableSchema().toString());
        mapIdNameDatasetSchema.put(tableSchema.getIdTableSchema().toString(),
            tableSchema.getNameTableSchema());
      }

      List<String> listIdDataSetSchema = new ArrayList<>();
      for (TableValue tableValue : allTableValues) {
        listIdDataSetSchema.add(tableValue.getIdTableSchema());

        TableStatisticsVO tableStats =
            processTableStats(tableValue, datasetId, mapIdNameDatasetSchema);
        if (tableStats.getTableErrors()) {
          stats.setDatasetErrors(true);
        }

        stats.getTables().add(tableStats);
      }

      // Check if there are empty tables
      listIdsDataSetSchema.removeAll(listIdDataSetSchema);
      for (String idTableSchem : listIdsDataSetSchema) {
        stats.getTables()
            .add(new TableStatisticsVO(idTableSchem, mapIdNameDatasetSchema.get(idTableSchem)));
      }

      // Check dataset validations
      for (DatasetValidation datasetValidation : dataset.getDatasetValidations()) {
        if (datasetValidation.getValidation() != null) {
          stats.setDatasetErrors(true);
        }
      }
      LOG.info("Statistics received from datasetId {}.", datasetId);
    } else {
      LOG_ERROR.error("No dataset founded to show statistics. DatasetId:{}", datasetId);
    }
    return stats;
  }


  /**
   * Process table stats.
   *
   * @param tableValue the table value
   * @param datasetId the dataset id
   * @param mapIdNameDatasetSchema the map id name dataset schema
   *
   * @return the table statistics VO
   */
  private TableStatisticsVO processTableStats(TableValue tableValue, Long datasetId,
      Map<String, String> mapIdNameDatasetSchema) {

    HashSet<Long> recordIdsFromRecordWithValidationError =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.ERROR);

    HashSet<Long> recordIdsFromRecordWithValidationWarning =
        recordValidationRepository.findRecordIdFromRecordWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.WARNING);

    HashSet<Long> recordIdsFromFieldWithValidationError =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.ERROR);

    HashSet<Long> recordIdsFromFieldWithValidationWarning =
        recordValidationRepository.findRecordIdFromFieldWithValidationsByLevelError(datasetId,
            tableValue.getIdTableSchema(), TypeErrorEnum.WARNING);

    Set<Long> idsErrors = new HashSet<>();
    idsErrors.addAll(recordIdsFromRecordWithValidationError);
    idsErrors.addAll(recordIdsFromFieldWithValidationError);

    Set<Long> idsWarnings = new HashSet<>();
    idsWarnings.addAll(recordIdsFromRecordWithValidationWarning);
    idsWarnings.addAll(recordIdsFromFieldWithValidationWarning);

    idsWarnings.removeAll(idsErrors);

    Long countRecords = tableRepository.countRecordsByIdTableSchema(tableValue.getIdTableSchema());

    TableStatisticsVO tableStats = new TableStatisticsVO();
    tableStats.setIdTableSchema(tableValue.getIdTableSchema());
    tableStats.setTotalRecords(countRecords);

    Long totalRecordsWithErrors = Long.valueOf(idsErrors.size());
    Long totalRecordsWithWarnings = Long.valueOf(idsWarnings.size());
    Long totalTableErrors = totalRecordsWithErrors + totalRecordsWithWarnings;

    totalTableErrors = totalTableErrors + tableValue.getTableValidations().size();

    tableStats.setNameTableSchema(mapIdNameDatasetSchema.get(tableValue.getIdTableSchema()));
    tableStats.setTotalErrors(totalTableErrors);
    tableStats.setTotalRecordsWithErrors(totalRecordsWithErrors);
    tableStats.setTotalRecordsWithWarnings(totalRecordsWithWarnings);
    tableStats.setTableErrors(totalTableErrors > 0 ? true : false);

    return tableStats;

  }


  /**
   * Returns map with key = IdField value=List of FieldValidation.
   *
   * @param recordIds the record ids
   *
   * @return the Map
   */
  private Map<Long, List<FieldValidation>> getFieldValidations(List<Long> recordIds) {
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
  private Map<Long, List<RecordValidation>> getRecordValidations(List<Long> recordIds) {

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
   * Gets the field errors.
   *
   * @param datasetId the dataset id
   * @param mapNameTableSchema the map name table schema
   * @return the field errors
   */
  @Override
  public List<ErrorsValidationVO> getFieldErrors(Long datasetId,
      Map<String, String> mapNameTableSchema) {
    List<FieldValidation> fieldValidations =
        fieldValidationRepository.findFieldValidationsByIdDataset(datasetId);
    List<ErrorsValidationVO> errors = new ArrayList<>();
    for (FieldValidation fieldValidation : fieldValidations) {

      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(fieldValidation.getFieldValue().getId());
      error.setIdValidation(fieldValidation.getValidation().getId());
      error.setLevelError(fieldValidation.getValidation().getLevelError().name());
      error.setMessage(fieldValidation.getValidation().getMessage());
      error.setNameTableSchema(mapNameTableSchema
          .get(fieldValidation.getFieldValue().getRecord().getTableValue().getIdTableSchema()));

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
  public List<ErrorsValidationVO> getRecordErrors(Long datasetId,
      Map<String, String> mapNameTableSchema) {
    List<RecordValidation> recordValidations =
        recordValidationRepository.findRecordValidationsByIdDataset(datasetId);
    List<ErrorsValidationVO> errors = new ArrayList<>();
    for (RecordValidation recordValidation : recordValidations) {

      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(recordValidation.getRecordValue().getId());
      error.setIdValidation(recordValidation.getValidation().getId());
      error.setLevelError(recordValidation.getValidation().getLevelError().name());
      error.setMessage(recordValidation.getValidation().getMessage());
      error.setNameTableSchema(mapNameTableSchema
          .get(recordValidation.getRecordValue().getTableValue().getIdTableSchema()));

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
  public List<ErrorsValidationVO> getTableErrors(Long datasetId,
      Map<String, String> mapNameTableSchema) {
    List<TableValidation> tableValidations =
        tableValidationRepository.findTableValidationsByIdDataset(datasetId);
    List<ErrorsValidationVO> errors = new ArrayList<>();
    for (TableValidation tableValidation : tableValidations) {

      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(tableValidation.getTableValue().getId());
      error.setIdValidation(tableValidation.getValidation().getId());
      error.setLevelError(tableValidation.getValidation().getLevelError().name());
      error.setMessage(tableValidation.getValidation().getMessage());
      error.setNameTableSchema(
          mapNameTableSchema.get(tableValidation.getTableValue().getIdTableSchema()));

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
   * @param dataset the dataset
   * @param mapNameTableSchema the map name table schema
   * @return the dataset errors
   */
  @Override
  public List<ErrorsValidationVO> getDatasetErrors(DatasetValue dataset,
      Map<String, String> mapNameTableSchema) {
    List<ErrorsValidationVO> errors = new ArrayList<>();
    for (DatasetValidation datasetValidation : dataset.getDatasetValidations()) {
      ErrorsValidationVO error = new ErrorsValidationVO();
      error.setIdObject(datasetValidation.getDatasetValue().getId());
      error.setIdValidation(datasetValidation.getValidation().getId());
      error.setLevelError(datasetValidation.getValidation().getLevelError().name());
      error.setMessage(datasetValidation.getValidation().getMessage());
      error.setNameTableSchema(mapNameTableSchema.get(dataset.getIdDatasetSchema()));
      error.setIdTableSchema(dataset.getIdDatasetSchema());
      error.setTypeEntity(datasetValidation.getValidation().getTypeEntity().name());
      error.setValidationDate(datasetValidation.getValidation().getValidationDate());

      errors.add(error);
    }
    return errors;
  }


  @Override
  public DatasetValue getDatasetValuebyId(final Long datasetId) throws EEAException {
    if (datasetId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return datasetRepository.findById(datasetId).orElse(new DatasetValue());
  }


  @Override
  public DataSetSchema getfindByIdDataSetSchema(Long datasetId, ObjectId datasetSchemaId)
      throws EEAException {
    if (datasetId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    return schemasRepository.findByIdDataSetSchema(datasetSchemaId);
  }

}
