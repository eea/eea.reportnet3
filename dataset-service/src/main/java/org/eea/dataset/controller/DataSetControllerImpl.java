package org.eea.dataset.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.eea.lock.service.LockService;
import org.eea.thread.ThreadPropertiesManager;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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

  /** The file treatment helper. */
  @Autowired
  private FileTreatmentHelper fileTreatmentHelper;

  /** The lock service. */
  @Autowired
  private LockService lockService;

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
   * @param fieldSchemaId the id field schema
   * @param fieldValue the field value
   * @return the data tables values
   */
  @Override
  @HystrixCommand
  @GetMapping("TableValueDataset/{id}")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Get the data in tables", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
      @ApiResponse(code = 400, message = "Dataset id incorrect"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 500, message = "Error getting the data")})
  public TableVO getDataTablesValues(
      @ApiParam(type = "Long", value = "dataset Id",
          example = "0") @PathVariable("id") Long datasetId,
      @ApiParam(type = "String", value = "table schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("idTableSchema") String idTableSchema,
      @ApiParam(type = "Integer", value = "page number", example = "0") @RequestParam(
          value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @ApiParam(type = "Integer", value = "page size",
          example = "0") @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @ApiParam(type = "String", value = "name of fields",
          example = "field1") @RequestParam(value = "fields", required = false) String fields,
      @ApiParam(value = "Type Error", example = "INFO,WARNING") @RequestParam(value = "levelError",
          required = false) ErrorTypeEnum[] levelError,
      @ApiParam(value = "list of id Rules", example = "a,b,c") @RequestParam(value = "idRules",
          required = false) String[] idRules,
      @ApiParam(type = "String", value = "field Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "fieldSchemaId",
              required = false) String fieldSchemaId,
      @ApiParam(type = "String", value = "value", example = "value") @RequestParam(
          value = "fieldValue", required = false) String fieldValue) {
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
          levelError, idRules, fieldSchemaId, fieldValue);
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
  @PreAuthorize("secondLevelAuthorize(#dataset.id,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @PutMapping("/update")
  @ApiOperation(value = "Update dataset", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated dataset"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 400, message = "Dataset not informed"),
      @ApiResponse(code = 500, message = "Error updating  dataset")})
  public void updateDataset(@ApiParam(value = "dataset object") @RequestBody DataSetVO dataset) {
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
   * Import file data.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param file the file
   * @param replace the replace
   * @param integrationId the integration id
   * @param delimiter the delimiter
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @PostMapping("/{datasetId}/importFileData")
  @ApiOperation(value = "Import file whith data")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported file"),
      @ApiResponse(code = 400, message = "Error importing file"),
      @ApiResponse(code = 500, message = "Error importing file")})
  public void importFileData(
      @ApiParam(type = "Long", value = "dataset Id", example = "0") @LockCriteria(
          name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
      @ApiParam(type = "String", value = "table Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
              required = false) String tableSchemaId,
      @ApiParam(value = "the file") @RequestParam("file") MultipartFile file,
      @ApiParam(type = "boolean", value = "replace data",
          example = "true") @RequestParam(value = "replace", required = false) boolean replace,
      @ApiParam(type = "Long", value = "integration Id", example = "0") @RequestParam(
          value = "integrationId", required = false) Long integrationId,
      @ApiParam(type = "String", value = "delimiter of file",
          example = ",") @RequestParam(value = "delimiter", required = false) String delimiter) {
    try {
      fileTreatmentHelper.importFileData(datasetId, tableSchemaId, file, replace, integrationId,
          delimiter);
    } catch (EEAException e) {
      LOG_ERROR.error(
          "File import failed: datasetId={}, tableSchemaId={}, fileName={}. Message: {}", datasetId,
          tableSchemaId, file.getOriginalFilename(), e.getMessage(), e);
      Map<String, Object> importFileData = new HashMap<>();
      importFileData.put(LiteralConstants.SIGNATURE, LockSignature.IMPORT_FILE_DATA.getValue());
      importFileData.put(LiteralConstants.DATASETID, datasetId);
      lockService.removeLockByCriteria(importFileData);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error importing file", e);
    }
  }

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   * @return the data flow id by id
   */
  @Override
  @HystrixCommand
  @GetMapping("/private/{id}/dataflow")
  @ApiOperation(value = "get Dataflow id by id", hidden = true)
  public Long getDataFlowIdById(
      @ApiParam(type = "Long", value = "Dataset Id", example = "0") Long datasetId) {
    return datasetService.getDataFlowIdById(datasetId);
  }

  /**
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   */
  @Override
  @HystrixCommand
  @PutMapping("/{id}/updateRecord")
  @LockMethod
  @ApiOperation(value = "update Records", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_STEWARD','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated record"),
      @ApiResponse(code = 404, message = "Record not found in dataset"),
      @ApiResponse(code = 400, message = "record informed not found or table is read only")})
  public void updateRecords(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @ApiParam(value = "list of records") @RequestBody List<RecordVO> records,
      @ApiParam(type = "boolean", value = "update cascade", example = "true") @RequestParam(
          value = "updateCascadePK", required = false) boolean updateCascadePK) {
    if (datasetId == null || records == null || records.isEmpty()) {
      LOG_ERROR.error(
          "Error updating records. The datasetId or the records to update are emtpy or null");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.RECORD_NOTFOUND);
    }
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, records.get(0).getIdRecordSchema(),
        EntityTypeEnum.RECORD)) {
      LOG_ERROR.error("Error updating records in the datasetId {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      updateRecordHelper.executeUpdateProcess(datasetId, records, updateCascadePK);
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
   * @param deleteCascadePK the delete cascade PK
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/{id}/record/{recordId}")
  @LockMethod
  @ApiOperation(value = "delete record", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
      @ApiResponse(code = 404, message = "record or dataset not found"), @ApiResponse(code = 400,
          message = "error because table is read only or fixed number of records is active")})
  public void deleteRecord(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @ApiParam(type = "String", value = "record Id",
          example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("recordId") String recordId,
      @ApiParam(type = "boolean", value = "delete cascade", example = "true") @RequestParam(
          value = "deleteCascadePK", required = false) boolean deleteCascadePK) {
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
        datasetService.findRecordSchemaIdById(datasetId, recordId), EntityTypeEnum.RECORD)) {
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
      updateRecordHelper.executeDeleteProcess(datasetId, recordId, deleteCascadePK);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Insert records in table", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully inserted"), @ApiResponse(
      code = 400,
      message = "error because table is read only or fixed number of records is active or inserting")})
  public void insertRecords(
      @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
          name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "table schema Id",
          example = "5cf0e9b3b793310e9ceca190") @PathVariable("tableSchemaId") String tableSchemaId,
      @ApiParam(value = "list of records") @RequestBody List<RecordVO> records) {
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, records.get(0).getIdRecordSchema(),
        EntityTypeEnum.RECORD)) {
      LOG_ERROR.error("Error inserting record in the datasetId {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    DatasetTypeEnum datasetType = datasetMetabaseService.getDatasetType(datasetId);
    if ((!DatasetTypeEnum.DESIGN.equals(datasetType)
        && !DatasetTypeEnum.REFERENCE.equals(datasetType))
        && Boolean.TRUE.equals(datasetService.getTableFixedNumberOfRecords(datasetId,
            records.get(0).getIdRecordSchema(), EntityTypeEnum.RECORD))) {
      LOG_ERROR.error(
          "Error inserting record in the datasetId {}. The table has a fixed number of records",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String
          .format(EEAErrorMessage.FIXED_NUMBER_OF_RECORDS, records.get(0).getIdRecordSchema()));
    }
    try {
      updateRecordHelper.executeCreateProcess(datasetId, records, tableSchemaId);
    } catch (EEAException e) {
      LOG_ERROR.error("Error inserting records: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Insert records multi table.
   *
   * @param datasetId the dataset id
   * @param tableRecords the table records
   */
  @Override
  @HystrixCommand
  @PostMapping("/{datasetId}/insertRecordsMultiTable")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Insert records in different tables", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully inserted"),
      @ApiResponse(code = 400, message = "error inserting records")})
  public void insertRecordsMultiTable(
      @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
          name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @ApiParam(value = "table Records") @RequestBody List<TableVO> tableRecords) {
    try {
      updateRecordHelper.executeMultiCreateProcess(datasetId, tableRecords);
    } catch (EEAException e) {
      LOG_ERROR.error("Error inserting records: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /**
   * Delete import data.
   *
   * @param datasetId the data set id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param deletePrefilledTables the delete prefilled tables
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @DeleteMapping("/{datasetId}/deleteImportData")
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_STEWARD', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId, #datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_STEWARD', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Delete Data")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
      @ApiResponse(code = 403, message = "dataset not belong dataflow"),
      @ApiResponse(code = 401, message = "Unauthorize"),
      @ApiResponse(code = 500, message = "error deleting data")})
  public void deleteImportData(
      @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
          name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
      @ApiParam(type = "boolean", value = "delete prefilled tables",
          example = "true") @RequestParam(value = "deletePrefilledTables", defaultValue = "false",
              required = false) Boolean deletePrefilledTables) {

    // Rest API only: Check if the dataflow belongs to the dataset
    if (null != dataflowId && !dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG_ERROR.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    deleteHelper.executeDeleteDatasetProcess(datasetId, deletePrefilledTables);
  }

  /**
   * Delete import table.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @Override
  @HystrixCommand
  @LockMethod(removeWhenFinish = false)
  @DeleteMapping("/{datasetId}/deleteImportTable/{tableSchemaId}")
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_STEWARD', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId, #datasetId, 'DATASCHEMA_CUSTODIAN', 'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASET_STEWARD', 'DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE', 'EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Delete table data")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
      @ApiResponse(code = 403, message = "dataset not belong dataflow"),
      @ApiResponse(code = 401, message = "Unauthorize"),
      @ApiResponse(code = 500, message = "error deleting data")})
  public void deleteImportTable(
      @ApiParam(type = "Long", value = "Dataset Id", example = "0") @LockCriteria(
          name = "datasetId") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "table schema Id",
          example = "5cf0e9b3b793310e9ceca190") @LockCriteria(
              name = "tableSchemaId") @PathVariable("tableSchemaId") String tableSchemaId,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {

    // Rest API only: Check if the dataflow belongs to the dataset
    if (null != dataflowId && !dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG_ERROR.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    // This method will release the lock
    deleteHelper.executeDeleteTableProcess(datasetId, tableSchemaId);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','DATASET_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Export file with data", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully export"),
      @ApiResponse(code = 400, message = "Id table schema is incorrect"),
      @ApiResponse(code = 500, message = "Error exporting file")})
  public ResponseEntity<byte[]> exportFile(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "table schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
              required = false) String tableSchemaId,
      @ApiParam(type = "String", value = "mimeType (file extension)",
          example = "csv") @RequestParam("mimeType") String mimeType) {

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
   * @param integrationId the integration id
   */
  @Override
  @HystrixCommand
  @GetMapping("/exportFileThroughIntegration")
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','DATASET_OBSERVER','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_OBSERVER')")
  @ApiOperation(value = "export file through integration", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
      @ApiResponse(code = 500, message = "Error exporting file")})
  public void exportFileThroughIntegration(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(type = "Long", value = "Integration Id",
          example = "0") @RequestParam("integrationId") Long integrationId) {
    try {
      datasetService.exportFileThroughIntegration(datasetId, integrationId);
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
  @PostMapping("/private/{id}/insertIdSchema")
  @ApiOperation(value = "insert dataschema id", hidden = true)
  public void insertIdDataSchema(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("id") Long datasetId,
      @ApiParam(type = "String", value = "dataset schema Id",
          example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @RequestParam("idDatasetSchema") String idDatasetSchema) {
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
   * @param updateCascadePK the update cascade PK
   */
  @Override
  @HystrixCommand
  @PutMapping("/{id}/updateField")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "Update field", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully updated field"),
      @ApiResponse(code = 404, message = "Error updating field, field not found"),
      @ApiResponse(code = 400, message = "Error updating field, table is read only")})
  public void updateField(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @LockCriteria(name = "datasetId") @PathVariable("id") Long datasetId,
      @ApiParam(value = "Field Object") @RequestBody FieldVO field,
      @ApiParam(type = "boolean", value = "update cascade", example = "true") @RequestParam(
          value = "updateCascadePK", required = false) boolean updateCascadePK) {
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId, field.getIdFieldSchema(),
        EntityTypeEnum.FIELD)) {
      LOG_ERROR.error("Error updating a field in the dataset {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
    try {
      updateRecordHelper.executeFieldUpdateProcess(datasetId, field, updateCascadePK);
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
   * @param datasetSchemaId the dataset schema id
   * @param fieldSchemaId the field schema id
   * @param conditionalValue the conditional value
   * @param searchValue the search value
   * @param resultsNumber the results number
   * @return the field values referenced
   */
  @Override
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "get Field Values Referenced", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully get data"),
      @ApiResponse(code = 500, message = "Error getting data")})
  @GetMapping("/{id}/datasetSchemaId/{datasetSchemaId}/fieldSchemaId/{fieldSchemaId}/getFieldsValuesReferenced")
  public List<FieldVO> getFieldValuesReferenced(@PathVariable("id") Long datasetIdOrigin,
      @PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("fieldSchemaId") String fieldSchemaId,
      @RequestParam(value = "conditionalValue", required = false) String conditionalValue,
      @RequestParam(value = "searchValue", required = false) String searchValue,
      @RequestParam(value = "resultsNumber", required = false) Integer resultsNumber) {
    try {
      return datasetService.getFieldValuesReferenced(datasetIdOrigin, datasetSchemaId,
          fieldSchemaId, conditionalValue, searchValue, resultsNumber);
    } catch (EEAException e) {
      LOG_ERROR.error("Error with dataset id {}  caused {}", datasetIdOrigin, e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Etl export dataset.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @return the ETL dataset VO
   */
  @Override
  @GetMapping("/{datasetId}/etlExport")
  @HystrixCommand(commandProperties = {@HystrixProperty(
      name = "execution.isolation.thread.timeoutInMilliseconds", value = "7200000")})
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','DATACOLLECTION_CUSTODIAN','DATASET_CUSTODIAN','DATASET_NATIONAL_COORDINATOR','REFERENCEDATASET_CUSTODIAN','TESTDATASET_STEWARD','TESTDATASET_CUSTODIAN','DATASET_OBSERVER','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_OBSERVER')")
  @ApiOperation(value = "ETL Export data")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported"),
      @ApiResponse(code = 500, message = "Error exporting data"),
      @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public ResponseEntity<StreamingResponseBody> etlExportDataset(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Provider Id",
          example = "0") @RequestParam(value = "providerId", required = false) Long providerId,
      @ApiParam(type = "String", value = "Table Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam(value = "tableSchemaId",
              required = false) String tableSchemaId,
      @ApiParam(type = "Integer", value = "offset,Number of data",
          example = "0") @RequestParam(value = "limit", required = false) Integer limit,
      @ApiParam(type = "Integer", value = "offset, Page number",
          example = "0") @RequestParam(value = "offset", required = false) Integer offset,
      @ApiParam(type = "String", value = "Filter value", example = "value") @RequestParam(
          value = "filterValue", required = false) String filterValue,
      @ApiParam(type = "String", value = "Column Name", example = "column") @RequestParam(
          value = "columnName", required = false) String columnName) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG_ERROR.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }

    StreamingResponseBody responsebody = outputStream -> datasetService.etlExportDataset(datasetId,
        outputStream, tableSchemaId, limit, offset, filterValue, columnName);

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_STREAM_JSON).body(responsebody);
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
  @PreAuthorize("checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD')")
  @ApiOperation(value = "ETL import data")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully imported"),
      @ApiResponse(code = 500, message = "Error importing data"),
      @ApiResponse(code = 403, message = "Error dataset not belong dataflow")})
  public void etlImportDataset(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(value = "Etl Dataset Object") @RequestBody ETLDatasetVO etlDatasetVO,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam("dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Provider Id",
          example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {

    if (!dataflowId.equals(datasetService.getDataFlowIdById(datasetId))) {
      String errorMessage =
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId);
      LOG_ERROR.error(errorMessage);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          String.format(EEAErrorMessage.DATASET_NOT_BELONG_DATAFLOW, datasetId, dataflowId));
    }
    // check if dataset is reportable
    if (!datasetService.isDatasetReportable(datasetId)) {
      LOG_ERROR.error("The dataset {} is not reportable", datasetId);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          String.format(EEAErrorMessage.DATASET_NOT_REPORTABLE, datasetId));
    }

    try {
      fileTreatmentHelper.etlImportDataset(datasetId, etlDatasetVO, providerId);
    } catch (EEAException e) {
      LOG_ERROR.error("The etlImportDataset failed on datasetId {} because {}", datasetId,
          e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the attachment
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}/field/{fieldId}/attachment",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "get Attachments")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully getted"),
      @ApiResponse(code = 500, message = "Error getting attachments"),
      @ApiResponse(code = 404, message = "Error downloading attachment from dataset")})
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD') OR checkApiKey(#dataflowId,#providerId,#datasetId,'DATASET_CUSTODIAN','DATASET_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATACOLLECTION_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_STEWARD','DATASCHEMA_EDITOR_WRITE','DATASCHEMA_EDITOR_READ','DATASET_NATIONAL_COORDINATOR','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','DATACOLLECTION_STEWARD','REFERENCEDATASET_OBSERVER','REFERENCEDATASET_STEWARD','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD')")
  public ResponseEntity<byte[]> getAttachment(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "Field Id",
          example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
      @ApiParam(type = "Long", value = "Dataflow Id",
          example = "0") @RequestParam(value = "dataflowId") Long dataflowId,
      @ApiParam(type = "Long", value = "Provider Id",
          example = "0") @RequestParam(value = "providerId", required = false) Long providerId) {

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
  @PreAuthorize("isAuthenticated()")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully getted"),
      @ApiResponse(code = 500, message = "Error updating attachment"),
      @ApiResponse(code = 400, message = "table is read only or file format is invalid ")})
  public void updateAttachment(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "Field Id",
          example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField,
      @ApiParam(value = "file") @RequestParam("file") MultipartFile file) {

    try {
      // Not allow insert attachment if the table is marked as read only. This not applies to design
      // datasets
      if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
          datasetService.findFieldSchemaIdById(datasetId, idField), EntityTypeEnum.FIELD)) {
        LOG_ERROR.error("Error updating an attachment in the datasetId {}. The table is read only",
            datasetId);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
      }
      // Remove comma "," character to avoid error with special characters
      String fileName = file.getOriginalFilename().replace(",", "");
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
  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{datasetId}/field/{fieldId}/attachment")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully deleted"),
      @ApiResponse(code = 404, message = "Error deleting attachment from dataset"),
      @ApiResponse(code = 400, message = "Table is read only")})
  public void deleteAttachment(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "Field Id",
          example = "19D0B971B7E0D2FB66B77F2A8DBA4964") @PathVariable("fieldId") String idField) {
    if (datasetService.checkIfDatasetLockedOrReadOnly(datasetId,
        datasetService.findFieldSchemaIdById(datasetId, idField), EntityTypeEnum.FIELD)) {
      LOG_ERROR.error("Error updating an attachment in the datasetId {}. The table is read only",
          datasetId);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.TABLE_READ_ONLY);
    }
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
  @ApiOperation(value = "Get referenced by dataset id", hidden = true)
  public Long getReferencedDatasetId(
      @ApiParam(type = "Long", value = "Dataset Id origin",
          example = "0") @PathVariable("dataflowId") @RequestParam("id") Long datasetIdOrigin,
      @ApiParam(type = "String", value = "Field Schema Id",
          example = "5cf0e9b3b793310e9ceca190") @RequestParam("idFieldSchema") String idFieldSchema) {
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
  @ApiOperation(value = "Get Dataset Type by dataset Id", hidden = true)
  public DatasetTypeEnum getDatasetType(@ApiParam(type = "Long", value = "Dataset Id",
      example = "0") @PathVariable("datasetId") Long datasetId) {
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
  @ApiOperation(value = "delete data before replacing", hidden = true)
  public void deleteDataBeforeReplacing(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("id") Long datasetId,
      @ApiParam(type = "Long", value = "Integration Id",
          example = "0") @RequestParam("integrationId") Long integrationId,
      @ApiParam(
          value = "Operation Object") @RequestParam("operation") IntegrationOperationTypeEnum operation) {
    // When deleting the data finishes, we send a kafka event to make the FME call to import data
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    deleteHelper.executeDeleteImportDataAsyncBeforeReplacing(datasetId, integrationId, operation);
  }


  /**
   * Export public file.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider I
   * @param fileName the file name
   * @return the http entity
   */
  @Override
  @ApiOperation(value = "export public file", hidden = true)
  @GetMapping("/exportPublicFile/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported file"),
      @ApiResponse(code = 404, message = "File doesn't exist")})
  public ResponseEntity<InputStreamResource> exportPublicFile(
      @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @PathVariable Long dataflowId,
      @ApiParam(type = "Long", value = "provider Id",
          example = "0") @PathVariable Long dataProviderId,
      @ApiParam(type = "String", value = "File Name",
          example = "value") @RequestParam String fileName) {

    try {
      File zipContent = datasetService.exportPublicFile(dataflowId, dataProviderId, fileName);
      return createResponseEntity(fileName, zipContent);
    } catch (IOException | EEAException e) {
      LOG_ERROR.error("File doesn't exist in the route {} ", fileName);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }


  /**
   * Export reference dataset file.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @return the response entity
   */
  @Override
  @ApiOperation(value = "export reference public file", hidden = true)
  @GetMapping("/exportPublicFile/dataflow/{dataflowId}")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully exported file"),
      @ApiResponse(code = 404, message = "File doesn't exist")})
  public ResponseEntity<InputStreamResource> exportReferenceDatasetFile(
      @ApiParam(type = "Long", value = "Dataflow Id", example = "0") @PathVariable Long dataflowId,
      @ApiParam(type = "String", value = "File name",
          example = "filename") @RequestParam String fileName) {

    try {
      File zipContent = datasetService.exportPublicFile(dataflowId, null, fileName);
      return createResponseEntity(fileName, zipContent);
    } catch (IOException | EEAException e) {
      LOG_ERROR.error("File doesn't exist in the route {} ", fileName);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }


  /**
   * Check any schema available in public.
   *
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @Override
  @ApiOperation(value = "Check if any schema is public", hidden = true)
  @GetMapping("/private/checkAnySchemaAvailableInPublic")
  public boolean checkAnySchemaAvailableInPublic(@ApiParam(type = "Long", value = "Dataflow Id",
      example = "0") @RequestParam("dataflowId") Long dataflowId) {
    return datasetService.checkAnySchemaAvailableInPublic(dataflowId);
  }


  /**
   * Export dataset file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{datasetId}/exportDatasetFile")
  @ApiOperation(value = "Export dataset file", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASET_OBSERVER','DATASET_CUSTODIAN','DATASCHEMA_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  public void exportDatasetFile(
      @ApiParam(type = "Long", value = "Dataset Id",
          example = "0") @PathVariable("datasetId") Long datasetId,
      @ApiParam(type = "String", value = "mime type (extension file)",
          example = "csv") @RequestParam("mimeType") String mimeType) {
    LOG.info("Export dataset data from datasetId {}, with type {}", datasetId, mimeType);
    fileTreatmentHelper.exportDatasetFile(datasetId, mimeType);

  }



  /**
   * Download file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @Override
  @GetMapping(value = "/{datasetId}/downloadFile",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASET_LEAD_REPORTER','DATASET_REPORTER_WRITE','DATASET_REPORTER_READ','DATASET_REQUESTER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','DATASCHEMA_EDITOR_WRITE','EUDATASET_CUSTODIAN','EUDATASET_STEWARD','EUDATASET_OBSERVER','DATASET_NATIONAL_COORDINATOR','TESTDATASET_CUSTODIAN','TESTDATASET_STEWARD','DATACOLLECTION_CUSTODIAN','DATACOLLECTION_STEWARD','DATACOLLECTION_OBSERVER','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD','REFERENCEDATASET_OBSERVER') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiOperation(value = "Download file", hidden = true)
  public void downloadFile(
      @ApiParam(type = "Long", value = "Dataset Id", example = "0") @PathVariable Long datasetId,
      @ApiParam(type = "String", value = "File name",
          example = "file.csv") @RequestParam String fileName,
      @ApiParam(value = "response") HttpServletResponse response) {
    try {
      LOG.info("Downloading file generated from export dataset. DatasetId {} Filename {}",
          datasetId, fileName);
      File file = datasetService.downloadExportedFile(datasetId, fileName);
      response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

      OutputStream out = response.getOutputStream();
      FileInputStream in = new FileInputStream(file);
      // copy from in to out
      IOUtils.copyLarge(in, out);
      out.close();
      in.close();
      // delete the file after downloading it
      FileUtils.forceDelete(file);
    } catch (IOException | EEAException e) {
      LOG_ERROR.error(
          "Error downloading file generated from export from the datasetId {}. Filename {}. Message: {}",
          datasetId, fileName, e.getMessage());
    }


  }


  /**
   * Creates the response entity.
   *
   * @param fileName the file name
   * @param content the content
   * @return the response entity
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private ResponseEntity<InputStreamResource> createResponseEntity(String fileName, File content)
      throws IOException {

    InputStreamResource resource = new InputStreamResource(FileUtils.openInputStream(content));
    HttpHeaders header = new HttpHeaders();
    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    return ResponseEntity.ok().headers(header).contentLength(content.length())
        .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
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
