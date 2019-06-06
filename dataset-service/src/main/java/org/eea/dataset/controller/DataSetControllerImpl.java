package org.eea.dataset.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.callable.LoadDataCallable;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * The type Data set controller.
 */
@RestController
@RequestMapping("/dataset")
public class DataSetControllerImpl implements DatasetController {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The dataset service.
   */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * Gets the data tables values.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the mongo ID
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param asc the asc
   *
   * @return the data tables values
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "TableValueDataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true") Boolean asc) {

    if (null == datasetId || null == idTableSchema) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    Pageable pageable = PageRequest.of(pageNum, pageSize);

    TableVO result = null;
    try {
      result = datasetService.getTableValuesById(datasetId, idTableSchema, pageable, fields, asc);
    } catch (EEAException e) {
      if (e.getMessage().equals(EEAErrorMessage.DATASET_NOTFOUND)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
      }
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    return result;
  }


  /**
   * Update dataset.
   *
   * @param dataset the dataset
   *
   * @return the data set VO
   */
  @Override
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetVO updateDataset(@RequestBody final DataSetVO dataset) {
    return null;
  }

  /**
   * Creates the empty data set.
   *
   * @param datasetname the datasetname
   */
  @Override
  @PostMapping(value = "/create")
  public void createEmptyDataSet(final String datasetname) {
    if (StringUtils.isBlank(datasetname)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    datasetService.createEmptyDataset(datasetname);
  }

  /**
   * Load dataset data.
   *
   * @param datasetId the dataset id
   * @param file the file
   */
  @Override
  @PostMapping("{id}/loadDatasetData")
  public void loadDatasetData(@PathVariable("id") final Long datasetId,
      @RequestParam("file") final MultipartFile file) {
    // filter if the file is empty
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    // extract the filename
    String fileName = file.getOriginalFilename();
    final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    LoadDataCallable callable = null;
    // extract the file content
    try {
      InputStream is = file.getInputStream();
      callable = new LoadDataCallable(this.datasetService, datasetId, fileName, is);
      executor.submit(callable);
      // NOPMD this cannot be avoid since Callable throws Exception in
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    } finally {
      executor.shutdown();
    }

  }

  /**
   * Call services delete.
   *
   * @param dataSetId id import
   */
  @Override
  @DeleteMapping(value = "/deleteImportData")
  public void deleteImportData(final Long dataSetId) {
    if (dataSetId == null || dataSetId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    datasetService.deleteImportData(dataSetId);
  }

  /**
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   */
  @Override
  @PostMapping("{id}/loadDatasetSchema")
  public void loadDatasetSchema(@PathVariable("id") final Long datasetId, Long dataFlowId,
      TableCollectionVO tableCollection) {
    if (datasetId == null || dataFlowId == null || tableCollection == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetService.setDataschemaTables(datasetId, dataFlowId, tableCollection);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

  }
  
  
  @Override
  @GetMapping(value = "loadTableFromAnyObject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public TableVO getTableFromAnyObjectId(@PathVariable("id") Long id, 
      @RequestParam(value = "datasetId", required = true) Long idDataset,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "type", required = true) Integer type) {
    
    TableVO table = new TableVO();
    try {
      Pageable pageable = PageRequest.of(1, pageSize);
      table = datasetService.getTableFromAnyObjectId(id, idDataset, pageable, type);
    } catch (EEAException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return table;
  }
  

}
