package org.eea.dataset.service.file.interfaces;

import java.io.IOException;
import java.util.List;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ExportFilterVO;

/**
 * The Interface IFileExportContext.
 */
public interface IFileExportContext {

  /**
   * File writer.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the partition id
   * @param idTableSchema the id table schema
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @param filters the filters
   * @return the string
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  byte[] fileWriter(Long dataflowId, Long datasetId, String idTableSchema,
      boolean includeCountryCode, boolean includeValidations, ExportFilterVO filters)
      throws InvalidFileException, IOException, EEAException;

  /**
   * File list writer.
   *
   * @param dataflowId the dataflow id
   * @param datasetId the dataset id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @return the list
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  List<byte[]> fileListWriter(Long dataflowId, Long datasetId, boolean includeCountryCode,
      boolean includeValidations) throws InvalidFileException, IOException, EEAException;
}
