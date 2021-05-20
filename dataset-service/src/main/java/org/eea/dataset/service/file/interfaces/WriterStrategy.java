package org.eea.dataset.service.file.interfaces;

import java.util.List;
import org.eea.exception.EEAException;

/**
 * The Interface ReaderStrategy.
 */
public interface WriterStrategy {

  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param includeCountryCode the include country code
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  byte[] writeFile(Long dataflowId, Long partitionId, String idTableSchema,
      boolean includeCountryCode) throws EEAException;

  /**
   * Write file List.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param includeCountryCode the include country code
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  List<byte[]> writeFileList(Long dataflowId, Long partitionId, String idTableSchema,
      boolean includeCountryCode) throws EEAException;
}
