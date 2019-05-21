package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Interface ReaderStrategy.
 */
public interface ReaderStrategy {

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param datasetId the dataset id
   * @param integer the integer
   * @return the data set VO
   */
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId);
}
