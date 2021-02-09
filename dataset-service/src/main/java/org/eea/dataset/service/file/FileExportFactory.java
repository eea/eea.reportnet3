package org.eea.dataset.service.file;

import org.eea.dataset.service.file.interfaces.IFileExportContext;
import org.eea.dataset.service.file.interfaces.IFileExportFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


/**
 * A factory for creating FileParser objects.
 */
@Component
@RefreshScope
public class FileExportFactory implements IFileExportFactory {

  /**
   * The parse common.
   */
  @Autowired
  private FileCommonUtils fileCommon;

  @Value("${loadDataDelimiter}")
  private char delimiter;

  /**
   * Creates a new FileParser object.
   *
   * @param mimeType the mime type
   *
   * @return the i file parse context
   */
  @Override
  public IFileExportContext createContext(String mimeType) {
    FileExportContextImpl context = null;

    switch (mimeType.toLowerCase()) {
      case "csv":
        context = new FileExportContextImpl(new CSVWriterStrategy(delimiter, fileCommon));
        break;
      case "xml":
        // Fill it with the xml strategy
        break;
      case "xls":
      case "xlsx":
        context = new FileExportContextImpl(new ExcelWriterStrategy(fileCommon, mimeType));
        break;
      default:
        break;
    }
    return context;
  }
}
