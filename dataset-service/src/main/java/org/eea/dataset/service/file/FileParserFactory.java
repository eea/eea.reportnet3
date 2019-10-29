package org.eea.dataset.service.file;

import org.eea.dataset.service.file.interfaces.IFileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * A factory for creating FileParser objects.
 */
@Component
public class FileParserFactory implements IFileParserFactory {

  /**
   * The parse common.
   */
  @Autowired
  private FileCommonUtils fileCommon;

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
  public IFileParseContext createContext(String mimeType, Long datasetId) {
    FileParseContextImpl context = null;

    switch (mimeType.toLowerCase()) {
      case "csv":
        context = new FileParseContextImpl(new CSVReaderStrategy(delimiter, fileCommon, datasetId));
        break;
      case "xml":
        // Fill it with the xml strategy
        break;
      case "xls":
      case "xlsx":
        context = new FileParseContextImpl(new ExcelReaderStrategy(fileCommon, datasetId));
        break;
      default:
        break;
    }
    return context;
  }
}
