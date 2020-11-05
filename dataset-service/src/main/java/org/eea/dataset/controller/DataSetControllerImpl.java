package org.eea.dataset.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.dataset.service.helper.DeleteHelper;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetController;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * The Class DataSetControllerImpl.
 */
@RestController
@RequestMapping("/dataset")
public class DataSetControllerImpl implements DatasetController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(DataSetControllerImpl.class);

  /** The dataset service. */
  @Autowired
  @Qualifier("proxyDatasetService")
  private DatasetService datasetService;

  /** The file treatment helper. */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /** The update record helper. */
  @Autowired
  private UpdateRecordHelper updateRecordHelper;

  /** The delete helper. */
  @Autowired
  private DeleteHelper deleteHelper;


  /** The dataset metabase service. */
  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset schema service. */
  @Autowired
  private DatasetSchemaService datasetSchemaService;


  /**
   * Gets the data tables values.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param pageNum the page num
   * @param pageSize the page size
   * @param fields the fields
   * @param levelError the level error
   * @param idRules the id rules
   * @return the data tables values
   */
  @Override
  @HystrixCommand
  @GetMapping("TableValueDataset/{id}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ') OR (hasRole('DATA_CUSTODIAN'))")
  public TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "levelError", required = false) ErrorTypeEnum[] levelError,
      @RequestParam(value = "idRules", required = false) String[] idRules) {

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
      result = datasetService.getTableValuesById(datasetId, idTableSchema, pageable, fields,
          levelError, idRules);
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
  @PutMapping("/update")
  public void updateDataset(@RequestBody DataSetVO dataset) {
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
   * Load table data.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param idTableSchema the id table schema
   * @param replace the replace
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping("{id}/loadTableData/{idTableSchema}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN')")
  public void loadTableData(@LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @RequestParam("file") MultipartFile file,
      @LockCriteria(name = "tableSchemaId") @PathVariable("idTableSchema") String idTableSchema,
      @RequestParam(value = "replace", required = false) boolean replace) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    // check if dataset is reportable
    if (!datasetService.isDatasetReportable(datasetId)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          String.format(EEAErrorMessage.DATASET_NOT_REPORTABLE, datasetId));
    }

    // filter if the file is empty
    if (file == null || file.isEmpty()) {
      datasetService.releaseLock(LockSignature.LOAD_TABLE.getValue(), datasetId, idTableSchema);
      LOG_ERROR.error(
          "Error importing a file into a table of the datasetId {}. The file is null or empty",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(
            datasetService.getTableReadOnly(datasetId, idTableSchema, EntityTypeEnum.TABLE))) {
      datasetService.releaseLock(LockSignature.LOAD_TABLE.getValue(), datasetId, idTableSchema);
      LOG_ERROR.error(
          "Error importing the file {} into a table of the dataset {}. The table is read only",
          file.getOriginalFilename(), datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(datasetService.getTableFixedNumberOfRecords(datasetId, idTableSchema,
            EntityTypeEnum.TABLE))) {
      datasetService.releaseLock(LockSignature.LOAD_TABLE.getValue(), datasetId, idTableSchema);
      LOG_ERROR.error(
          "Error importing the file {} into a table of the dataset {}. The table has a fixed number of records",
          file.getOriginalFilename(), datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS, idTableSchema));
    }

    // extract the filename
    String fileName = file.getOriginalFilename();

    // extract the file content
    try {
      InputStream is = file.getInputStream();
      // This method will release the lock
      fileTreatmentHelper.executeFileProcess(datasetId, fileName, is, idTableSchema, replace);
    } catch (IOException e) {
      LOG_ERROR.error("Error importing a file into a table of the dataset {}. Message: {}",
          datasetId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Load dataset data.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param replace the replace
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping("{id}/loadDatasetData")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ')")
  public void loadDatasetData(@LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "replace", required = false) boolean replace) {

    // check if dataset is reportable
    if (!datasetService.isDatasetReportable(datasetId)) {
      datasetService.releaseLock(LockSignature.LOAD_DATASET_DATA.getValue(), datasetId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          String.format(EEAErrorMessage.DATASET_NOT_REPORTABLE, datasetId));
    }

    // filter if the file is empty
    if (file == null || file.isEmpty()) {
      LOG_ERROR.error(
          "Error importing a file into a table of the datasetId {}. The file is null or empty",
          datasetId);
      datasetService.releaseLock(LockSignature.LOAD_DATASET_DATA.getValue(), datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
    }

    // extract the filename
    String fileName = file.getOriginalFilename();

    // extract the file content
    try {
      // this method would release the lock
      fileTreatmentHelper.executeExternalIntegrationFileProcess(datasetId, fileName,
          file.getInputStream(), replace);
    } catch (IOException e) {
      LOG_ERROR.error("Error importing a file into dataset {}. Message: {}", datasetId,
          e.getMessage());
      datasetService.releaseLock(LockSignature.LOAD_DATASET_DATA.getValue(), datasetId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Delete import data.
   *
   * @param dataSetId the data set id
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @DeleteMapping("{id}/deleteImportData")
  @PreAuthorize("secondLevelAuthorize(#dataSetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void deleteImportData(
      @LockCriteria(name = "datasetId") @PathVariable("id") Long dataSetId) {
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
   * Gets the position from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param type the type
   * @return the position from any object id
   */
  @Override
  @HystrixCommand
  @GetMapping("findPositionFromAnyObject/{id}")
  public ValidationLinkVO getPositionFromAnyObjectId(@PathVariable("id") String id,
      @RequestParam("datasetId") Long idDataset, @RequestParam("type") EntityTypeEnum type) {

    if (id == null || idDataset == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.DATASET_INCORRECT_ID);
    }

    try {
      return datasetService.getPositionFromAnyObjectId(id, idDataset, type);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the by id.
   *
   * @param datasetId the dataset id
   * @return the by id
   */
  @Deprecated
  @Override
  @HystrixCommand
  @GetMapping("{id}")
  public DataSetVO getById(@PathVariable("id") Long datasetId) {
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
   * @return the data flow id by id
   */
  @Override
  @HystrixCommand
  @GetMapping("{id}/dataflow")
  public Long getDataFlowIdById(Long datasetId) {
    return datasetService.getDataFlowIdById(datasetId);
  }

  /**
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   */
  @Override
  @HystrixCommand
  @PutMapping("/{id}/updateRecord")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void updateRecords(@LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @RequestBody List<RecordVO> records) {
    if (datasetId == null || records == null || records.isEmpty()) {
      LOG_ERROR.error(
          "Error updating records. The datasetId or the records to update are emtpy or null");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(datasetService.getTableReadOnly(datasetId,
            records.get(0).getIdRecordSchema(), EntityTypeEnum.RECORD))) {
      LOG_ERROR.error("Error updating records in the datasetId {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      updateRecordHelper.executeUpdateProcess(datasetId, records);
    } catch (EEAException e) {
      LOG_ERROR.error("Error updating records in the datasetId {}. Message: {}", datasetId,
          e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/{id}/record/{recordId}")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void deleteRecord(@LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @PathVariable("recordId") String recordId) {
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(datasetService.getTableReadOnly(datasetId,
            datasetService.findRecordSchemaIdById(datasetId, recordId), EntityTypeEnum.RECORD))) {
      LOG_ERROR.error("Error deleting record in the datasetId {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(datasetService.getTableFixedNumberOfRecords(datasetId,
            datasetService.findRecordSchemaIdById(datasetId, recordId), EntityTypeEnum.RECORD))) {
      LOG_ERROR.error(
          "Error deleting record in the datasetId {}. The table has a fixed number of records",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS,
              datasetService.findRecordSchemaIdById(datasetId, recordId)));
    }
    try {
      updateRecordHelper.executeDeleteProcess(datasetId, recordId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting record in the datasetId {}. Message: {}", datasetId,
          e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the id table schema
   * @param records the records
   */
  @Override
  @HystrixCommand
  @PostMapping("/{datasetId}/table/{tableSchemaId}/record")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void insertRecords(
      @LockCriteria(name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId, @RequestBody List<RecordVO> records) {
    try {
      updateRecordHelper.executeCreateProcess(datasetId, records, tableSchemaId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error inserting records: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }


  /**
   * Delete import table.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @DeleteMapping("{datasetId}/deleteImportTable/{tableSchemaId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void deleteImportTable(
      @LockCriteria(name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @LockCriteria(name = "tableSchemaId") @PathVariable("tableSchemaId") String tableSchemaId) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());

    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(
            datasetService.getTableReadOnly(datasetId, tableSchemaId, EntityTypeEnum.TABLE))) {
      datasetService.releaseLock(tableSchemaId, LockSignature.DELETE_IMPORT_TABLE.getValue(),
          datasetId);
      LOG_ERROR.error(
          "Error deleting the table values from the datasetId {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(datasetService.getTableFixedNumberOfRecords(datasetId, tableSchemaId,
            EntityTypeEnum.TABLE))) {
      datasetService.releaseLock(tableSchemaId, LockSignature.DELETE_IMPORT_TABLE.getValue(),
          datasetId);
      LOG_ERROR.error(
          "Error deleting the table values from the datasetId {}. The table has a fixed number of records",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS, tableSchemaId));
    }

    LOG.info("Executing delete table value with id {} from dataset {}", tableSchemaId, datasetId);
    try {
      // This method will release the lock
      deleteHelper.executeDeleteTableProcess(datasetId, tableSchemaId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting the table values from the datasetId {}. Message: {}",
          datasetId, e.getMessage());
      datasetService.releaseLock(tableSchemaId, LockSignature.DELETE_IMPORT_TABLE.getValue(),
          datasetId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param mimeType the mime type
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/exportFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public ResponseEntity<byte[]> exportFile(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam("mimeType") String mimeType) {

    String tableName =
        null != tableSchemaId ? datasetSchemaService.getTableSchemaName(null, tableSchemaId)
            : datasetMetabaseService.findDatasetMetabase(datasetId).getDataSetName();
    if (null == tableName) {
      LOG_ERROR.error("tableSchemaId not found: {}", tableSchemaId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }

    try {
      byte[] file = datasetService.exportFile(datasetId, mimeType, tableSchemaId);
      String fileName = tableName + "." + mimeType;
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Export file through integration.
   *
   * @param datasetId the dataset id
   * @param fileExtension the file extension
   */
  @Override
  @HystrixCommand
  @GetMapping("/exportFileThroughIntegration")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void exportFileThroughIntegration(@RequestParam("datasetId") Long datasetId,
      @RequestParam("fileExtension") String fileExtension) {
    try {
      datasetService.exportFileThroughIntegration(datasetId, fileExtension);
    } catch (EEAException e) {
      LOG_ERROR.error("Error exporting file through integration: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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
  @PostMapping("/{id}/insertIdSchema")
  public void insertIdDataSchema(@PathVariable("id") Long datasetId,
      @RequestParam("idDatasetSchema") String idDatasetSchema) {
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
  @PutMapping("/{id}/updateField")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void updateField(@LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @RequestBody FieldVO field) {
    if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
        && Boolean.TRUE.equals(datasetService.getTableReadOnly(datasetId, field.getIdFieldSchema(),
            EntityTypeEnum.FIELD))) {
      LOG_ERROR.error("Error updating a field in the dataset {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      updateRecordHelper.executeFieldUpdateProcess(datasetId, field);
    } catch (EEAException e) {
      LOG_ERROR.error("Error updating a field in the dataset {}. Message: {}", datasetId,
          e.getMessage());
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
  public List<FieldVO> getFieldValuesReferenced(@PathVariable("id") Long datasetIdOrigin,
      @RequestParam("idFieldSchema") String idFieldSchema,
      @RequestParam("searchValue") String searchValue) {
    return datasetService.getFieldValuesReferenced(datasetIdOrigin, idFieldSchema, searchValue);
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the ETL dataset VO
   */
  @Override
  @GetMapping("/{datasetId}/etlExport")
  @PreAuthorize("checkApiKey(#dataflowId,#providerId) AND secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN')")
  public ETLDatasetVO etlExportDataset(@PathVariable("datasetId") Long datasetId,
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG_ERROR.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }

    try {
      return datasetService.etlExportDataset(datasetId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @Override
  @PostMapping("/{datasetId}/etlImport")
  @LockMethod
  @PreAuthorize("checkApiKey(#dataflowId,#providerId) AND secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN')")
  public void etlImportDataset(
      @LockCriteria(name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @RequestBody ETLDatasetVO etlDatasetVO, @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG_ERROR.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    // check if dataset is reportable
    if (!datasetService.isDatasetReportable(datasetId)) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          String.format(EEAErrorMessage.DATASET_NOT_REPORTABLE, datasetId));
    }

    try {
      datasetService.etlImportDataset(datasetId, etlDatasetVO, providerId);
    } catch (EEAException e) {
      LOG_ERROR.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @return the attachment
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}/field/{fieldId}/attachment",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<byte[]> getAttachment(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String idField) {

    LOG.info("Downloading attachment from the datasetId {}", datasetId);
    try {
      AttachmentValue attachment = datasetService.getAttachment(datasetId, idField);
      byte[] file = attachment.getContent();
      String filename = attachment.getFileName();
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error downloading attachment from the datasetId {}, with message: {}",
          datasetId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
  }

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @param file the file
   */
  @Override
  @HystrixCommand
  @PutMapping("/{datasetId}/field/{fieldId}/attachment")
  public void updateAttachment(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String idField, @RequestParam("file") MultipartFile file) {

    try {
      // Not allow insert attachment if the table is marked as read only. This not applies to design
      // datasets
      if (!DatasetTypeEnum.DESIGN.equals(datasetMetabaseService.getDatasetType(datasetId))
          && Boolean.TRUE.equals(datasetService.getTableReadOnly(datasetId,
              datasetService.findFieldSchemaIdById(datasetId, idField), EntityTypeEnum.FIELD))) {
        LOG_ERROR.error("Error updating an attachment in the datasetId {}. The table is read only",
            datasetId);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
      }
      String fileName = file.getOriginalFilename();
      if (!validateAttachment(datasetId, idField, fileName, file.getSize())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_FORMAT);
      }
      InputStream is = file.getInputStream();
      datasetService.updateAttachment(datasetId, idField, fileName, is);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Error updating attachment from the datasetId {}, with message: {}",
          datasetId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/{datasetId}/field/{fieldId}/attachment")
  public void deleteAttachment(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String idField) {
    try {
      datasetService.deleteAttachment(datasetId, idField);
    } catch (EEAException e) {
      LOG_ERROR.error("Error deleting attachment from the datasetId {}, with message: {}",
          datasetId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }
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
      @RequestParam("idFieldSchema") String idFieldSchema) {
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
    return datasetMetabaseService.getDatasetType(datasetId);
  }



  /**
   * Delete data before replacing.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   * @param operation the operation
   */
  @Override
  @DeleteMapping("/private/{id}/deleteForReplacing")
  public void deleteDataBeforeReplacing(@PathVariable("id") Long datasetId,
      @RequestParam("integrationId") Long integrationId,
      @RequestParam("operation") IntegrationOperationTypeEnum operation) {
    // When deleting the data finishes, we send a kafka event to make the FME call to import data
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    deleteHelper.executeDeleteImportDataAsyncBeforeReplacing(datasetId, integrationId, operation);
  }


  /**
   * Validate attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @param originalFilename the original filename
   * @param size the size
   * @return true, if successful
   * @throws EEAException the EEA exception
   */
  private boolean validateAttachment(Long datasetId, String idField, String originalFilename,
      Long size) throws EEAException {

    Boolean result = true;
    String datasetSchemaId = datasetSchemaService.getDatasetSchemaId(datasetId);
    if (datasetSchemaId == null) {
      throw new EEAException(EEAErrorMessage.DATASET_SCHEMA_ID_NOT_FOUND);
    }
    FieldVO fieldVO = datasetService.getFieldById(datasetId, idField);
    FieldSchemaVO fieldSchema =
        datasetSchemaService.getFieldSchema(datasetSchemaId, fieldVO.getIdFieldSchema());
    if (fieldSchema == null || fieldSchema.getId() == null) {
      throw new EEAException(EEAErrorMessage.FIELD_SCHEMA_ID_NOT_FOUND);
    }
    // Validate property maxSize of the file. If the size is 0, it's ok, continue
    if (fieldSchema.getMaxSize() != null && fieldSchema.getMaxSize() != 0
        && fieldSchema.getMaxSize() * 1000000 < size) {
      result = false;
    }
    // Validate property extensions of the file. If no extensions provided, it's ok, continue
    if (fieldSchema.getValidExtensions() != null) {
      List<String> extensions = Arrays.asList(fieldSchema.getValidExtensions());
      if (!extensions.isEmpty()
          && !extensions.contains(datasetService.getMimetype(originalFilename))) {
        result = false;
      }
    }
    return result;
  }


}
