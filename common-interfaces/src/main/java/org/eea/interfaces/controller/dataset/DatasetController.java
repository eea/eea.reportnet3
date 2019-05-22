package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
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
   * Find by id data set vo.
   *
   * @param id the id
   *
   * @return the data set vo
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetVO findById(@PathVariable("id") Long id);

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

  @PostMapping("{id}/loadDatasetData")
  public void loadDatasetData(@PathVariable("id") Long datasetId,
      @RequestParam("file") MultipartFile file);

  /**
   * @param datasetName the dataset name
   */
  /*@Deprecated
  @RequestMapping(value = "/createDataSchema", method = RequestMethod.POST)
  void createDataSchema(@RequestParam("datasetName") String datasetName);*/
  
  /**
   * @param id the dataschema id
   */
  /*@RequestMapping(value = "dataschema/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id);*/
  
  /**
   * @param id the idFlow
   */
  /*@RequestMapping(value = "dataschema/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow);
  */
  /**
   * @param datasetName the dataset id
   */
  @DeleteMapping(value = "/deleteImportData")
  void deleteImportData(@RequestParam("datasetName") String datasetId);


}
