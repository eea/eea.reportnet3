package org.eea.dataflow.controller.fme;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataflow.integration.executor.fme.service.FMECommunicationService;
import org.eea.dataflow.integration.utils.StreamingUtil;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.dataflow.service.IntegrationService;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAUnauthorizedException;
import org.eea.interfaces.controller.dataflow.integration.fme.FMEController;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.vo.dataflow.enums.IntegrationOperationTypeEnum;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.eea.interfaces.vo.integration.fme.FMEOperationInfoVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobStatusEnum;
import org.eea.interfaces.vo.recordstore.enums.ProcessStatusEnum;
import org.eea.lock.service.LockService;
import org.eea.utils.LiteralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class FMEControllerImpl.
 */
@RestController
@RequestMapping("/fme")
@Api(tags = "FME : FME Manager")
@ApiIgnore
public class FMEControllerImpl implements FMEController {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(FMEControllerImpl.class);

  /** The fme communication service. */
  @Autowired
  private FMECommunicationService fmeCommunicationService;

  /** The streaming util. */
  @Autowired
  private StreamingUtil streamingUtil;

  /** The lock service. */
  @Autowired
  private LockService lockService;

  /**
   * The integration service.
   */
  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private JobControllerZuul jobControllerZuul;

  @Autowired
  private DataSetControllerZuul dataSetControllerZuul;



