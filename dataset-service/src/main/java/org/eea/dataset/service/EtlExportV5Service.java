package org.eea.dataset.service;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import software.amazon.awssdk.transfer.s3.config.DownloadFilter;

import java.util.Collection;

public interface EtlExportV5Service {
  /**
   * Build the parquet filtering
   *
   * @param s3Path             The S3 path
   * @param includeAttachments filter attachments
   * @param tableName          Filter the table name
   * @return The object filtering
   */
  DownloadFilter buildParquetFilters(String s3Path, boolean includeAttachments, String tableName, Collection<Long> providerIds, S3Service s3Service);

  /**
   * Calculate the S3 path for each case
   *
   * @param dataset        The Dataset metabase object
   * @return The String S3 path
   */
  String getS3KeyPath(DataSetMetabaseVO dataset, S3Service s3Service, String preparationCode);
}
