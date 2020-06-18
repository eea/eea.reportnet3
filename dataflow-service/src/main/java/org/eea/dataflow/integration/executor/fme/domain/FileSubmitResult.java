package org.eea.dataflow.integration.executor.fme.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
public class FileSubmitResult {

  private String date;
  private String name;
  private String path;
  private Integer size;
  private String type;

}
