package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;

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
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  DataSetVO parse(InputStream inputStream, Long dataflowId, Long partitionId, String idTableSchema)
      throws EEAException;
}
