package org.eea.dataset.service.file.interfaces;

/**
 * A factory for creating IFileParser objects.
 */
public interface IFileParserFactory {

  /**
   * Creates a new IFileParser object.
   *
   * @param mimeType the mime type
   * @return the i file parse context
   */
  public IFileParseContext createContext(String mimeType);
}
