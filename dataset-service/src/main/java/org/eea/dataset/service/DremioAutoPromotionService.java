package org.eea.dataset.service;

import lombok.extern.slf4j.Slf4j;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.service.impl.BigDataDatasetServiceImpl;
import org.eea.interfaces.controller.orchestrator.JobController.JobControllerZuul;
import org.eea.interfaces.controller.orchestrator.RedisLockController.RedisLockControllerZuul;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.orchestrator.JobVO;
import org.eea.interfaces.vo.orchestrator.enums.JobTypeEnum;
import org.eea.lock.redis.LockEnum;
import org.eea.lock.redis.RedisLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DremioAutoPromotionService {

  private static final Logger LOG = LoggerFactory.getLogger(BigDataDatasetServiceImpl.class);

  private final RedisLockService redisLockService;
  private final JobControllerZuul jobControllerZuul;
  private final DremioHelperService dremioHelperService;

  public DremioAutoPromotionService(RedisLockService redisLockService,
                                    JobControllerZuul jobControllerZuul,
                                    DremioHelperService dremioHelperService) {
    this.redisLockService = redisLockService;
    this.jobControllerZuul = jobControllerZuul;
    this.dremioHelperService = dremioHelperService;
  }

  /**
   * Try to promote the folder if it is not promoted and it is safe to do so.
   */
  public void ensureSafeFolderPromotion(DataSetMetabaseVO dataset, S3PathResolver s3PathResolver) {
    try {
      String tableName = s3PathResolver.getTableName();

      // First folder promotion check.
      if (dremioHelperService.checkFolderPromoted(s3PathResolver, tableName)) {
        return;
      }

      // Prerequisites check.
      if (!canAutoPromote(dataset)) {
        LOG.info("Skipping auto promotion of table {} for datasetId {} due to unsatisfied prerequisites.", tableName, dataset.getId());
        return;
      }

      // Promote table.
      LOG.info("Auto-promoting Dremio table {} for datasetId {}", tableName, dataset.getId());
      dremioHelperService.promoteFolderOrFile(s3PathResolver, tableName);

    } catch (Exception e) {
      LOG.error("Error during auto promotion for datasetId {} table {}: {}", dataset.getId(), s3PathResolver.getTableName(), e.getMessage(), e);
    }
  }

  private boolean canAutoPromote(DataSetMetabaseVO dataset) {
    if (hasParquetIcebergConversionLock(dataset)) {
      LOG.info("Detected parquet/iceberg conversion lock and cannot auto promote datasetId {}", dataset.getId());
      return false;
    }

    if (hasInProgressJobs(dataset)) {
      LOG.info("Detected IN_PROGRESS import/delete/validation job and cannot auto promote datasetId {}", dataset.getId());
      return false;
    }
    return true;
  }

  private boolean hasParquetIcebergConversionLock(DataSetMetabaseVO dataset) {
    try {
      Long datasetId = dataset.getId();
      String lockKey = LockEnum.PARQUET_CONVERSION.getValue() + "_" + datasetId;
      Map<String, String> activeLocks = redisLockService.listActiveLocks(lockKey);

      return activeLocks != null && !activeLocks.isEmpty();
    } catch (Exception e) {
      LOG.error("Error checking Redis conversion locks for datasetId {}: {}",dataset.getId(), e.getMessage(), e);
      return true;
    }
  }

  private boolean hasInProgressJobs(DataSetMetabaseVO dataset) {
    try {
      Long datasetId = dataset.getId();
      Long dataflowId = dataset.getDataflowId();
      Long providerId = dataset.getDataProviderId();

      List<JobVO> activeJobs = jobControllerZuul.findActiveJobsRelatedToADatasetId(datasetId, dataflowId, providerId);

      if (activeJobs == null || activeJobs.isEmpty()) {
        return false;
      }

      return activeJobs.stream().anyMatch(job ->
          job.getJobType() == JobTypeEnum.IMPORT ||
          job.getJobType() == JobTypeEnum.DELETE ||
          job.getJobType() == JobTypeEnum.VALIDATION ||
          job.getJobType() == JobTypeEnum.ETL_IMPORT
      );

    } catch (Exception e) {
      LOG.error("Error checking active jobs for datasetId {}: {}", dataset.getId(), e.getMessage(), e);
      return true;
    }
  }

  public void demoteAndRefreshMetadataAndPromote(DataSetMetabaseVO dataset, String tablePath, S3PathResolver s3PathResolver) {
    String tableName = s3PathResolver.getTableName();
    try {
      LOG.info("Demoting and refreshing metadata for datasetId {} table {} (path={})", dataset.getId(), tableName, tablePath);
      // Demote the folder in Dremio.
      dremioHelperService.demoteFolderOrFile(s3PathResolver, tableName);
      // Refresh metadata and auto promote.
      dremioHelperService.refreshTableMetadataAndPromote(null, tablePath, s3PathResolver, tableName);
      LOG.info("Successfully refreshed metadata and promoted table {} for datasetId {}", tableName, dataset.getId());
    } catch (Exception e) {
      LOG.error("Error while demoting and refreshing metadata for datasetId {} table {}: {}", dataset.getId(), tableName, e.getMessage(), e);
      throw new RuntimeException("Failed to refresh Dremio metadata", e);
    }
  }
}
