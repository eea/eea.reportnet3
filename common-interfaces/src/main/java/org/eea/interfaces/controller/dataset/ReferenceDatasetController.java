package org.eea.interfaces.controller.dataset;

import java.util.List;
import java.util.Set;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DatasetsSummaryVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetPublicVO;
import org.eea.interfaces.vo.dataset.ReferenceDatasetVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;



/**
 * The Interface ReferenceDatasetController.
 */
public interface ReferenceDatasetController {



  /**
   * The Interface ReferenceDatasetControllerZuul.
   */
  @FeignClient(value = "dataset", contextId = "referenceDataset", path = "/referenceDataset")
  interface ReferenceDatasetControllerZuul extends ReferenceDatasetController {

  }



  /**
   * Find reference dataset by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReferenceDatasetVO> findReferenceDatasetByDataflowId(@PathVariable("id") Long dataflowId);


  /**
   * Find reference data set public by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/private/referencePublic/dataflow/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<ReferenceDatasetPublicVO> findReferenceDataSetPublicByDataflowId(
      @PathVariable("id") Long dataflowId);


  /**
   * Find dataflows referenced by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the sets the
   */
  @GetMapping(value = "/referenced/dataflow/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Set<DataFlowVO> findDataflowsReferencedByDataflowId(@PathVariable("id") Long dataflowId);


  /**
   * Update reference dataset if it is marked as updateable.
   *
   * @param datasetId the dataset id
   * @param updatable the updatable
   */
  @PutMapping("/{datasetId}")
  void updateReferenceDataset(@PathVariable Long datasetId,
      @RequestParam("updatable") Boolean updatable);

  /**
   * Find reference dataset summary list.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping("/private/referenceDatasetsSummary/dataflow/{id}")
  List<DatasetsSummaryVO> findReferenceDatasetSummaryList(@PathVariable("id") Long dataflowId);

}
