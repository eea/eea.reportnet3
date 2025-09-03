package org.eea.dataset.service.impl;

import lombok.RequiredArgsConstructor;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.ResolveSnapshotTable;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResolveSnapshotTableImpl implements ResolveSnapshotTable {

  private final SnapshotRepository snapshotRepository;
  private final DatasetMetabaseController datasetMetabaseController;

  @Override
  @Transactional
  public void rollBackSnapshotTableValues(Long jobId, Long dataflowId, Long providerId) {

    if (jobId != null && dataflowId != null && providerId != null) {
      List<ReportingDatasetVO> reportingDatasets =
          datasetMetabaseController.findReportingDataSetIdByDataflowIdAndProviderId(dataflowId, providerId);

      for (ReportingDatasetVO reportingDataset : reportingDatasets) {
        // Step 1: set dc_released=false and date null for record with given job_id
        int updated = snapshotRepository.markAsNotReleased(jobId, reportingDataset.getId());
        if (updated != 0) {
          // Step 2: find latest record for reportingDataset with date_released not null
          Long recordId = snapshotRepository.findLatestRecordForRelease(reportingDataset.getId(), jobId);
          if (recordId != null) {
            // Step 3: set that record as released
            snapshotRepository.markAsReleased(recordId);
          }
        }
      }
    }
  }
}
