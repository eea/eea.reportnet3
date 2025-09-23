package org.eea.dataset.service;

import org.eea.datalake.service.model.SpatialFieldInfo;
import org.eea.exception.EEAException;

public interface ReleaseFieldLimitWarningComponent {
  void releaseFieldSizeNotification(SpatialFieldInfo spatialFieldInfo, Long dataflowId, Long datasetId) throws EEAException;
}
