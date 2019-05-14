package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.domain.Record;
import org.eea.dataset.persistence.repository.RecordRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.FileParserFactory;
import org.eea.dataset.service.file.interfaces.FileParseContext;
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
  private RecordStoreControllerZull recordStoreControllerZull;

  @Autowired
  private DataSource multiTenantDataSource;

  @Autowired
  private KafkaSender kafkaSender;

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
  public void processFile(MultipartFile file) throws IOException {
    // String mimeType = URLConnection.guessContentTypeFromName(file.getOriginalFilename());
    int i = file.getOriginalFilename().lastIndexOf('.');
    String mimeType = file.getOriginalFilename().substring(i + 1);

    try (InputStream inputStream = file.getInputStream()) {
      FileParseContext context = FileParserFactory.createContext(mimeType);
      DataSetVO datasetVO = context.parse(inputStream);
      sendMessage(EventType.DATASET_PARSED_FILE_EVENT, datasetVO);
    }
  }

  public void sendMessage(EventType eventType, Object data) {
    EEAEventVO event = new EEAEventVO();
    event.setEventType(eventType);
    Map<String, Object> dataOutput = new HashMap<>();
    dataOutput.put("data", data);
    event.setData(dataOutput);
    kafkaSender.sendMessage(event);
  }
}
