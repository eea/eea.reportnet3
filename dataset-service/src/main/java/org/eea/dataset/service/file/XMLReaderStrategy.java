package org.eea.dataset.service.file;

import java.io.InputStream;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.interfaces.ReaderStrategy;
import org.eea.interfaces.vo.dataset.DataSetVO;

/**
 * The Class XMLReaderStrategy.
 */
public class XMLReaderStrategy implements ReaderStrategy {

  private SchemasRepository schemasRepository;

  public XMLReaderStrategy(SchemasRepository schemasRepository) {
    this.schemasRepository = schemasRepository;
  }

  /**
   * Parses the file.
   *
   * @param inputStream the input stream
   * @return the data set VO
   */
  @Override
  public DataSetVO parseFile(InputStream inputStream, String datasetId, Long partitionId) {
    return null;
  }

}
