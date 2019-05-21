package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.data.domain.Dataset;
import org.eea.dataset.persistence.data.domain.Record;
import org.eea.dataset.persistence.data.repository.DatasetRepository;
import org.eea.dataset.persistence.data.repository.RecordRepository;
import org.eea.dataset.persistence.metabase.domain.PartitionDataSetMetabase;
import org.eea.dataset.persistence.metabase.repository.PartitionDataSetMetabaseRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
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
import org.eea.interfaces.vo.dataset.enums.TypeData;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
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

  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private DataSetMapper dataSetMapper;

  @Autowired
  private RecordRepository recordRepository;

  @Autowired
  private PartitionDataSetMetabaseRepository partitionDataSetMetabaseRepository;

  @Autowired
  private DatasetRepository datasetRepository;

  @Autowired
  private SchemasRepository schemasRepository;

  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  @Autowired
  private IFileParserFactory fileParserFactory;

  @Autowired
  private KafkaSender kafkaSender;

  @Override
  public DataSetVO getDatasetById(@DatasetId final String datasetId) {
    final DataSetVO dataset = new DataSetVO();
    final List<RecordVO> recordVOs = new ArrayList<>();
    final List<Record> records = recordRepository.specialFind(datasetId);
    if (!records.isEmpty()) {
      for (final Record record : records) {
        final RecordVO vo = new RecordVO();
        vo.setId(record.getId().toString());
        recordVOs.add(vo);
      }
      dataset.setId(datasetId);
    }

    return dataset;
  }

  @Override
  @Transactional
  public void addRecordToDataset(@DatasetId final String datasetId, final List<RecordVO> records) {

    for (final RecordVO recordVO : records) {
      final Record r = new Record();
      r.setId(Integer.valueOf(recordVO.getId()));
      recordRepository.save(r);
    }

  }

  @Override
  @Transactional
  public void createEmptyDataset(final String datasetName) {
    recordStoreControllerZull.createEmptyDataset(datasetName);
  }

  @Override
  public void createDataSchema(String datasetName) {

    TypeData headerType = TypeData.BOOLEAN;

    DataSetSchema dataSetSchema = new DataSetSchema();



    long numeroRegistros = schemasRepository.count();
    ObjectId objectid = new ObjectId();
    dataSetSchema.setIdDataSetSchema(objectid);
    dataSetSchema.setIdDataFlow(1L);
    List<TableSchema> tableSchemas = new ArrayList<>();
    Long dssID = 0L;
    Long fsID = 0L;

    for (int dss = 1; dss <= 2; dss++) {
      TableSchema tableSchema = new TableSchema();
      tableSchema.setIdTableSchema(new ObjectId());
      tableSchema.setNameSchema("tabla_"+dss);
      tableSchema.setIdDataSet(objectid);

      RecordSchema recordSchema = new RecordSchema();
      recordSchema.setIdRecordSchema(new ObjectId());
      recordSchema.setIdTableSchema(tableSchema.getIdTableSchema());
      List<FieldSchema> fieldSchemas = new ArrayList<>();

      for (int fs = 1; fs <= 20; fs++) {
        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema = new FieldSchema();
        fieldSchema.setIdFieldSchema(new ObjectId());
        fieldSchema.setIdRecord(recordSchema.getIdRecordSchema());
        if (dss / 2 == 1) {
          int dato = fs + 10;
          fieldSchema.setHeaderName("campo_" + dato);
          fieldSchema.setType(TypeData.FLOAT);
        } else {
          fieldSchema.setHeaderName("campo_" + fs);
          fieldSchema.setType(headerType);
        }

        fieldSchemas.add(fieldSchema);
      }
      recordSchema.setFieldSchema(fieldSchemas);
      tableSchema.setRecordSchema(recordSchema);
      tableSchemas.add(tableSchema);
    }
    dataSetSchema.setTableSchemas(tableSchemas);
    schemasRepository.save(dataSetSchema);



  }
  
  /**
   * Find the dataschema per id
   * @param dataschemaId the idDataschema
   */
  @Override
  public DataSetSchemaVO getDataSchemaById(String dataschemaId) {
    
    Optional<DataSetSchema> dataschema = schemasRepository.findById(new ObjectId(dataschemaId));
    LOG.info("devolviendo dataschema {}", dataschema);
    
    DataSetSchemaVO dataSchemaVO = new DataSetSchemaVO();
    if(dataschema.isPresent()) {
      DataSetSchema datasetSchema = dataschema.get();
      dataSchemaVO = mapeoDataSchema(datasetSchema);
    }
    
    return dataSchemaVO;
    
  }
  
  /**
   * Find the dataschema per idDataFlow
   * @param idFlow the idDataFlow to look for
   */
  @Override
  public DataSetSchemaVO getDataSchemaByIdFlow(Long idFlow) {
    
    DataSetSchema dataschema = schemasRepository.findSchemaByIdFlow(idFlow);
   
    final DataSetSchemaVO dataSchemaVO = mapeoDataSchema(dataschema);
    
    return dataSchemaVO;
    
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
   * Process file.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @throws EEAException the EEA exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  @Transactional
  public void processFile(@DatasetId String datasetId, MultipartFile file)
      throws EEAException, IOException {
    // obtains the file type from the extension
    String mimeType = getMimetype(file.getOriginalFilename());
    try (InputStream inputStream = file.getInputStream()) {
      PartitionDataSetMetabase partition = partitionDataSetMetabaseRepository
          .findOneByIdDataSetAndUsername(datasetId, "root").orNull();

      // create the right file parser for the file type
      IFileParseContext context = fileParserFactory.createContext(mimeType);
      DataSetVO datasetVO = context.parse(inputStream, datasetId, partition.getId());
      // move the VO to the entity
      if (datasetVO == null) {
        throw new IOException();
      }
      datasetVO.setId(datasetId);
      Dataset dataset = dataSetMapper.classToEntity(datasetVO);
      // save dataset to the database
      datasetRepository.save(dataset);
      // after the dataset has been saved, an event is sent to notify it
      sendMessage(EventType.DATASET_PARSED_FILE_EVENT, datasetId);
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
   * send message encapsulates the logic to send an event message to kafka.
   *
   * @param eventType the event type
   * @param datasetId the dataset id
   */
  private void sendMessage(EventType eventType, String datasetId) {

    EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("dataset_id", datasetId);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }
  
  
  /**
   * map DataSchema to DataSchemaVO
   * 
   * @param schema the DataSetSchema
   * @return the dataSchemaVO filled
   */
  private DataSetSchemaVO mapeoDataSchema(DataSetSchema schema) {
    
    DataSetSchemaVO data = new DataSetSchemaVO();
    data.setIdDataSetSchema(schema.getIdDataSetSchema());
    if(!schema.getTableSchemas().isEmpty()) {
      List<TableSchemaVO> tableVo = new ArrayList<TableSchemaVO>();
      for(TableSchema tabla : schema.getTableSchemas()) {
        TableSchemaVO table = new TableSchemaVO();
        table.setIdTableSchema(tabla.getIdTableSchema());
        table.setNameSchema(tabla.getNameSchema());
        if(tabla.getRecordSchema()!=null) {
          RecordSchemaVO registro = new RecordSchemaVO();
          registro.setIdRecordSchema(tabla.getRecordSchema().getIdRecordSchema());
          registro.setNameSchema(tabla.getNameSchema());
          if(!tabla.getRecordSchema().getFieldSchema().isEmpty()) {
            List<FieldSchemaVO> listaRegistro = new ArrayList<FieldSchemaVO>();
            for(FieldSchema field : tabla.getRecordSchema().getFieldSchema()) {
              FieldSchemaVO campo = new FieldSchemaVO();
              campo.setId(field.getIdFieldSchema().toString());
              campo.setIdRecord(field.getIdRecord().toString());
              campo.setName(field.getHeaderName());
              campo.setType(field.getType());
             
              listaRegistro.add(campo);
            }
            registro.setFieldSchema(listaRegistro);
          }
          table.setRecordSchema(registro);
        }
       
        tableVo.add(table);
       
      }
      data.setTableSchemas(tableVo);
    }
    return data;
  }
  
  
}
