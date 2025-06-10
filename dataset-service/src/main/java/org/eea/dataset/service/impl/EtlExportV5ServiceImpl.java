package org.eea.dataset.service.impl;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.service.EtlExportV5Service;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.transfer.s3.config.DownloadFilter;

import static org.eea.utils.LiteralConstants.*;

@Component
public class EtlExportV5ServiceImpl implements EtlExportV5Service {

  private static final Logger LOG = LoggerFactory.getLogger(EtlExportV5ServiceImpl.class);

  @Override
  public DownloadFilter buildParquetFilters(String s3Path, boolean includeAttachments, String tableName) {
    return s3Object -> {
      String key = s3Object.key();

      //exclude those folders
      boolean baseCondition = key.startsWith(s3Path)
          && !key.contains("/validation/")
          && !key.contains("/snapshots/")
          && !key.contains("/import/");

      //include or not attachments folder
      if (!includeAttachments && key.contains("/attachments/")) {
        return false;
      }

      // If tableName is provided, only include keys that contain the table name (as folder or file name)
      if (tableName != null && !key.contains("/" + tableName + "/")) {
        return false;
      }

      return baseCondition;
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
