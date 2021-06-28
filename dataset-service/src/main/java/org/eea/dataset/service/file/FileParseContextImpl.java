package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.exception.EEAException;

/**
 * The Class FileParseContextImpl.
 */
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
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @return the data set VO
   * @throws EEAException the EEA exception
   */
  @Override
  public void parse(InputStream inputStream, Long dataflowId, Long partitionId,
      String idTableSchema, Long datasetId, String fileName, boolean replace, DataSetSchema schema)
      throws EEAException {
    readerStrategy.parseFile(inputStream, dataflowId, partitionId, idTableSchema, datasetId,
        fileName, replace, schema);
  }
}
