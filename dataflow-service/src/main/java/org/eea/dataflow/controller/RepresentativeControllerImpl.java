
package org.eea.dataflow.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.vo.dataflow.*;
import org.eea.interfaces.vo.dataflow.enums.TypeDataProviderEnum;
import org.eea.interfaces.vo.dataset.enums.FileTypeEnum;
import org.eea.lock.annotation.LockCriteria;
import org.eea.lock.annotation.LockMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  /** The Constant ATTACHMENT_FILENAME: {@value}. */
  private static final String ATTACHMENT_FILENAME = "attachment; filename=";

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(RepresentativeControllerImpl.class);

  /** The Constant EMAIL_REGEX: {@value}. */
  private static final String EMAIL_REGEX =
      "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"; // NOSONAR

  /** The valid columns. */
  List<String> validColumns = Arrays.asList("id", "code", "group_id", "label");


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
  @LockMethod
  @HystrixCommand
  @PostMapping("/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD','DATAFLOW_STEWARD_SUPPORT')")
  @ApiOperation(value = "Create one Representative", response = Long.class, hidden = true)
  public Long createRepresentative(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Representative Object") @RequestBody RepresentativeVO representativeVO) {

    try {
      return representativeService.createRepresentative(dataflowId, representativeVO);
    } catch (EEAException e) {
      LOG.error("Error creating new representative for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CREATING_REPRESENTATIVE);
    } catch(Exception e){
      Long representativeId = (representativeVO != null) ? representativeVO.getId() : null;
      Long dataProviderId = (representativeVO != null) ? representativeVO.getDataProviderId() : null;
      LOG.error("Unexpected error! Could not create representative with id {} and dataProviderId {} for dataflowId {} Message: {}", representativeId, dataProviderId, dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Creates a provider.
   *
   * @param dataProviderVO the provider to be created
   */
  @Override
  @LockMethod
  @HystrixCommand
  @PostMapping("/provider/create")
  @PreAuthorize("hasAnyRole('ADMIN','DATA_CUSTODIAN')")
  @ApiOperation(value = "Creates a single provider", response = DataProviderVO.class, hidden = true)
  public void createProvider(
      @ApiParam(type = "Object", value = "Data Provider Object") @RequestBody DataProviderVO dataProviderVO) throws Exception {

    try {
      representativeService.createProvider(dataProviderVO);
    } catch (EEAException e) {
      LOG.error("Error creating new data provider {} Message: {}", dataProviderVO, e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CREATING_PROVIDER);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not create data provider {} Message: {}", dataProviderVO, e.getMessage());
      throw e;
    }
  }

  /**
   * Updates a provider.
   *
   * @param dataProviderVO the provider to be created
   */
  @Override
  @LockMethod
  @HystrixCommand
  @PutMapping("/provider/update")
  @PreAuthorize("hasAnyRole('ADMIN','DATA_CUSTODIAN')")
  @ApiOperation(value = "Updates a single provider", response = DataProviderVO.class, hidden = true)
  public void updateProvider(
      @ApiParam(type = "Object", value = "Data Provider Object") @RequestBody DataProviderVO dataProviderVO) throws Exception {

    if (dataProviderVO == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UPDATING_PROVIDER_NULL_OBJECT);
    }
    if (dataProviderVO.getId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UPDATING_PROVIDER_NULL_ID);
    }

    try {
      representativeService.updateProvider(dataProviderVO);
    } catch (EEAException e) {
      LOG.error("Error creating new data provider {} Message: {}", dataProviderVO, e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.UPDATING_PROVIDER);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not update data provider {} Message: {}", dataProviderVO, e.getMessage());
      throw e;
    }
  }

  /**
   * Find all data providers
   * @param pageNum
   * @param pageSize
   * @param asc
   * @param sortedColumn
   * @param providerCode
   * @param groupId
   * @param label
   * @return the data providers vo object
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProvider", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Find all DataProviders  by their Group Id",
          produces = MediaType.APPLICATION_JSON_VALUE, response = DataProviderVO.class,
          responseContainer = "List", hidden = true)
  public DataProvidersVO findAllDataProviders(@RequestParam(value = "pageNum", defaultValue = "0", required = false) Integer pageNum,
                                              @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
                                              @RequestParam(value = "asc", defaultValue = "true") boolean asc,
                                              @RequestParam(value = "sortedColumn", defaultValue = "code") String sortedColumn,
                                              @RequestParam(value = "providerCode", required = false) String providerCode,
                                              @RequestParam(value = "groupId", required = false) Integer groupId,
                                              @RequestParam(value = "label", required = false) String label) {
    try {

      Pageable pageable = PageRequest.of(pageNum, pageSize);
      if (!validColumns.contains(sortedColumn)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong sorting header provided.");
      }
      return representativeService.getAllDataProviders(pageable, asc, sortedColumn, providerCode, groupId, label);
    } catch (Exception e){
      LOG.error("Unexpected error! Could not retrieve all providers");
      throw e;
    }

  }

  /**
   * Find all data providers
   * @param pageNum
   * @param pageSize
   * @param asc
   * @param sortedColumn
   * @param providerCode
   * @param groupId
   * @param label
   * @return the data providers vo object
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataProviderGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @ApiOperation(value = "Find all DataProviders  by their Group Id",
          produces = MediaType.APPLICATION_JSON_VALUE, response = DataProviderVO.class,
          responseContainer = "List", hidden = true)
  public List<DataProviderCodeVO> findAllDataProviderGroups() {
    try {
      return representativeService.getAllDataProviderGroups();
    } catch (Exception e){
      LOG.error("Unexpected error! Could not retrieve all data group providers");
      throw e;
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
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_EDITOR_READ','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Get dataflow representatives by dataflow id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = RepresentativeVO.class,
      responseContainer = "List",
      notes = "Allowed roles: CUSTODIAN, STEWARD, EDITOR READ, EDITOR WRITE, OBSERVER, LEAD REPORTER, REPORTER WRITE, STEWARD SUPPORT")
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
      LOG.error("Error retrieving representatives for dataflowId {} Message: {}", dataflowId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not find representatives for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
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
  @PreAuthorize("secondLevelAuthorizeWithApiKey(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_OBSERVER','DATAFLOW_STEWARD_SUPPORT','DATAFLOW_EDITOR_READ','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE')")
  @ApiOperation(value = "Get Representatives by Dataflow Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = RepresentativeVO.class,
      responseContainer = "List", hidden = true)
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
      LOG.error("Representative not allowed: representativeVO={}", representativeVO);
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
  @ApiOperation(value = "Delete Representative", hidden = true)
  @ApiResponse(code = 404, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)
  public void deleteRepresentative(
      @ApiParam(value = "Dataflow Representative id",
          example = "0") @PathVariable("dataflowRepresentativeId") Long dataflowRepresentativeId,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      LOG.info("Deleting dataflow representative for dataflowId {} and dataflowRepresentativeId {}", dataflowId, dataflowRepresentativeId);
      representativeService.deleteDataflowRepresentative(dataflowRepresentativeId);
      LOG.info("Successfully deleted dataflow representative for dataflowId {} and dataflowRepresentativeId {}", dataflowId, dataflowRepresentativeId);
    } catch (EEAException e) {
      LOG.error("Error deleting representative for dataflowRepresentativeId {} and dataflowId {} Message: {}", dataflowRepresentativeId, dataflowId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not delete representative with id {} for dataflowId {} Message: {}", dataflowRepresentativeId, dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Delete all representatives for a dataflow.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
  @ApiOperation(value = "Delete all Representatives for a dataflow", hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 404, message = EEAErrorMessage.DATAFLOW_NOTFOUND),
          @ApiResponse(code = 403, message = EEAErrorMessage.NOT_DESIGN_DATAFLOW)})
  public void deleteRepresentatives(@ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      LOG.info("Deleting dataflow representatives for dataflowId {}", dataflowId);
      representativeService.deleteDataflowRepresentatives(dataflowId);
      LOG.info("Successfully deleted dataflow representatives for dataflowId {}", dataflowId);
    } catch (EEAException e) {
      LOG.error("Error deleting representatives for dataflowId {} Message: {}", dataflowId, e.getMessage(), e);
      if (e.getMessage() != null && e.getMessage().equals(EEAErrorMessage.NOT_DESIGN_DATAFLOW)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, EEAErrorMessage.NOT_DESIGN_DATAFLOW);
      } else {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, EEAErrorMessage.DATAFLOW_NOTFOUND);
      }
    } catch (Exception e) {
      LOG.error("Unexpected error! Could not delete representatives for dataflowId {} Message: {}",
          dataflowId, e.getMessage());
      throw e;
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
   * Export file of lead reporters.
   *
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
  @GetMapping(value = "/export/{dataflowId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Exports the file containing the info of Lead reporters",
      response = ResponseEntity.class, hidden = true)
  @ApiResponse(code = 500, message = "Internal server error exporting the file")
  public ResponseEntity<byte[]> exportLeadReportersFile(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      LOG.info("Exporting lead reporters file for dataflowId {}", dataflowId);
      byte[] file = representativeService.exportFile(dataflowId);
      String fileName = "Dataflow-" + dataflowId + "-Lead-Reporters.csv";
      LOG.info("Successfully exported lead reporters file {} for dataflowId {}", fileName, dataflowId);
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME + fileName);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG.error("Error while exporting lead reporters for dataflowId {} Message: {}", dataflowId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXPORT_LEAD_REPORTERS);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not export lead reporters file for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
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
      LOG.info("Exporting template reporters file for groupId {}", groupId);
      byte[] file = representativeService.exportTemplateReportersFile(groupId);
      String fileName = "CountryCodes-Lead-Reporters.csv";
      LOG.info("Successfully exported template reporters file {} for groupId {}", fileName, groupId);
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME + fileName);
      return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG.error("Error exporting lead reporters template for groupId {} ", groupId, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          EEAErrorMessage.EXPORT_LEAD_REPORTERS);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not export the template file for lead reporters for groupId {} Message: {}", groupId, e.getMessage());
      throw e;
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
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
    if (file == null || file.getOriginalFilename() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_NOT_FOUND);
    }
    String originalFileName = file.getOriginalFilename();
    final int location =
        StringUtils.isNotBlank(originalFileName) ? originalFileName.lastIndexOf('.') : -1;
    if (location == -1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_EXTENSION);
    }

    String mimeType =
        StringUtils.isNotBlank(originalFileName) ? originalFileName.substring(location + 1) : "";

    if (!FileTypeEnum.CSV.getValue().equalsIgnoreCase(mimeType) || mimeType.equals("")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CSV_FILE_ERROR);
    }

    try {
      LOG.info("Importing file country template for dataflowId {} and groupId {}", dataflowId, groupId);
      byte[] fileEnded = representativeService.importLeadReportersFile(dataflowId, groupId, file);
      String fileName = "Dataflow-" + dataflowId + "-Lead-Reporters-Errors-improt.csv";
      LOG.info("Importing file country template {} for dataflowId {} and groupId {}", fileName, dataflowId, groupId);
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_FILENAME + fileName);
      return new ResponseEntity<>(fileEnded, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG.error("File import failed lead reporters in dataflow={}, fileName={}. Message: ", dataflowId,
          file.getName(), e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.IMPORT_LEAD_REPORTERS);
    } catch(Exception e){
      String fileName = (file != null) ? file.getName() : null;
      LOG.error("Unexpected error! Could not import the country template file {} for groupId {} and dataflowId {} Message: {}", fileName, groupId, dataflowId, e.getMessage());
      throw e;
    }
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
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
    leadReporterVO.setEmail(leadReporterVO.getEmail().toLowerCase());
    Pattern p = Pattern.compile(EMAIL_REGEX);
    Matcher m = p.matcher(leadReporterVO.getEmail().toLowerCase());
    boolean result = m.matches();
    if (Boolean.FALSE.equals(result)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(EEAErrorMessage.NOT_EMAIL, leadReporterVO.getEmail()));
    }
    try {
      LOG.info("Creating lead reporter for dataflowId {} and representativeId {}", dataflowId, representativeId);
      Long reporterId = representativeService.createLeadReporter(representativeId, leadReporterVO);
      LOG.info("Successfully created lead reporter for dataflowId {} and representativeId {}", dataflowId, representativeId);
      return reporterId;
    } catch (EEAException e) {
      LOG.error("Error creating new lead reporter for dataflowId {} and representativeId {} Message: {}", dataflowId, representativeId, e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.CREATE_LEAD_REPORTER);
    } catch(Exception e){
      Long reporterId = (leadReporterVO != null) ? leadReporterVO.getId() : null;
      LOG.error("Unexpected error! Could not create lead reporter with representativeId {} and reporterId {} for dataflowId {} Message: {}", representativeId, reporterId, dataflowId, e.getMessage());
      throw e;
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
  @ApiOperation(value = "Updates a Lead reporter", response = Long.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 403, message = "LeadReporter not allowed"),
      @ApiResponse(code = 400, message = "Invalid email"),
      @ApiResponse(code = 404, message = "Representative not found")})
  public Long updateLeadReporter(@RequestBody LeadReporterVO leadReporterVO,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {

    // Authorization
    if (!representativeService.authorizeByRepresentativeId(leadReporterVO.getRepresentativeId())) {
      LOG.error("LeadReporter not allowed: leadReporterVO={}", leadReporterVO);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    if (null != leadReporterVO.getEmail()) {
      leadReporterVO.setEmail(leadReporterVO.getEmail().toLowerCase());
    }

    // Validate email
    if (null == leadReporterVO.getEmail() || !leadReporterVO.getEmail().matches(EMAIL_REGEX)) {
      LOG.error("Error updating lead reporter: invalid email. leadReporterVO={}",
          leadReporterVO);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email");
    }
    try {
      LOG.info("Updating lead reporter for dataflowId {}", dataflowId);
      Long reporterId = representativeService.updateLeadReporter(leadReporterVO);
      LOG.info("Successfully updated lead reporter for dataflowId {}", dataflowId);
      return reporterId;
    } catch (EEAException e) {
      LOG.error("Error updating lead reporter: duplicated representative. leadReporterVO={}",
          leadReporterVO, e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found");
    } catch(Exception e){
      Long representativeId = (leadReporterVO != null) ? leadReporterVO.getRepresentativeId() : null;
      Long reporterId = (leadReporterVO != null) ? leadReporterVO.getId() : null;
      LOG.error("Unexpected error! Could not update lead reporter with representativeId {} and reporterId {} for dataflowId {} Message: {}", representativeId, reporterId, dataflowId, e.getMessage());
      throw e;
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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
  @ApiOperation(value = "Deletes a Lead reporter", hidden = true)
  @ApiResponse(code = 400, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)
  public void deleteLeadReporter(@PathVariable("leadReporterId") Long leadReporterId,
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    try {
      LOG.info("Deleting lead reporter {} for dataflowId {}", leadReporterId, dataflowId);
      representativeService.deleteLeadReporter(leadReporterId);
      LOG.info("Successfully deleted lead reporter {} for dataflowId {}", leadReporterId, dataflowId);
    } catch (EEAException e) {
      LOG.error("Error deleting lead reporter: leadReporterId ={}", leadReporterId, e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          EEAErrorMessage.REPRESENTATIVE_NOT_FOUND);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not delete lead reporter with reporterId {} for dataflowId {} Message: {}", leadReporterId, dataflowId, e.getMessage());
      throw e;
    }
  }

  /**
   * Validate lead reporters checking if they are already registered in the system.
   *
   * @param dataflowId the dataflow id
   */
  @Override
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_STEWARD_SUPPORT')")
  @ApiOperation(
      value = "Validates all lead reporters, checking wether they are registered in the system or not",
      hidden = true)
  @ApiResponse(code = 400, message = "Could not validate lead reporters in the requested dataflow.")
  public void validateLeadReporters(@ApiParam(value = "Dataflow ID", required = true,
      example = "1") @PathVariable("dataflowId") Long dataflowId) {
    try {
      LOG.info("Validating lead reporters for dataflowId {}", dataflowId);
      representativeService.validateLeadReporters(dataflowId, true);
      LOG.info("Successfully validated lead reporters for dataflowId {}", dataflowId);
    } catch (EEAException e) {
      LOG.error("Error validating lead reporters: dataflowId {}", dataflowId, e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          EEAErrorMessage.ERROR_VALIDATING_LEAD_REPORTERS);
    } catch(Exception e){
      LOG.error("Unexpected error! Could not validate lead reporters for dataflowId {} Message: {}", dataflowId, e.getMessage());
      throw e;
    }
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
              defaultValue = "true") Boolean restrictFromPublic) {
    try {
      if (representativeService.checkDataHaveBeenRelease(dataflowId, dataProviderId)
          && representativeService.checkRestrictFromPublic(dataflowId, dataProviderId)) {
        LOG.info("Updating representative visibility restrictions for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
        representativeService.updateRepresentativeVisibilityRestrictions(dataflowId, dataProviderId,
            restrictFromPublic);
        LOG.info("Successfully updated representative visibility restrictions for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
      } else {
        LOG.error(
            "Error, you can't change the restrict from public value for the representative with dataflowId {} and dataProviderId {}",
            dataflowId, dataProviderId);
      }
    } catch (EEAException e) {
      LOG.info("Error: {}", e.getMessage());
    } catch(Exception e){
      LOG.error("Unexpected error! Could not update representative visibility for dataProviderId {} and dataflowId {} Message: {}", dataProviderId, dataflowId, e.getMessage());
      throw e;
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
    if(representativeVO != null){
      LOG.info("Updating internal representative with id {}", representativeVO.getId());
    }
    else{
      LOG.info("Updating internal representative but object is null");
    }

    Long representativeId = representativeService.updateDataflowRepresentative(representativeVO);
    LOG.info("Successfully updated internal representative with id {}", representativeVO.getId());
    return representativeId;
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
    LOG.info("Updating representative visibility restrictions for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
    representativeService.updateRepresentativeVisibilityRestrictions(dataflowId, dataProviderId,
        restrictFromPublic);
    LOG.info("Successfully updated representative visibility restrictions for dataflowId {} and dataProviderId {}", dataflowId, dataProviderId);
  }

  @Override
  @GetMapping(value = "/private/code/{code}/group/{groupId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public DataProviderVO findDataProviderByCodeAndGroupId(
          @ApiParam(value = "Code", example = "AT") @PathVariable("code") String code,
          @ApiParam(value = "Group id", example = "0") @PathVariable("groupId") Long groupId) {
    try{
       return representativeService.findDataProviderByCodeAndGroupId(code, groupId);
    }
    catch (Exception e){
      LOG.error("Could not find provider with code {} and group {} Error: {}", code, groupId, e.getMessage());
      throw e;
    }
  }

}
