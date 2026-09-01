package org.eea.recordstore.service;

public interface ReleaseEmailService {

  /**
   * Sends the email notification when a release has started.
   *
   * @param dataflowId the dataflow id
   * @param providerId the provider id
   * @param reason the reason message
   */
  void sendReleaseFailedEmail(Long dataflowId, Long providerId, String reason);
}


