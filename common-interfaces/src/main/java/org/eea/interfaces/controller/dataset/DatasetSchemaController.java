package org.eea.interfaces.controller.dataset;

import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The Interface DatasetSchemaController.
 */
public interface DatasetSchemaController {

  /**
   * The interface Data set controller zuul.
   */
  @FeignClient(value = "dataschema", path = "/dataschema")
  interface DataSetSchemaControllerZuul extends DatasetSchemaController {

  }


  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   */
  @RequestMapping(value = "/createDataSchema/{id}", method = RequestMethod.POST)
  void createDataSchema(@PathVariable("id") final Long datasetId);

  /**
   * Find data schema by id.
   *
   * @param id the dataschema id
   * @return the data set schema VO
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id);

  /**
   * Find data schema by dataflow.
   *
   * @param idFlow the id flow
   * @return the data set schema VO
   */
  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow);

}
