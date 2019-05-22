package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.data.domain.Dataset;
import org.eea.dataset.persistence.data.domain.Record;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.DataSetMetabase;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.DataSetMetabaseRepository;
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
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.io.KafkaSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

  /** The record repository. */
  @Autowired
  private RecordRepository recordRepository;

  @Autowired
  private DataSetMetabaseRepository dataSetMetabaseRepository;

  /** The partition data set metabase repository. */
  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  /** The dataset repository. */
  @Autowired
  private DatasetRepository datasetRepository;

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
    final List<Record> records = recordRepository.specialFind(datasetId);
    if (!records.isEmpty()) {
      for (final Record record : records) {
        final RecordVO vo = new RecordVO();
        // vo.setId(record.getId().toString());
        recordVOs.add(vo);
      }
      // dataset.setId(datasetId);
    }

    return dataset;
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
      final Record r = new Record();
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
   * 
   *
   * @param datasetId
   * @param file
   * @throws EEAException
   */
  @Override
  @Transactional
  public void processFile(@DatasetId Long datasetId, MultipartFile file)
      throws EEAException, IOException {
    // obtains the file type from the extension
    String mimeType = getMimetype(file.getOriginalFilename());
    try (InputStream inputStream = file.getInputStream()) {
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
      DataSetVO datasetVO =
          context.parse(inputStream, datasetMetabase.getDataflowId(), partition.getId());
      // map the VO to the entity
      if (datasetVO == null) {
        throw new IOException();
      }
      datasetVO.setId(datasetId);
      Dataset dataset = dataSetMapper.classToEntity(datasetVO);
      dataset.setId(datasetId);

      for (TableValue table : dataset.getTableValues()) {
        table.setDatasetId(dataset);
      }
      // save dataset to the database
      datasetRepository.save(dataset);
      // after the dataset has been saved, an event is sent to notify it
      sendMessage(EventType.DATASET_PARSED_FILE_EVENT, datasetId);
    } catch (Exception e) {
      LOG.info(e.getMessage());
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
   * We delete the data imported
   *
   * @param datasetName the id of the data
   */
  @Override
  public void deleteDataSchema(String datasetId) {
    schemasRepository.deleteById(new ObjectId(datasetId));

  }

  /**
   * send message encapsulates the logic to send an event message to kafka.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  private void sendMessage(EventType eventType, Long datasetId) {

    EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }


}
