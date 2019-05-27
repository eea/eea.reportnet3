package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Class XMLReaderStrategy.
 */
public class XMLReaderStrategy implements ReaderStrategy {

  /** The data set schema service. */
  private DatasetSchemaService dataSetSchemaService;

  /**
   * Instantiates a new XML reader strategy.
   *
   * @param dataSetSchemaService the data set schema service
   */
  public XMLReaderStrategy(DatasetSchemaService dataSetSchemaService) {
    this.dataSetSchemaService = dataSetSchemaService;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @return the data set VO
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, Long dataflowId, Long partitionId) {
    return null;
  }

}
