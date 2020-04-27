package org.eea.dataset.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.ws.rs.Produces;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.eea.thread.ThreadPropertiesManager;
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
import org.springframework.security.core.context.SecurityContextHolder;
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

/** The type Data set controller. */
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
  private UpdateRecordHelper updateRecordHelper;

  /**
   * The delete helper.
   */
  @Autowired
  private DeleteHelper deleteHelper;

  /**
   * The design dataset service.
   */
  @Autowired
  private DesignDatasetService designDatasetService;

  /**
   * Gets the data tables values.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param levelError the level error
   *
   * @return the data tables values
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "TableValueDataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATACOLLECTION_CUSTODIAN') OR (hasRole('DATA_CUSTODIAN'))")
  public TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "levelError", required = false) ErrorTypeEnum[] levelError) {

    if (null == datasetId || null == idTableSchema) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    // check if the parameters received from the frontend are the needed to get the table values
    // WITHOUT PAGINATION
    Pageable pageable = null;
    if (pageSize != null) {
      pageable = PageRequest.of(pageNum, pageSize);
    }
    // else pageable will be null, it will be created inside the service
    TableVO result = null;
    try {
      result =
          datasetService.getTableValuesById(datasetId, idTableSchema, pageable, fields, levelError);
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
  @HystrixCommand
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
   * Load dataset data.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param idTableSchema the id table schema
   */
  @LockMethod(removeWhenFinish = false)
  @Override
  @HystrixCommand
  @PostMapping("{id}/loadTableData/{idTableSchema}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  public void loadTableData(
      @LockCriteria(name = "datasetId") @PathVariable("id") final Long datasetId,
      @RequestParam("file") final MultipartFile file, @LockCriteria(
          name = "idTableSchema") @PathVariable(value = "idTableSchema") String idTableSchema) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // filter if the file is empty
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (datasetId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetService.getDatasetType(datasetId))
        && datasetService.getTableReadOnly(datasetId, idTableSchema, EntityTypeEnum.TABLE)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    // extract the filename
    String fileName = file.getOriginalFilename();
    // extract the file content
    try {
      InputStream is = file.getInputStream();
      // This method will release the lock
      fileTreatmentHelper.executeFileProcess(datasetId, fileName, is, idTableSchema);
    } catch (IOException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Delete import data.
   *
   * @param dataSetId the data set id
   */
  @Override
  @LockMethod(removeWhenFinish = false)
  @HystrixCommand
  @DeleteMapping(value = "{id}/deleteImportData")
  @PreAuthorize("secondLevelAuthorize(#dataSetId,'DATASET_PROVIDER') AND checkPermission('Dataset','MANAGE_DATA')")
  public void deleteImportData(
      @LockCriteria(name = "id") @PathVariable("id") final Long dataSetId) {
    if (dataSetId == null || dataSetId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      // This method will release the lock
      deleteHelper.executeDeleteDatasetProcess(dataSetId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
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
  @HystrixCommand
  @GetMapping(value = "findPositionFromAnyObject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ValidationLinkVO getPositionFromAnyObjectId(@PathVariable("id") String id,
      @RequestParam(value = "datasetId", required = true) Long idDataset,
      @RequestParam(value = "type", required = true) EntityTypeEnum type) {

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
  @HystrixCommand
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
  @HystrixCommand
  @RequestMapping(value = "{id}/dataflow", method = RequestMethod.GET)
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
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/{id}/updateRecord", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  public void updateRecords(@PathVariable("id") final Long datasetId,
      @RequestBody final List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetService.getDatasetType(datasetId)) && datasetService
        .getTableReadOnly(datasetId, records.get(0).getIdRecordSchema(), EntityTypeEnum.RECORD)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
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
   * @param recordId the record id
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/{id}/record/{recordId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  public void deleteRecord(@PathVariable("id") final Long datasetId,
      @PathVariable("recordId") final String recordId) {
    if (datasetId == null || recordId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetService.getDatasetType(datasetId))
        && datasetService.getTableReadOnly(datasetId, recordId, EntityTypeEnum.RECORD)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      updateRecordHelper.executeDeleteProcess(datasetId, recordId);
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
  @HystrixCommand
  @RequestMapping(value = "/{id}/table/{idTableSchema}/record", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  public void insertRecords(@PathVariable("id") final Long datasetId,
      @PathVariable("idTableSchema") final String idTableSchema,
      @RequestBody final List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    // Not allow insert if the table is marked as read only. This not applies to design datasets
    if (!DatasetTypeEnum.DESIGN.equals(datasetService.getDatasetType(datasetId))
        && datasetService.getTableReadOnly(datasetId, idTableSchema, EntityTypeEnum.TABLE)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
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
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @LockMethod(removeWhenFinish = false)
  @Override
  @HystrixCommand
  @DeleteMapping(value = "{datasetId}/deleteImportTable/{tableSchemaId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  public void deleteImportTable(
      @LockCriteria(name = "datasetId") @PathVariable("datasetId") final Long datasetId,
      @LockCriteria(
          name = "tableSchemaId") @PathVariable("tableSchemaId") final String tableSchemaId) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (datasetId == null || datasetId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    } else if (tableSchemaId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    } else if (!DatasetTypeEnum.DESIGN.equals(datasetService.getDatasetType(datasetId))
        && datasetService.getTableReadOnly(datasetId, tableSchemaId, EntityTypeEnum.TABLE)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }

    LOG.info("Executing delete table value with id {} from dataset {}", tableSchemaId, datasetId);
    try {
      // This method will release the lock
      deleteHelper.executeDeleteTableProcess(datasetId, tableSchemaId);
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
   */
  @Override
  @HystrixCommand
  @GetMapping("/exportFile")
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN')")
  public ResponseEntity exportFile(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "idTableSchema", required = false) String idTableSchema,
      @RequestParam("mimeType") String mimeType) {
    LOG.info("Init the export controller");
    byte[] file;
    try {
      file = datasetService.exportFile(datasetId, mimeType, idTableSchema);

      // set file name and content type
      // Depending on the dataset type (REPORTING/DESIGN) one method or another is used to get the
      // fileName
      String filename = "";
      if (datasetService.isReportingDataset(datasetId)) {
        filename = datasetService.getFileName(mimeType, idTableSchema, datasetId);
      } else {
        filename = designDatasetService.getFileNameDesign(mimeType, idTableSchema, datasetId);
      }

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

      return new ResponseEntity(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Insert id data schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   */
  @Override
  @HystrixCommand
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
  @HystrixCommand
  @PutMapping(value = "/{id}/updateField", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_PROVIDER','DATASCHEMA_CUSTODIAN')")
  public void updateField(@PathVariable("id") final Long datasetId,
      @RequestBody final FieldVO field) {
    if (datasetId == null || field == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FIELD_NOT_FOUND);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetService.getDatasetType(datasetId)) && datasetService
        .getTableReadOnly(datasetId, field.getIdFieldSchema(), EntityTypeEnum.FIELD)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      updateRecordHelper.executeFieldUpdateProcess(datasetId, field);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Gets the field values referenced.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idFieldSchema the id field schema
   * @param searchValue the search value
   * @return the field values referenced
   */
  @Override
  @GetMapping("/{id}/getFieldsValuesReferenced")
  @Produces(value = {MediaType.APPLICATION_JSON_VALUE})
  public List<FieldVO> getFieldValuesReferenced(@PathVariable("id") Long datasetIdOrigin,
      @RequestParam("idFieldSchema") String idFieldSchema,
      @RequestParam("searchValue") String searchValue) {
    return datasetService.getFieldValuesReferenced(datasetIdOrigin, idFieldSchema, searchValue);
  }

  /**
   * Gets the referenced dataset id.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idFieldSchema the id field schema
   * @return the referenced dataset id
   */
  @Override
  @GetMapping("/private/getReferencedDatasetId")
  public Long getReferencedDatasetId(@RequestParam("id") Long datasetIdOrigin,
      @RequestParam(value = "idFieldSchema") String idFieldSchema) {
    return datasetService.getReferencedDatasetId(datasetIdOrigin, idFieldSchema);
  }

  /**
   * Gets the dataset type.
   *
   * @param datasetId the dataset id
   * @return the dataset type
   */
  @Override
  @GetMapping("/private/datasetType/{datasetId}")
  public DatasetTypeEnum getDatasetType(@PathVariable("datasetId") Long datasetId) {
    return datasetService.getDatasetType(datasetId);
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @return the ETL dataset VO
   */
  @Override
  @GetMapping("/etlExport/dataset/{datasetId}")
  public ETLDatasetVO etlExportDataset(@PathVariable("datasetId") Long datasetId) {
    try {
      return datasetService.etlExportDataset(datasetId);
    } catch (EEAException e) {
      return null;
    }
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @return the ETL dataset VO
   */
  @Override
  @PostMapping("/etlImport/dataset/{datasetId}")
  public void etlImportDataset(@PathVariable("datasetId") Long datasetId,
      @RequestBody ETLDatasetVO etlDatasetVO) {
    try {
      datasetService.etlImportDataset(datasetId, etlDatasetVO);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}
