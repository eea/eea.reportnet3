package org.eea.dataset.service.file.interfaces;

import java.io.IOException;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.exception.EEAException;

/**
 * The Interface IFileParseContext.
 */
@FunctionalInterface
public interface IFileExportContext {

  /**
   * File writer.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the partition id
   * @param idTableSchema the id table schema
   * @return the string
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  byte[] fileWriter(Long dataflowId, Long datasetId, String idTableSchema)
      throws InvalidFileException, IOException, EEAException;
}
