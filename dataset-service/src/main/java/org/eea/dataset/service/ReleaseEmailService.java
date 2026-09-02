package org.eea.dataset.service;

import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;

public interface ReleaseEmailService {

  /**
   * Sends the email notification when a release has finished successfully.
   *
   * @param dateRelease the release date
   * @param dataset the dataset
   * @param dataflowVO the dataflow
   */
  void sendReleaseFinishedEmail(String dateRelease, DataSetMetabaseVO dataset, DataFlowVO dataflowVO);

  /**
   * Sends the email notification when a release has started.
   *
   * @param dataflowId the dataflow
   * @param providerId the dataflow
   */
  void sendReleaseStartedEmail(Long dataflowId, Long providerId);

  /**
   * Sends the email notification when a release has started.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param reason the reason message
   */
  void sendReleaseFailedEmail(Long dataflowId, Long providerId, String reason);
}


