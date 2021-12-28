package org.eea.dataset.controller;

import java.util.List;
import java.util.Set;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;


/**
 * The Class ReferenceDatasetControllerImpl.
 */
@RestController
@RequestMapping("/referenceDataset")
@ApiIgnore
public class ReferenceDatasetControllerImpl implements ReferenceDatasetController {


  /** The reference dataset service. */
  @Autowired
  private ReferenceDatasetService referenceDatasetService;

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");


  /**
   * Find reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @ApiOperation(value = "Find reference dataset  by dataflow Id", hidden = true)
  @GetMapping(value = "/private/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ReferenceDatasetVO> findReferenceDatasetByDataflowId(@ApiParam(type = "Long",
      value = "Dataflow Id", example = "0") @PathVariable("id") Long dataflowId) {

    return referenceDatasetService.getReferenceDatasetByDataflowId(dataflowId);
  }


  /**
   * Find reference data set public by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/referencePublic/dataflow/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find reference dataset public by dataflow Id", hidden = true)
  public List<ReferenceDatasetPublicVO> findReferenceDataSetPublicByDataflowId(@ApiParam(
      type = "Long", value = "Dataflow Id", example = "0") @PathVariable("id") Long dataflowId) {
    return referenceDatasetService.getReferenceDatasetPublicByDataflow(dataflowId);
  }


  /**
   * Find dataflows referenced by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the sets the
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/referenced/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find referenced dataflows reference by dataflow Id", hidden = true)
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD', 'ADMIN') OR checkAccessReferenceEntity('DATAFLOW',#id)")
  public Set<DataFlowVO> findDataflowsReferencedByDataflowId(@ApiParam(type = "Long",
      value = "Dataflow Id", example = "0") @PathVariable("id") Long dataflowId) {
    return referenceDatasetService.getDataflowsReferenced(dataflowId);
  }


  /**
   * Update reference dataset if it is marked as updateable
   */
  @Override
  @HystrixCommand
  @PutMapping("/{datasetId}")
  @ApiOperation(value = "update referenced dataset", hidden = true)
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD','DATASET_OBSERVER','DATASCHEMA_CUSTODIAN','DATASET_CUSTODIAN','TESTDATASET_CUSTODIAN','REFERENCEDATASET_CUSTODIAN','REFERENCEDATASET_STEWARD') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATASET',#datasetId))")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully update dataset"),
      @ApiResponse(code = 404, message = "Dataset not found")})
  public void updateReferenceDataset(
      @ApiParam(type = "Long", value = "dataset Id", example = "0") @PathVariable Long datasetId,
      @ApiParam(type = "Boolean", value = "updatable",
          example = "0") @RequestParam("updatable") Boolean updatable) {
    try {
      referenceDatasetService.updateUpdatable(datasetId, updatable);
    } catch (EEAException e) {
      LOG_ERROR.error("Error updating reference dataset. DatasetId: {}. Error Message: {}",
          datasetId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.UPDATING_REFERENCE_DATASET);
    }
  }
}
