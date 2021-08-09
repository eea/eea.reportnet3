package org.eea.dataflow.integration.executor.fme.service;

import java.io.InputStream;
import org.eea.dataflow.integration.executor.fme.domain.FMEAsyncJob;
import org.eea.dataflow.integration.executor.fme.domain.FileSubmitResult;
import org.eea.dataflow.persistence.domain.FMEJob;
import org.eea.exception.EEAForbiddenException;
import org.eea.exception.EEAUnauthorizedException;
import org.eea.interfaces.vo.integration.fme.FMECollectionVO;
import org.springframework.http.HttpStatus;

/**
 * The Class FMECommunicationService.
 */
public interface FMECommunicationService {


  /**
   * Submit async job.
   *
   * @param repository the repository
   * @param workspace the workspace
   * @param fmeAsyncJob the fme async job
   * @param dataflowId the dataflow id
   * @return the integer
   */
  Integer submitAsyncJob(String repository, String workspace, FMEAsyncJob fmeAsyncJob,
      Long dataflowId);

  /**
   * Send file.
   *
   * @param file the file
   * @param idDataset the id dataset
   * @param idProvider the id provider
   * @param fileName the file name
   *
   * @return the file submit result
   */
  FileSubmitResult sendFile(byte[] file, Long idDataset, String idProvider, String fileName);

  /**
   * Creates the directory.
   *
   * @param idDataset the id dataset
   * @param idProvider the id provider
   * @return the http status
   */
  HttpStatus createDirectory(Long idDataset, String idProvider);


  /**
   * Receive file.
   *
   * @param idDataset the id dataset
   * @param providerId the provider id
   * @param fileName the file name
   * @return the file submit result
   */
  InputStream receiveFile(Long idDataset, Long providerId, String fileName);


  /**
   * Find repository.
   *
   * @param datasetId the dataset id
   * @return the FME collection VO
   */
  FMECollectionVO findRepository(Long datasetId);


  /**
   * Find items.
   *
   * @param repository the repository
   * @param datasetId the dataset id
   * @return the FME collection VO
   */
  FMECollectionVO findItems(String repository, Long datasetId);

  /**
   * Authenticate and authorize.
   *
   * @param apiKey the api key
   * @param rn3JobId the rn 3 job id
   * @return the FME job
   * @throws EEAForbiddenException the EEA forbidden exception
   * @throws EEAUnauthorizedException the EEA unauthorized exception
   */
  FMEJob authenticateAndAuthorize(String apiKey, Long rn3JobId)
      throws EEAForbiddenException, EEAUnauthorizedException;

  /**
   * Release notifications.
   *
   * @param fmeJob the fme job
   * @param statusNumber the status number
   * @param notificationRequired the notification required
   */
  void releaseNotifications(FMEJob fmeJob, long statusNumber, boolean notificationRequired);

  /**
   * Update job status.
   *
   * @param fmeJob the fme job
   * @param statusNumber the status number
   */
  void updateJobStatus(FMEJob fmeJob, long statusNumber);

  /**
   * Update job status by id.
   *
   * @param jobId the job id
   * @param status the status
   */
  void updateJobStatusById(Long jobId, Long status);
}
