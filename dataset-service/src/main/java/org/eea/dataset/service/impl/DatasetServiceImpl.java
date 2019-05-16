package org.eea.dataset.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.eea.dataset.multitenancy.DatasetId;
import org.eea.dataset.persistence.domain.Record;
import org.eea.dataset.persistence.repository.RecordRepository;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
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

	private static final Logger LOG = LoggerFactory.getLogger(DatasetServiceImpl.class);
	private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

	@Autowired
	private RecordRepository recordRepository;

//	@Autowired
//	private DatasetRepository datasetRepository;

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
	@Transactional
	public void createEmptyDataset(final String datasetName) {
		recordStoreControllerZull.createEmptyDataset(datasetName);
	}

	@Override
	@Transactional
	public void processFile(@DatasetId String datasetId, MultipartFile file) throws EEAException {
		if (file == null) {
			throw new EEAException("Empty file");
		}
		if (datasetId == null) {
			throw new EEAException("Bad Request");
		}
		// obtains the file type from the extension
		String mimeType = getMimetype(file);
		try (InputStream inputStream = file.getInputStream()) {
			// create the right file parser for the file type
			IFileParseContext context = fileParserFactory.createContext(mimeType);
			DataSetVO datasetVO = context.parse(inputStream);
			// move the VO to the entity
//			mapper.getmap();
//			Dataset dataset = mapper(datasetVO);
			// save dataset to the database
//			 recordRepository.save(dataset);
			// after the dataset has been saved, an event is sent to notify it
			sendMessage(EventType.DATASET_PARSED_FILE_EVENT, datasetId);
		} catch (IOException e) {
			LOG_ERROR.error(e.getMessage());
		} catch (Exception e) {
			LOG_ERROR.error(e.getMessage());
		}
	}

	/**
	 * Gets the mimetype.
	 *
	 * @param file the file
	 * @return the mimetype
	 */
	private String getMimetype(MultipartFile file) {
		String mimeType = null;
		try {
			mimeType = file.getContentType();
			if (mimeType == null) {
				int i = file.getOriginalFilename().lastIndexOf('.');
				if (i == -1) {
					throw new EEAException("the file needs an extension");
				}
				mimeType = file.getOriginalFilename().substring(i + 1);
			} else {
				mimeType = mimeType.split("/")[0];
			}
		} catch (EEAException e) {
			LOG.error(e.getMessage());
		}
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
}
