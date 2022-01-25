package org.eea.dataset.service.file;

import java.io.IOException;
import java.util.List;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.ExportFilterVO;

/**
 * The Class FileExportContextImpl.
 */
public class FileExportContextImpl implements IFileExportContext {

  /** The writer strategy. */
  private WriterStrategy writerStrategy;

  /**
   * Instantiates a new file export context impl.
   *
   * @param writerStrategy the writer strategy
   */
  public FileExportContextImpl(WriterStrategy writerStrategy) {
    this.writerStrategy = writerStrategy;
  }

  /**
   * File writer.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param tableSchemaId the table schema id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @param filters the filters
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  public byte[] fileWriter(Long dataflowId, Long partitionId, String tableSchemaId,
      boolean includeCountryCode, boolean includeValidations, ExportFilterVO filters)
      throws IOException, EEAException {
    return writerStrategy.writeFile(dataflowId, partitionId, tableSchemaId, includeCountryCode,
        includeValidations, filters);
  }

  /**
   * File list writer.
   *
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param includeCountryCode the include country code
   * @param includeValidations the include validations
   * @return the list
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  public List<byte[]> fileListWriter(Long dataflowId, Long partitionId, boolean includeCountryCode,
      boolean includeValidations) throws IOException, EEAException {
    return writerStrategy.writeFileList(dataflowId, partitionId, includeCountryCode,
        includeValidations);
  }

}
