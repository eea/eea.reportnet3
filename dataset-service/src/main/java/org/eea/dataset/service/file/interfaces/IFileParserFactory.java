package org.eea.dataset.service.file.interfaces;

public interface IFileParserFactory {
  public FileParseContext createContext(String mimeType);
}
