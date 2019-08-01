package org.eea.document.type;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class FileResponse.
 */
@Getter
@Setter
@ToString
public class FileResponse {

  /** The bytes. */
  private byte[] bytes;

  /** The content type. */
  private String contentType;


  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(bytes, contentType);
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FileResponse other = (FileResponse) obj;
    return Arrays.equals(bytes, other.bytes) && Objects.equals(contentType, other.contentType);
  }



}
