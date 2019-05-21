package org.eea.dataset.controller;

import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
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
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Timed("FIND_BY_ID_TIMER")
  public DataSetVO findById(@PathVariable("id") Long datasetId) {
    DataSetVO result = null;

    result = datasetService.getDatasetById(datasetId);
    // TenantResolver.clean();
    return result;
  }

  @Override
  @HystrixCommand
  @RequestMapping(value = "/getDatasetValues/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetVO findValuesById(@PathVariable("id") Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND,
          new Exception());
    }
    DataSetVO result = null;

    try {
      result = datasetService.getDatasetValuesById(datasetId);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return result;
  }


  @Override
  @RequestMapping(value = "/update", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetVO updateDataset(@RequestBody DataSetVO dataset) {
    // datasetService.addRecordToDataset(dataset.getId(), dataset.getRecords());

    return null;
  }

  @Override
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  public void createEmptyDataSet(String datasetname) {
    datasetService.createEmptyDataset(datasetname);
  }

  @Override
  @RequestMapping(value = "/createDataSchema", method = RequestMethod.POST)
  public void createDataSchema(String datasetName) {
    datasetService.createDataSchema(datasetName);
  }

  public DataSetVO errorHandler(@PathVariable("id") Long id) {
    DataSetVO dataset = new DataSetVO();
    dataset.setId(null);
    return dataset;
  }

  @Override
  @PostMapping("{id}/loadDatasetData")
  public void loadDatasetData(@PathVariable("id") Long datasetId,
      @RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT,
          new Exception());
    }
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATASET_NOTFOUND,
          new Exception());
    }
    try {
      datasetService.processFile(datasetId, file);
    } catch (EEAException e) {
      if (e.getMessage().equals(EEAErrorMessage.FILE_FORMAT)
          || e.getMessage().equals(EEAErrorMessage.FILE_EXTENSION)) {
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
      }
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
