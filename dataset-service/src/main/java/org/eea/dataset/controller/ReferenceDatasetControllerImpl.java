package org.eea.dataset.controller;

import java.util.List;
import java.util.Set;
import org.eea.dataset.service.ReferenceDatasetService;
import org.eea.interfaces.controller.dataset.ReferenceDatasetController;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * The Class ReferenceDatasetControllerImpl.
 */
@RestController
@RequestMapping("/referenceDataset")
public class ReferenceDatasetControllerImpl implements ReferenceDatasetController {


  /** The reference dataset service. */
  @Autowired
  private ReferenceDatasetService referenceDatasetService;


  /**
   * Find reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ReferenceDatasetVO> findReferenceDatasetByDataflowId(
      @PathVariable("id") Long dataflowId) {

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
  public List<ReferenceDatasetPublicVO> findReferenceDataSetPublicByDataflowId(
      @PathVariable("id") Long dataflowId) {
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
  @PreAuthorize("hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') OR checkAccessReferenceEntity('DATAFLOW',#id)")
  public Set<DataFlowVO> findDataflowsReferencedByDataflowId(@PathVariable("id") Long dataflowId) {
    return referenceDatasetService.getDataflowsReferenced(dataflowId);
  }


}
