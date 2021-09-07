package org.eea.interfaces.controller.dataflow;

import java.util.Date;
import java.util.List;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicPaginatedVO;
import org.eea.interfaces.vo.dataflow.DataflowPublicVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataflowEnum;
import org.eea.interfaces.vo.dataflow.enums.TypeStatusEnum;
import org.eea.interfaces.vo.document.DocumentVO;
import org.eea.interfaces.vo.enums.EntityClassEnum;
import org.eea.interfaces.vo.ums.DataflowUserRoleVO;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The Interface DataFlowController.
 */
public interface DataFlowController {

  /**
   * The Interface DataFlowControllerZuul.
   */
  @FeignClient(value = "dataflow", path = "/dataflow")
  interface DataFlowControllerZuul extends DataFlowController {

  }

  /**
   * Find by id.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @return the data flow VO
   */
  @GetMapping(value = "/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataFlowVO findById(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam(value = "providerId", required = false) Long providerId);

  /**
   * Find by status.
   *
   * @param status the status
   * @return the list
   */
  @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findByStatus(@PathVariable("status") TypeStatusEnum status);

  /**
   * Find completed.
   *
   * @param pageNum the page num
   * @param pageSize the page size
   * @return the list
   */
  @GetMapping(value = "/completed", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findCompleted(
      @RequestParam(value = "pageNum", defaultValue = "0") Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize);


  /**
   * Find dataflows.
   *
   * @return the list
   */
  @GetMapping(value = "/getDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findDataflows();

  /**
   * Find reference dataflows.
   *
   * @return the list
   */
  @GetMapping(value = "/referenceDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findReferenceDataflows();


  /**
   * Find business dataflows.
   *
   * @return the list
   */
  @GetMapping(value = "/businessDataflows", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataFlowVO> findBusinessDataflows();

  /**
   * Adds the contributor.
   *
   * @param dataflowId the dataflow id
   * @param idContributor the id contributor
   */
  @PostMapping("/{dataflowId}/contributor/add")
  void addContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("idContributor") String idContributor);

  /**
   * Removes the contributor.
   *
   * @param dataflowId the dataflow id
   * @param idContributor the id contributor
   */
  @DeleteMapping("{dataflowId}/contributor/remove")
  void removeContributor(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("idContributor") String idContributor);

  /**
   * Creates the data flow.
   *
   * @param dataFlowVO the data flow VO
   * @return the response entity
   */
  @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity createDataFlow(@RequestBody DataFlowVO dataFlowVO);

  /**
   * Update data flow.
   *
   * @param dataFlowVO the data flow VO
   * @return the response entity
   */
  @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity updateDataFlow(@RequestBody DataFlowVO dataFlowVO);

  /**
   * Gets the metabase by id.
   *
   * @param dataflowId the dataflow id
   * @return the metabase by id
   */
  @GetMapping(value = "/{dataflowId}/getmetabase", produces = MediaType.APPLICATION_JSON_VALUE)
  DataFlowVO getMetabaseById(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Delete data flow.
   *
   * @param dataflowId the dataflow id
   */
  @DeleteMapping("/{dataflowId}")
  void deleteDataFlow(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Update data flow status.
   *
   * @param dataflowId the dataflow id
   * @param status the status
   * @param deadLineDate the dead line date
   */
  @PutMapping("/{dataflowId}/updateStatus")
  void updateDataFlowStatus(@PathVariable("dataflowId") Long dataflowId,
      @RequestParam("status") TypeStatusEnum status,
      @RequestParam(value = "deadLineDate", required = false) Date deadLineDate);

  /**
   * Gets the public dataflows.
   *
   * @return the public dataflows
   */
  @GetMapping("/getPublicDataflows")
  List<DataflowPublicVO> getPublicDataflows();

  /**
   * Gets the public dataflows.
   *
   * @param dataflowId the dataflow id
   * @return the public dataflows
   */

  @GetMapping("/getPublicDataflow/{dataflowId}")
  DataflowPublicVO getPublicDataflow(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Update data flow public status.
   *
   * @param dataflowId the dataflow id
   * @param showPublicInfo the show public info
   */
  @PutMapping("private/updatePublicStatus")
  void updateDataFlowPublicStatus(@RequestParam("dataflowId") Long dataflowId,
      @RequestParam("showPublicInfo") boolean showPublicInfo);

  /**
   * Gets the user roles all dataflows.
   *
   * @return the user roles all dataflows
   */
  @GetMapping("/getUserRolesAllDataflows")
  List<DataflowUserRoleVO> getUserRolesAllDataflows();

  /**
   * Gets the public dataflows by country.
   *
   * @param countryCode the country code
   * @param pageNum the page num
   * @param pageSize the page size
   * @param sortField the sort field
   * @param asc the asc
   * @return the public dataflows by country
   */
  @GetMapping("/public/country/{countryCode}")
  DataflowPublicPaginatedVO getPublicDataflowsByCountry(
      @PathVariable("countryCode") String countryCode,
      @RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
      @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
      @RequestParam(value = "sortField", required = false) String sortField,
      @RequestParam(value = "asc", defaultValue = "true") boolean asc);



  /**
   * Access reference entity.
   *
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  @GetMapping("/private/isReferenceDataflowDraft/entity/{entity}/{entityId}")
  boolean accessReferenceEntity(@PathVariable("entity") EntityClassEnum entity,
      @PathVariable("entityId") Long entityId);


  /**
   * Access entity.
   *
   * @param dataflowType the dataflow type
   * @param entity the entity
   * @param entityId the entity id
   * @return true, if successful
   */
  @GetMapping("/private/isDataflowType/{type}/entity/{entity}/{entityId}")
  boolean accessEntity(@PathVariable("type") TypeDataflowEnum dataflowType,
      @PathVariable("entity") EntityClassEnum entity, @PathVariable("entityId") Long entityId);

  /**
   * Gets the all documents by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all documents by dataflow
   */
  @GetMapping(value = "document/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DocumentVO> getAllDocumentsByDataflow(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Gets the all weblinks by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow
   */
  @GetMapping(value = "weblink/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<WeblinkVO> getAllWeblinksByDataflow(@PathVariable("dataflowId") Long dataflowId);

}
