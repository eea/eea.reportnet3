package org.eea.dataset.service.file;

import org.eea.dataset.service.file.interfaces.FileParseContext;
import org.eea.dataset.service.file.interfaces.IFileParserFactory;

public class FileParserFactory implements IFileParserFactory{
  public FileParseContext createContext(String mimeType) {
    FileParseContextImpl context = null;
    switch (mimeType) {
      case "csv":
        context = new FileParseContextImpl(new CSVReaderStrategy());
        break;
      case "xml":
        context = new FileParseContextImpl(new XMLReaderStrategy());
        break;
      default:
        context = null;
        break;
    }
    return context;
  }
}
