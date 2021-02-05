
package org.eea.dataflow.controller;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eea.dataflow.service.RepresentativeService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.RepresentativeController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.dataflow.DataProviderCodeVO;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataflow.RepresentativeVO;
import org.eea.interfaces.vo.ums.UserRepresentationVO;
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

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;


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
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN')")
  @ApiOperation(value = "Create one Representative", response = Long.class)
  @ApiResponse(code = 400, message = "Email field provider is not an email")
  public Long createRepresentative(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId,
      @ApiParam(type = "Object",
          value = "Representative Object") @RequestBody RepresentativeVO representativeVO) {

    if (null == representativeVO.getProviderAccount()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.USER_NOTFOUND);
    }
    Pattern p = Pattern.compile(EMAIL_REGEX);
    Matcher m = p.matcher(representativeVO.getProviderAccount());
    boolean result = m.matches();
    if (Boolean.FALSE.equals(result)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(EEAErrorMessage.NOT_EMAIL, representativeVO.getProviderAccount()));
    }

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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @ApiOperation(value = "Find all DataProviders  by their Group Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = DataProviderVO.class,
      responseContainer = "List")
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
  @GetMapping(value = "/dataProvider/types", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @ApiOperation(value = "Find all DataProvider types", produces = MediaType.APPLICATION_JSON_VALUE,
      response = DataProviderVO.class, responseContainer = "List")
  public List<DataProviderCodeVO> findAllDataProviderTypes() {
    return representativeService.getAllDataProviderTypes();
  }

  /**
   * Find representatives by id data flow.
   *
   * @param dataflowId the dataflow id
   * @return the list
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER') OR secondLevelAuthorize(#dataflowId,'DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ')")
  @ApiOperation(value = "Find Representatives by Dataflow Id",
      produces = MediaType.APPLICATION_JSON_VALUE, response = RepresentativeVO.class,
      responseContainer = "List")
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
   * Update representative.
   *
   * @param representativeVO the representative VO
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('DATA_CUSTODIAN') OR hasRole('LEAD_REPORTER')")
  @ApiOperation(value = "Update a Representative", produces = MediaType.APPLICATION_JSON_VALUE,
      response = ResponseEntity.class)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Email field provider is not an email"),
      @ApiResponse(code = 404, message = "1-Representative not found \n 2-User request not found "),
      @ApiResponse(code = 409, message = EEAErrorMessage.REPRESENTATIVE_DUPLICATED)})
  public ResponseEntity updateRepresentative(@ApiParam(value = "RepresentativeVO Object",
      type = "Object") @RequestBody RepresentativeVO representativeVO) {
    String message = null;
    HttpStatus status = HttpStatus.OK;

    if (null != representativeVO.getProviderAccount()) {
      Pattern p = Pattern.compile(EMAIL_REGEX);
      Matcher m = p.matcher(representativeVO.getProviderAccount());
      boolean result = m.matches();
      if (Boolean.FALSE.equals(result)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format(EEAErrorMessage.NOT_EMAIL, representativeVO.getProviderAccount()));
      }
    }

    if (representativeVO.getProviderAccount() != null) {
      List<UserRepresentationVO> users = userManagementControllerZull.getUsers();
      UserRepresentationVO userRepresentationVO = users.stream()
          .filter(user -> representativeVO.getProviderAccount().equalsIgnoreCase(user.getEmail()))
          .findFirst().orElse(null);
      if (userRepresentationVO == null) {
        message = EEAErrorMessage.USER_REQUEST_NOTFOUND;
        status = HttpStatus.NOT_FOUND;
      }
    }
    try {
      message = message == null
          ? String.valueOf(representativeService.updateDataflowRepresentative(representativeVO))
          : message;
    } catch (EEAException e) {
      if (EEAErrorMessage.REPRESENTATIVE_DUPLICATED.equals(e.getMessage())) {
        LOG_ERROR.error("Duplicated representative relationship", e.getCause());
        message = EEAErrorMessage.REPRESENTATIVE_DUPLICATED;
        status = HttpStatus.CONFLICT;
      } else if (EEAErrorMessage.USER_AND_COUNTRY_EXIST.equals(e.getMessage())) {
        LOG_ERROR.error("Duplicated user and country relationship", e.getCause());
        message = EEAErrorMessage.USER_AND_COUNTRY_EXIST;
        status = HttpStatus.CONFLICT;
      } else {
        LOG_ERROR.error("Bad Request", e.getCause());
        message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND;
        status = HttpStatus.BAD_REQUEST;
      }
    }
    return new ResponseEntity<>(message, status);
  }

  /**
   * Delete representative.
   *
   * @param dataflowRepresentativeId the dataflow representative id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{dataflowRepresentativeId}")
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @ApiOperation(value = "Delete Representative")
  @ApiResponse(code = 404, message = EEAErrorMessage.REPRESENTATIVE_NOT_FOUND)
  public void deleteRepresentative(@ApiParam(value = "Dataflow Representative id",
      example = "0") @PathVariable("dataflowRepresentativeId") Long dataflowRepresentativeId) {
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
  @ApiOperation(value = "Find a DataProvider based on its Id")
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
   * Export file.
   *
   * @param dataflowId the dataflow id
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @GetMapping(value = "/export/{dataflowId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Export file lead reporters")
  public ResponseEntity<byte[]> exportFile(
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
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @GetMapping(value = "/exportTemplateReportersFile/{groupId}",
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "Export template file lead reporters")
  public ResponseEntity<byte[]> exportTemplateReportersFile(@PathVariable("groupId") Long groupId) {

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
   * Import file data.
   *
   * @param dataflowId the dataflow id
   * @param groupId the group id
   * @param file the file
   * @return the response entity
   */
  @Override
  @HystrixCommand
  @PreAuthorize("hasRole('DATA_CUSTODIAN')")
  @ApiOperation(value = "Import file lead reporters")
  @PostMapping("/import/{dataflowId}/group/{groupId}")
  public ResponseEntity<byte[]> importFileData(@PathVariable(value = "dataflowId") Long dataflowId,
      @PathVariable(value = "groupId") Long groupId, @RequestParam("file") MultipartFile file) {
    System.err.println(System.currentTimeMillis());
    // we check if the field is a csv
    final int location = file.getOriginalFilename().lastIndexOf('.');
    if (location == -1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.FILE_EXTENSION);
    }
    String mimeType = file.getOriginalFilename().substring(location + 1);
    if (!"csv".equalsIgnoreCase(mimeType)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EEAErrorMessage.CSV_FIELD_ERROR);
    }

    try {
      byte[] fileEnded = representativeService.importFile(dataflowId, groupId, file);
      String fileName = "Dataflow-" + dataflowId + "-Lead-Reporters-Errors-improt.csv";
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
      System.err.println(System.currentTimeMillis());
      return new ResponseEntity<>(fileEnded, httpHeaders, HttpStatus.OK);
    } catch (EEAException | IOException e) {
      LOG_ERROR.error("File import failed lead reporters in dataflow={}, fileName={}", dataflowId,
          file.getName());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error importing file", e);
    }
  }


}
