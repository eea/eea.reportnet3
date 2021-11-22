
package org.eea.dataflow.controller;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.FMEUserVO;
import org.eea.interfaces.vo.dataflow.LeadReporterVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Class RepresentativeControllerImpl.
 */
@RestController
@RequestMapping(value = "/representative")
@Api(tags = "Representatives : Representatives Manager")
public class RepresentativeControllerImpl implements RepresentativeController {

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The Constant EMAIL_REGEX: {@value}. */
  private static final String EMAIL_REGEX =
      "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";

  /** The representative service. */
  @Autowired
  private RepresentativeService representativeService;

  /**
   * Creates the representative.
   *
   * @param dataflowId the dataflow id
   * @param representativeVO the representative VO
   * @return the long
   */
  @Override
  @HystrixCommand
  @PostMapping("/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD')")
  @ApiOperation(value = "Create one Representative", response = Long.class, hidden = true)
  public Long createRepresentative(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Representative Object") @RequestBody RepresentativeVO representativeVO) {

    try {
      return representativeService.createRepresentative(dataflowId, representativeVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating new representative: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Find all data provider by group id.
   *
   * @param groupId the group id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Find all DataProviders  by their Group Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataProviderVO.class,
      responseContainer = "List", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.REPRESENTATIVE_TYPE_INCORRECT)
  public List<DataProviderVO> findAllDataProviderByGroupId(
      @ApiParam(value = "Group id", example = "0") @PathVariable("groupId") Long groupId) {
    if (null == groupId) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.REPRESENTATIVE_TYPE_INCORRECT);
    }
    return representativeService.getAllDataProviderByGroupId(groupId);
  }

  /**
   * Find all data provider types.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/countryGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Find all DataProvider types", produces = MediaType.APPLICATION_JSON_VALUE,
      response = DataProviderVO.class, responseContainer = "List", hidden = true)
  public List<DataProviderCodeVO> findAllDataProviderCountryType() {
    return representativeService.getDataProviderGroupByType(TypeDataProviderEnum.COUNTRY);
  }

  /**
   * Find all data provider business types.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/companyGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(value = "Find all DataProvider business types",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataProviderVO.class,
      responseContainer = "List", hidden = true)
  public List<DataProviderCodeVO> findAllDataProviderCompanyType() {
    return representativeService.getDataProviderGroupByType(TypeDataProviderEnum.COMPANY);
  }

  /**
   * Find all data provider organization type.
   *
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/organizationGroups",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Find all DataProvider organization types",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataProviderVO.class,
      responseContainer = "List", hidden = true)
  public List<DataProviderCodeVO> findAllDataProviderOrganizationType() {
    return representativeService.getDataProviderGroupByType(TypeDataProviderEnum.ORGANIZATION);
  }

  /**
   * Find representatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/v1/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_OBSERVER','DATAFLOW_EDITOR_READ','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Get Representatives by Dataflow Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = RepresentativeVO.class,
      responseContainer = "List",
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR READ, EDITOR WRITE, OBSERVER, LEAD REPORTER, REPORTER WRITE")
  @ApiResponses(value = {@ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_NOTFOUND),
      @ApiResponse(code = 404, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)})
  public List<RepresentativeVO> findRepresentativesByIdDataFlow(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    if (dataflowId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.DATAFLOW_NOTFOUND);
    }
    List<RepresentativeVO> representativeVOs;
    try {
      representativeVOs = representativeService.getRepresetativesByIdDataFlow(dataflowId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
    return representativeVOs;
  }

  /**
   * Find representatives by id data flow legacy.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_OBSERVER','DATAFLOW_EDITOR_READ','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Get Representatives by Dataflow Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = RepresentativeVO.class,
      responseContainer = "List", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR READ, EDITOR WRITE, OBSERVER, LEAD REPORTER, REPORTER WRITE")
  @ApiResponses(value = {@ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_NOTFOUND),
      @ApiResponse(code = 404, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)})
  public List<RepresentativeVO> findRepresentativesByIdDataFlowLegacy(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    return this.findRepresentativesByIdDataFlow(dataflowId);
  }

  /**
   * Update representative.
   *
   * @param representativeVO the representative VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping("/update")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Update a representative", produces = MediaType.APPLICATION_JSON_VALUE,
      response = Long.class, hidden = true)
  public Long updateRepresentative(@ApiParam(value = "Representative object",
      type = "Object") @RequestBody RepresentativeVO representativeVO) {

    // Authorization
    if (!representativeService.authorizeByRepresentativeId(representativeVO.getId())) {
      LOG_ERROR.error("Representative not allowed: representativeVO={}", representativeVO);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    return representativeService.updateDataflowRepresentative(representativeVO);
  }

  /**
   * Delete representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{dataflowRepresentativeId}/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @ApiOperation(value = "Delete Representative", hidden = true)
  @ApiResponse(code = 404, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)
  public void deleteRepresentative(
      @ApiParam(value = "Dataflow Representative id",
          example = "0") @PathVariable("dataflowRepresentativeId") Long dataflowRepresentativeId,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      representativeService.deleteDataflowRepresentative(dataflowRepresentativeId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
  }

  /**
   * Find data provider by id.
   *
   * @param dataProviderId the data provider id
   * @return the data provider VO
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider/id/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Find a DataProvider based on its Id", response = DataProviderVO.class,
      hidden = true)
  @ApiResponse(code = 404, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)
  public DataProviderVO findDataProviderById(
      @ApiParam(value = "Dataprovider id", example = "0") @PathVariable("id") Long dataProviderId) {
    if (null == dataProviderId) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    }
    return representativeService.getDataProviderById(dataProviderId);
  }

  /**
   * Find data providers by ids.
   *
   * @param dataProviderIds the data provider ids
   * @return the list
   */
  @Override
  @GetMapping("/private/dataProvider")
  @ApiOperation(value = "Find DataProviders based on a list of Id's",
      response = DataProviderVO.class, responseContainer = "List", hidden = true)
  public List<DataProviderVO> findDataProvidersByIds(@ApiParam(value = "Dataproviders List",
      type = "Long List") @RequestParam("id") List<Long> dataProviderIds) {
    return representativeService.findDataProvidersByIds(dataProviderIds);
  }


