package org.eea.dataset.controller;

import org.eea.dataset.service.DataschemaService;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


@RestController
@RequestMapping("/dataschema")
public class DataSetSchemaControllerImpl implements DatasetSchemaController {

  @Autowired
  private DataschemaService dataschemaService;


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


  @Override
  @HystrixCommand(fallbackMethod = "errorHandlerSchemaDataFlow")
  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow) {

    return dataschemaService.getDataSchemaByIdFlow(idFlow);

  }

  public DataSetSchemaVO errorHandlerSchema(@PathVariable("id") String id) {
    DataSetSchemaVO dataschema = new DataSetSchemaVO();

    return dataschema;
  }

  public DataSetSchemaVO errorHandlerSchemaDataFlow(@PathVariable("id") Long id) {
    DataSetSchemaVO dataschema = new DataSetSchemaVO();

    return dataschema;
  }


}
