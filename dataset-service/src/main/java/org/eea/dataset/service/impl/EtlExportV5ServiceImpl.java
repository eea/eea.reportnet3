package org.eea.dataset.service.impl;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.service.EtlExportV5Service;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.transfer.s3.config.DownloadFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.eea.utils.LiteralConstants.*;

@Component
public class EtlExportV5ServiceImpl implements EtlExportV5Service {

  private static final Logger LOG = LoggerFactory.getLogger(EtlExportV5ServiceImpl.class);

  @Override
  public DownloadFilter buildParquetFilters(String s3Path, boolean includeAttachments, String tableName, Collection<Long> providerIds, S3Service s3Service) {

    // Precompute provider folter to be filtered.
    final List<String> dpFolders = new ArrayList<>();
    if (providerIds != null && !providerIds.isEmpty()) {
      for (Long pid : providerIds) {
        if (pid != null) {
          String dpFolder = s3Service.formatFolderName(pid, S3_DATA_PROVIDER_PATTERN);
          dpFolders.add("/" + dpFolder + "/");
        }
      }
    }

    return s3Object -> {
      String key = s3Object.key();
      if (key == null) {
        return false;
      }

      // Exclude key for validation, snapshot and import directories.
      if (key.contains("/validation/") || key.contains("/snapshots/") || key.contains("/import/")) {
        return false;
      }

      if (!includeAttachments && key.contains("/attachments/")) {
        return false;
      }

      if (tableName != null && !key.contains("/" + tableName + "/")) {
        return false;
      }

      // For provider data that are inside other folders.
      if (!dpFolders.isEmpty()) {
        for (String dp : dpFolders) {
          if (key.contains(dp)) {
            return true;
          }
        }
        return false;
      }

      // No provider given.
      return true;
    };
  }

  @Override
  public String getS3KeyPath(DataSetMetabaseVO dataset, S3Service s3Service) {
    S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId());
    switch (dataset.getDatasetTypeEnum()) {
      case REPORTING:
        s3PathResolver.setPath(S3_PROVIDER_PATH);
        s3PathResolver.setDataProviderId(dataset.getDataProviderId());
        s3PathResolver.setDatasetId(dataset.getId());
        return s3Service.getS3Path(s3PathResolver);
      case TEST:
      case DESIGN:
        s3PathResolver.setPath(S3_PROVIDER_PATH);
        s3PathResolver.setDataProviderId(0L);
        s3PathResolver.setDatasetId(dataset.getId());
        return s3Service.getS3Path(s3PathResolver);
      case COLLECTION:
        s3PathResolver.setPath(S3_TABLE_NAME_ROOT_DC_FOLDER_PATH);
        s3PathResolver.setDatasetId(dataset.getId());
        return s3Service.getS3Path(s3PathResolver);
      case EUDATASET:
        s3PathResolver.setPath(S3_EU_SNAPSHOT_ROOT_PATH);
        s3PathResolver.setDatasetId(dataset.getId());
        return s3Service.getS3Path(s3PathResolver);
      case REFERENCE:
        s3PathResolver.setPath(S3_REFERENCE_FOLDER_PATH);
        return s3Service.getS3Path(s3PathResolver);
      default:
        LOG.info("Dataset Type does not exist!");
        return null;
    }
  }

}
