package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.dataset.exception.InvalidFileException;
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
   * @throws InvalidFileException
   */
  DataSetVO parse(InputStream inputStream, Long dataflowId, Long partitionId)
      throws InvalidFileException;
}
