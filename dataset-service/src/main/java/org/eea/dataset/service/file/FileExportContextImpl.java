package org.eea.dataset.service.file;

import java.io.IOException;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.WriterStrategy;
import org.eea.exception.EEAException;

/**
 * The Class FileParseContextImpl.
 */
public class FileExportContextImpl implements IFileExportContext {

  /** The reader strategy. */
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
   * @param idTableSchema the id table schema
   * @param includeCountryCode the include country code
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException the EEA exception
   */
  @Override
  public byte[] fileWriter(Long dataflowId, Long partitionId, String idTableSchema,
      boolean includeCountryCode) throws IOException, EEAException {
    return writerStrategy.writeFile(dataflowId, partitionId, idTableSchema, includeCountryCode);
  }
}
