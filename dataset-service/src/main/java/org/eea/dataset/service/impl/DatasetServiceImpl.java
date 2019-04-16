package org.eea.dataset.service.impl;

import ch.qos.logback.core.ConsoleAppender;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.multitenancy.MultiTenantDataSource;
import org.eea.dataset.persistence.domain.Record;
import org.eea.dataset.persistence.repository.RecordRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
  private RecordStoreControllerZull recordStoreControllerZull;

  @Autowired
  private DataSource multiTenantDataSource;

  @Override
  public DataSetVO getDatasetById(@DatasetId String datasetId)  {
    DataSetVO dataset = new DataSetVO();
    List<RecordVO> recordVOs = new ArrayList<>();
    LOG.info("devolviendo datos chulos {}", dataset);
    LOG_ERROR.error("hola  {}", datasetId);
    ConsoleAppender a;
    Date start = new Date();
    List<Record> records = recordRepository.specialFind(datasetId);
    if (records.size() > 0) {
      for (Record record : records) {
        RecordVO vo = new RecordVO();
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
  public void addRecordToDataset(@DatasetId String datasetId, List<RecordVO> records) {

    for (RecordVO recordVO : records) {
      Record r = new Record();
      r.setId(Integer.valueOf(recordVO.getId()));
      r.setName(recordVO.getName());
      recordRepository.save(r);
    }

  }

  @Override
  public void createEmptyDataset(String datasetName) {
    ConnectionDataVO connectionDataVO = recordStoreControllerZull.createEmptyDataset(datasetName);
    ((MultiTenantDataSource) multiTenantDataSource).addDataSource(connectionDataVO);

  }
}
