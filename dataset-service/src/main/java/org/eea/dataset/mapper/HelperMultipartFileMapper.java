package org.eea.dataset.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@Getter
@Setter
public class HelperMultipartFileMapper {
  private byte[] bytes;
  private InputStream inputStream;
  private String originalFilename;
  private boolean fileNull = true;
}
