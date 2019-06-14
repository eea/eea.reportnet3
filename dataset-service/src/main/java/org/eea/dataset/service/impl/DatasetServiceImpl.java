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
import javax.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.DataSetTablesMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.data.SortFieldsHelper;
import org.eea.dataset.persistence.data.domain.DatasetValidation;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.FieldValidation;
import org.eea.dataset.persistence.data.domain.RecordValidation;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.FieldRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.data.repository.TableRepository;
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
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableStatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
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

  /** The table repository. */
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
   * Creates the empty dataset.
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
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   */
  @Override
  @Transactional
  public void processFile(@DatasetId final Long datasetId, final String fileName,
      final InputStream is) throws EEAException, IOException, InterruptedException {
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
      final DataSetMetabase datasetMetabase = obtainDatasetMetabase(datasetId);
      // create the right file parser for the file type
      final IFileParseContext context = fileParserFactory.createContext(mimeType);
      final DataSetVO datasetVO =
          context.parse(is, datasetMetabase.getDataflowId(), partition.getId());
      // map the VO to the entity
      if (datasetVO == null) {
        throw new IOException("Empty dataset");
      }
      datasetVO.setId(datasetId);
      final DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
      if (dataset == null) {
        throw new IOException("Error mapping file");
      }
      // save dataset to the database
      datasetRepository.saveAndFlush(dataset);
      LOG.info("File processed and saved into DB");
    } finally {
      is.close();
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
  private static String getMimetype(final String file) throws EEAException {
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
  private static void validateFileType(final String mimeType) throws EEAException {
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
    datasetRepository.empty(dataSetId);
    LOG.info("All data value deleted from dataSetId {}", dataSetId);
  }


  /**
   * Gets the table values by id. It additionally can page the results and sort them
   *
   * sort is handmade since the criteria is the idFieldValue of the Fields inside the records.
   *
   * @param datasetId the dataset id
   * @param mongoID the mongo ID
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
  public TableVO getTableValuesById(final Long datasetId, final String mongoID,
      final Pageable pageable, final String idFieldSchema, final Boolean asc) throws EEAException {
    List<RecordValue> records = retrieveRecordValue(mongoID, idFieldSchema);
    if (records == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    TableVO result = new TableVO();
    if (records.isEmpty()) {
      result.setTotalRecords(0L);
      result.setRecords(new ArrayList<>());
      LOG.info("No records founded in datasetId {}", datasetId);

    } else {
      records = this.sanitizeRecords(records);

      List<RecordVO> recordVOs = recordMapper.entityListToClass(records);
      Optional.ofNullable(idFieldSchema).ifPresent(field -> {
        recordVOs.sort((RecordVO v1, RecordVO v2) -> {
          String sortCriteria1 = v1.getSortCriteria();
          String sortCriteria2 = v2.getSortCriteria();
          // process the sort criteria
          // it could happen that some values has no sortCriteria due to a matching error
          // during the load process. If this is the case we need to ensure that sort logic does not
          // fail
          int sort = 0;
          if (null == sortCriteria1) {
            if (null != sortCriteria2) {
              sort = -1;
            }
          } else {
            if (null != sortCriteria2) {
              sort = asc ? sortCriteria1.compareTo(sortCriteria2)
                  : sortCriteria1.compareTo(sortCriteria2) * -1;
            } else {
              sort = 1;
            }
          }
          return sort;
        });
      });
      int initIndex = pageable.getPageNumber() * pageable.getPageSize();
      int endIndex = (pageable.getPageNumber() + 1) * pageable.getPageSize() > recordVOs.size()
          ? recordVOs.size()
          : (pageable.getPageNumber() + 1) * pageable.getPageSize();
      result.setRecords(recordVOs.subList(initIndex, endIndex));
      result.setTotalRecords(Long.valueOf(recordVOs.size()));
      LOG.info("Total records founded in datasetId {}: {}. Now in page {}, {} records by page",
          datasetId, recordVOs.size(), pageable.getPageNumber(), pageable.getPageSize());
      if (StringUtils.isNotBlank(idFieldSchema)) {
        LOG.info("Ordered by idFieldSchema {}", idFieldSchema);
      }
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
   * @return the list
   */
  private List<RecordValue> retrieveRecordValue(String idTableSchema, String idFieldSchema) {
    Optional.ofNullable(idFieldSchema).ifPresent(field -> SortFieldsHelper.setSortingField(field));
    List<RecordValue> records = null;
    try {
      records = recordRepository.findByTableValue_IdTableSchema(idTableSchema);
    } finally {
      SortFieldsHelper.cleanSortingField();
    }

    return records;
  }

  /**
   * Removes duplicated records in the query.
   *
   * @param records the records
   * @return the list
   */
  private List<RecordValue> sanitizeRecords(List<RecordValue> records) {
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
  public void setDataschemaTables(@DatasetId final Long datasetId, final Long dataFlowId,
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
   * @return the by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
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
    // datasetValue = datasetRepository.findById(datasetId).orElse(null);
    // if (datasetValue != null) {
    // //return multiThreadMapper(datasetValue);
    // }
    return dataSetMapper.entityToClass(datasetValue);
  }


  /**
   * Update dataset.
   *
   * @param datasetId the dataset id
   * @param dataset the dataset
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
   * @return the data flow id by id
   * @throws EEAException the EEA exception
   */
  @Override
  public Long getDataFlowIdById(Long datasetId) throws EEAException {
    return dataSetMetabaseRepository.findDataflowIdById(datasetId);
  }



  /**
   * Gets the statistics.
   *
   * @param datasetId the dataset id
   * @return the statistics
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public StatisticsVO getStatistics(final Long datasetId) throws EEAException {

    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(new DatasetValue());

    List<TableValue> allTableValues = dataset.getTableValues();
    StatisticsVO stats = new StatisticsVO();
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

      TableStatisticsVO tableStats = processTableStats(tableValue, datasetId);
      if (tableStats.getTableErrors()) {
        stats.setDatasetErrors(true);
      }

      stats.getTables().add(tableStats);
    }

    // Check if there are empty tables
    listIdsDataSetSchema.removeAll(listIdDataSetSchema);
    for (String idTableSchem : listIdsDataSetSchema) {
      stats.getTables()
          .add(createEmptyTableStat(idTableSchem, mapIdNameDatasetSchema.get(idTableSchem)));
    }

    // Check dataset validations
    for (DatasetValidation datasetValidation : dataset.getDatasetValidations()) {
      if (datasetValidation.getValidation() != null) {
        stats.setDatasetErrors(true);
      }
    }

    return stats;
  }


  /**
   * Process table stats.
   *
   * @param tableValue the table value
   * @param datasetId the dataset id
   * @return the table statistics VO
   */
  private TableStatisticsVO processTableStats(TableValue tableValue, Long datasetId) {

    Long countRecords = tableRepository.countRecordsByIdTable(tableValue.getId());
    List<RecordValidation> recordValidations =
        recordRepository.findRecordValidationsByIdDatasetAndIdTable(datasetId, tableValue.getId());
    TableStatisticsVO tableStats = new TableStatisticsVO();
    tableStats.setIdTableSchema(tableValue.getIdTableSchema());
    tableStats.setNameTableSchema(tableValue.getName());
    tableStats.setTotalRecords(countRecords);
    Long totalTableErrors = 0L;
    Long totalRecordsWithErrors = 0L;
    Long totalRecordsWithWarnings = 0L;
    // Record validations
    for (RecordValidation recordValidation : recordValidations) {
      if (TypeErrorEnum.ERROR == recordValidation.getValidation().getLevelError()) {
        totalRecordsWithErrors++;
        totalTableErrors++;
      }
      if (TypeErrorEnum.WARNING == recordValidation.getValidation().getLevelError()) {
        totalRecordsWithWarnings++;
        totalTableErrors++;
      }
    }
    // Table validations
    totalTableErrors = totalTableErrors + tableValue.getTableValidations().size();
    // Field validations
    List<FieldValidation> fieldValidations =
        fieldRepository.findFieldValidationsByIdDatasetAndIdTable(datasetId, tableValue.getId());
    for (FieldValidation fieldValidation : fieldValidations) {
      if (TypeErrorEnum.ERROR == fieldValidation.getValidation().getLevelError()) {
        totalRecordsWithErrors++;
        totalTableErrors++;
      }
      if (TypeErrorEnum.WARNING == fieldValidation.getValidation().getLevelError()) {
        totalRecordsWithWarnings++;
        totalTableErrors++;
      }
    }

    tableStats.setTotalErrors(totalTableErrors);
    tableStats.setTotalRecordsWithErrors(totalRecordsWithErrors);
    tableStats.setTotalRecordsWithWarnings(totalRecordsWithWarnings);
    tableStats.setTableErrors(totalTableErrors > 0 ? true : false);

    return tableStats;

  }


  /**
   * Creates the empty table stat.
   *
   * @param idTableSchema the id table schema
   * @param nameTableSchema the name table schema
   * @return the table statistics VO
   */
  private TableStatisticsVO createEmptyTableStat(String idTableSchema, String nameTableSchema) {

    TableStatisticsVO tableStats = new TableStatisticsVO();
    tableStats.setIdTableSchema(idTableSchema);
    tableStats.setNameTableSchema(nameTableSchema);
    tableStats.setTableErrors(false);
    tableStats.setTotalErrors(0L);
    tableStats.setTotalRecords(0L);
    tableStats.setTotalRecordsWithErrors(0L);
    tableStats.setTotalRecordsWithWarnings(0L);

    return tableStats;
  }



}
