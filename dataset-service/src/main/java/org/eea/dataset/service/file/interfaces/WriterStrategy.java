package org.eea.dataset.service.file.interfaces;

import java.io.IOException;
import org.eea.dataset.exception.InvalidFileException;

/**
 * The Interface ReaderStrategy.
 */
@FunctionalInterface
public interface WriterStrategy {


  /**
   * Write file.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  byte[] writeFile(Long dataflowId, Long partitionId, String idTableSchema)
      throws InvalidFileException, IOException;
}
