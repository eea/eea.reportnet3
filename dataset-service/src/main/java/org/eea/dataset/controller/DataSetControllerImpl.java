package org.eea.dataset.controller;

import java.io.IOException;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.micrometer.core.annotation.Timed;

/**
 * The type Data set controller.
 */
@RestController
@RequestMapping("/dataset")
public class DataSetControllerImpl implements DatasetController {

	private static final Logger LOG = LoggerFactory.getLogger(DataSetControllerImpl.class);
	private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

	@Autowired
	@Qualifier("proxyDatasetService")
	private DatasetService datasetService;

	@Override
	@HystrixCommand
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed("FIND_BY_ID_TIMER")
	public DataSetVO findById(@PathVariable("id") String datasetId) {
		DataSetVO result = null;

		result = datasetService.getDatasetById(datasetId);
		// TenantResolver.clean();
		return result;
	}

	@Override
	@RequestMapping(value = "/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public DataSetVO updateDataset(@RequestBody DataSetVO dataset) {
//		datasetService.addRecordToDataset(dataset.getId(), dataset.getRecords());

		return null;
	}

	@Override
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public void createEmptyDataSet(String datasetname) {
		datasetService.createEmptyDataset(datasetname);
	}

	public DataSetVO errorHandler(@PathVariable("id") String id) {
		DataSetVO dataset = new DataSetVO();
		dataset.setId("ERROR");
		return dataset;
	}

	@Override
	@PostMapping("{id}/uploadFile")
	public void loadDatasetData(@PathVariable("id") String datasetId, @RequestParam("file") MultipartFile file) {
		try {
			if (file == null || file.isEmpty()) {
				throw new IOException("File invalid");
			}
			if (datasetId == null) {
				throw new EEAException("File invalid");
			}
			datasetService.processFile(datasetId, file);
		} catch (IOException | EEAException e) {
			LOG_ERROR.error(e.getMessage());
		}
	}
}
