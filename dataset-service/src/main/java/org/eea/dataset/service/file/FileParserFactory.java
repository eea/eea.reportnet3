package org.eea.dataset.service.file;

import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * A factory for creating FileParser objects.
 */
@Component
public class FileParserFactory implements IFileParserFactory {

  /** The data set schema service. */
  @Autowired
  private DatasetSchemaService dataSetSchemaService;

  @Autowired
  private ParseCommon parseCommon;

  /**
   * Creates a new FileParser object.
   *
   * @param mimeType the mime type
   * @return the i file parse context
   */
  @Override
  public IFileParseContext createContext(String mimeType) {
    FileParseContextImpl context = null;

    switch (mimeType.toLowerCase()) {
      case "csv":
        context =
            new FileParseContextImpl(new CSVReaderStrategy(dataSetSchemaService, parseCommon));
        break;
      case "xml":
        // Fill it with the xml strategy
        break;
      default:
        break;
    }
    return context;
  }
}
