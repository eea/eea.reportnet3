package org.eea.dataset.controller;

import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class DataSetSchemaControllerImpl.
 */
@RestController
@RequestMapping("/dataschema")
public class DataSetSchemaControllerImpl implements DatasetSchemaController {

  /**
   * The dataschema service.
   */
  @Autowired
  private DatasetSchemaService dataschemaService;

  /** The dataset service. */
  @Autowired
  private DatasetService datasetService;

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/createDataSchema/{id}", method = RequestMethod.POST)
  public void createDataSchema(@PathVariable("id") final Long datasetId,
      @RequestParam("idDataflow") final Long dataflowId) {
    dataschemaService.createDataSchema(datasetId, dataflowId);
  }


  /**
   * Find data schema by id.
   *
   * @param id the id
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @RequestMapping(value = "/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public DataSetSchemaVO findDataSchemaById(@PathVariable("id") String id) {

    return dataschemaService.getDataSchemaById(id);

  }


  /**
   * Find data schema by dataflow.
   *
   * @param idFlow the id flow
   *
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand()
  @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idFlow,'DATAFLOW_PROVIDER') OR secondLevelAuthorize(#idFlow,'DATAFLOW_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow) {

    return dataschemaService.getDataSchemaByIdFlow(idFlow, true);

  }

  @Override
  @HystrixCommand()
  @RequestMapping(value = "/noRules/dataflow/{id}", method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idFlow,'DATAFLOW_PROVIDER') OR secondLevelAuthorize(#idFlow,'DATAFLOW_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaWithNoRulesByDataflow(@PathVariable("id") Long idFlow) {

    return dataschemaService.getDataSchemaByIdFlow(idFlow, false);

  }

  /**
   * Delete table schema.
   *
   * @param datasetId the dataset id
   * @param idTableSchema the id table schema
   */
  @Override
  @HystrixCommand()
  @RequestMapping(value = "/{datasetId}/tableschema/{tableSchemaId}", method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void deleteTableSchema(@PathVariable("datasetId") Long datasetId,
      @PathVariable("tableSchemaId") String idTableSchema) {
    if (idTableSchema == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IDTABLESCHEMA_INCORRECT);
    }
    dataschemaService.deleteTableSchema(datasetId, idTableSchema);
    datasetService.deleteTableValue(datasetId, idTableSchema);
  }
}
