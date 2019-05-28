package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.metabese.TableCollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
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
   * Find values by id.
   *
   * @param datasetId the dataset id
   * @return the data set VO
   */
  @RequestMapping(value = "/getDatasetValues/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetVO findById(Long datasetId);

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
  public void loadDatasetData(@PathVariable("id") Long datasetId,
      @RequestParam("file") MultipartFile file);



  /**
   * Delete import data.
   *
   * @param datasetId the id of dataset
   */
  @DeleteMapping(value = "/deleteImportData")
  void deleteImportData(@RequestParam("datasetName") Long datasetId);



  /**
   * Load schema mongo.
   *
   * @param datasetId the dataset id
   * @param dataFlowId the data flow id
   * @param tableName the table name
   * @param Headers the headers
   */
  @RequestMapping("{id}/loadDatasetData")
  void loadSchemaMongo(@PathVariable("id") Long datasetId,
      @RequestParam("dataFlowId") Long dataFlowId, @RequestBody TableCollectionVO tableCollections);

}
