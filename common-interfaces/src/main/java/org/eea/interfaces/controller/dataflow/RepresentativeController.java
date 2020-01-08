package org.eea.interfaces.controller.dataflow;


import java.util.List;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * The Interface RepresentativeController.
 */
public interface RepresentativeController {

  /**
   * The Interface RepresentativeControllerZuul.
   */
  @FeignClient(value = "dataflow", contextId = "representative", path = "/representative")
  interface RepresentativeControllerZuul extends RepresentativeController {

  }

  /**
   * Insert representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   */
  @PostMapping(value = "/{dataflowId}")
  ResponseEntity<?> insertRepresentative(@PathVariable("dataflowId") final Long dataflowId,
      @RequestBody RepresentativeVO representativeVO);

  /**
   * Find all data provider by group id.
   *
   * @param groupId the group id
   * @return the list
   */
  @GetMapping(value = "/dataProvider/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataProviderVO> findAllDataProviderByGroupId(@PathVariable("groupId") Long groupId);

  /**
   * Find all data provider types.
   *
   * @return the list
   */
  @GetMapping(value = "/dataProvider/types", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataProviderCodeVO> findAllDataProviderTypes();

  /**
   * Find represetatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<RepresentativeVO> findRepresetativesByIdDataFlow(
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Update representative.
   *
   * @param dataflowRepresentativeVO the dataflow representative VO
   */
  @PutMapping(value = "/update")
  ResponseEntity<?> updateRepresentative(@RequestBody RepresentativeVO dataflowRepresentativeVO);

  /**
   * Delete representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   */
  @DeleteMapping(value = "/{dataflowRepresentativeId}")
  void deleteRepresentative(
      @PathVariable("dataflowRepresentativeId") Long dataflowRepresentativeId);
}
