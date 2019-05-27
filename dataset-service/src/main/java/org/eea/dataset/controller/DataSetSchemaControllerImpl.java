package org.eea.dataset.controller;

import org.eea.dataset.service.DatasetSchemaService;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The type Data set Schema controller.
 */
@RestController
@RequestMapping("/dataschema")
public class DataSetSchemaControllerImpl implements DatasetSchemaController {

  /** The dataschema service. */
  @Autowired
  private DatasetSchemaService dataschemaService;


  /**
   * Creates the data schema.
   *
   * @param datasetName the dataset name
   */
  @Override
  @RequestMapping(value = "/createDataSchema", method = RequestMethod.POST)
  public void createDataSchema(String datasetName) {
    dataschemaService.createDataSchema(datasetName);
  }


  /**
   * Find data schema by id.
   *
   * @param id the id
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand(fallbackMethod = "errorHandlerSchema")
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id) {

    return dataschemaService.getDataSchemaById(id);

  }


  /**
   * Find data schema by dataflow.
   *
   * @param idFlow the id flow
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand(fallbackMethod = "errorHandlerSchemaDataFlow")
  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow) {

    return dataschemaService.getDataSchemaByIdFlow(idFlow);

  }

  /**
   * Error handler schema.
   *
   * @param id the id
   * @return the data set schema VO
   */
  public DataSetSchemaVO errorHandlerSchema(@PathVariable("id") String id) {
    DataSetSchemaVO dataschema = new DataSetSchemaVO();

    return dataschema;
  }

  /**
   * Error handler schema data flow.
   *
   * @param id the id
   * @return the data set schema VO
   */
  public DataSetSchemaVO errorHandlerSchemaDataFlow(@PathVariable("id") Long id) {
    DataSetSchemaVO dataschema = new DataSetSchemaVO();

    return dataschema;
  }


}
