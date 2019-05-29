package org.eea.document.type;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class FileResponse.
 */
@Getter
@Setter
public class FileResponse {

  /** The bytes. */
  private byte[] bytes;

  /** The content type. */
  private String contentType;

}
