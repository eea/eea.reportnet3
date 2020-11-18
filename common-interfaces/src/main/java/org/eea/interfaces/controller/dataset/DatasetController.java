package org.eea.interfaces.controller.dataset;

import java.util.List;
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
import org.springframework.cloud.openfeign.FeignClient;
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
   * @return the data tables values
   */
  @GetMapping("TableValueDataset/{id}")
  TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "levelError", required = false) ErrorTypeEnum[] levelError,
      @RequestParam(value = "idRules", required = false) String[] idRules);

  /**
   * Update dataset.
   *
   * @param dataset the dataset
   */
  @PutMapping("/update")
  void updateDataset(@RequestBody DataSetVO dataset);

  /**
   * Load table data.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param idTableSchema the id table schema
   * @param replace the replace
   */
  @PostMapping("{id}/loadTableData/{idTableSchema}")
  void loadTableData(@PathVariable("id") Long datasetId, @RequestParam("file") MultipartFile file,
      @PathVariable("idTableSchema") String idTableSchema,
      @RequestParam(value = "replace", required = false) boolean replace);

  /**
   * Load dataset data.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param replace the replace
   */
  @PostMapping("{id}/loadDatasetData")
  void loadDatasetData(@PathVariable("id") Long datasetId, @RequestParam("file") MultipartFile file,
      @RequestParam(value = "replace", required = false) boolean replace);

  /**
   * Delete import data.
   *
   * @param datasetId the dataset id
   */
  @DeleteMapping("{id}/deleteImportData")
  void deleteImportData(@PathVariable("id") Long datasetId);

  /**
   * Gets the position from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param type the type
   * @return the position from any object id
   */
  @GetMapping("findPositionFromAnyObject/{id}")
  ValidationLinkVO getPositionFromAnyObjectId(@PathVariable("id") String id,
      @RequestParam("datasetId") Long idDataset, @RequestParam("type") EntityTypeEnum type);

  /**
   * Gets the by id.
   *
   * @param datasetId the dataset id
   * @return the by id
   */
  @GetMapping("{id}")
  DataSetVO getById(@PathVariable("id") Long datasetId);

  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   * @return the data flow id by id
   */
  @GetMapping("{id}/dataflow")
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
   */
  @PutMapping("/{id}/updateRecord")
  void updateRecords(@PathVariable("id") Long datasetId, @RequestBody List<RecordVO> records);

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   */
  @DeleteMapping("/{id}/record/{recordId}")
  void deleteRecord(@PathVariable("id") Long datasetId, @PathVariable("recordId") String recordId);

  /**
   * Delete import table.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @DeleteMapping("{datasetId}/deleteImportTable/{tableSchemaId}")
  void deleteImportTable(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String tableSchemaId);

  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   * @param mimeType the mime type
   * @return the response entity
   */
  @GetMapping(value = "/exportFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<byte[]> exportFile(@RequestParam("datasetId") Long datasetId,
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
  @PostMapping("/{id}/insertIdSchema")
  void insertIdDataSchema(@PathVariable("id") Long datasetId,
      @RequestParam("idDatasetSchema") String idDatasetSchema);

  /**
   * Update field.
   *
   * @param datasetId the dataset id
   * @param field the field
   */
  @PutMapping("/{id}/updateField")
  void updateField(@PathVariable("id") Long datasetId, @RequestBody FieldVO field);

  /**
   * Gets the field values referenced.
   *
   * @param datasetIdOrigin the dataset id origin
   * @param idFieldSchema the id field schema
   * @param searchValue the search value
   * @return the field values referenced
   */
  @GetMapping("/{id}/getFieldsValuesReferenced")
  List<FieldVO> getFieldValuesReferenced(@PathVariable("id") Long datasetIdOrigin,
      @RequestParam("idFieldSchema") String idFieldSchema,
      @RequestParam("searchValue") String searchValue);

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
   * @return the ETL dataset VO
   */
  @GetMapping("/{datasetId}/etlExport")
  ETLDatasetVO etlExportDataset(@PathVariable("datasetId") Long datasetId,
      @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Etl import dataset.
   *
   * @param datasetId the dataset id
   * @param etlDatasetVO the etl dataset VO
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   */
  @PostMapping("/{datasetId}/etlImport")
  void etlImportDataset(@PathVariable("datasetId") Long datasetId,
      @RequestBody ETLDatasetVO etlDatasetVO, @RequestParam("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Gets the attachment.
   *
   * @param datasetId the dataset id
   * @param fieldId the field id
   * @return the attachment
   */
  @GetMapping(value = "/{datasetId}/field/{fieldId}/attachment",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<byte[]> getAttachment(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String fieldId);

  /**
   * Update attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   * @param file the file
   */
  @PutMapping("/{datasetId}/field/{fieldId}/attachment")
  public void updateAttachment(@PathVariable("datasetId") Long datasetId,
      @PathVariable("fieldId") String idField, @RequestParam("file") MultipartFile file);

  /**
   * Delete attachment.
   *
   * @param datasetId the dataset id
   * @param idField the id field
   */
  @DeleteMapping("/{datasetId}/field/{fieldId}/attachment")
  public void deleteAttachment(@PathVariable("datasetId") Long datasetId,
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
}
