package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.exception.EEAException;

/**
 * The Interface IFileParseContext.
 */
@FunctionalInterface
public interface IFileParseContext {

  /**
   * Parses the.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  void parse(InputStream inputStream, Long dataflowId, Long partitionId, String idTableSchema,
      Long datasetId, String fileName, boolean replace, DataSetSchema schema) throws EEAException;
}
