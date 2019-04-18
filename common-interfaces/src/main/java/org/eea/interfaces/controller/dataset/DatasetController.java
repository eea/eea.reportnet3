package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.DataSetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
  @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetVO findById(@PathVariable("id") String id);

  /**
   * Update dataset data set vo.
   *
   * @param dataset the dataset
   *
   * @return the data set vo
   */
  @RequestMapping(value = "/update", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetVO updateDataset(@RequestBody DataSetVO dataset);

  @RequestMapping(value = "/create", method = RequestMethod.POST)
  void createEmptyDataSet(@RequestParam("datasetName") String datasetName);


}
