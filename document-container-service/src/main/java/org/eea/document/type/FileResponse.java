package org.eea.document.type;

/**
 * The Class FileResponse.
 */
public class FileResponse {

  /** The bytes. */
  private byte[] bytes;

  /** The content type. */
  private String contentType;

  /**
   * Gets the bytes.
   *
   * @return the bytes
   */
  public byte[] getBytes() {
    return bytes;
  }

  /**
   * Sets the bytes.
   *
   * @param bytes the new bytes
   */
  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Gets the content type.
   *
   * @return the content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Sets the content type.
   *
   * @param contentType the new content type
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

}
