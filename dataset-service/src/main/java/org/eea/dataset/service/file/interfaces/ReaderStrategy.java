package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Interface ReaderStrategy.
 */
public interface ReaderStrategy {

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @return the data set VO
   * @throws InvalidFileException the invalid file exception
   */
  public DataSetVO parseFile(InputStream inputStream);
}