  /**
   * Export file of lead reporters.
   *
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @GetMapping(value = "/export/{dataflowId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Exports the file containing the info of Lead reporters",
      response = ResponseEntity.class, hidden = true)
  @ApiResponse(code = 500, message = "Internal server error exporting the file")
  public ResponseEntity<byte[]> exportLeadReportersFile(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      byte[] file = representativeService.exportFile(dataflowId);
      String fileName = "Dataflow-" + dataflowId + "-Lead-Reporters.csv";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Internal server error: {}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Export template reporters file.
   *
   * @param groupId the group id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("isAuthenticated()")
  @GetMapping(value = "/exportTemplateReportersFile/{groupId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Exports the template file for lead reporters",
      response = ResponseEntity.class, hidden = true)
  @ApiResponse(code = 500, message = "Internal server error exporting the template file")
  public ResponseEntity<byte[]> exportTemplateReportersFile(
      @ApiParam(value = "Group Id", example = "0") @PathVariable("groupId") Long groupId) {

    try {
      byte[] file = representativeService.exportTemplateReportersFile(groupId);
      String fileName = "CountryCodes-Lead-Reporters.csv";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("Internal server error: {}", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }


  /**
   * Import file country template.With that controller we can download a country template to import
   * data with the countrys with this group id
   *
   * @param dataflowId the dataflow id
   * @param groupId the group id
   * @param file the file
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @ApiOperation(value = "Import file lead reporters", hidden = true)
  @PostMapping("/import/{dataflowId}/group/{groupId}")
  @ApiResponses(value = {@ApiResponse(code = 400, message = EEAErrorMessage.FILE_EXTENSION),
      @ApiResponse(code = 400, message = EEAErrorMessage.CSV_FILE_ERROR),
      @ApiResponse(code = 400, message = "Error importing file")})
  public ResponseEntity<byte[]> importFileCountryTemplate(
      @ApiParam(value = "Dataflow Id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId,
      @ApiParam(value = "Group Id", example = "0") @PathVariable(value = "groupId") Long groupId,
      @ApiParam(value = "File to be imported") @RequestParam("file") MultipartFile file) {
    // we check if the field is a csv
    final int location = file.getOriginalFilename().lastIndexOf('.');
    if (location == -1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_EXTENSION);
    }
    String mimeType = file.getOriginalFilename().substring(location + 1);
    if (!FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CSV_FILE_ERROR);
    }

    try {
      byte[] fileEnded = representativeService.importFile(dataflowId, groupId, file);
      String fileName = "Dataflow-" + dataflowId + "-Lead-Reporters-Errors-improt.csv";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      return new ResponseEntity<>(fileEnded, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("File import failed lead reporters in dataflow={}, fileName={}", dataflowId,
          file.getName());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error importing file", e);
    }
  }

  /**
   * Update representative visibility restrictions.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   */
  @Override
  @PostMapping("/private/updateRepresentativeVisibilityRestrictions")
  @ApiOperation(value = "Update representative visibility", hidden = true)
  public void updateRepresentativeVisibilityRestrictions(
      @ApiParam(value = "Dataflow Id", example = "0",
          required = true) @RequestParam(value = "dataflowId", required = true) Long dataflowId,
      @ApiParam(value = "Dataprovider Id", required = true) @RequestParam(value = "dataProviderId",
          required = true) Long dataProviderId,
      @ApiParam(value = "Should the representative be restricted to public view?", required = true,
          defaultValue = "false") @RequestParam(value = "restrictFromPublic", required = true,
              defaultValue = "false") boolean restrictFromPublic) {
    representativeService.updateRepresentativeVisibilityRestrictions(dataflowId, dataProviderId,
        restrictFromPublic);
  }

