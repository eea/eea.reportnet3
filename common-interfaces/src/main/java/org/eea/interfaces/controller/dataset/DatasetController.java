/*
 *
 */
package org.eea.interfaces.controller.dataset;

import java.util.List;
import javax.ws.rs.Produces;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.ValidationLinkVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.dataset.enums.TypeErrorEnum;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * The interface Dataset controller.
 */
public interface DatasetController {

  /**
   * The interface Data set controller zuul.
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
   * @return the data tables values
   */
  @GetMapping(value = "TableValueDataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "levelError", required = false) TypeErrorEnum[] levelError);


  /**
   * Update dataset.
   *
   * @param dataset the dataset
   */
  @RequestMapping(value = "/update", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void updateDataset(@RequestBody DataSetVO dataset);


  /**
   * Load table data.
   *
   * @param datasetId the dataset id
   * @param file the file
   * @param idTableSchema the id table schema
   */
  @PostMapping("{id}/loadTableData/{idTableSchema}")
  void loadTableData(@PathVariable("id") Long datasetId, @RequestParam("file") MultipartFile file,
      @PathVariable(value = "idTableSchema") String idTableSchema);

  /**
   * Delete import data.
   *
   * @param datasetId the id of dataset
   */
  @DeleteMapping(value = "{id}/deleteImportData")
  void deleteImportData(@PathVariable("id") Long datasetId);


  /**
   * Load dataset schema.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableCollections the table collections
   */
  @RequestMapping("{id}/loadDatasetSchema")
  void loadDatasetSchema(@PathVariable("id") Long datasetId,
      @RequestParam("dataFlowId") Long dataFlowId, @RequestBody TableCollectionVO tableCollections);



  /**
   * Gets the table from any object id.
   *
   * @param id the id
   * @param idDataset the id dataset
   * @param type the type
   * @return the table from any object id
   */
  @GetMapping(value = "findPositionFromAnyObject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  ValidationLinkVO getPositionFromAnyObjectId(@PathVariable("id") String id,
      @RequestParam(value = "datasetId", required = true) Long idDataset,
      @RequestParam(value = "type", required = true) TypeEntityEnum type);


  /**
   * Gets the by id.
   *
   * @param datasetId the dataset id
   * @return the by id
   */
  @RequestMapping(value = "{id}", method = RequestMethod.GET)
  DataSetVO getById(@PathVariable("id") Long datasetId);


  /**
   * Gets the data flow id by id.
   *
   * @param datasetId the dataset id
   * @return the data flow id by id
   */
  @RequestMapping(value = "{id}/dataflow", method = RequestMethod.GET)
  Long getDataFlowIdById(@PathVariable("id") Long datasetId);

  /**
   * Insert records.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param records the records
   */
  @RequestMapping(value = "/{id}/table/{idTableSchema}/record", method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void insertRecords(@PathVariable("id") final Long datasetId,
      @PathVariable("idTableSchema") final String idTableSchema,
      @RequestBody List<RecordVO> records);

  /**
   * Update record.
   *
   * @param datasetId the dataset id
   * @param records the records
   */
  @RequestMapping(value = "/{id}/updateRecord", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void updateRecords(@PathVariable("id") Long datasetId, @RequestBody List<RecordVO> records);

  /**
   * Delete record.
   *
   * @param datasetId the dataset id
   * @param recordId the record id
   */
  @RequestMapping(value = "/{id}/record/{recordId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void deleteRecord(@PathVariable("id") Long datasetId, @PathVariable("recordId") String recordId);

  /**
   * Delete import table.
   *
   * @param datasetId the dataset id
   * @param tableSchemaId the table schema id
   */
  @DeleteMapping(value = "{datasetId}/deleteImportTable/{tableSchemaId}")
  void deleteImportTable(@PathVariable("datasetId") final Long datasetId,
      @PathVariable("tableSchemaId") final String tableSchemaId);



  /**
   * Export file.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   * @param mimeType the mime type
   * @return the response entity
   * @throws Exception the exception
   */
  @GetMapping("/exportFile")
  @Produces(value = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
  ResponseEntity exportFile(@RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "idTableSchema", required = false) String idTableSchema,
      @RequestParam("mimeType") String mimeType) throws Exception;


  /**
   * Insert id data schema.
   *
   * @param datasetId the dataset id
   * @param idDatasetSchema the id dataset schema
   */
  @PostMapping(value = "/{id}/insertIdSchema", produces = MediaType.APPLICATION_JSON_VALUE)
  void insertIdDataSchema(@PathVariable("id") Long datasetId,
      @RequestParam(value = "idDatasetSchema", required = true) String idDatasetSchema);


  /**
   * Update field.
   *
   * @param datasetId the dataset id
   * @param field the field
   */
  @RequestMapping(value = "/{id}/updateField", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  void updateField(@PathVariable("id") Long datasetId, @RequestBody FieldVO field);

}
