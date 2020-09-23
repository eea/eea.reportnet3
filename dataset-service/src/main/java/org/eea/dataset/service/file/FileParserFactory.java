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

  /** The parse common. */
  @Autowired
  private FileCommonUtils fileCommon;

  /** The delimiter. */
  @Value("${dataset.loadDataDelimiter}")
  private char delimiter;

  /** The field max length. */
  @Value("${dataset.fieldMaxLength}")
  private int fieldMaxLength;

  /**
   * Creates a new FileParser object.
   *
   * @param mimeType the mime type
   * @param datasetId the dataset id
   * @return the i file parse contextd
   */
  @Override
  public IFileParseContext createContext(String mimeType, Long datasetId) {
    FileParseContextImpl context = null;

    switch (mimeType.toLowerCase()) {
      case "csv":
        context = new FileParseContextImpl(
            new CSVReaderStrategy(delimiter, fileCommon, datasetId, fieldMaxLength));
        break;
      case "xml":
        // Fill it with the xml strategy
        break;
      case "xls":
      case "xlsx":
        context = new FileParseContextImpl(
            new ExcelReaderStrategy(fileCommon, datasetId, fieldMaxLength));
        break;
      default:
        break;
    }
    return context;
  }
}
