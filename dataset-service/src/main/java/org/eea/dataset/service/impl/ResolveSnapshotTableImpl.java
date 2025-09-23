package org.eea.dataset.service.impl;

import lombok.RequiredArgsConstructor;
import org.eea.dataset.persistence.metabase.repository.SnapshotRepository;
import org.eea.dataset.service.ResolveSnapshotTable;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.ReportingDatasetVO;
import org.eea.interfaces.vo.lock.enums.LockSignature;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResolveSnapshotTableImpl implements ResolveSnapshotTable {

  private final SnapshotRepository snapshotRepository;
  private final DatasetMetabaseController datasetMetabaseController;
  private final RedisLockService redisLockService;

  private static final Logger LOG = LoggerFactory.getLogger(ResolveSnapshotTableImpl.class);

  @Override
  @Transactional
  public void rollBackSnapshotTableValues(Long jobId, Long dataflowId, Long providerId) {

    long lockExpirationInMillis = 60000L;
    String lockKey = LockEnum.ROLLBACK_SNAPSHOT.getValue() + "_" + jobId + "_" + dataflowId + "_" + providerId;
    String value = LockSignature.ROLLBACK_SNAPSHOT.toString();

    LOG.info("inside rollBackSnapshotTableValues.  jobId : {}, dataflowId : {}, providerId : {}", jobId, dataflowId, providerId);

    if (redisLockService.checkAndAcquireLock(lockKey, value, lockExpirationInMillis)) {
      try {
        if (jobId != null && dataflowId != null && providerId != null) {
          List<ReportingDatasetVO> reportingDatasets =
              datasetMetabaseController.findReportingDataSetIdByDataflowIdAndProviderId(dataflowId, providerId);

          for (ReportingDatasetVO reportingDataset : reportingDatasets) {
            // Step 1: set dc_released=false and date null for record with given job_id
            int updated = snapshotRepository.markAsNotReleased(jobId, reportingDataset.getId());
            if (updated != 0) {
              LOG.info("Last record {} updated as not released.  jobId : {}, dataflowId : {}, providerId : {}", updated ,jobId, dataflowId, providerId);
              // Step 2: find latest record for reportingDataset with date_released not null
              Long snapshotId = snapshotRepository.findLatestRecordForRelease(reportingDataset.getId(), jobId);
              if (snapshotId != null) {
                // Step 3: set that record as released
                snapshotRepository.markAsReleased(snapshotId);
                LOG.info("Previous snapshot id {} updated as released.  jobId : {}, dataflowId : {}, providerId : {}", snapshotId ,jobId, dataflowId, providerId);
              }
            }
          }
        } else {
          LOG.warn("One of those variables are null. The rollback of snapshot table will not continue.  jobId : {}, dataflowId : {}, providerId : {}", jobId, dataflowId, providerId);
        }
      } catch (Exception ex) {
        LOG.error("Failed rolling back snapshot table values. jobId : {}, dataflowId : {}, providerId : {}, with ERROR : {}", jobId, dataflowId, providerId, ex.getCause().toString());
      } finally {
        redisLockService.releaseLock(lockKey, value);
      }
    }
  }
}
