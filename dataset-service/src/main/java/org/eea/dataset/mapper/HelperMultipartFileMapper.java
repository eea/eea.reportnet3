package org.eea.dataset.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter
@Setter
public class HelperMultipartFileMapper {
  private File file;
  private InputStream inputStream;
  private String originalFilename;
  private boolean fileNull = true;

  public InputStream getInputStream() throws IOException {
    if (file != null) {
      return new FileInputStream(file); // always fresh stream
    } else if (inputStream != null) {
      return inputStream; // only works once!
    } else {
      throw new IllegalStateException("No file or stream set in HelperMultipartFileMapper");
    }
  }
}
