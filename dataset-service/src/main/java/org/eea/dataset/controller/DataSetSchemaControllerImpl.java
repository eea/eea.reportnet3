package org.eea.dataset.controller;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.vo.dataset.enums.TypeDatasetEnum;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;



/**
 * The Class DataSetSchemaControllerImpl.
 */
@RestController
@RequestMapping("/dataschema")
public class DataSetSchemaControllerImpl implements DatasetSchemaController {

  /** The dataschema service. */
  @Autowired
  private DatasetSchemaService dataschemaService;

  @Autowired
  private DatasetMetabaseService datasetMetabaseService;

  private static final Logger LOG = LoggerFactory.getLogger(DataSetSchemaControllerImpl.class);

  /**
   * Creates the data schema.
   *
   * @param datasetId the dataset id
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createDataSchema/{id}")
  public void createDataSchema(@PathVariable("id") final Long datasetId,
      @RequestParam("idDataflow") final Long dataflowId) {
    dataschemaService.createDataSchema(datasetId, dataflowId);
  }

  /**
   * Creates the empty data set schema.
   *
   * @param nameDataSetSchema the name data set schema
   * @param idDataFlow the id data flow
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/createEmptyDataSetSchema")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  public void createEmptyDataSetSchema(
      @RequestParam("nameDataSetSchema") final String nameDataSetSchema,
      @RequestParam("idDataFlow") final Long idDataFlow) {
    try {
      datasetMetabaseService.createEmptyDataset(TypeDatasetEnum.DESIGN,
          nameDataSetSchema + "_dataset",
          dataschemaService.createEmptyDataSetSchema(idDataFlow, nameDataSetSchema).toString(),
          idDataFlow);
    } catch (EEAException e) {
      LOG.error("Unexpected exception: {}", e.getLocalizedMessage());
    }
  }

  /**
   * Find data schema by id.
   *
   * @param id the id
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
  @HystrixCommand()
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idFlow,'DATAFLOW_PROVIDER') OR secondLevelAuthorize(#idFlow,'DATAFLOW_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaByDataflow(@PathVariable("id") Long idFlow) {
    return dataschemaService.getDataSchemaByIdFlow(idFlow, true);
  }

  /**
   * Find data schema with no rules by dataflow.
   *
   * @param idFlow the id flow
   * @return the data set schema VO
   */
  @Override
  @HystrixCommand()
  @GetMapping(value = "/noRules/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorize(#idFlow,'DATAFLOW_PROVIDER') OR secondLevelAuthorize(#idFlow,'DATAFLOW_CUSTODIAN')")
  public DataSetSchemaVO findDataSchemaWithNoRulesByDataflow(@PathVariable("id") Long idFlow) {
    return dataschemaService.getDataSchemaByIdFlow(idFlow, false);
  }
}
