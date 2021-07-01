package org.eea.dataset.service.file.interfaces;

/**
 * A factory for creating IFileParser objects.
 */
@FunctionalInterface
public interface IFileParserFactory {


  /**
   * Creates a new IFileParser object.
   *
   * @param mimeType the mime type
   * @param datasetId the dataset id
   * @return the i file parse context
   */
  IFileParseContext createContext(String mimeType, Long datasetId, String delimiter);
}
