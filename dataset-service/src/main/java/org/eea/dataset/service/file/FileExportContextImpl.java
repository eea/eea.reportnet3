package org.eea.dataset.service.file;

import java.io.IOException;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.WriterStrategy;

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
   * Parses the.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   * @throws IOException
   */
  @Override
  public byte[] fileWriter(Long dataflowId, Long partitionId, String idTableSchema)
      throws InvalidFileException, IOException {
    return writerStrategy.writeFile(dataflowId, partitionId, idTableSchema);
  }
}
