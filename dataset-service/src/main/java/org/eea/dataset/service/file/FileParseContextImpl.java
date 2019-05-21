package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

public class FileParseContextImpl implements IFileParseContext {

  /** The reader strategy. */
  private ReaderStrategy readerStrategy;

  /**
   * Instantiates a new file parse context impl.
   *
   * @param readerStrategy the reader strategy
   */
  public FileParseContextImpl(ReaderStrategy readerStrategy) {
    this.readerStrategy = readerStrategy;
  }

  /**
   * Parses the.
   *
   * @param inputStream the input stream
   * @param datasetId the dataset id
   * @param integer the integer
   * @return the data set VO
   * @throws InvalidFileException
   */
  @Override
  public DataSetVO parse(InputStream inputStream, String datasetId, Long partitionId)
      throws InvalidFileException {
    return readerStrategy.parseFile(inputStream, datasetId, partitionId);
  }
}
