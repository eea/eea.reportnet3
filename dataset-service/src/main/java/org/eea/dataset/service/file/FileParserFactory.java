package org.eea.dataset.service.file;

import org.eea.dataset.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * A factory for creating FileParser objects.
 */
@Component
public class FileParserFactory implements IFileParserFactory {

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;

  /**
   * Creates a new FileParser object.
   *
   * @param mimeType the mime type
   * @return the i file parse context
   */
  @Override
  public IFileParseContext createContext(String mimeType) {
    FileParseContextImpl context = null;
    mimeType = mimeType.toLowerCase();
    switch (mimeType) {
      case "csv":
        context = new FileParseContextImpl(new CSVReaderStrategy(schemasRepository));
        break;
      case "xml":
        context = new FileParseContextImpl(new XMLReaderStrategy(schemasRepository));
        break;
      default:
        context = null;
        break;
    }
    return context;
  }
}
