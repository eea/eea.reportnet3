package org.eea.interfaces.controller.dataflow;

import java.util.List;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderGroupVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.FMEUserVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** The Interface RepresentativeController. */
public interface RepresentativeController {

  /** The Interface RepresentativeControllerZuul. */
  @FeignClient(value = "dataflow", contextId = "representative", path = "/representative")
  interface RepresentativeControllerZuul extends RepresentativeController {

  }

  /**
   * Creates the representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   */
  @PostMapping("/{dataflowId}")
  Long createRepresentative(@PathVariable("dataflowId") final Long dataflowId,
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
  @GetMapping(value = "/dataProvider/countryGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataProviderCodeVO> findAllDataProviderCountryType();

  /**
   * Find all data provider business types.
   *
   * @return the list
   */
  @GetMapping(value = "/dataProvider/companyGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataProviderCodeVO> findAllDataProviderCompanyType();

  /**
   * Find all data provider organization type.
   *
   * @return the list
   */
  @GetMapping(value = "/dataProvider/organizationGroups",
      produces = MediaType.APPLICATION_JSON_VALUE)
  List<DataProviderCodeVO> findAllDataProviderOrganizationType();


  /**
   * Find data provider group by dataflow id.
   *
   * @param dataflowId the dataflow id
   * @return the data provider code VO
   */
  @GetMapping(value = "/dataProviderGroup/dataflow/{dataflowId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  DataProviderCodeVO findDataProviderGroupByDataflowId(@PathVariable("dataflowId") Long dataflowId);


  /**
   * Find representatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<RepresentativeVO> findRepresentativesByIdDataFlow(
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Update representative.
   *
   * @param dataflowRepresentativeVO the dataflow representative VO
   * @return the response entity
   */
  @PutMapping("/update")
  Long updateRepresentative(@RequestBody RepresentativeVO dataflowRepresentativeVO);

  /**
   * Delete representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   * @param dataflowId the dataflow id
   */
  @DeleteMapping(value = "/{dataflowRepresentativeId}/dataflow/{dataflowId}")
  void deleteRepresentative(@PathVariable("dataflowRepresentativeId") Long dataflowRepresentativeId,
      @PathVariable("dataflowId") Long dataflowId);


  /**
   * Find data provider by id.
   *
   * @param dataProviderId the data provider id
   * @return the data provider VO
   */
  @GetMapping(value = "/dataProvider/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  DataProviderVO findDataProviderById(@PathVariable("id") Long dataProviderId);

  /**
   * Find data providers by ids.
   *
   * @param dataProviderIds the data provider id
   * @return the list
   */
  @GetMapping("/private/dataProvider")
  List<DataProviderVO> findDataProvidersByIds(@RequestParam("id") List<Long> dataProviderIds);


  /**
   * Export file of all lead reporters.
   *
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @GetMapping(value = "/export/{dataflowId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<byte[]> exportLeadReportersFile(@PathVariable("dataflowId") Long dataflowId);

  /**
   * Export template reporters file.
   *
   * @param groupId the group id
   * @return the response entity
   */
  @GetMapping(value = "/exportTemplateReportersFile/{groupId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  ResponseEntity<byte[]> exportTemplateReportersFile(@PathVariable("groupId") Long groupId);

  /**
   * Import file country template.With that controller we can download a country template to import
   * data with the countrys with this group id
   *
   * @param dataflowId the dataflow id
   * @param groupId the group id
   * @param file the file
   * @return the response entity
   */
  @PostMapping("/import/{dataflowId}/group/{groupId}")
  ResponseEntity<byte[]> importFileCountryTemplate(
      @PathVariable(value = "dataflowId") Long dataflowId,
      @PathVariable(value = "groupId") Long groupId, @RequestParam("file") MultipartFile file);


  /**
   * Update representative visibility restrictions.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   */
  @PostMapping("/private/updateRepresentativeVisibilityRestrictions")
  void updateRepresentativeVisibilityRestrictions(
      @RequestParam(value = "dataflowId", required = true) Long dataflowId,
      @RequestParam(value = "dataProviderId", required = true) Long dataProviderId,
      @RequestParam(value = "restrictFromPublic", required = true,
          defaultValue = "false") boolean restrictFromPublic);



  /**
   * Creates the lead reporter.
   *
   * @param representativeId the representative id
   * @param leadReporterVO the lead reporter VO
   * @param dataflowId the dataflow id
   * @return the long
   */
  @PostMapping("/{representativeId}/leadReporter/dataflow/{dataflowId}")
  Long createLeadReporter(@PathVariable("representativeId") final Long representativeId,
      @RequestBody LeadReporterVO leadReporterVO, @PathVariable("dataflowId") Long dataflowId);

  /**
   * Update lead reporter.
   *
   * @param leadReporterVO the lead reporter VO
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @PutMapping("/leadReporter/update/dataflow/{dataflowId}")
  Long updateLeadReporter(@RequestBody LeadReporterVO leadReporterVO,
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Delete lead reporter.
   *
   * @param leadReporterId the lead reporter id
   * @param dataflowId the dataflow id
   */
  @DeleteMapping("/leadReporter/{leadReporterId}/dataflow/{dataflowId}")
  void deleteLeadReporter(@PathVariable("leadReporterId") Long leadReporterId,
      @PathVariable("dataflowId") Long dataflowId);

  /**
   * Find fme users.
   *
   * @return the list
   */
  @GetMapping(value = "/fmeUsers", produces = MediaType.APPLICATION_JSON_VALUE)
  List<FMEUserVO> findFmeUsers();

  /**
   * Update internal representative.
   *
   * @param dataflowRepresentativeVO the dataflow representative VO
   * @return the response entity
   */
  @PutMapping("/private/update")
  Long updateInternalRepresentative(@RequestBody RepresentativeVO dataflowRepresentativeVO);

  /**
   * Find data providers by code.
   *
   * @param code the code
   * @return the list
   */
  @GetMapping("/private/dataProviderByCode/{code}")
  List<DataProviderVO> findDataProvidersByCode(@PathVariable("code") String code);

  /**
   * Find representatives by data flow id and provider id list.
   *
   * @param dataflowId the dataflow id
   * @param providerIdList the provider id list
   * @return the list
   */
  @GetMapping(value = "/private/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  List<RepresentativeVO> findRepresentativesByDataFlowIdAndProviderIdList(
      @PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerIdList") List<Long> providerIdList);


  /**
   * Find data provider group by id.
   *
   * @param groupId the group id
   * @return the data provider group VO
   */
  @GetMapping("/private/dataProviderGroup/{groupId}")
  DataProviderGroupVO findDataProviderGroupById(@PathVariable("groupId") Long groupId);

}
