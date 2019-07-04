package org.eea.dataset.service.file.interfaces;

import java.io.IOException;
import org.eea.dataset.exception.InvalidFileException;

/**
 * The Interface IFileParseContext.
 */
@FunctionalInterface
public interface IFileExportContext {

  /**
   * File writer.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the string
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  String fileWriter(Long dataflowId, Long partitionId, String idTableSchema)
      throws InvalidFileException, IOException;
}
