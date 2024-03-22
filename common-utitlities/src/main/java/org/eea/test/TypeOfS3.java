package org.eea.test;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeOfS3 {
  LOCAL_S3(S3LocalConfiguration.class.getSimpleName()),
  PUBLIC_S3(S3PublicConfiguration.class.getSimpleName());

  private final String name;
}
