package org.eea.dataset.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.ws.rs.Produces;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DataSetControllerImpl.class);

  /**
   * The dataset service.
   */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /**
   * The file treatment helper.
   */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /**
   * The load validations helper.
   */
  @Autowired
  UpdateRecordHelper updateRecordHelper;

  /**
   * The delete helper.
   */
  @Autowired
  DeleteHelper deleteHelper;


  /**
   * Gets the data tables values.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   *
   * @return the data tables values
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "TableValueDataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER') AND checkPermission('Dataset','READ') OR (secondLevelAuthorize(#datasetId,'DATASET_REQUESTOR'))")
  public TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields) {

    if (null == datasetId || null == idTableSchema) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    // check if the parameters received from the frontend are the needed to get the table values
    // WITHOUT PAGINATION
    Pageable pageable = null;
    if (pageSize == null && pageNum == null) {
      pageable = PageRequest.of(0, 9999);
    } else {
      pageable = PageRequest.of(pageNum, pageSize);
    }


    TableVO result = null;
    try {
      result = datasetService.getTableValuesById(datasetId, idTableSchema, pageable, fields);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
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
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);

    }
  }


  /**
   * Creates the empty data set.
   *
   * @param datasetname the datasetname
   * @param idDatasetSchema the id dataset schema
   * @param idDataflow the id dataflow
   */
  @Override
  @PostMapping(value = "/create")
  public void createEmptyDataSet(
      @RequestParam(value = "datasetName", required = true) final String datasetname,
      @RequestParam(value = "idDatasetSchema", required = false) String idDatasetSchema,
      @RequestParam(value = "idDataflow", required = false) Long idDataflow) {
    if (StringUtils.isBlank(datasetname)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    try {
      datasetService.createEmptyDataset(datasetname, idDatasetSchema, idDataflow);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
    }
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
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
  public void loadTableData(@PathVariable("id") final Long datasetId,
      @RequestParam("file") final MultipartFile file,
      @PathVariable(value = "idTableSchema") String idTableSchema) {
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
    // extract the file content
    try {
      InputStream is = file.getInputStream();
      fileTreatmentHelper.executeFileProcess(datasetId, fileName, is, idTableSchema);
      // NOPMD this cannot be avoid since Callable throws Exception in
    } catch (IOException | EEAException | InterruptedException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }

  /**
   * Call services delete.
   *
   * @param dataSetId id import
   */
  @Override
  @DeleteMapping(value = "{id}/deleteImportData")
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
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
   * @return the by id
   *
   * @deprecated this method is deprecated
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
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
  public void updateRecords(@PathVariable("id") final Long datasetId,
      @RequestBody final List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    try {
      updateRecordHelper.executeUpdateProcess(datasetId, records);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
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
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
  public void deleteRecords(@PathVariable("id") final Long datasetId,
      @RequestParam final List<Long> recordIds) {
    if (datasetId == null || recordIds == null || recordIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    try {
      updateRecordHelper.executeDeleteProcess(datasetId, recordIds);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }


  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param records the records
   */
  @Override
  @RequestMapping(value = "/{id}/table/{idTableSchema}/record", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
  public void insertRecords(@PathVariable("id") final Long datasetId,
      @PathVariable("idTableSchema") final String idTableSchema,
      @RequestBody final List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    try {
      updateRecordHelper.executeCreateProcess(datasetId, records, idTableSchema);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
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
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
  public void deleteImportTable(@PathVariable("id") final Long dataSetId,
      @PathVariable("idTableSchema") final String idTableSchema) {
    if (dataSetId == null || dataSetId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    } else if (idTableSchema == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }
    LOG.info("Executing delete table value with id {} from dataset {}", idTableSchema, dataSetId);
    try {
      deleteHelper.executeDeleteProcess(dataSetId, idTableSchema);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }


  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param mimeType the mime type
   *
   * @return the response entity
   *
   * @throws Exception the exception
   */
  @Override
  @GetMapping("/exportFile")
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER') AND checkPermission('Dataset','READ') OR (secondLevelAuthorize(#datasetId,'DATASET_REQUESTOR'))")
  public ResponseEntity exportFile(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "idTableSchema", required = false) String idTableSchema,
      @RequestParam("mimeType") String mimeType) throws Exception {
    LOG.info("Init the export controller");
    byte[] file = datasetService.exportFile(datasetId, mimeType, idTableSchema);

    // set file name and content type
    String filename = datasetService.getFileName(mimeType, idTableSchema, datasetId);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
    return new ResponseEntity(file, httpHeaders, HttpStatus.OK);
  }


  /**
   * Insert id data schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  @PostMapping(value = "/{id}/insertIdSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  public void insertIdDataSchema(@PathVariable("id") Long datasetId,
      @RequestParam(value = "idDatasetSchema", required = true) String idDatasetSchema) {

    try {
      datasetService.insertSchema(datasetId, idDatasetSchema);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Update field.
   *
   * @param datasetId the dataset id
   * @param field the field
   */
  @Override
  @PutMapping(value = "/{id}/updateField", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#id,'DATASET_PROVIDER') AND checkPermission('Dataset','UPDATE')")
  public void updateField(@PathVariable("id") final Long datasetId,
      @RequestBody final FieldVO field) {
    if (datasetId == null || field == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FIELD_NOT_FOUND);
    }
    try {
      updateRecordHelper.executeFieldUpdateProcess(datasetId, field);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

}
