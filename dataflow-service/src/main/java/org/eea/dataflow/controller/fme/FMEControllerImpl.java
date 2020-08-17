package org.eea.dataflow.controller.fme;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.integration.utils.StreamingUtil;
import org.eea.interfaces.controller.dataflow.integration.fme.FMEController;
import org.eea.interfaces.controller.ums.UserManagementController.UserManagementControllerZull;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.interfaces.vo.ums.TokenVO;
import org.eea.security.jwt.expression.EeaSecurityExpressionRoot;
import org.eea.security.jwt.utils.AuthenticationDetails;
import org.eea.security.jwt.utils.EeaUserDetails;
import org.eea.thread.ThreadPropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

/**
 * The Class FMEControllerImpl.
 */
@RestController
@RequestMapping("/fme")
@Api(tags = "FME : FME Manager")
public class FMEControllerImpl implements FMEController {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEControllerImpl.class);

  /** The Constant LOG_ERROR. */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /** The fme communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /** The user management controller zull. */
  @Autowired
  private UserManagementControllerZull userManagementControllerZull;

  /** The streaming util. */
  @Autowired
  private StreamingUtil streamingUtil;

  /**
   * Find repositories.
   *
   * @param datasetId the dataset id
   *
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find FME Repositories", produces = MediaType.APPLICATION_JSON_VALUE,
      response = FMECollectionVO.class)
  public FMECollectionVO findRepositories(
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId) {
    return fmeCommunicationService.findRepository();
  }

  /**
   * Find items.
   *
   * @param datasetId the dataset id
   * @param repository the repository
   *
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_EDITOR_WRITE') OR (hasRole('DATA_CUSTODIAN')) OR (hasRole('DATA_STEWARD'))")
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find FME Items", produces = MediaType.APPLICATION_JSON_VALUE,
      response = FMECollectionVO.class)
  public FMECollectionVO findItems(
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Repository name") @RequestParam("repository") String repository) {
    return fmeCommunicationService.findItems(repository);
  }

  /**
   * Operation finished.
   *
   * @param fmeOperationInfoVO the fme operation info VO
   */
  @Override
  @PostMapping("/operationFinished")
  @PreAuthorize("checkApiKey(#fmeOperationInfoVO.dataflowId, #fmeOperationInfoVO.providerId)")
  @ApiOperation(value = "Notify a FME Operation finished")
  @ApiResponse(code = 400, message = "Internal Server Error")
  public void operationFinished(@ApiParam(value = "FME Operation info",
      type = "Object") @RequestBody FMEOperationInfoVO fmeOperationInfoVO) {
    // Set the user name on the thread
    ThreadPropertiesManager.setVariable("user",
        SecurityContextHolder.getContext().getAuthentication().getName());
    try {
      fmeCommunicationService.operationFinished(fmeOperationInfoVO);
    } catch (Exception e) {
      LOG_ERROR.error("Error in the operationFinished controller with the message: {}",
          e.getMessage());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @Override
  @PostMapping("/operationFinishedDummy")
  @ApiOperation(value = "PoC for operationFinished")
  @ApiResponse(code = 400, message = "Internal Server Error")
  public void operationFinishedDummy(@ApiParam(value = "FME Operation info",
      type = "Object") @RequestBody FMEOperationInfoVO fmeOperationInfoVO) {
    LOG.info("Invocation to /operationFinishedDummy with params {}", fmeOperationInfoVO);
    Authentication authorization = authenticate(fmeOperationInfoVO.getApiKey());
    authorize(authorization, fmeOperationInfoVO.getDataflowId(),
        fmeOperationInfoVO.getProviderId());
    LOG.info("Successfully completed operationFinishedDummy");
  }

  /**
   * Download export file.
   *
   * @param datasetId the dataset id
   * @param providerId the provider id
   * @param fileName the file name
   *
   * @return the response entity
   */
  @Override
  @GetMapping(value = "/downloadExportFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @PreAuthorize("secondLevelAuthorize(#datasetId, 'DATASCHEMA_EDITOR_WRITE', 'DATASCHEMA_CUSTODIAN', 'DATASET_CUSTODIAN', 'DATASET_STEWARD', 'DATASET_LEAD_REPORTER', 'DATASET_REPORTER_READ', 'DATASET_REPORTER_WRITE')")
  @ApiOperation(value = "Download an exported data file from FME")
  public ResponseEntity<StreamingResponseBody> downloadExportFile(
      @RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam("fileName") String fileName) {

    StreamingResponseBody stream = out -> {
      InputStream is = fmeCommunicationService.receiveFile(datasetId, providerId, fileName);
      streamingUtil.copy(is, out);
      is.close();
    };

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

    return new ResponseEntity<>(stream, httpHeaders, HttpStatus.OK);
  }

  private Authentication authenticate(String apiKey) {
    if (null != apiKey && !apiKey.isEmpty()) {
      TokenVO tokenVO = userManagementControllerZull.authenticateUserByApiKey(apiKey);
      if (null != tokenVO) {
        String username = tokenVO.getPreferredUsername();
        Set<String> roles = tokenVO.getRoles();
        Set<String> groups = tokenVO.getGroups();
        if (null != groups && !groups.isEmpty()) {
          groups.stream().map(group -> {
            if (group.startsWith("/")) {
              group = group.substring(1);
            }
            return group.toUpperCase();
          }).forEach(roles::add);
        }
        UserDetails userDetails = EeaUserDetails.create(username, roles);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userDetails, "ApiKey " + apiKey,
                userDetails.getAuthorities());
        Map<String, String> details = new HashMap<>();
        details.put(AuthenticationDetails.USER_ID, tokenVO.getUserId());
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        ThreadPropertiesManager.setVariable("user", username);
        return authentication;
      }
    }
    LOG_ERROR.error("Invalid apiKey: {}", apiKey);
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  }

  private void authorize(Authentication authentication, Long dataflowId, Long providerId) {
    EeaSecurityExpressionRoot eeaSecurityExpressionRoot =
        new EeaSecurityExpressionRoot(authentication, userManagementControllerZull);
    boolean authorized = eeaSecurityExpressionRoot.checkApiKey(dataflowId, providerId);
    if (!authorized) {
      LOG_ERROR.error("User not allowed: authentication={}, dataflowId={}, providerId={}",
          authentication, dataflowId, providerId);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }
}
