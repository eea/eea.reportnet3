package org.eea.dataset.service.file;

import java.io.InputStream;

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
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  public DataSetVO parse(InputStream inputStream) {
    return readerStrategy.parseFile(inputStream);
  }
}
