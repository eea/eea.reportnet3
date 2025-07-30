package org.eea.dataset.service;

import org.eea.exception.EEAException;

import java.util.List;

public interface ReleaseFieldLimitWarningComponent {
  void releaseFieldSizeNotification(List<Long> recordLines, Long dataflowId, Long datasetId) throws EEAException;
}
