package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Class XMLReaderStrategy.
 */
public class XMLReaderStrategy implements ReaderStrategy {

  private DatasetSchemaService dataSetSchemaService;

  public XMLReaderStrategy(DatasetSchemaService dataSetSchemaService) {
    this.dataSetSchemaService = dataSetSchemaService;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @return the data set VO
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId) {
    return null;
  }

}
