package org.eea.interfaces.controller.dataset;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.ETLDatasetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * The Interface DatasetController.
 */
public interface DatasetController {

  /**
   * The Interface DataSetControllerZuul.
   */
  @FeignClient(value = "dataset", path = "/dataset")
  interface DataSetControllerZuul extends DatasetController {
  }

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
   * @param fieldSchemaId the field schema id
   * @param fieldValue the field value
   * @return the data tables values
   */
  @GetMapping("TableValueDataset/{id}")
  TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "levelError", required = false) ErrorTypeEnum[] levelError,
      @RequestParam(value = "idRules", required = false) String[] idRules,
      @RequestParam(value = "fieldSchemaId", required = false) String fieldSchemaId,
      @RequestParam(value = "fieldValue", required = false) String fieldValue);

  /**
   * Update dataset.
   *
   * @param dataset the dataset
   */
  @PutMapping("/update")
  void updateDataset(@RequestBody DataSetVO dataset);

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   * @return the data flow id by id
   */
  @GetMapping("/private/{id}/dataflow")
  Long getDataFlowIdById(@PathVariable("id") Long datasetId);

  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the id table schema
   * @param records the records
   */
  @PostMapping("/{datasetId}/table/{tableSchemaId}/record")
  void insertRecords(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId, @RequestBody List<RecordVO> records);

  /**
   * Update records.
   *
   * @param datasetId the dataset id
   * @param records the records
   * @param updateCascadePK the update cascade PK
   */
  @PutMapping("/{id}/updateRecord")
  void updateRecords(@PathVariable("id") Long datasetId, @RequestBody List<RecordVO> records,
      @RequestParam(value = "updateCascadePK", required = false) boolean updateCascadePK);

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   * @param deleteCascadePK the delete cascade PK
   */
  @DeleteMapping("/{id}/record/{recordId}")
  void deleteRecord(@PathVariable("id") Long datasetId, @PathVariable("recordId") String recordId,
      @RequestParam(value = "deleteCascadePK", required = false) boolean deleteCascadePK);

  /**
   * Delete import data.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param deletePrefilledTables the delete prefilled tables
   */
  @DeleteMapping("/v1/{datasetId}/deleteDatasetData")
  void deleteDatasetData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "deletePrefilledTables", defaultValue = "false",
          required = false) Boolean deletePrefilledTables);

  /**
   * Delete import data legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param deletePrefilledTables the delete prefilled tables
   */
  @DeleteMapping("/{datasetId}/deleteImportData")
  void deleteImportDataLegacy(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "deletePrefilledTables", defaultValue = "false",
          required = false) Boolean deletePrefilledTables);

  /**
   * Delete import table.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @DeleteMapping("/v1/{datasetId}/deleteTableData/{tableSchemaId}")
  void deleteTableData(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Delete import table legacy.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @DeleteMapping("/{datasetId}/deleteImportTable/{tableSchemaId}")
  void deleteImportTableLegacy(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);


  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param mimeType the mime type
   */
  @GetMapping(value = "/exportFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  void exportFile(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam("mimeType") String mimeType);

  /**
   * Export file through integration.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   */
  @GetMapping("/exportFileThroughIntegration")
  void exportFileThroughIntegration(@RequestParam("datasetId") Long datasetId,
      @RequestParam("integrationId") Long integrationId);

  /**
   * Insert id data schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   */
  @PostMapping("/private/{id}/insertIdSchema")
  void insertIdDataSchema(@PathVariable("id") Long datasetId,
      @RequestParam("idDatasetSchema") String idDatasetSchema);

  /**
   * Update field.
   *
   * @param datasetId the dataset id
   * @param field the field
   * @param updateCascadePK the update cascade PK
   */
  @PutMapping("/{id}/updateField")
  void updateField(@PathVariable("id") Long datasetId, @RequestBody FieldVO field,
      @RequestParam(value = "updateCascadePK", required = false) boolean updateCascadePK);

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
  @GetMapping("/{id}/datasetSchemaId/{datasetSchemaId}/fieldSchemaId/{fieldSchemaId}/getFieldsValuesReferenced")
  List<FieldVO> getFieldValuesReferenced(@PathVariable("id") Long datasetIdOrigin,
      @PathVariable("datasetSchemaId") String datasetSchemaId,
      @PathVariable("fieldSchemaId") String fieldSchemaId,
      @RequestParam(value = "conditionalValue", required = false) String conditionalValue,
      @RequestParam(value = "searchValue", required = false) String searchValue,
      @RequestParam(value = "resultsNumber", required = false) Integer resultsNumber);

  /**
   * Gets the referenced dataset id.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idFieldSchema the id field schema
   * @return the referenced dataset id
   */
  @GetMapping("/private/getReferencedDatasetId")
  Long getReferencedDatasetId(@RequestParam("id") Long datasetIdOrigin,
      @RequestParam("idFieldSchema") String idFieldSchema);

  /**
   * Gets the dataset type.
   *
   * @param datasetId the dataset id
   * @return the dataset type
   */
  @GetMapping("/private/datasetType/{datasetId}")
  DatasetTypeEnum getDatasetType(@PathVariable("datasetId") Long datasetId);

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
  @GetMapping("/v1/{datasetId}/etlExport")
  ResponseEntity<StreamingResponseBody> etlExportDataset(@PathVariable("datasetId") Long datasetId,
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "offset", required = false) Integer offset,
      @RequestParam(value = "filterValue", required = false) String filterValue,
      @RequestParam(value = "columnName", required = false) String columnName);


  /**
   * Etl export dataset legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param tableSchemaId the table schema id
   * @param limit the limit
   * @param offset the offset
   * @param filterValue the filter value
   * @param columnName the column name
   * @return the response entity
   */
  @GetMapping("/{datasetId}/etlExport")
  ResponseEntity<StreamingResponseBody> etlExportDatasetLegacy(
      @PathVariable("datasetId") Long datasetId, @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam(value = "limit", required = false) Integer limit,
      @RequestParam(value = "offset", required = false) Integer offset,
      @RequestParam(value = "filterValue", required = false) String filterValue,
      @RequestParam(value = "columnName", required = false) String columnName);

  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @PostMapping("/v1/{datasetId}/etlImport")
  void etlImportDataset(@PathVariable("datasetId") Long datasetId,
      @RequestBody ETLDatasetVO etlDatasetVO, @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Etl import dataset legacy.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @PostMapping("/{datasetId}/etlImport")
  void etlImportDatasetLegacy(@PathVariable("datasetId") Long datasetId,
      @RequestBody ETLDatasetVO etlDatasetVO, @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the attachment
   */
  @GetMapping(value = "/v1/{datasetId}/field/{fieldId}/attachment",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<byte[]> getAttachment(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String fieldId, @RequestParam(value = "dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Gets the attachment legacy.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the attachment legacy
   */
  @GetMapping(value = "/{datasetId}/field/{fieldId}/attachment",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<byte[]> getAttachmentLegacy(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String fieldId, @RequestParam(value = "dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param idField the id field
   * @param file the file
   */
  @PutMapping("/v1/{datasetId}/field/{fieldId}/attachment")
  void updateAttachment(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @PathVariable("fieldId") String idField, @RequestParam("file") MultipartFile file);

  /**
   * Update attachment legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param idField the id field
   * @param file the file
   */
  @PutMapping("/{datasetId}/field/{fieldId}/attachment")
  void updateAttachmentLegacy(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @PathVariable("fieldId") String idField, @RequestParam("file") MultipartFile file);

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param idField the id field
   */
  @DeleteMapping("/v1/{datasetId}/field/{fieldId}/attachment")
  void deleteAttachment(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @PathVariable("fieldId") String idField);

  /**
   * Delete attachment legacy.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param idField the id field
   */
  @DeleteMapping("/{datasetId}/field/{fieldId}/attachment")
  void deleteAttachmentLegacy(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @PathVariable("fieldId") String idField);

  /**
   * Delete data before replacing.
   *
   * @param datasetId the dataset id
   * @param integrationId the integration id
   * @param operation the operation
   */
  @DeleteMapping("/private/{id}/deleteForReplacing")
  void deleteDataBeforeReplacing(@PathVariable("id") Long datasetId,
      @RequestParam("integrationId") Long integrationId,
      @RequestParam("operation") IntegrationOperationTypeEnum operation);

  /**
   * Insert records multi table.
   *
   * @param datasetId the dataset id
   * @param tableRecords the table records
   */
  @PostMapping("/{datasetId}/insertRecordsMultiTable")
  void insertRecordsMultiTable(@PathVariable("datasetId") Long datasetId,
      @RequestBody List<TableVO> tableRecords);

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
  @PostMapping("/v1/{datasetId}/importFileData")
  void importFileData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "replace", required = false) boolean replace,
      @RequestParam(value = "integrationId", required = false) Long integrationId,
      @RequestParam(value = "delimiter", required = false) String delimiter);

  /**
   * Import file data legacy.
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
  @PostMapping("/{datasetId}/importFileData")
  void importFileDataLegacy(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "replace", required = false) boolean replace,
      @RequestParam(value = "integrationId", required = false) Long integrationId,
      @RequestParam(value = "delimiter", required = false) String delimiter);


  /**
   * Export file.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param fileName the file name
   * @return the response entity
   */
  @GetMapping("/exportPublicFile/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
  ResponseEntity<InputStreamResource> exportPublicFile(@PathVariable Long dataflowId,
      @PathVariable Long dataProviderId, @RequestParam String fileName);


  /**
   * Check any schema available in public.
   *
   * @param dataflowId the dataflow id
   * @return true, if successful
   */
  @GetMapping("/private/checkAnySchemaAvailableInPublic")
  boolean checkAnySchemaAvailableInPublic(@RequestParam("dataflowId") Long dataflowId);


  /**
   * Export dataset file.
   *
   * @param datasetId the dataset id
   * @param mimeType the mime type
   */
  @GetMapping(value = "/{datasetId}/exportDatasetFile")
  void exportDatasetFile(@PathVariable("datasetId") Long datasetId,
      @RequestParam("mimeType") String mimeType);



  /**
   * Download file.
   *
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param response the response
   */
  @GetMapping("/{datasetId}/downloadFile")
  void downloadFile(@PathVariable Long datasetId, @RequestParam String fileName,
      HttpServletResponse response);



  /**
   * Export reference dataset file.
   *
   * @param dataflowId the dataflow id
   * @param fileName the file name
   * @return the response entity
   */
  @GetMapping("/exportPublicFile/dataflow/{dataflowId}")
  ResponseEntity<InputStreamResource> exportReferenceDatasetFile(@PathVariable Long dataflowId,
      @RequestParam String fileName);

  /**
   * Update check view.
   *
   * @param datasetId the dataset id
   * @param updated the updated
   */
  @PutMapping("/private/viewUpdated/{datasetId}")
  public void updateCheckView(@PathVariable("datasetId") Long datasetId,
      @RequestParam Boolean updated);

  /**
   * Gets the check view.
   *
   * @param datasetId the dataset id
   * @return the check view
   */
  @GetMapping("/{datasetId}/viewUpdated")
  public Boolean getCheckView(@PathVariable("datasetId") Long datasetId);


  /**
   * Stream import file data.
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
  @PostMapping(path = "/v1/{datasetId}/streamImportFileData",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  void streamImportFileData(@PathVariable("datasetId") Long datasetId,
      @RequestParam(value = "dataflowId", required = false) Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam(value = "tableSchemaId", required = false) String tableSchemaId,
      @RequestParam(value = "replace", required = false) boolean replace,
      @RequestParam(value = "integrationId", required = false) Long integrationId,
      @RequestParam(value = "delimiter", required = false) String delimiter,
      @RequestBody HttpServletRequest request);
}
