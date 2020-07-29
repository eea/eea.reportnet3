package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Interface ReaderStrategy.
 */
@FunctionalInterface
public interface ReaderStrategy {



  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   * @throws EEAException the EEA exception
   */
  DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId,
      String idTableSchema) throws InvalidFileException, EEAException;
}
