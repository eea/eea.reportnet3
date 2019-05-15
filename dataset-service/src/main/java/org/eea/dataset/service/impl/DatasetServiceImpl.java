package org.eea.dataset.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.domain.Record;
import org.eea.dataset.persistence.repository.RecordRepository;
import org.eea.dataset.schemas.domain.DataSetSchema;
import org.eea.dataset.schemas.domain.FiledSchema;
import org.eea.dataset.schemas.domain.HeaderType;
import org.eea.dataset.schemas.domain.RecordSchema;
import org.eea.dataset.schemas.domain.TableSchema;
import org.eea.dataset.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.qos.logback.core.ConsoleAppender;

/**
 * The type Dataset service.
 */
@Service("datasetService")
public class DatasetServiceImpl implements DatasetService {

  private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  @Autowired
  private RecordRepository recordRepository;

  @Autowired
  private SchemasRepository schemasRepository;

  @Autowired
  private RecordStoreControllerZull recordStoreControllerZull;

  @Autowired
  private DataSource multiTenantDataSource;

  @Override
  public DataSetVO getDatasetById(@DatasetId final String datasetId) {
    final DataSetVO dataset = new DataSetVO();
    final List<RecordVO> recordVOs = new ArrayList<>();
    LOG.info("devolviendo datos chulos {}", dataset);
    LOG_ERROR.error("hola  {}", datasetId);
    ConsoleAppender a;
    final Date start = new Date();
    final List<Record> records = recordRepository.specialFind(datasetId);
    if (records.size() > 0) {
      for (final Record record : records) {
        final RecordVO vo = new RecordVO();
        vo.setId(record.getId().toString());
        vo.setName(record.getName());
        recordVOs.add(vo);
      }
      dataset.setRecords(recordVOs);
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
      r.setName(recordVO.getName());
      recordRepository.save(r);
    }

  }

  @Override
  public void createEmptyDataset(final String datasetName) {
    recordStoreControllerZull.createEmptyDataset(datasetName);
  }

  @Override
  public void createDataSchema(String datasetName) {

    HeaderType headerType = HeaderType.BOOLEAN;

    DataSetSchema dataSetSchema = new DataSetSchema();



    long numeroRegistros = schemasRepository.count();
    dataSetSchema.setId(numeroRegistros + 1);
    List<TableSchema> tableSchemas = new ArrayList<>();
    Long dssID = 0L;
    Long fsID = 0L;

    for (int dss = 1; dss <= 2; dss++) {
      TableSchema tableSchema = new TableSchema();
      tableSchema.setId(dssID + dss);

      RecordSchema recordSchema = new RecordSchema();
      recordSchema.setId(dssID + dss);
      recordSchema.setIdTable(tableSchema.getId());
      List<FiledSchema> fieldSchemas = new ArrayList<>();

      for (int fs = 1; fs <= 20; fs++) {
        FiledSchema fieldSchema = new FiledSchema();
        fieldSchema = new FiledSchema();
        fieldSchema.setId(fsID + fs);
        fieldSchema.setIdRecord(recordSchema.getId());
        fieldSchema.setHeaderName("Existencia:");
        fieldSchema.setHeaderType(headerType);
        fieldSchemas.add(fieldSchema);
      }
      recordSchema.setFiledSchemas(fieldSchemas);
      tableSchema.setRecordSchema(recordSchema);
      tableSchemas.add(tableSchema);
    }
    dataSetSchema.setTableSchemas(tableSchemas);
    schemasRepository.save(dataSetSchema);



  }

}
