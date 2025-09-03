package org.eea.dataset.service;

public interface ResolveSnapshotTable {
  /**
   * Rolling back snapshot record in case of a failure or cancellation
   * @param jobId The jobId
   */
  void rollBackSnapshotTableValues(Long jobId, Long dataflowId, Long providerId);
}
