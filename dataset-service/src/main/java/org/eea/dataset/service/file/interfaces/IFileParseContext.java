package org.eea.dataset.service.file.interfaces;

import java.io.InputStream;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;

/**
 * The Interface IFileParseContext.
 */
@FunctionalInterface
public interface IFileParseContext {

  /**
   * Parses the.
   *
   * @param inputStream the input stream
   * @param dataflowId the dataflow id
   * @param partitionId the partition id
   * @param idTableSchema the id table schema
   * @param datasetId the dataset id
   * @param fileName the file name
   * @param replace the replace
   * @param schema the schema
   * @param connectionDataVO the connection data VO
   * @throws EEAException the EEA exception
   */
  void parse(InputStream inputStream, Long dataflowId, Long partitionId, String idTableSchema,
      Long datasetId, String fileName, boolean replace, DataSetSchema schema,
      ConnectionDataVO connectionDataVO) throws EEAException;
}
