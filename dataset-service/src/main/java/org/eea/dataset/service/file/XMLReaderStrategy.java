package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Class XMLReaderStrategy.
 */
public class XMLReaderStrategy implements ReaderStrategy {

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @return the data set VO
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream) {
    return null;
  }

}
