package org.eea.dataset.controller;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileCopyUtils;

/**
 * The Class EEAMockMultipartFile.
 */
public class EEAMockMultipartFile extends MockMultipartFile {


  /** The simulate IO exception. */
  private boolean simulateIOException;

  /**
   * Instantiates a new EEA mock multipart file.
   *
   * @param name the name
   * @param content the content
   * @param simulateIOException the simulate IO exception
   */
  public EEAMockMultipartFile(String name, @Nullable byte[] content, boolean simulateIOException) {
    super(name, "", null, content);
    this.simulateIOException = simulateIOException;
  }

  /**
   * Instantiates a new EEA mock multipart file.
   *
   * @param name the name
   * @param contentStream the content stream
   * @param simulateIOException the simulate IO exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public EEAMockMultipartFile(String name, InputStream contentStream, boolean simulateIOException)
      throws IOException {
    super(name, "", null, FileCopyUtils.copyToByteArray(contentStream));
    this.simulateIOException = simulateIOException;
  }

  /**
   * Create a new MockMultipartFile with the given content.
   *
   * @param name the name of the file
   * @param originalFilename the original filename (as on the client's machine)
   * @param contentType the content type (if known)
   * @param content the content of the file
   * @param simulateIOException the simulate IO exception
   */
  public EEAMockMultipartFile(String name, @Nullable String originalFilename,
      @Nullable String contentType, @Nullable byte[] content, boolean simulateIOException) {

    super(name, originalFilename, contentType, content);
    this.simulateIOException = simulateIOException;
  }

  /**
   * Create a new MockMultipartFile with the given content.
   *
   * @param name the name of the file
   * @param originalFilename the original filename (as on the client's machine)
   * @param contentType the content type (if known)
   * @param contentStream the content of the file as stream
   * @param simulateIOException the simulate IO exception
   * @throws IOException if reading from the stream failed
   */
  public EEAMockMultipartFile(String name, @Nullable String originalFilename,
      @Nullable String contentType, InputStream contentStream, boolean simulateIOException)
      throws IOException {

    super(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
    this.simulateIOException = simulateIOException;
  }

  /**
   * Gets the input stream.
   *
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public InputStream getInputStream() throws IOException {
    if (this.simulateIOException) {
      throw new IOException("Controlled Error");
    }
    return super.getInputStream();
  }
}