  /**
   * Find repositories.
   *
   * @param datasetId the dataset id
   *
   * @return the collection VO
   */
  @Override
  @HystrixCommand
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASCHEMA_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD')")
  @GetMapping(value = "/findRepositories", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find FME Repositories", produces = MediaType.APPLICATION_JSON_VALUE,
      response = FMECollectionVO.class, hidden = true)
  public FMECollectionVO findRepositories(
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId) {
    return fmeCommunicationService.findRepository(datasetId);
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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASCHEMA_CUSTODIAN','EUDATASET_CUSTODIAN','EUDATASET_STEWARD')")
  @GetMapping(value = "/findItems", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Find FME Items", produces = MediaType.APPLICATION_JSON_VALUE,
      response = FMECollectionVO.class, hidden = true)
  public FMECollectionVO findItems(
      @ApiParam(value = "Dataset id", example = "0") @RequestParam("datasetId") Long datasetId,
      @ApiParam(value = "Repository name") @RequestParam("repository") String repository) {
    return fmeCommunicationService.findItems(repository, datasetId);
  }

  /**
   * Operation finished.
   *
   * @param fmeOperationInfoVO the fme operation info VO
   */
  @Override
  @PostMapping("/operationFinished")
  @ApiOperation(value = "Notify a FME Operation finished", hidden = true)
  @ApiResponse(code = 400, message = "Internal Server Error")
  public void operationFinished(@ApiParam(value = "FME Operation info",
      type = "Object") @RequestBody FMEOperationInfoVO fmeOperationInfoVO) {

    Exception exception = null;
    HttpStatus httpStatus = null;
    Map<String, Object> lockCriteria = new HashMap<>();
    lockCriteria.put(LiteralConstants.SIGNATURE,
        LockSignature.EXECUTE_EXTERNAL_INTEGRATION.getValue());
    lockCriteria.put(LiteralConstants.DATASETID, fmeOperationInfoVO.getDatasetId());

    try {
      FMEJob fmeJob = fmeCommunicationService.authenticateAndAuthorize(
          fmeOperationInfoVO.getApiKey(), fmeOperationInfoVO.getRn3JobId());
      LOG.info("FME endpoint /operationFinished has been called for fmeJobId {} with status number {} ", fmeJob.getJobId(), fmeOperationInfoVO.getStatusNumber());
      fmeCommunicationService.releaseNotifications(fmeJob, fmeOperationInfoVO.getStatusNumber(),
          fmeOperationInfoVO.isNotificationRequired());
      fmeCommunicationService.updateJobStatus(fmeJob, fmeOperationInfoVO.getStatusNumber());
      if(fmeJob.getOperation() == IntegrationOperationTypeEnum.IMPORT) {
        updateFailedJobAndProcessStatus(fmeJob.getJobId(), fmeOperationInfoVO.getStatusNumber());
      }
    } catch (EEAForbiddenException e) {
      exception = e;
      httpStatus = HttpStatus.FORBIDDEN;
    } catch (EEAUnauthorizedException e) {
      exception = e;
      httpStatus = HttpStatus.UNAUTHORIZED;
    } catch(Exception e){
      Long datasetId = (fmeOperationInfoVO != null) ? fmeOperationInfoVO.getDatasetId() : null;
      Long rn3JobId = (fmeOperationInfoVO != null) ? fmeOperationInfoVO.getRn3JobId() : null;
      LOG.error("Unexpected error! Could not notify a FME Operation finished for datasetId {} and rn3JobId {} Message: {}", datasetId, rn3JobId, e.getMessage());
      throw e;
    }

    lockService.removeLockByCriteria(lockCriteria);
    integrationService.releaseLocks(fmeOperationInfoVO.getDatasetId());

    if (null != exception) {
      throw new ResponseStatusException(httpStatus, exception.getMessage(), exception);
    }
  }

  private void updateFailedJobAndProcessStatus(Long fmeJobId, Long fmeStatusNum){
    try{
      JobVO jobVO = jobControllerZuul.findJobByFmeJobId(fmeJobId.toString());
      if(fmeStatusNum == 0L){
        Map<String, Object> insertedParameters = jobVO.getParameters();
        if(insertedParameters.get("fmeCallback") == null || (Boolean) insertedParameters.get("fmeCallback") == true){
          LOG.info("Fme job with jobId {} and fmeJobId {} is successful and a callback has been made with a file", jobVO.getId(), fmeJobId);
          return;
        }
      }
      if(jobVO.getJobStatus().getValue().equals(JobStatusEnum.IN_PROGRESS.getValue())) {
        //if the job is in progress and the job either failed in fme or was successful but no file was sent to Reportnet3, the job will be failed.
        jobControllerZuul.updateJobAndProcess(jobVO.getId(), JobStatusEnum.FAILED, ProcessStatusEnum.CANCELED);
        LOG.info("Updated fme job, job and process in /operationFinished for jobId {} and fmeJobId {}", jobVO.getId(), fmeJobId);

        dataSetControllerZuul.deleteLocksToImportProcess(jobVO.getDatasetId());
        jobControllerZuul.sendFmeImportFailedNotification(jobVO);

      }
    }
    catch (Exception e) {
      LOG.error("Could not update failed job and process for fme job id {} ", fmeJobId, e);
    }

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
  @PreAuthorize("secondLevelAuthorize(#datasetId,'DATASET_STEWARD','DATASCHEMA_STEWARD', 'DATASCHEMA_EDITOR_WRITE', 'DATASCHEMA_CUSTODIAN', 'DATASET_CUSTODIAN', 'DATASET_STEWARD', 'DATASET_LEAD_REPORTER', 'DATASET_REPORTER_READ', 'DATASET_REPORTER_WRITE','DATASET_NATIONAL_COORDINATOR','DATASET_OBSERVER','DATASET_STEWARD_SUPPORT')")
  @ApiOperation(value = "Download an exported data file from FME", hidden = true)
  public ResponseEntity<StreamingResponseBody> downloadExportFile(
      @RequestParam("datasetId") Long datasetId,
      @RequestParam(value = "providerId", required = false) Long providerId,
      @RequestParam("fileName") String fileName) {

    StreamingResponseBody stream = out -> {
      InputStream is = fmeCommunicationService.receiveFile(datasetId, providerId, fileName);
      try {
        streamingUtil.copy(is, out);
      } catch (Exception e){
        LOG.error("Error copying file {} for datasetId {} and providerId {} Message: {}", fileName, datasetId, providerId, e.getMessage());
        throw e;
      } finally {
        is.close();
      }
    };

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

    return new ResponseEntity<>(stream, httpHeaders, HttpStatus.OK);
  }

  /**
   * Update job status by id.
   *
   * @param jobId the job id
   * @param status the status
   */
  @Override
  @GetMapping("/private/updateJobStatusById")
  @ApiOperation(value = "Update job status by id", hidden = true)
  public void updateJobStatusById(@RequestParam("jobId") Long jobId,
      @RequestParam("status") Long status) {
    fmeCommunicationService.updateJobStatusById(jobId, status);
  }
}
