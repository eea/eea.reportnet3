package org.eea.dataset.service.file;

import javax.servlet.http.HttpServletResponse;
import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * A factory for creating FileParser objects.
 */
@Component
public class FileExportFactory implements IFileExportFactory {

  /**
   * The parse common.
   */
  @Autowired
  private ParseCommon parseCommon;

  @Value("${dataset.loadDataDelimiter}")
  private char delimiter;

  /**
   * Creates a new FileParser object.
   *
   * @param mimeType the mime type
   *
   * @return the i file parse contextd
   */
  @Override
  public IFileExportContext createContext(String mimeType, HttpServletResponse response) {
    FileExportContextImpl context = null;

    switch (mimeType.toLowerCase()) {
      case "csv":
        context =
            new FileExportContextImpl(new CSVWriterStrategy(delimiter, parseCommon, response));
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
