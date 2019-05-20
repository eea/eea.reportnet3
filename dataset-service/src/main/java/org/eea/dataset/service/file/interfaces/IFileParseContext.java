package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Interface IFileParseContext.
 */
public interface IFileParseContext {

  /**
   * Parses the.
   *
   * @param inputStream the input stream
   * @param datasetId the dataset id
   * @param integer the integer
   * @return the data set VO
   */
  public DataSetVO parse(InputStream inputStream, String datasetId, String name);
}
