package org.eea.dataset.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.callable.DeleteTableCallable;
import org.eea.dataset.service.callable.LoadDataCallable;
import org.eea.dataset.service.callable.UpdateRecordHelper;
import org.eea.dataset.service.file.FileTreatmentHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.StatisticsVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

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

  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  @Autowired
  UpdateRecordHelper updateRecordHelper;

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
   */
  @Override
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateDataset(@RequestBody final DataSetVO dataset) {
    if (dataset == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATASET_NOTFOUND);
    }
    try {
      datasetService.updateDataset(dataset.getId(), dataset);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);

    }
  }

  /**
   * Creates the removeDatasetData data set.
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
   * @param idTableSchema the id table schema
   */

  @Override
  @PostMapping("{id}/loadTableData/{idTableSchema}")
  public void loadTableData(@PathVariable("id") final Long datasetId,
      @RequestParam("file") final MultipartFile file,
      @PathVariable("idTableSchema") String idTableSchema) {
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
      callable = new LoadDataCallable(fileTreatmentHelper, datasetId, fileName, is, idTableSchema);
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
  @DeleteMapping(value = "{id}/deleteImportData")
  public void deleteImportData(@PathVariable("id") final Long dataSetId) {
    if (dataSetId == null || dataSetId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    datasetService.deleteImportData(dataSetId);
  }

  /**
   * Load dataset schema.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableCollection the table collection
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


  /**
   * Gets the table from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param pageSize the page size
   * @param type the type
   *
   * @return the table from any object id
   */
  @Override
  @GetMapping(value = "findPositionFromAnyObject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ValidationLinkVO getPositionFromAnyObjectId(@PathVariable("id") Long id,
      @RequestParam(value = "datasetId", required = true) Long idDataset,
      @RequestParam(value = "type", required = true) TypeEntityEnum type) {

    ValidationLinkVO result = null;
    if (id == null || idDataset == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    try {
      result = datasetService.getPositionFromAnyObjectId(id, idDataset, type);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    return result;
  }


  /**
   * Gets the by id.
   *
   * @param datasetId the dataset id
   *
   * @return the dataset
   */
  @Override
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  @Deprecated
  public DataSetVO getById(Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    DataSetVO result = null;
    try {
      result = datasetService.getById(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return result;
  }


  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   *
   * @return the data flow id by id
   */
  @Override
  public Long getDataFlowIdById(Long datasetId) {
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    Long result = null;
    try {
      result = datasetService.getDataFlowIdById(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
    return result;
  }


  /**
   * Gets the statistics by id.
   *
   * @param datasetId the dataset id
   *
   * @return the statistics by id
   */
  @Override
  @GetMapping(value = "loadStatistics/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public StatisticsVO getStatisticsById(@PathVariable("id") Long datasetId) {

    StatisticsVO statistics = null;
    try {
      statistics = datasetService.getStatistics(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }

    return statistics;
  }


  /**
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   */
  @Override
  @PutMapping(value = "/{id}/updateRecord", produces = MediaType.APPLICATION_JSON_VALUE)
  public void updateRecords(@PathVariable("id") final Long datasetId,
      @RequestBody final List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    try {
      updateRecordHelper.executeUpdateProcess(datasetId, records);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }


  /**
   * Delete records.
   *
   * @param datasetId the dataset id
   * @param recordIds the record ids
   */
  @Override
  @RequestMapping(value = "/{id}/record/", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteRecords(@PathVariable("id") final Long datasetId,
      @RequestBody final List<Long> recordIds) {
    if (datasetId == null || recordIds == null || recordIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    try {
      updateRecordHelper.executeDeleteProcess(datasetId, recordIds);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }


  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param records the records
   */
  @Override
  @RequestMapping(value = "/{id}/record", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void insertRecords(final Long datasetId, final List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    try {
      updateRecordHelper.executeCreateProcess(datasetId, records);
      datasetService.updateRecords(datasetId, records);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Delete import table.
   *
   * @param dataSetId the data set id
   * @param idTableSchema the id table schema
   */
  @Override
  @DeleteMapping(value = "{id}/deleteImportTable/{idTableSchema}")
  public void deleteImportTable(@PathVariable("id") final Long dataSetId,
      @PathVariable("idTableSchema") final String idTableSchema) {
    if (dataSetId == null || dataSetId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    final DeleteTableCallable callable =
        new DeleteTableCallable(this.datasetService, idTableSchema, dataSetId);
    try {
      executor.submit(callable);
    } finally {
      executor.shutdown();
    }
  }
}