  /**
   * Creates the lead reporter.
   *
   * @param representativeId the representative id
   * @param leadReporterVO the lead reporter VO
   * @param dataflowId the dataflow id
   * @return the created lead reporter id
   */
  @Override
  @HystrixCommand
  @PostMapping("/{representativeId}/leadReporter/dataflow/{dataflowId}")
  @LockMethod
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @ApiOperation(value = "Create one Lead reporter", response = Long.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 400, message = EEAErrorMessage.USER_NOTFOUND),
      @ApiResponse(code = 400, message = EEAErrorMessage.NOT_EMAIL),
      @ApiResponse(code = 400, message = "Error creating new lead reporter")})
  public Long createLeadReporter(
      @ApiParam(value = "Representative id", example = "0") @LockCriteria(
          name = "representativeId") @PathVariable("representativeId") Long representativeId,
      @ApiParam(type = "Object",
          value = "Lead reporter Object") @RequestBody LeadReporterVO leadReporterVO,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {

    if (null == leadReporterVO || null == leadReporterVO.getEmail()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.USER_NOTFOUND);
    }
    Pattern p = Pattern.compile(EMAIL_REGEX);
    Matcher m = p.matcher(leadReporterVO.getEmail().toLowerCase());
    boolean result = m.matches();
    if (Boolean.FALSE.equals(result)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(EEAErrorMessage.NOT_EMAIL, leadReporterVO.getEmail()));
    }
    try {
      return representativeService.createLeadReporter(representativeId, leadReporterVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error creating new lead reporter: {}", e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Update lead reporter.
   *
   * @param leadReporterVO the lead reporter VO
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping("/leadReporter/update/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @ApiOperation(value = "Updates a Lead reporter", response = Long.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 403, message = "LeadReporter not allowed"),
      @ApiResponse(code = 400, message = "Invalid email"),
      @ApiResponse(code = 404, message = "Representative not found")})
  public Long updateLeadReporter(@RequestBody LeadReporterVO leadReporterVO,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {

    // Authorization
    if (!representativeService.authorizeByRepresentativeId(leadReporterVO.getRepresentativeId())) {
      LOG_ERROR.error("LeadReporter not allowed: leadReporterVO={}", leadReporterVO);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    // Validate email
    if (null == leadReporterVO.getEmail() || !leadReporterVO.getEmail().matches(EMAIL_REGEX)) {
      LOG_ERROR.error("Error updating lead reporter: invalid email. leadReporterVO={}",
          leadReporterVO);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
    }
    try {
      return representativeService.updateLeadReporter(leadReporterVO);
    } catch (EEAException e) {
      LOG_ERROR.error("Error updating lead reporter: duplicated representative. leadReporterVO={}",
          leadReporterVO);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found");
    }
  }

  /**
   * Delete lead reporter.
   *
   * @param leadReporterId the lead reporter id
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @DeleteMapping("/leadReporter/{leadReporterId}/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN')")
  @ApiOperation(value = "Deletes a Lead reporter", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)
  public void deleteLeadReporter(@PathVariable("leadReporterId") Long leadReporterId,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      representativeService.deleteLeadReporter(leadReporterId);
    } catch (EEAException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND, e);
    }
  }

  /**
   * Find fme users.
   *
   * @return the list
   */
  @Override
  @GetMapping(value = "/fmeUsers", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ApiOperation(value = "Find all the fme users for business dataflow",
      response = RepresentativeControllerZuul.class, responseContainer = "List", hidden = true)
  public List<FMEUserVO> findFmeUsers() {
    return representativeService.findFmeUsers();
  }

  /**
   * Update internal representative.
   *
   * @param representativeVO the representative VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping("/private/update")
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Updates the internal representative", response = Long.class, hidden = true)
  public Long updateInternalRepresentative(@ApiParam(
      value = "Representative VO object") @RequestBody RepresentativeVO representativeVO) {

    return representativeService.updateDataflowRepresentative(representativeVO);
  }

  /**
   * Find data providers by code.
   *
   * @param code the code
   * @return the list
   */
  @Override
  @GetMapping("/private/dataProviderByCode/{code}")
  @ApiOperation(value = "Find DataProviders based on a code", response = DataProviderVO.class,
      responseContainer = "List", hidden = true)
  public List<DataProviderVO> findDataProvidersByCode(
      @ApiParam(value = "Code", example = "EL") @PathVariable("code") String code) {
    return representativeService.findDataProvidersByCode(code);
  }

  /**
   * Find representatives by id data flow and provider id list.
   *
   * @param dataflowId the dataflow id
   * @param providerIdList the provider id list
   * @return the list
   */
  @Override
  @GetMapping(value = "/private/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find Representatives based on a dataflowId and providerId List",
      response = RepresentativeControllerZuul.class, responseContainer = "List", hidden = true)
  public List<RepresentativeVO> findRepresentativesByDataFlowIdAndProviderIdList(
      @ApiParam(value = "Dataflow Id", example = "1") @PathVariable("dataflowId") Long dataflowId,
      @RequestParam("providerIdList") List<Long> providerIdList) {
    return representativeService.findRepresentativesByDataflowIdAndDataproviderList(dataflowId,
        providerIdList);
  }


  /**
   * Update restrict from public.
   *
   * @param dataflowId the dataflow id
   * @param dataProviderId the data provider id
   * @param restrictFromPublic the restrict from public
   */
  @Override
  @PutMapping(
      value = "/update/restrictFromPublic/dataflow/{dataflowId}/dataProvider/{dataProviderId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_LEAD_REPORTER')")
  @ApiOperation(value = "Update representative visibility", hidden = true)
  public void updateRestrictFromPublic(
      @ApiParam(value = "Dataflow Id", example = "0",
          required = true) @PathVariable(value = "dataflowId", required = true) Long dataflowId,
      @ApiParam(value = "Dataprovider Id", required = true) @PathVariable(value = "dataProviderId",
          required = true) Long dataProviderId,
      @ApiParam(value = "Should the representative be restricted to public view?", required = true,
          defaultValue = "false") @RequestParam(value = "restrictFromPublic", required = true,
              defaultValue = "false") Boolean restrictFromPublic) {
    representativeService.updateRepresentativeVisibilityRestrictions(dataflowId, dataProviderId,
        restrictFromPublic);
  }

}
