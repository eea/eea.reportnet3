package org.eea.interfaces.controller.dataset;

import java.util.Map;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.TypeEntityEnum;
import org.eea.interfaces.vo.metabase.TableCollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
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
   * @param asc the asc
   *
   * @return the data tables values
   */
  @GetMapping(value = "TableValueDataset/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public TableVO getDataTablesValues(@PathVariable("id") Long datasetId,
      @RequestParam("idTableSchema") String idTableSchema,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "fields", required = false) String fields,
      @RequestParam(value = "asc", defaultValue = "true") Boolean asc);

  /**
   * Update dataset data set vo.
   *
   * @param dataset the dataset
   *
   * @return the data set vo
   */
  @RequestMapping(value = "/update", method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetVO updateDataset(@RequestBody DataSetVO dataset);

  /**
   * Creates the empty data set.
   *
   * @param datasetName the dataset name
   */
  @RequestMapping(value = "/create", method = RequestMethod.POST)
  void createEmptyDataSet(@RequestParam("datasetName") String datasetName);

  /**
   * Load dataset data.
   *
   * @param datasetId the dataset id
   * @param file the file
   */
  @PostMapping("{id}/loadDatasetData")
  void loadDatasetData(@PathVariable("id") Long datasetId,
      @RequestParam("file") MultipartFile file);


  /**
   * Delete import data.
   *
   * @param datasetId the id of dataset
   */
  @DeleteMapping(value = "/deleteImportData")
  void deleteImportData(@RequestParam("datasetName") Long datasetId);


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
   * @param pageSize the page size
   * @param type the type
   * @return the table from any object id
   */
  @GetMapping(value = "loadTableFromAnyObject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Map<String,TableVO> getTableFromAnyObjectId(@PathVariable("id") Long id,
      @RequestParam(value = "datasetId", required = true) Long idDataset,
      @RequestParam(value = "pageSize", defaultValue = "20", required = false) Integer pageSize,
      @RequestParam(value = "type", required = true) TypeEntityEnum type);

}
