package org.eea.dataset.service.file.interfaces;

import java.io.IOException;
import java.util.List;
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
   * @param includeCountryCode the include country code
   * @return the string
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  List<byte[]> fileWriter(Long dataflowId, Long datasetId, String idTableSchema,
      boolean includeCountryCode) throws InvalidFileException, IOException, EEAException;
}
