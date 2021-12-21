package org.eea.dataflow.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataflow.exception.EntityNotFoundException;
import org.eea.dataflow.exception.ResourceNoFoundException;
import org.eea.dataflow.exception.WrongDataExceptions;
import org.eea.dataflow.service.DataflowWebLinkService;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowWebLinkController;
import org.eea.interfaces.vo.weblink.WeblinkVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * The Class DataflowWebLinkControllerImpl.
 */
@RestController
@RequestMapping(value = "/weblink")
@Api(tags = "Weblinks : Weblinks Manager")
public class DataflowWebLinkControllerImpl implements DataFlowWebLinkController {


  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * The dataflow web link service.
   */
  @Autowired
  private DataflowWebLinkService dataflowWebLinkService;

  /**
   * Gets the link.
   *
   * @param idLink the id link
   *
   * @return the link
   */
  @Override
  @HystrixCommand
  @GetMapping(value = "/private/{idLink}")
  @ApiOperation(value = "Get a weblink by weblink id", response = WeblinkVO.class, hidden = true)
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public WeblinkVO getLink(
      @ApiParam(value = "Weblink id", example = "0") @PathVariable("idLink") Long idLink) {

    try {
      return dataflowWebLinkService.getWebLink(idLink);
    } catch (EntityNotFoundException e) {
      LOG_ERROR.error(HttpStatus.NOT_FOUND.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (ResourceNoFoundException e) {
      LOG_ERROR.error(HttpStatus.FORBIDDEN.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    } catch (EEAException e) {
      LOG_ERROR.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }


  }

  /**
   * Save link.
   *
   * @param dataflowId the dataflow id
   * @param weblinkVO the weblink VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/v1/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Create a weblink into dataflow help", response = WeblinkVO.class,
      notes = "Allowed rolles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully created Weblink"),
      @ApiResponse(code = 404, message = "Dataflow Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public void saveLink(
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId,
      @ApiParam(type = "Object", value = "Weblink object") WeblinkVO weblinkVO) {

    try {
      dataflowWebLinkService.saveWebLink(dataflowId, weblinkVO);
    } catch (EntityNotFoundException e) {
      LOG_ERROR.error(HttpStatus.NOT_FOUND.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (WrongDataExceptions e) {
      LOG_ERROR.error(HttpStatus.BAD_REQUEST.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (EEAException e) {
      if (EEAErrorMessage.WEBLINK_ALREADY_EXIST.equals(e.getMessage())) {
        LOG_ERROR.error("Weblink url already exist in dataflow : {}", dataflowId);
        throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
      }
      LOG_ERROR.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }

  /**
   * Save link legacy.
   *
   * @param dataflowId the dataflow id
   * @param weblinkVO the weblink VO
   */
  @Override
  @HystrixCommand
  @PostMapping(value = "/dataflow/{dataflowId}")
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  @ApiOperation(value = "Create a dataflow weblink", response = WeblinkVO.class, hidden = true,
      notes = "Allowed rolles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successfully created Weblink"),
      @ApiResponse(code = 404, message = "Dataflow Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public void saveLinkLegacy(
      @ApiParam(value = "Dataflow Id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId,
      @ApiParam(type = "Object", value = "Weblink Object") WeblinkVO weblinkVO) {
    this.saveLink(dataflowId, weblinkVO);
  }

  /**
   * Removes the link.
   *
   * @param idLink the id link
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/v1/{idLink}/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete weblink from dataflow help by weblink id", response = WeblinkVO.class,
      notes = "Allowed rolles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  public void removeLink(
      @ApiParam(value = "Weblink id", example = "0") @PathVariable(value = "idLink") Long idLink,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId) {

    try {
      dataflowWebLinkService.removeWebLink(idLink);
    } catch (EntityNotFoundException e) {
      LOG_ERROR.error(HttpStatus.NOT_FOUND.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (ResourceNoFoundException e) {
      LOG_ERROR.error(HttpStatus.FORBIDDEN.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    } catch (EEAException e) {
      LOG_ERROR.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Removes the link legacy.
   *
   * @param idLink the id link
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @DeleteMapping(value = "/{idLink}/dataflow/{dataflowId}")
  @ApiOperation(value = "Delete weblink from dataflow help by weblink id", response = WeblinkVO.class,
      hidden = true, notes = "Allowed rolles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  public void removeLinkLegacy(
      @ApiParam(value = "Weblink id", example = "0") @PathVariable(value = "idLink") Long idLink,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId) {
    this.removeLink(idLink, dataflowId);
  }

  /**
   * Update link.
   *
   * @param weblinkVO the weblink VO
   * @param dataflowId the dataflow id
   */
  @Override
  @HystrixCommand
  @PutMapping(value = "/v1/dataflow/{dataflowId}")
  @ApiOperation(value = "Update dataflow weblink", response = WeblinkVO.class,
      notes = "Allowed rolles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  public void updateLink(
      @ApiParam(type = "Object", value = "Weblink object") @RequestBody WeblinkVO weblinkVO,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId) {
    try {
      dataflowWebLinkService.updateWebLink(weblinkVO);
    } catch (EntityNotFoundException e) {
      LOG_ERROR.error(HttpStatus.NOT_FOUND.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
    } catch (ResourceNoFoundException e) {
      LOG_ERROR.error(HttpStatus.FORBIDDEN.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
    } catch (WrongDataExceptions e) {
      LOG_ERROR.error(HttpStatus.BAD_REQUEST.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (EEAException e) {
      if (EEAErrorMessage.WEBLINK_ALREADY_EXIST.equals(e.getMessage())) {
        LOG_ERROR.error(HttpStatus.CONFLICT.getReasonPhrase(), e);
        throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
      }
      LOG_ERROR.error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

  }

  @Override
  @HystrixCommand
  @PutMapping(value = "/dataflow/{dataflowId}")
  @ApiOperation(value = "Update dataflow weblink", response = WeblinkVO.class, hidden = true,
      notes = "Allowed rolles: CUSTODIAN, STEWARD, EDITOR WRITE, ADMIN")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 403, message = "Forbidden"),
      @ApiResponse(code = 404, message = "Not Found"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE') OR hasAnyRole('ADMIN')")
  public void updateLinkLegacy(
      @ApiParam(type = "Object", value = "Weblink object") @RequestBody WeblinkVO weblinkVO,
      @ApiParam(value = "Dataflow id",
          example = "0") @PathVariable(value = "dataflowId") Long dataflowId) {
    this.updateLink(weblinkVO, dataflowId);
  }


  /**
   * Gets the all weblinks by dataflow.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/v1/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get list of dataflow weblinks from dataflow help",
      produces = MediaType.APPLICATION_JSON_VALUE, response = WeblinkVO.class,
      responseContainer = "List",
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR WRITE, EDITOR READ, NATIONAL COORDINATOR, ADMIN ")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public List<WeblinkVO> getAllWeblinksByDataflow(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    List<WeblinkVO> weblinks = new ArrayList<>();
    try {
      weblinks = dataflowWebLinkService.getAllWeblinksByDataflowId(dataflowId);
    } catch (EEAException e) {
      LOG_ERROR.error("Not found dataflow {}", dataflowId);
    }
    return weblinks;
  }

  /**
   * Gets the all weblinks by dataflow legacy.
   *
   * @param dataflowId the dataflow id
   * @return the all weblinks by dataflow legacy
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR (hasAnyRole('DATA_CUSTODIAN','DATA_STEWARD') AND checkAccessReferenceEntity('DATAFLOW',#dataflowId)) OR checkApiKey(#dataflowId,#providerId,#dataflowId,'DATAFLOW_STEWARD','DATAFLOW_OBSERVER','DATAFLOW_CUSTODIAN_SUPPORT','DATAFLOW_LEAD_REPORTER','DATAFLOW_REPORTER_WRITE','DATAFLOW_REPORTER_READ','DATAFLOW_CUSTODIAN','DATAFLOW_EDITOR_WRITE','DATAFLOW_EDITOR_READ','DATAFLOW_NATIONAL_COORDINATOR') OR hasAnyRole('ADMIN')")
  @GetMapping(value = "/dataflow/{dataflowId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Get list of dataflow weblinks from dataflow help",
      produces = MediaType.APPLICATION_JSON_VALUE, response = WeblinkVO.class,
      responseContainer = "List", hidden = true,
      notes = "Allowed roles: CUSTODIAN, STEWARD, OBSERVER, LEAD REPORTER, REPORTER WRITE, REPORTER READ, EDITOR WRITE, EDITOR READ, NATIONAL COORDINATOR, ADMIN ")
  @ApiResponse(code = 400, message = EEAErrorMessage.DATAFLOW_INCORRECT_ID)
  public List<WeblinkVO> getAllWeblinksByDataflowLegacy(
      @ApiParam(value = "Dataflow id", example = "0") @PathVariable("dataflowId") Long dataflowId) {
    return this.getAllWeblinksByDataflow(dataflowId);
  }

}
