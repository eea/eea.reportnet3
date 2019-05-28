package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.mapper.DataSetNoDataMapper;
import org.eea.dataset.mapper.DataSetTablesMapper;
import org.eea.dataset.mapper.RecordMapper;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.TableCollection;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseTableCollection;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.metabese.TableCollectionVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * The type Dataset service.
 */
@Service("datasetService")
public class DatasetServiceImpl implements DatasetService {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The data set mapper. */
  @Autowired
  private DataSetMapper dataSetMapper;

  /** The data set no data mapper. */
  @Autowired
  private DataSetNoDataMapper dataSetNoDataMapper;

  /** The data set tables mapper. */
  @Autowired
  private DataSetTablesMapper dataSetTablesMapper;

  /** The record mapper. */
  @Autowired
  private RecordMapper recordMapper;

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  /** The data set metabase repository. */
  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The partition data set metabase repository. */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

  /** The data set metabase table collection. */
  @Autowired
  private DataSetMetabaseTableCollection dataSetMetabaseTableCollection;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /** The record store controller zull. */
  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  /** The file parser factory. */
  @Autowired
  private IFileParserFactory fileParserFactory;

  /** The kafka sender. */
  @Autowired
  private KafkaSender kafkaSender;

  /**
   * Gets the dataset by id.
   *
   * @param datasetId the dataset id
   * @return the dataset by id
   */
  @Override
  public DataSetVO getDatasetById(@DatasetId final Long datasetId) {
    final DataSetVO dataset = new DataSetVO();
    final List<RecordVO> recordVOs = new ArrayList<>();



    return dataset;
  }

  /**
   * Gets the dataset values by id.
   *
   * @param datasetId the dataset id
   * @return the dataset values by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public DataSetVO getDatasetValuesById(@DatasetId final Long datasetId) throws EEAException {

    DatasetValue dataset = datasetRepository.findById(datasetId).orElse(null);
    if (dataset == null) {
      throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
    }
    DataSetVO result = dataSetNoDataMapper.entityToClass(dataset);
    Pageable p = PageRequest.of(0, 20, Sort.by("id").descending());
    // this result has no records since we need'em in a pagination way
    result.getTableVO().stream().forEach(table -> {
      table.setRecords(
          recordMapper.entityListToClass(recordRepository.findByTableValue_Id(table.getId(), p)));
    });

    return result;
  }

  /**
   * Adds the record to dataset.
   *
   * @param datasetId the dataset id
   * @param records the records
   */
  @Override
  @Transactional
  public void addRecordToDataset(@DatasetId final Long datasetId, final List<RecordVO> records) {

    for (final RecordVO recordVO : records) {
      final RecordValue r = new RecordValue();
      // r.setId(Integer.valueOf(recordVO.getId()));
      recordRepository.save(r);
    }

  }

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
   */
  @Override
  @Transactional
  public void processFile(@DatasetId Long datasetId, String fileName, InputStream is)
      throws EEAException, IOException {
    // obtains the file type from the extension
    String mimeType = getMimetype(fileName);
    // validates file types for the data load
    validateFileType(mimeType);
    try {
      PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
          .findFirstByIdDataSet_idAndUsername(datasetId, "root").orElse(null);
      if (partition == null) {
        throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
      }
      // We get the dataFlowId from the metabase
      DataSetMetabase datasetMetabase = dataSetMetabaseRepository.findById(datasetId).orElse(null);
      if (datasetMetabase == null) {
        throw new EEAException(EEAErrorMessage.DATASET_NOTFOUND);
      }
      // create the right file parser for the file type
      IFileParseContext context = fileParserFactory.createContext(mimeType);
      DataSetVO datasetVO = context.parse(is, datasetMetabase.getDataflowId(), partition.getId());
      // map the VO to the entity
      if (datasetVO == null) {
        throw new IOException("Empty dataset");
      }
      datasetVO.setId(datasetId);
      DatasetValue dataset = dataSetMapper.classToEntity(datasetVO);
      if (dataset == null) {
        throw new IOException("Error mapping file");
      }
      // save dataset to the database
      datasetRepository.save(dataset);
      // after the dataset has been saved, an event is sent to notify it
      releaseKafkaEvent(EventType.DATASET_PARSED_FILE_EVENT, datasetId);
    } finally {
      is.close();
    }
  }



  /**
   * Gets the mimetype.
   *
   * @param file the file
   * @return the mimetype
   * @throws EEAException the EEA exception
   */
  private String getMimetype(String file) throws EEAException {
    String mimeType = null;
    int location = file.lastIndexOf('.');
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
   * @throws EEAException the EEA exception
   */
  private void validateFileType(String mimeType) throws EEAException {
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
  public void deleteDataSchema(String datasetId) {
    schemasRepository.deleteById(new ObjectId(datasetId));

  }

  /**
   * We call jpaRepository and delete.
   *
   * @param dataSetId the data set id
   */
  @Override
  public void deleteImportData(Long dataSetId) {
    datasetRepository.deleteById(dataSetId);
  }

  /**
   * send message encapsulates the logic to send an event message to kafka.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  private void releaseKafkaEvent(EventType eventType, Long datasetId) {

    EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }

  /**
   * Gets the table values by id.
   *
   * @param MongoID the mongo ID
   * @param pageable the pageable
   * @return the table values by id
   * @throws EEAException the EEA exception
   */
  @Override
  @Transactional
  public TableVO getTableValuesById(final String MongoID, final Pageable pageable)
      throws EEAException {

    List<RecordValue> record = recordRepository.findByTableValue_IdMongo(MongoID, pageable);


    Long resultcount = countTableData(record.get(0).getTableValue().getId());

    TableVO result = new TableVO();
    result.setRecords(recordMapper.entityListToClass(record));

    result.setTotalRecords(resultcount);
    return result;
  }


  /**
   * Count table data.
   *
   * @param tableId the table id
   * @return the long
   */
  @Override
  public Long countTableData(Long tableId) {
    return recordRepository.countByTableValue_id(tableId);
  }



  /**
   * Sets the mongo tables.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableName the table name
   * @param Headers the headers
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public void setMongoTables(@DatasetId Long datasetId, Long dataFlowId,
      TableCollectionVO tableCollectionVO) throws EEAException, IOException {

    TableCollection tableCollection = dataSetTablesMapper.classToEntity(tableCollectionVO);

    tableCollection.setDataSetId(datasetId);
    tableCollection.setDataFlowId(dataFlowId);

    dataSetMetabaseTableCollection.save(tableCollection);

    System.out.println("guardado");

  }
}
