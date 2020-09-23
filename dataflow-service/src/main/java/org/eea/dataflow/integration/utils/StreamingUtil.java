package org.eea.dataflow.integration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.stereotype.Component;

/**
 * The Class StreamingUtil.
 */
@Component
public class StreamingUtil {

  /**
   * Copy.
   *
   * @param source the source
   * @param target the target
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void copy(InputStream source, OutputStream target) throws IOException {
    byte[] buf = new byte[8192];
    int length;
    while ((length = source.read(buf)) > 0) {
      target.write(buf, 0, length);
    }
  }

}
