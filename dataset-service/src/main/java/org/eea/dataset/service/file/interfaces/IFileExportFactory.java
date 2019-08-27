package org.eea.dataset.service.file.interfaces;

/**
 * A factory for creating IFileParser objects.
 */
@FunctionalInterface
public interface IFileExportFactory {


  /**
   * Creates a new IFileExport object.
   *
   * @param mimeType the mime type
   * @return the i file export context
   */
  IFileExportContext createContext(String mimeType);
}
